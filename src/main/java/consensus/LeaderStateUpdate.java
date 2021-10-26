package consensus;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import server.ServerState;

public class LeaderStateUpdate extends Thread {
    int numberOfServersWithLowerIds = ServerState.getInstance().getSelfID() - 1;
    int numberOfUpdatesReceived = 0;
    volatile boolean leaderUpdateInProgress = true;

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        long end = start + 5000;
        try {

            while ( leaderUpdateInProgress ) {
                if( System.currentTimeMillis() > end || numberOfUpdatesReceived == numberOfServersWithLowerIds ) {
                    leaderUpdateInProgress = false;
                    System.out.println("INFO : Leader update completed");
                    System.out.println(LeaderState.getInstance().getRoomIDList());
                    BullyAlgorithm.leaderUpdateComplete = true;
                }
                Thread.sleep(10);
            }

        } catch( Exception e ) {
            System.out.println( "WARN : Exception in leader update thread" );
        }

    }

    // update client list and chat room list of leader
    public void receiveUpdate( JSONObject j_object ) {
        numberOfUpdatesReceived += 1;
        JSONArray clientIdList = ( JSONArray ) j_object.get( "clients" );
        JSONArray chatRoomsList = ( JSONArray ) j_object.get( "chatrooms" );
        //System.out.println(chatRoomsList);

        for( Object clientID : clientIdList ) {
            LeaderState.getInstance().addClientLeaderUpdate( clientID.toString() );
        }

        for( Object chatRoom : chatRoomsList ) {
            JSONObject j_room = (JSONObject)chatRoom;
            LeaderState.getInstance().addApprovedRoom( j_room.get("clientid").toString(),
                    j_room.get("roomid").toString(), Integer.parseInt(j_room.get("serverid").toString()) );
        }
    }
}
