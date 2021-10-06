package server;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private String owner;
    private String roomId;
    private List<clientState> participants = new ArrayList<clientState>();

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

    public synchronized String getParticipants() {
        return roomId;
    }

    public synchronized void addParticipants(clientState participantID) {
        this.participants.add(participantID);
    }

    public synchronized List<clientState> getParticipants(List<clientState> participantID) {
        return this.participants;
    }

    public synchronized void removeParticipants(clientState participantID) {
        this.participants.remove(participantID);
    }

    public String getOwnerIdentity() {
        return owner;
    }

}
