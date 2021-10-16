package server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SharedAttributes {

    private static List<String> rooms;
    private static boolean added = false;
    private static int sender_;
    public SharedAttributes() {}

    public void setRoom(String rooms) {
        this.rooms = new ArrayList<>();
        this.rooms.add(rooms);
    }
    public static void setNeighbourIndex(int sender) {
        sender_ = sender;
    }

    public static int getNeighbourIndex() {
        return sender_;
    }
    public static List<String> getRooms() {

        List<String> roomsList = new ArrayList<>(ServerState.getInstance().getRoomMap().keySet());
        for (String room: roomsList) {
            if (rooms != null) {
                if (!(rooms.contains(room))) {
                    rooms.add(room);
                    if (!rooms.contains("MainHall-s"+ getNeighbourIndex())) {
                        rooms.add("MainHall-s" + getNeighbourIndex());
                    }
                }
            } else {
                rooms = new ArrayList<>();
                for (String room_: roomsList) {
                    rooms.add(room_);
                    if (!rooms.contains("MainHall-s"+ getNeighbourIndex())) {
                        rooms.add("MainHall-s" + getNeighbourIndex());
                    }
                }
            }
        }
        return rooms;
    }

    public static void removeRoomFromGlobalRoomList(String roomID) {
        rooms.remove(roomID);
    }

    public static void addNewRoomToGlobalRoomList(String newRoomID, List<String> roomList) {
        rooms = roomList;
    }
}
