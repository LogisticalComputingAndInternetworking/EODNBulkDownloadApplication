package bulkdownloadapplication;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message
{
    private String title;
    private String message;
    private Date date;
    
    public Message(String title, String message)
    {
        this.title = title;
        this.message = message;
        date = new Date();
    }
    
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    public void setMessage(String message)
    {
        this.message = message;
    }
    
    public void setDate(Date date)
    {
        this.date = date;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public String getMessage()
    {
        return message;
    }
    
    public Date getDate()
    {
        return date;
    }
    
    public String getFormattedDate()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
        
        return formatter.format(date);
    }
}
