package server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ClientHandlerThread extends Thread {

    private final Socket clientSocket;
    //TODO : have client state in thread

    //TODO : check input stream local var
    private DataOutputStream dataOutputStream;

    public ClientHandlerThread(Socket clientSocket) {
        String serverID = ServerState.getInstance().getServerID();
        ServerState.getInstance().getRoomMap().put("MainHall-" + serverID, ServerState.getInstance().getMainHall());

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
    private void newID(String id, Socket connected, String jsonStringFromClient) throws IOException {
        if (checkID(id) && !ServerState.getInstance().getClientStateMap().containsKey(id)) {
            System.out.println("INFO : Recieved correct ID ::" + jsonStringFromClient);

            ClientState client = new ClientState(id, ServerState.getInstance().getMainHall().getRoomID(), connected.getPort());
            ServerState.getInstance().getMainHall().addParticipants(client);
            ServerState.getInstance().getClientStateMap().put(id, client);

            ServerState.getInstance().getClientPortMap().put(id, connected.getPort());
            ServerState.getInstance().getPortClientMap().put(connected.getPort(), id);

            synchronized (connected) {
                messageSend(connected, "newid true", null);
                messageSend(connected, "roomchange " + id + " MainHall-" + ServerState.getInstance().getServerID(), null);
            }
        } else {
            System.out.println("WARN : Recieved wrong ID type or ID already in use");
            messageSend(connected, "newid false", null);
        }
    }

    //create room
    private void createRoom(String roomID, Socket connected, String jsonStringFromClient) throws IOException {
        String id = ServerState.getInstance().getPortClientMap().get(connected.getPort());
        if (checkID(roomID) && !ServerState.getInstance().getRoomMap().containsKey(roomID)) {
            System.out.println("INFO : Received correct room ID ::" + jsonStringFromClient);

            ClientState client = ServerState.getInstance().getClientStateMap().get(id);
            String former = client.getRoomID();
            ServerState.getInstance().getRoomMap().get(former).removeParticipants(client);

            Room newRoom = new Room(id, roomID);
            ServerState.getInstance().getRoomMap().put(roomID, newRoom);

            client.setRoomID(roomID);
            newRoom.addParticipants(client);

            synchronized (connected) {
                messageSend(connected, "createroom " + roomID + " true", null);
                messageSend(connected, "createroomchange " + id + " " + former + " " + roomID, null);
            }
        } else {
            System.out.println("WARN : Recieved wrong room ID type or room ID already in use");
            messageSend(connected, "createroom " + roomID + " false", null);
        }
    }

    //who
    private void who(Socket connected, String jsonStringFromClient) throws IOException {
        String id = ServerState.getInstance().getPortClientMap().get(connected.getPort());
        ClientState client = ServerState.getInstance().getClientStateMap().get(id);
        String roomID = client.getRoomID();
        Room room = ServerState.getInstance().getRoomMap().get(roomID);
        List<ClientState> clients = room.getParticipants();

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
    private void list(Socket connected, String jsonStringFromClient) throws IOException {
        List<String> rooms = new ArrayList<>();
        System.out.println("INFO : rooms in the system :");
        for (String r : ServerState.getInstance().getRoomMap().keySet()) {
            rooms.add(r);
            System.out.println(r);
        }

        messageSend(connected, "roomlist ", rooms);
    }

    @Override
    public void run() {
        try {
            System.out.println("INFO : THE CLIENT" + " " + clientSocket.getInetAddress()
                    + ":" + clientSocket.getPort() + " IS CONNECTED ");

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));

            this.dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());


            while (true) {

                String jsonStringFromClient = bufferedReader.readLine();

                if (jsonStringFromClient.equalsIgnoreCase("exit")) {
                    break;
                }

                try {
                    //convert received message to json object
                    Object object = null;
                    JSONParser jsonParser = new JSONParser();
                    object = jsonParser.parse(jsonStringFromClient);
                    JSONObject j_object = (JSONObject) object;

                    if (hasKey(j_object, "type")) {
                        //check new identity format
                        if (j_object.get("type").equals("newidentity") && j_object.get("identity") != null) {
                            String id = j_object.get("identity").toString();
                            newID(id, clientSocket, jsonStringFromClient);
                        } //check create room
                        if (j_object.get("type").equals("createroom") && j_object.get("roomid") != null) {
                            String roomID = j_object.get("roomid").toString();
                            createRoom(roomID, clientSocket, jsonStringFromClient);
                        } //check who
                        if (j_object.get("type").equals("who")) {
                            who(clientSocket, jsonStringFromClient);
                        } //check list
                        if (j_object.get("type").equals("list")) {
                            list(clientSocket, jsonStringFromClient);
                        }
                    } else {
                        System.out.println("WARN : Command error, Corrupted JSON");
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