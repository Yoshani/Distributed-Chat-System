package heartbeat;

import consensus.LeaderState;
import messaging.MessageTransfer;
import messaging.ServerMessage;
import org.json.simple.JSONObject;
import org.quartz.*;
import server.Server;
import server.ServerState;

import java.util.ArrayList;

public class ConsensusJob implements Job {

    private ServerState serverState = ServerState.getInstance();
    private LeaderState leaderState = LeaderState.getInstance();
    private ServerMessage serverMessage = ServerMessage.getInstance();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (!serverState.onGoingConsensus().get()) {
            // This is a leader based Consensus.
            // If no leader elected at the moment then no consensus task to perform.
            if (leaderState.getLeaderID() != null) {
                serverState.onGoingConsensus().set(true);
                performConsensus(context); // critical region
                serverState.onGoingConsensus().set(false);
            }
        } else {
            System.out.println("[SKIP] There seems to be on going consensus at the moment, skip.");
        }
    }

    private void performConsensus(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String consensusVoteDuration = dataMap.get("consensusVoteDuration").toString();

        Integer suspectServerId = null;

        // initialise vote set
        serverState.getVoteSet().put("YES", 0);
        serverState.getVoteSet().put("NO", 0);

        Integer leaderServerId = leaderState.getLeaderID();
        Integer myServerId = serverState.getSelfID();

        // if I am leader, and suspect someone, I want to start voting to KICK him!
        if (myServerId.equals(leaderServerId)) {

            // find the next suspect to vote and break the loop
            for (Integer serverId : serverState.getSuspectList().keySet()) {
                if (serverState.getSuspectList().get(serverId).equals("SUSPECTED")) {
                    suspectServerId = serverId;
                    break;
                }
            }

            ArrayList<Server> serverList = new ArrayList<>();
            for (Integer serverid : serverState.getServers().keySet()) {
                if (!serverid.equals(serverState.getSelfID()) && serverState.getSuspectList().get(serverid).equals("NOT_SUSPECTED")) {
                    serverList.add(serverState.getServers().get(serverid));
                }
            }

            //got a suspect
            if (suspectServerId != null) {

                serverState.getVoteSet().put("YES", 1); // I suspect it already, so I vote yes.
                JSONObject startVoteMessage = new JSONObject();
                startVoteMessage = serverMessage.startVoteMessage(serverState.getSelfID(), suspectServerId);
                try {
                    MessageTransfer.sendServerBroadcast(startVoteMessage, serverList);
                    System.out.println("INFO : Leader calling for vote to kick suspect-server: " + startVoteMessage);
                } catch (Exception e) {
                    System.out.println("ERROR : Leader calling for vote to kick suspect-server is failed");
                }

                //wait for consensus vote duration period
                try {
                    Thread.sleep(Integer.parseInt(consensusVoteDuration) * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println((String.format("Consensus votes to kick server [%s]: %s", suspectServerId, serverState.getVoteSet())));

                if (serverState.getVoteSet().get("YES") > serverState.getVoteSet().get("NO")) {

                    JSONObject notifyServerDownMessage = new JSONObject();
                    notifyServerDownMessage = serverMessage.notifyServerDownMessage(suspectServerId);
                    try {

                        MessageTransfer.sendServerBroadcast(notifyServerDownMessage, serverList);
                        System.out.println("INFO : Notify server " + suspectServerId + " down. Removing...");
                        serverState.removeServer(suspectServerId);
                        // TODO : handle remote rooms and users
//                        serverState.removeRemoteChatRoomsByServerId(suspectServerId);
//                        serverState.removeRemoteUserSessionsByServerId(suspectServerId);
                        serverState.removeServerInCountList(suspectServerId);
                        serverState.removeServerInSuspectList(suspectServerId);

                    } catch (Exception e) {
                        System.out.println("ERROR : " + suspectServerId + "Removing is failed");
                    }

                    System.out.println(("INFO : Number of servers in group: " + serverState.getServers().size()));
                }
            }
        }
    }
}
