import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ServerSocket server;

    public Server() throws Exception {
//        if (ipAddress != null && !ipAddress.isEmpty())
//            this.server = new ServerSocket(4444, 1, InetAddress.getByName(ipAddress));
//        else
        this.server = new ServerSocket(4444, 1, InetAddress.getLocalHost());
    }

    private void listen() throws Exception {
        String data = null;
        Socket client = this.server.accept();
        String clientAddress = client.getInetAddress().getHostAddress();
        System.out.println("\r\nNew connection from " + clientAddress);

        // create a new thread object
        ClientHandler clientSock = new ClientHandler(client);

        // This thread will handle the client separately
        new Thread(clientSock).start();

    }
    public InetAddress getSocketAddress() {
        return this.server.getInetAddress();
    }

    public int getPort() {
        return this.server.getLocalPort();
    }

    public static void main(String[] args) throws Exception {
        Server app = new Server();
        System.out.println("\r\nRunning Server: " +
                                   "Host=" + app.getSocketAddress().getHostAddress() +
                                   " Port=" + app.getPort());

        app.listen();
    }

    // ClientHandler class
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        // Constructor
        public ClientHandler(Socket socket)
        {
            this.clientSocket = socket;
        }

        public void run()
        {
//
//        BufferedReader in = new BufferedReader(
//                new InputStreamReader(client.getInputStream()));
//        while ( (data = in.readLine()) != null ) {
//            System.out.println("\r\nMessage from " + clientAddress + ": " + data);
//        }
            PrintWriter out = null;
            BufferedReader in = null;
            try {

                // get the outputstream of client
                out = new PrintWriter(
                        clientSocket.getOutputStream(), true);

                // get the inputstream of client
                in = new BufferedReader(
                        new InputStreamReader(
                                clientSocket.getInputStream()));

                String line;
                while ((line = in.readLine()) != null) {

                    // writing the received message from client
                    System.out.println("\r\nMessage from " + clientSocket.getInetAddress().getHostAddress() + ": " + line);
                    System.out.printf(
                            " Sent from the client: %s\n",
                            line);
                    out.println(line);
                }
            }
            catch ( IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
