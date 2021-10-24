package consensus;

import client.ClientState;
import server.Room;
import server.ServerState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LeaderState
{
    private Integer leaderID;

    private final List<String> activeClientsList = new ArrayList<>();
    private final HashMap<String, Room> activeChatRooms = new HashMap<>(); // <roomID, room obj>

    // singleton
    private static LeaderState leaderStateInstance;

    private LeaderState() {
    }

    public static LeaderState getInstance() {
        if (leaderStateInstance == null) {
            synchronized (LeaderState.class) {
                if (leaderStateInstance == null) {
                    leaderStateInstance = new LeaderState(); //instance will be created at request time
                    leaderStateInstance.addServerDefaultMainHalls();
                }
            }
        }
        return leaderStateInstance;
    }

    public boolean isLeader() {
        return ServerState.getInstance().getSelfID() == LeaderState.getInstance().getLeaderID();
    }

    public boolean isLeaderElected() {
        return BullyAlgorithm.leaderFlag;
    }

    public boolean isClientIDAlreadyTaken(String clientID) {
        return activeClientsList.contains(clientID);
    }

    public void addClient(ClientState client) {
        activeClientsList.add(client.getClientID());
        activeChatRooms.get(client.getRoomID()).addParticipants(client);
    }

    public void removeClient(String clientID, String formerRoomID) {
        activeClientsList.remove(clientID);
        activeChatRooms.get(formerRoomID).removeParticipants(clientID);
    }

    public void localJoinRoomClient(ClientState clientState, String formerRoomID) {
        removeClient(clientState.getClientID(), formerRoomID);
        addClient(clientState);
    }

    public boolean isRoomCreationApproved( String roomID ) {
        return !(activeChatRooms.containsKey( roomID ));
    }

    public void addApprovedRoom(String clientID, String roomID, int serverID) {
        Room room = new Room(clientID, roomID, serverID);
        activeChatRooms.put(roomID, room);

        //add client to the new room
        ClientState clientState = new ClientState(clientID, roomID, null);
        clientState.setRoomOwner(true);
        room.addParticipants(clientState);
    }

    public void removeRoom(String roomID, String mainHallID, String ownerID) {
        HashMap<String, ClientState> formerClientStateMap = this.activeChatRooms.get(roomID).getClientStateMap();
        Room mainHall = this.activeChatRooms.get(mainHallID);

        //update client room to main hall , add clients to main hall
        formerClientStateMap.forEach((clientID, clientState) -> {
            clientState.setRoomID(mainHallID);
            mainHall.getClientStateMap().put(clientState.getClientID(), clientState);
        });

        //set to room owner false, remove room from map
        formerClientStateMap.get(ownerID).setRoomOwner(false);
        this.activeChatRooms.remove(roomID);
    }

    public void addServerDefaultMainHalls(){
        ServerState.getInstance().getServers()
                .forEach((serverID, server) -> {
                    String roomID = ServerState.getMainHallIDbyServerInt(serverID);
                    this.activeChatRooms.put(roomID, new Room("", roomID, serverID));
                });
    }

    public void removeApprovedRoom(String roomID) {
        //TODO : move clients already in room (update server state) on delete
        activeChatRooms.remove( roomID );
    }


    public int getServerIdIfRoomExist(String roomID) {
        if (this.activeChatRooms.containsKey(roomID)) {
            Room targetRoom = activeChatRooms.get(roomID);
            return targetRoom.getServerID();
        } else {
            return -1;
        }
    }

    public Integer getLeaderID()
    {
        return leaderID;
    }

    public void setLeaderID( int leaderID )
    {
        this.leaderID = leaderID;
    }

    public ArrayList<String> getRoomIDList() {
        return new ArrayList<>(this.activeChatRooms.keySet());
    }

    public void removeRemoteChatRoomsByServerId(Integer serverId) {
        ArrayList<String> roomIdList = new ArrayList<String>();
        for (String entry : activeChatRooms.keySet()) {
            Room remoteRoom = activeChatRooms.get(entry);
            if(remoteRoom.getServerID()==serverId){
                roomIdList.add(remoteRoom.getRoomID());
                activeChatRooms.remove(entry);
            }
        }

//        for (String clientId : activeClientsList){
//            if()
//        }
    }

//    public void removeRemoteUserSessionsByServerId(Integer serverId) {
//        for (String entry : activeClientsList) {
//
//            RemoteUserSession remoteUserSession = remoteUserSessions.get(entry);
//            if (remoteUserSession.getManagingServerId().equalsIgnoreCase(serverId)) {
//                remoteUserSessions.remove(entry);
//            }
//        }
//    }
}
