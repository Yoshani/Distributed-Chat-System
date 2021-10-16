package server;

import client.ClientHandlerThread;
import consensus.BullyAlgorithm;
import consensus.LeaderState;
import messaging.MessageTransfer;
import messaging.ServerMessage;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ServerHandlerThread extends Thread {

    private final ServerSocket serverCoordinationSocket;
    private int [] rooms;
    public ServerHandlerThread( ServerSocket serverCoordinationSocket ) {
        this.serverCoordinationSocket = serverCoordinationSocket;
        this.rooms = new int[5];
    }

    @Override
    public void run() {
        try {
            while( true ) {
                Socket serverSocket = serverCoordinationSocket.accept();
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader( serverSocket.getInputStream(), StandardCharsets.UTF_8 )
                );
                String jsonStringFromServer = bufferedReader.readLine();

                // convert received message to json object
                JSONObject j_object = MessageTransfer.convertToJson( jsonStringFromServer );

                int index = 0;
                if (j_object.containsKey("leader") && j_object.containsKey("coordinatoor")) {
                    index = Integer.parseInt(j_object.get("leader").toString());
                    SharedAttributes.setNeighbourIndex(index);
                } else if (j_object.containsKey("sender")) {
                    index = Integer.parseInt(j_object.get("sender").toString());
                    SharedAttributes.setNeighbourIndex(index);
                }
//                SharedAttributes.setNeighbourIndex(index);

                if (MessageTransfer.hasKey( j_object, "room")) {
                    String rooms = (String) j_object.get("room");
                    SharedAttributes sharedAttributes = new SharedAttributes();
                    sharedAttributes.setRoom(rooms);
                }

                if (MessageTransfer.hasKey( j_object, "delete-room")) {
                    String deletedRoom = (String) j_object.get("delete-room");
                    SharedAttributes sharedAttributes = new SharedAttributes();
                    sharedAttributes.removeRoomFromGlobalRoomList(deletedRoom);
                }

                if( MessageTransfer.hasKey( j_object, "option" ) ) {
                    // messages with 'option' tag will be handled inside BullyAlgorithm
                    BullyAlgorithm.receiveMessages( j_object );
                }
                else if (MessageTransfer.hasKey( j_object, "type" )) {

                    if (j_object.get("type").equals("clientidapprovalrequest")
                                && j_object.get("clientid") != null && j_object.get( "sender" ) != null
                                && j_object.get( "threadid" ) != null) {

                        // leader processes client ID approval request received
                        String clientID = j_object.get("clientid").toString();
                        int sender = Integer.parseInt(j_object.get("sender").toString());
                        String threadID = j_object.get("threadid").toString();
                        SharedAttributes.setNeighbourIndex(sender);

                        boolean approved = !LeaderState.getInstance().isClientIDAlreadyTaken( clientID );
                        if( approved ) {
                            LeaderState.getInstance().addApprovedClient( clientID, sender );
                        }
                        Server destServer = ServerState.getInstance().getServers()
                                                       .get( sender );
                        try {
                            // send client id approval reply to sender
                            MessageTransfer.sendServer(
                                    ServerMessage.getClientIdApprovalReply( String.valueOf(approved), threadID ),
                                    destServer
                            );
                            System.out.println("INFO : Client ID '"+ clientID +
                                                       "' from s" + sender + " is" + (approved ? " ":" not ") + "approved");
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }

                    } else if ( j_object.get("type").equals("clientidapprovalreply")
                                  && j_object.get("approved") != null && j_object.get( "threadid" ) != null){

                        // non leader processes client ID approval request reply received
                        int approved = Boolean.parseBoolean( j_object.get("approved").toString() ) ? 1 : 0;
                        Long threadID = Long.parseLong( j_object.get("threadid").toString() );

                        ClientHandlerThread clientHandlerThread = ServerState.getInstance()
                                                                             .getClientHandlerThread( threadID );
                        clientHandlerThread.setApprovedClientID( approved );
                        Object lock = clientHandlerThread.getLock();
                        synchronized( lock ) {
                            lock.notify();
                        }

                    } else if ( j_object.get("type").equals("roomcreateapprovalrequest") ) {

                        // leader processes room create approval request received
                        String clientID = j_object.get("clientid").toString();
                        String roomID = j_object.get("roomid").toString();
                        int sender = Integer.parseInt(j_object.get("sender").toString());
                        String threadID = j_object.get("threadid").toString();

                        boolean approved = LeaderState.getInstance().isRoomCreationApproved(roomID);

                        if( approved ) {
                            LeaderState.getInstance().addApprovedRoom( clientID, roomID, sender );
                        }
                        Server destServer = ServerState.getInstance().getServers()
                                                       .get( sender );
                        try {
                            // send room create approval reply to sender
                            MessageTransfer.sendServer(
                                    ServerMessage.getRoomCreateApprovalReply( String.valueOf(approved), threadID ),
                                    destServer
                            );
                            System.out.println("INFO : Room '"+ roomID +
                                               "' creation request from client " + clientID +
                                               " is" + (approved ? " ":" not ") + "approved");
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    } else if ( j_object.get("type").equals("roomcreateapprovalreply") ) {

                        // non leader processes room create approval request reply received
                        int approved = Boolean.parseBoolean( j_object.get("approved").toString() ) ? 1 : 0;
                        Long threadID = Long.parseLong( j_object.get("threadid").toString() );

                        ClientHandlerThread clientHandlerThread = ServerState.getInstance()
                                                                             .getClientHandlerThread( threadID );
                        clientHandlerThread.setApprovedRoomCreation( approved );
                        Object lock = clientHandlerThread.getLock();
                        synchronized( lock ) {
                            lock.notify();
                        }
                    }
                    else {
                        System.out.println( "WARN : Command error, Corrupted JSON from Server" );
                    }
                }
                else {
                    System.out.println( "WARN : Command error, Corrupted JSON from Server" );
                }
                serverSocket.close();
            }
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }
}
