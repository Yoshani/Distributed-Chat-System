package server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

public class Server extends Thread {

    private String serverID;   //server id which is given when starting the server
    private ArrayList<Server> threadList;
    private ArrayList<Socket> connectionList;
    private Socket connected;

    private DataOutputStream out;
    private ServerSocket Server;
    private static HashMap<String, Integer> clientList = new HashMap<>(); //client list
    private static HashMap<Integer, String> reverseClientList = new HashMap<>(); //client list
    private static HashMap<String, String> globalRoomList = new HashMap<>(); //global rooms with their owners

    private static HashMap<String, clientState> clientObjectList = new HashMap<>();  //maintain room object list  clientID:clientObject
    private static HashMap<String, Room> roomObjectList = new HashMap<>();  //maintain room object list roomID:roomObject
    private Room mainhall;

    public Server(String id, Socket socket, ArrayList<Server> threads, ArrayList<Socket> connections) {
        this.serverID = id;
        mainhall = new Room("default-" + serverID, "MainHall-" + serverID);
        roomObjectList.put("MainHall-" + serverID, mainhall);
        globalRoomList.put("MainHall-" + serverID, "default-" + serverID);
        this.connected = socket;
        this.threadList = threads;
        this.connectionList = connections;
    }

    //check the existence of a key in json object
    private boolean hasKey(JSONObject jsonObject, String key) {
        return (jsonObject != null && jsonObject.get(key) != null);
    }

    //check validity of the ID
    private boolean checkID(String id) {
        return (Character.toString(id.charAt(0)).matches("[a-zA-Z]+") && id.matches("[a-zA-Z0-9]+") && id.length() >= 3 && id.length() <= 16);
    }

    private void sendBroadcast(JSONObject obj, ArrayList<Socket> socketList) throws IOException {
        for (Socket each : socketList) {
            Socket TEMP_SOCK = (Socket) each;
            PrintWriter TEMP_OUT = new PrintWriter(TEMP_SOCK.getOutputStream());
            TEMP_OUT.println(obj);
            TEMP_OUT.flush();
            System.out.println("Sent to: " + TEMP_SOCK.getLocalAddress().getHostName() + TEMP_SOCK.getPort());    //displayed in the console
        }
    }

    //send message to client
    private void send(JSONObject obj) throws IOException {
        out.write((obj.toJSONString() + "\n").getBytes("UTF-8"));
        out.flush();
    }

    //format message before sending it to client
    private void messageSend(ArrayList<Socket> socketList, String msg, List<String> msgList) throws IOException {
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
        if (array[0].equals("roomchangeAll")) {
            sendToClient = ServerMessage.getRoomChange(array[1], array[2], array[3]);
            sendBroadcast(sendToClient, socketList);
        }
        if (array[0].equals("roomcontents")) {
            sendToClient = ServerMessage.getWho(array[1], msgList, array[2]);
            send(sendToClient);
        }
        if (array[0].equals("roomlist")) {
            sendToClient = ServerMessage.getList(msgList);
            send(sendToClient);
        }
        if (array[0].equals("joinroomchange")) {
            sendToClient = ServerMessage.getRoomChange(array[1], array[2], array[3]);
            send(sendToClient);
        }
        if (array[0].equals("message")) {
            sendToClient = ServerMessage.getMessage(array[1], String.join(" ",Arrays.copyOfRange(array, 2, array.length)));
            sendBroadcast(sendToClient, socketList);
        }
    }

    //new identity
    private void newID(String id, Socket connected, String fromclient) throws IOException {
        if (checkID(id) && !clientObjectList.containsKey(id)) {
            System.out.println("Recieved correct ID ::" + fromclient);

            clientState client = new clientState(id, mainhall.getRoomId(), connected.getPort(), connected);

            mainhall.addParticipants(client);
            mainhall.addSocket(connected);

            clientObjectList.put(id, client);

            clientList.put(id, connected.getPort());
            reverseClientList.put(connected.getPort(), id);

            synchronized (connectionList) {
                messageSend(connectionList, "newid true", null);
                messageSend(connectionList, "roomchange " + id + " MainHall-" + serverID, null);
            }
        } else {
            System.out.println("Recieved wrong ID type or ID already in use");
            messageSend(connectionList, "newid false", null);
        }
    }

