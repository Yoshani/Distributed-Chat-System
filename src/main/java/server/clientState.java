package server;

import java.net.Socket;

public class clientState {

    private String id;
    private String roomID;
    private Integer port;
    private Socket socket;

    public clientState(String id, String roomID, Integer port, Socket socket){
        this.id = id;
        this.roomID = roomID;
        this.port = port;
        this.socket = socket;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
