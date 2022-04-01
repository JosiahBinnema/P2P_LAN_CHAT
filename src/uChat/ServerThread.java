package uChat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ServerThread extends Thread{
    private final int port;
    private List<Integer> portList;
    public int getPort() {
        return this.port;
    }
    private final ServerSocket serverSocket;
    private final LinkedList<ChildThread> childThread = new LinkedList<>();


    public ServerThread(int portNumber) throws IOException {
        this.port = portNumber;
        this.serverSocket = new ServerSocket(portNumber);
    }


    /**
     * This is simply sending the message using serverThread class's method getWriter.
     */
    public void sendMessage(String msg) throws InterruptedException {
        for (int i = 0; i < childThread.size(); i++){
            Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000)); //randomness here
            childThread.get(i).getWriter().write(msg);
            childThread.get(i).getWriter().write("\n");
            childThread.get(i).getWriter().flush();
        }
    }

    /**
     * this overridden method first creating a ChildThread by passing this ServerThread and the socket that is accepted
        by serverSocket, then adds the childThread to the list present in this class.
     */
    @Override
    public void run(){
        try {
            while (true) {
                ChildThread childThread = new ChildThread(serverSocket.accept(), this);
                this.childThread.add(childThread);
                childThread.start();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * getter for the list.
     */
    public LinkedList<ChildThread> getServerThreadThread() {
        return childThread;
    }
}