    //create room
    private void createRoom(String roomID, Socket connected, String fromclient) throws IOException {
        String id = reverseClientList.get(connected.getPort());
        if (checkID(roomID) && !roomObjectList.containsKey(roomID) && !globalRoomList.containsValue(id)) {
            System.out.println("Recieved correct room ID ::" + fromclient);

            clientState client = clientObjectList.get(id);

            String former = client.getRoomID();
            ArrayList<Socket> formerSocket = new ArrayList<>();
            for (String each:clientObjectList.keySet()){
                if (clientObjectList.get(each).getRoomID().equals(former)){
                    formerSocket.add(clientObjectList.get(each).getSocket());
                }
            }

            roomObjectList.get(former).removeParticipants(client);
            roomObjectList.get(former).removeSocket(connected);

            Room newRoom = new Room(id, roomID);
            roomObjectList.put(roomID, newRoom);
            globalRoomList.put(roomID, id);

            client.setRoomID(roomID);

            newRoom.addParticipants(client);
            newRoom.addSocket(connected);

            synchronized (connectionList) {
                messageSend(connectionList, "createroom " + roomID + " true", null);
                messageSend(formerSocket, "roomchangeAll " + id + " " + former + " " + roomID, null);
            }
        } else {
            System.out.println("Recieved wrong room ID type or room ID already in use");
            messageSend(connectionList, "createroom " + roomID + " false", null);
        }
    }

    //who
    private void who(Socket connected, String fromclient) throws IOException {
        String id = reverseClientList.get(connected.getPort());
        clientState client = clientObjectList.get(id);
        String roomID = client.getRoomID();
        Room room = roomObjectList.get(roomID);

        List<String> participants = new ArrayList<>();
        System.out.println("room contains :");
        for (String each:clientObjectList.keySet()){
            if (clientObjectList.get(each).getRoomID().equals(roomID)){
                participants.add(each);
            }
        }
        String owner = room.getOwnerIdentity();
        messageSend(connectionList, "roomcontents " + roomID + " " + owner, participants);
    }

    //list
    private void list(Socket connected, String fromclient) throws IOException {
        List<String> rooms = new ArrayList<>();
        System.out.println("rooms in the system :");
        for (String r : roomObjectList.keySet()) {
            rooms.add(r);
            System.out.println(r);
        }

        messageSend(connectionList, "roomlist ", rooms);
    }

    //joinroom
    private void joinroom(String roomid, Socket connected, String fromclient) throws IOException {
        String id = reverseClientList.get(connected.getPort());
        clientState client = clientObjectList.get(id);
        Room room = roomObjectList.get(client.getRoomID());
        String former = client.getRoomID();

        if (roomObjectList.containsKey(roomid) && !room.getOwnerIdentity().equals(id)) {
            System.out.println("Recieved correct join room message :" + fromclient);
            //-------------have to change when there are multiple servers---------------------
            ArrayList<Socket> roomList = new ArrayList<>();
            for (String each:clientObjectList.keySet()){
                if (clientObjectList.get(each).getRoomID().equals(former) || clientObjectList.get(each).getRoomID().equals(roomid)){
                    roomList.add(clientObjectList.get(each).getSocket());
                }
            }
            roomObjectList.get(former).removeParticipants(client);
            roomObjectList.get(former).removeSocket(connected);

            client.setRoomID(roomid);

            roomObjectList.get(roomid).addParticipants(client);
            roomObjectList.get(roomid).addSocket(connected);

            messageSend(roomList, "roomchangeAll " + id + " " + former + " " + roomid, null);

        } else {
            messageSend(connectionList, "joinroomchange " + id + " " + former + " " + roomid, null);
        }
    }

    //message
    private void message(String content, Socket connected, String fromclient) throws IOException {
        String id = reverseClientList.get(connected.getPort());
        clientState client = clientObjectList.get(id);
        String roomid = client.getRoomID();

        ArrayList<Socket> roomList = new ArrayList<>();
        for (String each:clientObjectList.keySet()){
            if (clientObjectList.get(each).getRoomID().equals(roomid) && !clientObjectList.get(each).getId().equals(id)){
                roomList.add(clientObjectList.get(each).getSocket());
            }
        }

        messageSend(roomList, "message "+ id + " " + content, null);
    }

    @Override
    public void run() {
        try {
            System.out.println(" THE CLIENT" + " " + connected.getInetAddress()
                    + ":" + connected.getPort() + " IS CONNECTED ");

            BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader(connected.getInputStream(), "UTF-8"));

            out = new DataOutputStream(connected.getOutputStream());


            while (true) {

                String fromclient = inFromClient.readLine();

                if (fromclient.equalsIgnoreCase("exit")) {
                    break;
                }

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
                        } //check list
                        if (j_object.get("type").equals("list")) {
                            list(connected, fromclient);
                        } //check joinroom
                        if (j_object.get("type").equals("joinroom") && j_object.get("roomid") != null) {
                            String roomID = j_object.get("roomid").toString();
                            joinroom(roomID, connected, fromclient);
                        } //check message
                        if (j_object.get("type").equals("message")) {
                            String content = j_object.get("content").toString();
                            message(content, connected, fromclient);
                        }
                    } else {
                        System.out.println("Something went wrong");
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}