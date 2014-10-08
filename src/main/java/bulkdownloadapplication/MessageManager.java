package bulkdownloadapplication;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;

public class MessageManager implements Observer
{
    private static final MessageManager INSTANCE = new MessageManager();
    private ArrayList<Growl> growlList = new ArrayList<Growl>();
    private HashMap<Integer, Message> messageMap = new HashMap<Integer, Message>();
    
    private static final int GROWL_MARGIN_X = 52;
    private static final int GROWL_MARGIN_Y = 135;
    private static final int GROWL_SPACER = 5;
    
    private MessageManager()
    {
        // Exists only to defeat instantiation
    }
    
    public static MessageManager getInstance()
    {
        return INSTANCE;
    }
    
    /**
     * Add the message to the list and display it to the user as a growl.
     */
    public void addMessage(int fileID, Message message)
    {
        // Add the message to the message map
        messageMap.put(fileID, message);
        
        displayGrowlMessage(message, false);
    }
    
    /**
     * Display a message as a growl. This can be used for application 
     * warnings/notices and also for re-displaying errors saved in the list.
     */
    public void displayGrowlMessage(Message message, boolean showDate)
    {
        if (message == null)
        {
            return;
        }
        
        // Add the message to the growl list
        Growl growl = null;
        
        int growlListSize = getGrowlListSize();
        
        // If the growl list already contains 4 errors, don't display more
        if (growlListSize > 3)
        {
            return;
        }
        
        String errorTitle = message.getTitle();
        String errorMessage = (showDate)? message.getFormattedDate() + ": " + message.getMessage() : message.getMessage();
        
        int growlPositionX, growlPositionY;
        
        // Determine the growl placement location
        if (getGrowlListSize() > 0)
        {
            Growl lastGrowl = growlList.get(getGrowlListSize() - 1);
            Point lastGrowlLocation = lastGrowl.getLocation();
            
            growl = new Growl(errorTitle, errorMessage);
            growl.setLocation(lastGrowlLocation.x, lastGrowlLocation.y - GROWL_SPACER - growl.getHeight());
            
            growlPositionX = lastGrowlLocation.x;
            growlPositionY = lastGrowlLocation.y - GROWL_SPACER - growl.getHeight();
        }
        else
        {
            growl = new Growl(errorTitle, errorMessage);
            JFrame mainFrame = BulkDownloadApplication.getWindowFrame();
            Point windowLocation = mainFrame.getLocation();
            int windowWidth = mainFrame.getWidth();
            int windowHeight = mainFrame.getHeight();
            int growlWidth = growl.getWidth();
            int growlHeight = growl.getHeight();
            growl.setLocation(windowLocation.x + windowWidth - GROWL_MARGIN_X - growlWidth,
                    windowLocation.y + windowHeight - GROWL_MARGIN_Y - growlHeight);
            
            growlPositionX = windowLocation.x + windowWidth - GROWL_MARGIN_X - growlWidth;
            growlPositionY = windowLocation.y + windowHeight - GROWL_MARGIN_Y - growlHeight;
        }
        
        // Attach the observer
        growl.addObserver(MessageManager.this);
        
        // Add the growl to the list
        growlList.add(growl);
        growl.setLocation(growlPositionX, growlPositionY);
    }
    
    public void moveMessages()
    {
        if (getGrowlListSize() < 1)
        {
            return;
        }
        
        // Get the application window's location
        JFrame mainFrame = BulkDownloadApplication.getWindowFrame();
        Point windowLocation = mainFrame.getLocation();
        int windowWidth = mainFrame.getWidth();
        int windowHeight = mainFrame.getHeight();
        
        // Retrieve the bottom growl
        Growl growl = growlList.get(0);
        
        // Set the location of the bottom growl
        growl.setLocation(windowLocation.x + windowWidth - GROWL_MARGIN_X - growl.getWidth(),
                windowLocation.y + windowHeight - GROWL_MARGIN_Y - growl.getHeight());
        
        // If there are more growls, set those as well
        if (getGrowlListSize() > 1)
        {
            for (int i = 1; i < getGrowlListSize(); i++)
            {
                Growl iterator = growlList.get(i);
                
                Growl lastGrowl = growlList.get(i - 1);
                Point lastGrowlLocation = lastGrowl.getLocation();
                
                iterator.setLocation(lastGrowlLocation.x, lastGrowlLocation.y - GROWL_SPACER - iterator.getHeight());
            }
        }
    }
    
    public Message getMessage(int fileID)
    {
        return (Message) messageMap.get(fileID);
    }
    
    public void clearMessage(int fileID)
    {
        messageMap.remove(fileID);
    }
    
    public void clearAll()
    {
        messageMap.clear();
    }
    
    public void hideGrowlMessages()
    {
        for (Growl i : growlList)
        {
            i.hide();
        }
    }
    
    public void showGrowlMessages()
    {
        for (Growl i : growlList)
        {
            i.show();
        }
    }
    
    public int getGrowlListSize()
    {
        return growlList.size();
    }
    
    public Growl getGrowl(int index)
    {
        return growlList.get(index);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        int index = growlList.indexOf(o);
        
        if (index < 0)
        {
            return;
        }
        
        // Get the updating Growl
        Growl growl = growlList.get(index);
        
        // Get the status returned
        int status = growl.getStatus();
        
        switch(status)
        {
            case Growl.DELETED :
                // If there are more growls
                if (getGrowlListSize() > 1 && (index + 1) < getGrowlListSize())
                {
                    Growl dropper = growlList.get(index + 1);
                    
                    // Decrease the vertical location limit for only the next growl above
                    dropper.decreaseVerticalLocationLimit(growl.getHeight() + GROWL_SPACER);
                }
                
                // Remove the Growl
                growlList.remove(index);
                break;
            case Growl.DROPPING :
                // Get the location for where the bottom-most growl should be
                Growl bottomGrowl = growlList.get(0);
                JFrame mainFrame = BulkDownloadApplication.getWindowFrame();
                Point windowLocation = mainFrame.getLocation();
                int windowHeight = mainFrame.getHeight();
                int bGrowlHeight = bottomGrowl.getHeight();
                growl.setVerticalLocationLimit(windowLocation.y + windowHeight - GROWL_MARGIN_Y - bGrowlHeight);
                
                // Get the number of growls
                int growlListSize = getGrowlListSize();
                
                // Calculate the vertical location limit for the rest of the growls
                if (growlListSize > 1)
                {
                    for (int i = 1; i < growlListSize; i++)
                    {
                        Growl iteratorGrowl = growlList.get(i);
                        
                        // Set the vertical location limit based on the one below
                        iteratorGrowl.setLocation(iteratorGrowl.getLocation().x,
                                growlList.get(i - 1).getLocation().y - GROWL_SPACER - iteratorGrowl.getHeight());
                    }
                }
                
                break;
        }
    }
}
