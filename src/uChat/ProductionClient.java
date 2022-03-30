package uChat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class ProductionClient {
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("<- Enter your username: ");
            String username = reader.readLine();

            //find a free port and find other clients' ports
            int startPort = 6000;
            int endPort = 6999;
            int port = findFreePort(startPort, endPort);
            ServerThread serverThread = new ServerThread(port);
            serverThread.start();
            System.out.println("you're on port " + port);

            //list of ports I'm listening to
            List<Integer> ports = new ProductionClient().updateClients(reader, username, serverThread, startPort);
        }catch (IOException io){
            io.printStackTrace();
        }
    }

    /**
     * DOESN'T WORK.
     * needs to be finished
     * tells all clients in the list to update their list of clients
     */
    private void syncPorts(List<Integer> ports, ServerThread serverThread) {
        serverThread.sendMessage("You need to update your list of clients!" + ports.toString());
        System.out.println("told them to update their porst! " + ports.toString());
    }

    /**
     * listens to each active port with a lower value than this one. and starts chatting with them.
     */
    public List<Integer> updateClients(BufferedReader reader, String username, ServerThread serverThread, int startPort) throws IOException {
        int myPort = serverThread.getPort();
        int i = myPort - 1;
        List<Integer> ports = new ArrayList<>();
        while (i >= startPort) {
            try {
                System.out.println("listening to localhost:" + i);
                Socket clientSocket = new Socket("localhost", i);
                new ClientThread(clientSocket).start();
                ports.add(i);
            } catch (IOException e) {
                System.err.println("$ Bad Network with" + "localhost:" + i + "!");
            }
            i--;
        }
        chatWithClients(reader, username, serverThread);
        System.out.println("updated clients");
        syncPorts(ports, serverThread);
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
     * OUTDATED
     * it's binding all the clients with this client connected with the same hosting port or in the same chat room,
        including this client, and then starting the chat with them.
     Required: somehow make the input better than just writing :, & symbols between the names.
     */
    public void updateToClients(BufferedReader reader, String user, ServerThread serverThread) throws IOException {
        System.out.println("<- Enter #s to skip the need to write the hostNames and hostingPorts!");
        System.out.print("<- Enter multiple hostNames and hostingPorts (hostName:hostingPort&hostName:hostingPort....): \n");



        String line = reader.readLine();
        String[] userArray = line.split("&");

        if(!line.toLowerCase().trim().equals("#s")) {
            for (int i = 0; i < userArray.length; i++) {
                String[] hostingPorts = userArray[i].split(":");
                Socket clientSocket = null;
                try {
                    System.out.println(hostingPorts[0] + Integer.parseInt(hostingPorts[1]));
                    clientSocket = new Socket(hostingPorts[0], Integer.parseInt(hostingPorts[1]));
                    new ClientThread(clientSocket).start();
                } catch (IOException e) {
                    System.err.println("$ Bad Network with" + hostingPorts[0] + hostingPorts[1] + "!");
                }
            }
        }

        chatWithClients(reader, user, serverThread);
    }


    /**
     * it's binding all the clients with this client connected with the same hosting port or in the same chat room
     */
    public void chatWithClients(BufferedReader reader, String user, ServerThread serverThread){
        try {
            System.out.print("Welcome to the chat room! Message #e to exit.\n");
            boolean isChattingAllowed = true;

            while (isChattingAllowed){
                String msgToSend = reader.readLine().trim();
                String msg = msgToSend.toLowerCase();

                if (msg.equals("#e")){
                    isChattingAllowed = false;
                    System.out.println("Goodbye!");
                }
                else{
                    serverThread.sendMessage(user + "%--%" + msgToSend);
                }
            }
            exit(0);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
