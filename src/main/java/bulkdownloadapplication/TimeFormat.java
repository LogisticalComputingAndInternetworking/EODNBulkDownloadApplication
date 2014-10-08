package bulkdownloadapplication;

class TimeFormat
{   
    public static String format(int seconds)
    {
        if (seconds < 60) // seconds
        {
            return getSuffix(seconds, "second");
        }
        else if (seconds > 60 && seconds < 3600) // minutes
        {
            return getSuffix( (seconds / 60), "minute") + ", " + 
                getSuffix( (seconds % 60), "second"); 
        }
        else // hours
        {
           int h = seconds / 3600;
           int m = (seconds % 3600) / 60;
           int s = (seconds % 3600) % 60;
           
           return getSuffix(h, "hour") + ", " +
               getSuffix(m, "minute") + ", " +
               getSuffix(s, "second");
        }
    }
    
    public static String getSuffix(int value, String units)
    {
        if (value == 1)
        {
            return value + " " + units;
        }
        else
        {
            return value + " " + units + "s";
        }
    }
}
