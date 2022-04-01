package uChat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChildThread extends Thread{
    private ServerThread serverThread;
    private Socket childThreadSocket;
    private PrintWriter writer;

    /**
     * This instance of the class is created when ServerThread class runs to store the client socket information, and
        its connected ServerThread class's instance.
     */
    public ChildThread(Socket socket, ServerThread serverThread){
        this.serverThread = serverThread;
        this.childThreadSocket = socket;
    }

    public int getPort(){
        return childThreadSocket.getPort();
    }

    /**
     * this overridden method removes this ChildThread from the list that serverThread contains if there is an exception
        with this class' socket
     */
    @Override
    public void run(){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.childThreadSocket.getInputStream()));
            this.writer = new PrintWriter(this.childThreadSocket.getOutputStream());

            while (true){
                this.serverThread.sendMessage(reader.readLine());
            }
        } catch (IOException e) {
            this.serverThread.getServerThreadThread().remove(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This returns the writer of this class to be used in ServerThread class.
     */
    public PrintWriter getWriter() {
        return writer;
    }


}
