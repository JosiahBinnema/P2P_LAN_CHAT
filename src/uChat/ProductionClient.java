package uChat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class ProductionClient {
    public static int startPort = 6000;
    public static int endPort = 6010;
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("<- Enter your username: ");
            String username = reader.readLine();

            //find a free port and find other clients' ports

            int port = findFreePort(startPort, endPort);
            ServerThread serverThread = new ServerThread(port);
            serverThread.start();
            System.out.println("you're on port " + port);
            List<Integer> ports = new ArrayList<>();
            //list of ports I'm listening to
            new ProductionClient().updateClients(reader, username, serverThread, startPort, endPort, true, ports);
        }catch (IOException io){
            io.printStackTrace();
        }
    }

    public void printConnections(List<ChildThread> ports) {
        System.out.println("$ Connected to: ");
        for (ChildThread port : ports) {
            System.out.println("$ " + port.getPort());
        }
    }

    /**
     * updates the list of clients this client is listening to
     */
    public List<Integer> updateClients(BufferedReader reader, String username, ServerThread serverThread,
                                       int startPort, int endPort, boolean isFirst, List<Integer> ports) throws IOException {
        int myPort = serverThread.getPort();
        int i = startPort;
        System.out.println("Searching for clients. Please wait...");
        while (i <= endPort) {
            if (i != myPort && !ports.contains(i)) {
                try {
                    Socket clientSocket = new Socket("localhost", i);
                    new ClientThread(clientSocket).start();
                    ports.add(i);

                    System.out.println("Client found on port " + i + "!");
                } catch (IOException e) {
                    ports.remove((Integer) i);
                }
            }
            i++;
        }
        System.out.println("$ Found " + ports.size() + " clients!");
        if (isFirst) {
            chatWithClients(reader, username, serverThread, ports);
        }
        return ports;
    }

    /**
     * find a free port on the network (starts at 6000)
     */
    public static int findFreePort(int startPort, int endPort) {
        while (startPort <= endPort) {
            try {
                new Socket("localhost", startPort);
                startPort++;
            } catch (IOException e) {
                return startPort;
            }
        }
        System.err.println("$ No free ports found!");
        exit(-1);
        return -1;
    }

    /**
     * it's binding all the clients with this client connected with the same hosting port or in the same chat room
     */
    public void chatWithClients(BufferedReader reader, String user, ServerThread serverThread, List<Integer> ports) {
        try {
            System.out.print("Welcome to the chat room! Message #e to exit, #u to find new clients that may be listening to you.\n");
            boolean isChattingAllowed = true;

            while (isChattingAllowed){
                String msgToSend = reader.readLine().trim();
                String msg = msgToSend.toLowerCase();

                if (msg.equals("#e")){
                    isChattingAllowed = false;
                    System.out.println("Goodbye!");
                } else if (msg.equals("#u")){
                    updateClients(reader, user, serverThread,startPort, endPort, false, ports);
                }
                else{
                    try {
                        serverThread.sendMessage(user + "%--%" + msgToSend);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            exit(0);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
