import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.ServerSocket;
import java.net.Socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.json.simple.parser.ParseException;
import java.io.IOException;


public class ChatServer {
    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(ChatServer.class);

        logger.debug("Application Started");

        try {
            ServerSocket serverSocket = new ServerSocket(6666);
            System.out.println(serverSocket.getInetAddress());
            System.out.println(serverSocket.getLocalSocketAddress());
            System.out.println(serverSocket.getLocalPort());


            Socket socket = serverSocket.accept();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));

            boolean close = false;
            JSONParser jsonParser = new JSONParser();

            while (!close) {
                String response = bufferedReader.readLine();

                try {
                    JSONObject obj = (JSONObject) jsonParser.parse(response);
                    logger.debug("Obj : " + obj);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

//                close = str.contains("#quit");
            }

            serverSocket.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
