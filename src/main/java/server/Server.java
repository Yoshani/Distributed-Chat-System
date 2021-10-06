package server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server implements Runnable {

    private DataOutputStream out;
    private ServerSocket Server;
    private HashMap<String, Integer> clientList = new HashMap<String, Integer>(); //client list
    private HashMap<Integer, String> reverseClientList = new HashMap<Integer, String>(); //client list
    private HashMap<String, String> globalRoomList = new HashMap<String, String>(); //global rooms with their owners
    private String serverID;   //server id which is given when starting the server

    private HashMap<String, clientState> clientObjectList = new HashMap<String, clientState>();  //maintain room object list  clientID:clientObject
    private HashMap<String, Room> roomObjectList = new HashMap<String, Room>();  //maintain room object list roomID:roomObject
    private Room mainhall;

    public Server(String id) {
        this.serverID = id;
        mainhall = new Room("default-" + serverID, "MainHall-" + serverID);
        roomObjectList.put("MainHall-" + serverID, mainhall);
        globalRoomList.put("MainHall-" + serverID, "default-" + serverID);
    }

    //check the existence of a key in json object
    private boolean hasKey(JSONObject jsonObject, String key) {
        return (jsonObject != null && jsonObject.get(key) != null);
    }

    //check validity of the ID
    private boolean checkID(String id) {
        return (Character.toString(id.charAt(0)).matches("[a-zA-Z]+") && id.matches("[a-zA-Z0-9]+") && id.length() >= 3 && id.length() <= 16);
    }

    //send message to client
    private void send(JSONObject obj) throws IOException {
        out.write((obj.toJSONString() + "\n").getBytes("UTF-8"));
        out.flush();
    }

    //format message before sending it to client
    private void messageSend(Socket socket, String msg, List<String> msgList) throws IOException {
        JSONObject sendToClient = new JSONObject();
        String[] array = msg.split(" ");
        if (array[0].equals("newid")) {
            sendToClient = ServerMessage.getApprovalNewID(array[1]);
            send(sendToClient);
        }
        if (array[0].equals("roomchange")) {
            sendToClient = ServerMessage.getRoomChange(array[1], array[2]);
            send(sendToClient);
        }
        if (array[0].equals("createroom")) {
            sendToClient = ServerMessage.getCreateRoom(array[1], array[2]);
            send(sendToClient);
        }
        if (array[0].equals("createroomchange")) {
            sendToClient = ServerMessage.getCreateRoomChange(array[1], array[2], array[3]);
            send(sendToClient);
        }
        if (array[0].equals("roomcontents")) {
            sendToClient = ServerMessage.getWho(array[1], msgList, array[2]);
            send(sendToClient);
        }
    }

    //new identity
    private void newID(String id, Socket connected, String fromclient) throws IOException {
        if (checkID(id) && !clientObjectList.containsKey(id)) {
            System.out.println("Recieved correct ID ::" + fromclient);

            clientState client = new clientState(id, mainhall.getRoomId(), connected.getPort());
            mainhall.addParticipants(client);
            clientObjectList.put(id, client);

            clientList.put(id, connected.getPort());
            reverseClientList.put(connected.getPort(), id);

            synchronized (connected) {
                messageSend(connected, "newid true",null);
                messageSend(connected, "roomchange " + id + " MainHall-" + serverID,null);
            }
        } else {
            System.out.println("Recieved wrong ID type or ID already in use");
            messageSend(connected, "newid false",null);
        }
    }

    //create room
    private void createRoom(String roomID, Socket connected, String fromclient) throws IOException {
        String id = reverseClientList.get(connected.getPort());
        if (checkID(roomID) && !roomObjectList.containsKey(roomID) && !globalRoomList.containsValue(id)) {
            System.out.println("Recieved correct room ID ::" + fromclient);

            clientState client = clientObjectList.get(id);
            String former = client.getRoomID();
            roomObjectList.get(former).removeParticipants(client);

            Room newRoom = new Room(id,roomID);
            roomObjectList.put(roomID,newRoom);
            globalRoomList.put(roomID, id);

            client.setRoomID(roomID);
            newRoom.addParticipants(client);

            synchronized (connected) {
                messageSend(connected, "createroom " + roomID + " true",null);
                messageSend(connected, "createroomchange " + id + " "+ former +" " + roomID,null);
            }
        } else {
            System.out.println("Recieved wrong room ID type or room ID already in use");
            messageSend(connected, "createroom " + roomID + " false",null);
        }
    }

    //who
    private void who(Socket connected, String fromclient) throws IOException {
        String id = reverseClientList.get(connected.getPort());
        clientState client = clientObjectList.get(id);
        String roomID = client.getRoomID();
        Room room = roomObjectList.get(roomID);
        List<clientState> clients = room.getParticipants();

        List<String> participants = new ArrayList<String>();
        System.out.println("room contains :");
        for(int i=0;i< clients.size();i++){
            participants.add(clients.get(i).getId());
            System.out.println(clients.get(i).getId());
        }
        String owner = room.getOwnerIdentity();
        messageSend(connected, "roomcontents " + roomID + " " + owner,participants);
    }

    @Override
    public void run() {
        String fromclient;

        try {
            Server = new ServerSocket(5000);
            System.out.println(Server.getInetAddress());
            System.out.println(Server.getLocalSocketAddress());
            System.out.println(Server.getLocalPort());

            System.out.println("TCPServer Waiting for client on port 5000"); //client should use 5000 as port

            while (true) {
                Socket connected = Server.accept();
                System.out.println(" THE CLIENT" + " " + connected.getInetAddress()
                        + ":" + connected.getPort() + " IS CONNECTED ");

                BufferedReader inFromClient = new BufferedReader(
                        new InputStreamReader(connected.getInputStream(), "UTF-8"));

                out = new DataOutputStream(connected.getOutputStream());

                boolean close = false;

                while (!close) {

                    fromclient = inFromClient.readLine();

                    try {
                        //convert received message to json object
                        Object object = null;
                        JSONParser jsonParser = new JSONParser();
                        object = jsonParser.parse(fromclient);
                        JSONObject j_object = (JSONObject) object;

                        if (hasKey(j_object, "type")) {
                            //check new identity format
                            if (j_object.get("type").equals("newidentity") && j_object.get("identity") != null) {
                                String id = j_object.get("identity").toString();
                                newID(id, connected, fromclient);
                            } //check create room
                            if (j_object.get("type").equals("createroom") && j_object.get("roomid") != null) {
                                String roomID = j_object.get("roomid").toString();
                                createRoom(roomID, connected, fromclient);
                            } //check who
                            if (j_object.get("type").equals("who")) {
                                who(connected, fromclient);
                            }
                        } else {
                            System.out.println("Something went wrong");
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                connected.close();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}