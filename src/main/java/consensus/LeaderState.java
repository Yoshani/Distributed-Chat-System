package consensus;

import client.ClientState;
import server.Room;
import server.ServerState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LeaderState
{
    private int leaderID;

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
        room.addParticipants(clientState);
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

    public int getLeaderID()
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
}
