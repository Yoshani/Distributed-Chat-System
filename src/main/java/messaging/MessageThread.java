package messaging;

import messaging.protocol.ServerMessage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MessageThread implements Runnable
{
    private Socket socket;
//    private State state;
    private boolean debug;
    private BufferedReader in;
    private DataOutputStream out;
    private JSONParser parser = new JSONParser();
    private boolean run = true;

    public MessageThread( Socket socket, boolean debug) throws IOException
    {
        this.socket = socket;
        this.out = new DataOutputStream(socket.getOutputStream());
        this.debug = debug;
    }

    @Override
    public void run() {
        try {
            this.in = new BufferedReader(new InputStreamReader( socket.getInputStream(), "UTF-8"));
            JSONObject message;
            while (run) {
                message = (JSONObject) parser.parse(in.readLine());
                if (debug) {
                    System.out.println("Receiving: " + message.toJSONString());
//                    System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
                }
                MessageReceive(socket, message);
            }
            System.exit(0);
            in.close();
            socket.close();
        } catch ( ParseException e) {
            System.out.println("Message Error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Communication Error: " + e.getMessage());
            System.exit(1);
        }
    }

    public void MessageReceive(Socket socket, JSONObject message)
            throws IOException, ParseException
    {
        String type = ( String ) message.get( "type" );

        // client request of #newidentity
        if( type.equals( "newidentity" ) )
        {
            String approved = "true";

            JSONObject request = ServerMessage.getNewIdentityReply(approved);
//            if (debug) {
//                System.out.println("Sending: " + request.toJSONString());
//                System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
//            }
            // send approve message to client
            send(out, request);

            //System.out.println( "User has been approved." );
        }
    }

    private void send( DataOutputStream out, JSONObject obj) throws IOException {
        out.write((obj.toJSONString() + "\n").getBytes("UTF-8"));
        out.flush();
    }
}

