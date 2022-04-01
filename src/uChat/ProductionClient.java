package uChat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.*;
import static java.lang.System.exit;

public class ProductionClient {
    Stack<String> messages = new Stack<>();

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
                ClientThread clientThread = new ClientThread(clientSocket);
                clientThread.start();
                this.messages.add(clientThread.getReader().readLine());
                ports.add(i);
            } catch (IOException e) {
                System.err.println("$ Bad Network with" + "localhost:" + i + "!");
            }
            i--;
        }
        chatWithClients(reader, username, serverThread);
        System.out.println("Connected clients");
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
     * All available ports in the localhost
     */
    public List<Integer> findAvailablePorts(ServerThread serverThread){
        int myPort = serverThread.getPort(), i = 6000, flag = 1;
        List<Integer> availablePorts = new ArrayList<>();

        while (flag == 1){
            try {
                Socket socket = new Socket("localhost", i);
                if (i != myPort){
                    availablePorts.add(i);
                }
            }catch (IOException e){
                flag = -1;
            }
            i++;
        }

        return availablePorts;
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
                    this.messages.add(user + "%--%" + msgToSend);
                    // this is to see if it's printing all available ports or not
                    System.out.println(findAvailablePorts(serverThread));
                }
            }
            exit(0);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
