package uChat;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class ServerThread extends Thread{
    private final ServerSocket serverSocket;
    private final LinkedList<ChildThread> childThreadList = new LinkedList<>();
    private final int port;
    private List<Integer> portList;

    public ServerThread(int portNumber) throws IOException {
        this.port = portNumber;
        this.serverSocket = new ServerSocket(portNumber);
    }

    public int getPort() {
        return this.port;
    }

    public void setPortList(List<Integer> portList) {
        this.portList = portList;
    }

    /**
     * This is simply sending the message using ChildThread class's method getWriter.
     */
    public void sendMessage(String msg){
        for (int i = 0; i < childThreadList.size(); i++){
            childThreadList.get(i).getWriter().write(msg);
            childThreadList.get(i).getWriter().write("\n");
            childThreadList.get(i).getWriter().flush();
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
                this.childThreadList.add(childThread);
                childThread.start();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * return the list.
     */
    public LinkedList<ChildThread> getChildThreadList() {
        return childThreadList;
    }
}
