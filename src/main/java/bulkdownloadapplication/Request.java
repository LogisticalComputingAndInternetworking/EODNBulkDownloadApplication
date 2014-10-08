package bulkdownloadapplication;

import java.util.Observable;

public class Request extends Observable implements Runnable
{
    // TODO: Create multiple states for the request
    
    // TODO: Create multiple types of requests
    
    @SuppressWarnings("unused")
    private static final String STATUSES[] = { "TODO: List of statuses here" };
    
    private int status = 0;
    private int requestType = -1;
    @SuppressWarnings("unused")
	private Object request = null;
    private Object result = null;
    
    protected static final int MAX_ATTEMPTS = 5;
    
    public Request(Object request, int requestType)
    {
        this.request = request;
        this.requestType = requestType;
    }
    
    public void beginRequest()
    {
        Thread thread = new Thread(this);
        thread.start();
    }
        
    public void setStatus(int status)
    {
        this.status = status;
        stateChanged();
    }
    
    public int getStatus()
    {
        return this.status;
    }
    
    public int getObjectRequestType()
    {
        return 0;
    }
    
    public int getRequestType()
    {
        return this.requestType;
    }
    
    // Send the request
    @SuppressWarnings("unused")
	private void send()
    {
        // Check if the server is available
        if (!ServerConnection.getInstance().isServerAvailable())
        {
            ServerConnection.getInstance().showReconnectDialog();
            
            return;
        }
        
        // TODO: Send the request to the server and await a response
        
        Thread.currentThread().interrupt();
    }
    
    // Message the server (no return needed)
    @SuppressWarnings("unused")
	private void message()
    {
        // TODO: Send the message to the server and continue
        
        Thread.currentThread().interrupt();
    }
    
    // Retrieve the message
    public Object retrieve()
    {
        return result;
    }
    
    // Interrupt the thread and cancel the request
    public void cancel()
    {
        Thread.currentThread().interrupt();
        
        setStatus(0);
    }
    
    public void run()
    {
        // TODO: Run the thread to send the request/message
    }
    
    private void stateChanged()
    {
        setChanged();
        notifyObservers();
    }
}
