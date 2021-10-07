package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ServerState {

    private String serverID;
    private String serverAddress = null;
    private int coordinationPort;
    private int clientsPort;

    private Room mainHall;
    private final ArrayList<ClientHandlerThread> clientHandlerThreadList = new ArrayList<>();

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

    public void initializeWithConfigs(String serverID, String serverConfPath) {

        this.serverID = serverID;
        try {
            File conf = new File( serverConfPath ); // read configuration
            Scanner myReader = new Scanner(conf);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] params = data.split( " " );
                if( params[0].equals( serverID ) ) {
                    this.serverAddress = params[1];
                    this.clientsPort = Integer.parseInt(params[2]);
                    this.coordinationPort = Integer.parseInt(params[3]);
                }
            }
            myReader.close();

        } catch ( FileNotFoundException e) {
            System.out.println("Configs file not found");
            e.printStackTrace();
        }

        this.mainHall = new Room("default-" + serverID, "MainHall-" + serverID);
        this.roomMap.put("MainHall-" + serverID, mainHall);

    }

    public void addClientHandlerThreadToList(ClientHandlerThread clientHandlerThread) {
        clientHandlerThreadList.add(clientHandlerThread);
    }

    public boolean isClientIDAlreadyTaken(String clientID){
        for (Map.Entry<String, Room> entry : this.getRoomMap().entrySet()) {
            Room room = entry.getValue();
            if (room.getClientStateMap().containsKey(clientID)) return true;
        }
        return false;
    }

    public String getServerID() {
        return serverID;
    }

    public int getClientsPort() {
        return clientsPort;
    }

    public String getServerAddress()
    {
        return serverAddress;
    }

    public int getCoordinationPort()
    {
        return coordinationPort;
    }

    public Room getMainHall() {
        return mainHall;
    }

    public HashMap<String, Room> getRoomMap() {
        return roomMap;
    }

    public ArrayList<ClientHandlerThread> getClientHandlerThreadList() {
        return clientHandlerThreadList;
    }
}
