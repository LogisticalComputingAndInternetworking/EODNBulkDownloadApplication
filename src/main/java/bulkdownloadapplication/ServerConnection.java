package bulkdownloadapplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.swing.JOptionPane;

public class ServerConnection extends Thread
{
    private static final ServerConnection INSTANCE = new ServerConnection();
    
    // TODO: Create statuses for the connection 
    
    private String HOST_NAME = "";
    private int PORT_NUMBER = 0;
    private String requester = "";
    
    private SocketFactory socketFactory = (SocketFactory) SocketFactory.getDefault();
    private Socket socket = null;
    
    private boolean reconnecting = false;
    
    private Identity identity = null;
    
    private PrintWriter out = null;
    private BufferedReader in = null;
    
    private boolean disconnect = false;
    
    private ServerConnection()
    {
        // Prevent instantiation
    }
    
    public static ServerConnection getInstance()
    {
        return INSTANCE;
    }
    
    public void setRequester(String requester)
    {
        this.requester = requester;
    }
    
    public void setHostName(String host)
    {
        this.HOST_NAME = host;
    }
    
    public String getHostName()
    {
        return this.HOST_NAME;
    }
    
    public void setPort(int port)
    {
        this.PORT_NUMBER = port;
    }
    
    public int getPort()
    {
        return this.PORT_NUMBER;
    }
    
    public String getRequester()
    {
        return this.requester;
    }
    
    public boolean isServerAvailable()
    {
        Socket sock = null;
        
        try
        {
            sock = (Socket)socketFactory.createSocket(HOST_NAME, PORT_NUMBER);
        }
        catch (UnknownHostException e)
        {
            return false;
        }
        catch (IOException e)
        {
            return false;
        }
        finally 
        {
            if (sock != null)
            {
                // Close the connection
                try
                {
                    sock.close();
                }
                catch (IOException e)
                {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public boolean setKeepAlive(boolean keepAlive)
    {
        try
        {
            socket.setKeepAlive(keepAlive);
        }
        catch (SocketException e)
        {
            return false;
        }
        
        return true;
    }
    
    public boolean isServerReconnecting()
    {
        return reconnecting;
    }
    
    private void createSocket()
    {        
        try
        {
            socket = (Socket) socketFactory.createSocket(HOST_NAME, PORT_NUMBER);
        }
        catch (UnknownHostException e)
        {
        }
        catch (IOException e)
        {
        }
    }
    
    public boolean reconnect()
    {
        // Close the old connection
        disconnect();
        
        disconnect = false;
        
        // Reset the Socket, PrintWriter, and BufferedReader
        try 
        {
            createSocket();
            
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (UnknownHostException e)
        {
            return false;
        }
        catch (IOException e)
        {
            return false;
        }
        
        return true;
    }
    
    public void disconnect()
    {
        disconnect = true;
        
        try
        {
            out.close();
            in.close();
            socket.close();
        }
        catch (IOException e)
        {
        }
    }
    
    public void setIdentity(String username, String password)
    {
        if (!hasIdentity())
        {
            identity = new Identity(username, password);
        }
        else
        {
            identity.setUsername(username);
            identity.setPassword(password);
        }
    }
    
    public Identity getIdentity()
    {
        return identity;
    }
    
    public boolean hasIdentity()
    {
        return (identity == null)? false : true;
    }
    
    // Only used for reconnecting
    public boolean authenticate(boolean disconnect)
    {   
        if (!hasIdentity())
        {
            return false;
        }
        
        // TODO: Authenticate the user with the stored identity credentials
        
        return true;
    }
    
    public boolean authenticate()
    {
        return authenticate(false);
    }
    
    public void sendRequest(Object object) 
    {
    	// TODO: Send a request to the server
    }
    
    public void run()
    {
        try 
        {
            if (socket == null)
            {
                createSocket();
            }
            
            if (out == null)
            {
                out = new PrintWriter(socket.getOutputStream(), true);
            }
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (UnknownHostException e)
        {
            JOptionPane.showMessageDialog(BulkDownloadApplication.getWindowFrame(), 
                    "The bulk download service is unavailable at this time. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(BulkDownloadApplication.getWindowFrame(), 
                    "The bulk download service is unavailable at this time. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        while (disconnect == false)
        {
        }

        out.close();
        try
        {
            in.close();
            socket.close();
        }
        catch (IOException e)
        {
        }
    }
    
    public void showReconnectDialog()
    {
        ReconnectDialog reconnectDialog = new ReconnectDialog(BulkDownloadApplication.getWindowFrame(), true, "Reconnecting...");
        reconnectDialog.beginReconnect();
        reconnectDialog.showDialog();
    }
}
