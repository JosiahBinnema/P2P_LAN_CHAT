package uChat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientThread extends Thread{
    private final BufferedReader reader;

    public ClientThread(Socket socket) throws IOException {
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /**
     * This is used to filter the message get from another client, reciprocal of what was done in chatWithClient method
        of ProductionClient class.
     */
    private String filterMessage(String msg){
        String[] arr = msg.split("%--%");

        return String.format("%s (By: %s)", arr[1], arr[0]);
    }

    /**
     * This overridden method of thread reads the data when this class's instance is created and print the message after
        filtering it on the console.
     */
    @Override
    public void run(){
        boolean flag = true;
        while (flag){
            try {
                String msg = reader.readLine();
                System.out.println("$ " + filterMessage(msg));
            } catch (IOException e) {
                flag = false;
                interrupt();
            }
        }
    }
}
