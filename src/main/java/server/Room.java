package server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Room {
    private String owner;
    private String roomId;
    private List<clientState> participants = new ArrayList<clientState>();
    private ArrayList<Socket> socketList = new ArrayList<Socket>();

    public Room(String identity, String roomId) {
        this.owner = identity;
        this.roomId = roomId;
    }

    public synchronized String getRoomId() {
        return roomId;
    }

    public synchronized void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public synchronized List<clientState> getParticipants() {
        return this.participants;
    }

    public synchronized void addParticipants(clientState participantID) {
        this.participants.add(participantID);
    }

    public synchronized void removeParticipants(clientState participantID) {
        this.participants.remove(participantID);
    }

    public synchronized ArrayList<Socket> getSocket() {
        return this.socketList;
    }

    public synchronized void addSocket(Socket socket) {
        this.socketList.add(socket);
    }

    public synchronized void removeSocket(Socket socket) {
        this.socketList.remove(socket);
    }

    public String getOwnerIdentity() {
        return owner;
    }

}
