package server;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private String ownerID;
    private String roomID;
    private List<ClientState> participants = new ArrayList<ClientState>();

    public Room(String identity, String roomID) {
        this.ownerID = identity;
        this.roomID = roomID;
    }

    public synchronized String getRoomID() {
        return roomID;
    }

    public synchronized void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public synchronized List<ClientState> getParticipants() {
        return this.participants;
    }

    public synchronized void addParticipants(ClientState participantID) {
        this.participants.add(participantID);
    }

    public synchronized void removeParticipants(ClientState participantID) {
        this.participants.remove(participantID);
    }

    public String getOwnerIdentity() {
        return ownerID;
    }

}
