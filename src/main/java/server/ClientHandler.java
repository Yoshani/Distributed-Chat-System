package server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientHandler extends Thread {

    private String serverID;   //server id which is given when starting the server
    private Socket clientSocket;

    private DataOutputStream dataOutputStream;

    private static HashMap<String, Integer> clientList = new HashMap<String, Integer>(); //client list
    private static HashMap<Integer, String> reverseClientList = new HashMap<Integer, String>(); //client list
    private static HashMap<String, String> globalRoomList = new HashMap<String, String>(); //global rooms with their owners

    private static HashMap<String, clientState> clientObjectList = new HashMap<String, clientState>();  //maintain room object list  clientID:clientObject
    private static HashMap<String, Room> roomObjectList = new HashMap<String, Room>();  //maintain room object list roomID:roomObject

    public ClientHandler(Socket clientSocket) {
        this.serverID = ServerState.getInstance().getServerID();
        roomObjectList.put("MainHall-" + serverID, ServerState.getInstance().getMainHall());
        globalRoomList.put("MainHall-" + serverID, "default-" + serverID);
        this.clientSocket = clientSocket;
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
        dataOutputStream.write((obj.toJSONString() + "\n").getBytes("UTF-8"));
        dataOutputStream.flush();
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
        if (array[0].equals("roomlist")) {
            sendToClient = ServerMessage.getList(msgList);
            send(sendToClient);
        }
    }

    //new identity
    private void newID(String id, Socket connected, String fromClient) throws IOException {
        if (checkID(id) && !clientObjectList.containsKey(id)) {
            System.out.println("Recieved correct ID ::" + fromClient);

            clientState client = new clientState(id, ServerState.getInstance().getMainHall().getRoomId(), connected.getPort());
            ServerState.getInstance().getMainHall().addParticipants(client);
            clientObjectList.put(id, client);

            clientList.put(id, connected.getPort());
            reverseClientList.put(connected.getPort(), id);

            synchronized (connected) {
                messageSend(connected, "newid true", null);
                messageSend(connected, "roomchange " + id + " MainHall-" + serverID, null);
            }
        } else {
            System.out.println("Recieved wrong ID type or ID already in use");
            messageSend(connected, "newid false", null);
        }
    }

    //create room
    private void createRoom(String roomID, Socket connected, String fromClient) throws IOException {
        String id = reverseClientList.get(connected.getPort());
        if (checkID(roomID) && !roomObjectList.containsKey(roomID) && !globalRoomList.containsValue(id)) {
            System.out.println("Recieved correct room ID ::" + fromClient);

            clientState client = clientObjectList.get(id);
            String former = client.getRoomID();
            roomObjectList.get(former).removeParticipants(client);

            Room newRoom = new Room(id, roomID);
            roomObjectList.put(roomID, newRoom);
            globalRoomList.put(roomID, id);

            client.setRoomID(roomID);
            newRoom.addParticipants(client);

            synchronized (connected) {
                messageSend(connected, "createroom " + roomID + " true", null);
                messageSend(connected, "createroomchange " + id + " " + former + " " + roomID, null);
            }
        } else {
            System.out.println("Recieved wrong room ID type or room ID already in use");
            messageSend(connected, "createroom " + roomID + " false", null);
        }
    }

    //who
    private void who(Socket connected, String fromClient) throws IOException {
        String id = reverseClientList.get(connected.getPort());
        clientState client = clientObjectList.get(id);
        String roomID = client.getRoomID();
        Room room = roomObjectList.get(roomID);
        List<clientState> clients = room.getParticipants();

        List<String> participants = new ArrayList<String>();
        System.out.println("room contains :");
        for (int i = 0; i < clients.size(); i++) {
            participants.add(clients.get(i).getId());
            System.out.println(clients.get(i).getId());
        }
        String owner = room.getOwnerIdentity();
        messageSend(connected, "roomcontents " + roomID + " " + owner, participants);
    }

    //list
    private void list(Socket connected, String fromClient) throws IOException {
        List<String> rooms = new ArrayList<>();
        System.out.println("rooms in the system :");
        for (String r : roomObjectList.keySet()) {
            rooms.add(r);
            System.out.println(r);
        }

        messageSend(connected, "roomlist ", rooms);
    }

    @Override
    public void run() {
        try {
            System.out.println(" THE CLIENT" + " " + clientSocket.getInetAddress()
                    + ":" + clientSocket.getPort() + " IS CONNECTED ");

            BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));

            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());


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
                            newID(id, clientSocket, fromclient);
                        } //check create room
                        if (j_object.get("type").equals("createroom") && j_object.get("roomid") != null) {
                            String roomID = j_object.get("roomid").toString();
                            createRoom(roomID, clientSocket, fromclient);
                        } //check who
                        if (j_object.get("type").equals("who")) {
                            who(clientSocket, fromclient);
                        } //check list
                        if (j_object.get("type").equals("list")) {
                            list(clientSocket, fromclient);
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