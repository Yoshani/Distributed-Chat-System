package server;

import java.util.ArrayList;

public class ServerState {

    private String serverID;
    private int serverPort;

    private Room mainHall;
    private final ArrayList<ClientHandler> clientHandlerList = new ArrayList<>();

    //singleton
    private static ServerState serverStateInstance;

    private ServerState() {
    }

    public static ServerState getInstance() {
        if (serverStateInstance == null) {
            synchronized (ServerState.class) {
                if (serverStateInstance == null) {
                    serverStateInstance = new ServerState();//instance will be created at request time
                }
            }
        }
        return serverStateInstance;
    }

    //TODO : make private, init with get instance and configs at startup
    public void initializeWithConfigs(String serverID, int serverPort) {
        this.serverID = serverID;
        this.serverPort = serverPort;
        this.mainHall = new Room("default-" + serverID, "MainHall-" + serverID);
    }

    public String getServerID() {
        return serverID;
    }

    public int getServerPort() {
        return serverPort;
    }

    public Room getMainHall() {
        return mainHall;
    }

    public ArrayList<ClientHandler> getClientHandlerList() {
        return clientHandlerList;
    }

}
