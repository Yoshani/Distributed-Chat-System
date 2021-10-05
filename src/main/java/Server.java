import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

class server {

    private static boolean hasKey(JSONObject jsonObject, String key) {
        return (jsonObject != null && jsonObject.get(key) != null);
    }

    private static boolean checkID(String id) {
        return (Character.toString(id.charAt(0)).matches("[a-zA-Z]+") && id.matches("[a-zA-Z0-9]+") && id.length() >= 3 && id.length() <= 16);
    }

    public static void main(String argv[]) throws Exception {
        String fromclient;

        ServerSocket Server = new ServerSocket(5000);
        System.out.println(Server.getInetAddress());
        System.out.println(Server.getLocalSocketAddress());
        System.out.println(Server.getLocalPort());

        System.out.println("TCPServer Waiting for client on port 5000"); //client should use 5000 as port

        while (true) {
            Socket connected = Server.accept();
            System.out.println(" THE CLIENT" + " " + connected.getInetAddress()
                    + ":" + connected.getPort() + " IS CONNECTED ");

            BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader(connected.getInputStream()));


            boolean close = false;

            while (!close) {

                fromclient = inFromClient.readLine();

                //convert received message to json object
                Object object = null;
                JSONParser jsonParser = new JSONParser();
                object = jsonParser.parse(fromclient);
                JSONObject j_object = (JSONObject) object;

                if (hasKey(j_object, "type")) {
                    //check new identity format
                    if (j_object.get("type").equals("newidentity") && j_object.get("identity") != null) {
                        String id = j_object.get("identity").toString();
                        if (checkID(id)) {
                            System.out.println("Recieved correct ID type::" + fromclient);
                        } else {
                            System.out.println("Recieved wrong ID type:: The identity must be an alphanumeric string starting with an upper or lower case character. It must be at least 3 characters and no more than 16 characters long.");
                        }
                    }
                } else {
                    System.out.println("Something went wrong");
                }
            }
            connected.close();

        }
    }
}