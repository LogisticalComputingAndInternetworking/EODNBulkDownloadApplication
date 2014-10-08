package bulkdownloadapplication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class InstanceServer extends Thread
{
    public static final int PORT = 9999;
    
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    
    public void run() 
    {
        try
        {
            // Create the server socket
            serverSocket = new ServerSocket(PORT, 1);
            
            while (true)
            {
                // Wait for a connection
                clientSocket = serverSocket.accept();
                clientSocket.close();
            }
        }
        catch (IOException ioe)
        {
        }
    }
}
