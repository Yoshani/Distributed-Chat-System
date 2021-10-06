package server;

import java.util.ArrayList;
import java.util.HashMap;

public class ServerState {

    private String serverID;
    private int serverPort;

    private Room mainHall;
    private final ArrayList<ClientHandlerThread> clientHandlerThreadList = new ArrayList<>();

    private final HashMap<String, Integer> clientPortMap = new HashMap<>(); //client list <clientID,port>
    private final HashMap<Integer, String> portClientMap = new HashMap<>(); //client list  <port,clientID>

    private final HashMap<String, ClientState> clientStateMap = new HashMap<>();  //maintain room object list  <clientID,clientState>
    private final HashMap<String, Room> roomMap = new HashMap<>();  //maintain room object list <roomID,roomObject>

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
        this.roomMap.put("MainHall-" + serverID, mainHall);
    }

    public void addClientHandlerThreadToList(ClientHandlerThread clientHandlerThread) {
        clientHandlerThreadList.add(clientHandlerThread);
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


    public HashMap<String, Integer> getClientPortMap() {
        return clientPortMap;
    }

    public HashMap<Integer, String> getPortClientMap() {
        return portClientMap;
    }

    public HashMap<String, ClientState> getClientStateMap() {
        return clientStateMap;
    }

    public HashMap<String, Room> getRoomMap() {
        return roomMap;
    }
}
