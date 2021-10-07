import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public final class Server {

    private static Server server;
    public String serverid;
    public String server_conf_path;
    private String server_address;
    private int coordination_port;
    private int clients_port;

    private Server(String serverid, String server_conf_path) {
        this.serverid = serverid;
        this.server_conf_path = server_conf_path;

        setConf();
    }

    public static Server getInstance(String serverid, String server_conf_path) {
        if (server == null) {
            server = new Server(serverid, server_conf_path);
        }
        return server;
    }

    public void setConf() {
        try {
            File conf = new File( server_conf_path );
            Scanner myReader = new Scanner(conf);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] args = data.split( " " );
                System.out.println(data);
                if( args[0].equals( serverid ) ) {
                    server_address = args[1];
                    clients_port = Integer.parseInt(args[2]);
                    coordination_port = Integer.parseInt(args[3]);
                }
            }
            myReader.close();
        } catch ( FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
        }
    }

    public String getServer_address()
    {
        return server_address;
    }

    public int getCoordination_port()
    {
        return coordination_port;
    }

    public int getClients_port()
    {
        return clients_port;
    }
}
