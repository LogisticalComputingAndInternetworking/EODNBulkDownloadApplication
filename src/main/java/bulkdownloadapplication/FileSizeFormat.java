package bulkdownloadapplication;

import java.text.DecimalFormat;

public class FileSizeFormat
{
    public static final int B = 0;
    public static final int KB = 1;
    public static final int MB = 2;
    public static final int GB = 3;
    public static final int TB = 4;
    
    public static String format(double size)
    {
        String label = "";

        if ( size < 1024.0 )
        {
            label = "B";
        }
        else if ( size < 1048576.0 )
        {
            label = "KB";
            size = size / 1024.0;
        }
        else if ( size < 1073741824.0 )
        {
            label = "MB";
            size = size / 1048576.0;
        }
        else if ( size < 1099511627776.0 )
        {
            label = "GB";
            size = size / 1073741824.0;
        }
        else
        {
            label = "TB";
            size = size / 1099511627776.0;
        }

        DecimalFormat sizeFormat = new DecimalFormat("#,##0.0");

        return sizeFormat.format(size) + ' ' + label;
    }
    
    public static String format(double size, int units)
    {
        String label = "";
        
        switch(units)
        {
            case B : label = "B";
                break;
            case KB : label = "KB";
                size = size / 1024.0;
                break;
            case MB : label = "MB";
                size = size / 1048576.0;
                break;
            case GB : label = "GB";
                size = size / 1073741824.0;
                break;
            case TB : label = "TB";
                size = size / 1099511627776.0;
                break;
        }
        
        DecimalFormat sizeFormat = new DecimalFormat("#,##0.0");
        
        return sizeFormat.format(size) + ' ' + label;
    }
}
