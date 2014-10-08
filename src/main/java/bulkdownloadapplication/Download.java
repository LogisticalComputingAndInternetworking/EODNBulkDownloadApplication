package bulkdownloadapplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Download extends Observable implements Runnable
{
    private static final int MAX_BUFFER_SIZE = 4096;
    
    public static final String STATUSES[] = {"TODO: LIST OF STATUSES HERE"};
    
    // TODO: Create statuses for a download

    private int fileID;
    private URL url; 				// download URL
    private String entityID;
    private String dataSet;
    private String directory; 		// location to save the download
    private long size; 				// size of download in bytes
    private long downloaded; 		// number of bytes downloaded
    private int status; 			// current status of download
    private float currentSpeed; 	// current download speed (Bps)
    private float averageSpeed; 	// average download speed (Bps)
    private double alpha = 0.25; 	// smoothing factor
    private Timer downloadTimer;
    private DownloadTimerTask downloadTimerTask;
    @SuppressWarnings("unused")
	private String timeDescription, sizeDescription, speedDescription;
    private String downloadDescription = "Pending...";
    private long downloadedLast;
    private String errorTitle;
    private String errorMessage;
    private String eulaCode;

    // Constructor for Download.
    public Download(int fileID,
            long size, 
            String entityID, 
            String dataSet, 
            String directory, 
            String eulaCode,
            int status)
    {
        this.fileID = fileID;
        this.entityID = entityID;
        this.dataSet = dataSet;
        
        // If the directory path does not include an ending separator, add one        
        this.directory = (Character.toString(directory.charAt(directory.length() - 1)).equals(Settings.PATH_SEPARATOR))? directory : directory + Settings.PATH_SEPARATOR;
        
        this.size = size;
        this.eulaCode = eulaCode;
        downloaded = 0;
        this.status = (status == 0)? 0 : 0;
    }

    // Set the directory to save this download.
    public void setDirectory(String directory)
    {
        // If the directory path does not include an ending separator, add one
        this.directory = (Character.toString(directory.charAt(directory.length() - 1)).equals(Settings.PATH_SEPARATOR))? directory : directory + Settings.PATH_SEPARATOR;
        
        // Since the directory has changed, they should have nothing downloaded
        // (unless completed already)
        if (status != 0) // TODO: Use a meaningful status value for "Complete"
        {
            this.downloaded = 0;
        }
    }

    // Set the status of the download.
    public void setStatus(int status)
    {
        if (status > -1 && status < STATUSES.length)
        {
        	if (status == 0) // TODO: Use a meaningful status value for "Complete"
        	{
        		this.downloaded = this.size;
        	}
        	
            this.status = status;
            
            createDownloadDescriptionString();
            
            stateChanged();
        }
    }
    
    // Set the URL of the download.
    public void setUrl(URL url)
    {
        this.url = url;
    }
    
    // Set the EULA code of the download.
    public void setEULACode(String eulaCode)
    {
        this.eulaCode = eulaCode;
    }

    // Start the download
    public void beginDownload()
    {
        if (status == 0) // TODO: Use a meaningful status value for "Paused"
        {
            return;
        }
        
        setStatus(0);
        
        download();
    }

    // Get this download's EULA Code.
    public String getEULACode()
    {
        return eulaCode;
    }
    
    // Get this download's file ID.
    public int getFileID() 
    {
        return fileID;
    }

    // Get this download's URL.
    public String getUrl() 
    {
        return url.toString();
    }

    // Get this download's Entity ID.
    public String getEntityID() 
    {
        return entityID;
    }

    // Get this download's Data Set.
    public String getDataSet() 
    {
        return dataSet;
    }

    // Get this download's size.
    public long getSize() 
    {
        return size;
    }

    // Get this download's progress.
    public float getProgress() 
    {
        return ((float) downloaded / size) * 100;
    }

    public int getStatus() 
    {
        return status;
    }
    
    public void setError(String title, String message)
    {
        errorTitle = title;
        errorMessage = message;
    }
    
    public String getErrorTitle()
    {
        return errorTitle;
    }
    
    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void pause() 
    {
        Thread.currentThread().interrupt();
        
        stopDownloadTimer();
        
        setStatus(0); // TODO: Use a meaningful status value for "Paused"
    }
    
    public void stop()
    {
        Thread.currentThread().interrupt();
        
        stopDownloadTimer();
        
        // If the file is completed or in error state, do not run stop()
        if (status != 0 && status != 0) // TODO: Use a meaningful status value for "Error"
        {
            setStatus(0); // TODO: Use a meaningful status value for "Stopped"
        }
    }

    public void resume() 
    {
        status = 0; // TODO: Use a meaningful status value for "Active"
        timeDescription = "Resuming";
        createDownloadDescriptionString();
        
        // This should never happen but in the off chance it does, check if
        // downloadTimerTask exists before trying to run it.
        if (downloadTimerTask != null)
        {
            downloadTimerTask.run();
        }
        
        stateChanged();
        download();
    }

    public void skip() 
    {
        Thread.currentThread().interrupt();
        
        stopDownloadTimer();
        
        setStatus(0); // TODO: Use a meaningful status value for "Skipped"
    }

    private void error() 
    {
        Thread.currentThread().interrupt();
        
        stopDownloadTimer();
        
        setStatus(0); // TODO: Use a meaningful status value for "Error"
    }

    private void download() 
    {        
        Thread thread = new Thread(this);
        thread.start();
    }

    // Get file name portion of URL.
    private String getFileName(URL url)
    {
        String fileName = url.getPath();
        
        // Remove the directory path and retrieve just the filename
        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }
    
    public String getDownloadDescription()
    {
        return downloadDescription;
    }
    
    private void createDownloadDescriptionString()
    {
        switch (status)
        {
        	// TODO: Handle the different states of download and modify the description labels accordingly
        }
    }
    
    class DownloadTimerTask extends TimerTask
    {
        int iterations = 0;
        
        public void run()
        {
            if (iterations == 0)
            {
                // Set initial values
                downloadedLast = downloaded;
                averageSpeed = currentSpeed;
            }
            else if (iterations > 1)
            {
                createDownloadDescriptionString();
            }
            
            // Calculate download speed
            speedDescription = FileSizeFormat.format(calculateCurrentSpeed()) + "/sec";
            
            // Calculate the average speed
            averageSpeed = calculateAverageSpeed();
            
            // Calculate time remaining in seconds
            timeDescription = TimeFormat.format(calculateTimeRemaining()) + " remaining";
            
            // Calculate DOWNLOADED of TOTAL
            sizeDescription = FileSizeFormat.format(downloaded) + " of " + FileSizeFormat.format(size);
            
            // Set the description
            if (status == 0) // TODO: Use a meaningful status value for "Active"
            {
                stateChanged();
            }
            else
            {
                Thread.currentThread().interrupt();
                stopDownloadTimer();
                return;
            }
            
            iterations++;
        }
    }
    
    private void runDownloadTimer()
    {
        downloadTimer = new Timer();
        downloadTimerTask = new DownloadTimerTask();
        downloadTimer.schedule(downloadTimerTask, 0, 1 * 1000);
    }
    
    private void stopDownloadTimer()
    {
        if (downloadTimerTask != null)
        {
            downloadTimerTask.cancel();
            downloadTimerTask = null;
        }
        
        if (downloadTimer != null)
        {
            downloadTimer.cancel();
            downloadTimer = null;
        }
    }
    
    private float calculateAverageSpeed()
    {   
        return (float) (alpha * currentSpeed + (1 - alpha) * averageSpeed); 
    }
    
    private int calculateTimeRemaining()
    {
        long sizeRemaining = size - downloaded;
        int secondsRemaining = (int) (sizeRemaining / averageSpeed);
        
        return secondsRemaining;
    }
    
    private float calculateCurrentSpeed()
    {
        currentSpeed = downloaded - downloadedLast;
        downloadedLast = downloaded;
        
        return currentSpeed;
    }

    // Download file.
    public void run()
    {   
        RandomAccessFile file = null;
        InputStream stream = null;

        // Check if the URL is set.
        if (url == null)
        {
            this.errorTitle = "Error";
            this.errorMessage = "URL is null. The download will be skipped.";
            
            error();
            return;
        }

        try
        {
            // Open connection to URL.
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Specify what portion of file to download.
            connection.setRequestProperty("Range", "bytes=" + downloaded + "-");
            
            // Give the server 5 minutes to respond
            connection.setReadTimeout(5 * 60 * 1000);

            // Connect to server.
            connection.connect();
            
            // Make sure response code is in the 200 range.
            if (connection.getResponseCode() / 100 != 2)
            {
                // Set the error message since we know why it failed
                switch (connection.getResponseCode())
                {
                    case 403 :
                        this.errorTitle = "403 Forbidden";
                        this.errorMessage = "You do not have permission to access the download for scene " + this.entityID + " on this server. It will be skipped.";
                        break;
                    case 404 :
                        this.errorTitle = "404 Not Found";
                        this.errorMessage = "The download for scene " + this.entityID + " does not exist. It will be skipped.";
                        break;
                    default : 
                        this.errorTitle = connection.getResponseCode() + " Notice";
                        this.errorMessage = "The download for scene " + this.entityID + " is unavailable at this time. It will be skipped.";
                        break;
                }
                
                if (this.status != 0) // TODO: Use a meaningful status value for "Active"
                {
                    return;
                }
                
                error();
                return;
            }

            // Check for valid content length.
            long contentLength = connection.getContentLength();
            
            /** 
             * Note: getContentLength() cannot handle file sizes that are larger
             * than an integer can hold. If the contentLength comes back as -1,
             * get the Content-Length header field and convert to a larger data
             * type.
             */
            if (contentLength < 0)
            {
                if (connection.getHeaderField("Content-Length") == null)
                {
                    contentLength = -1;
                }
                else
                {
                    contentLength = Long.parseLong(connection.getHeaderField("Content-Length"));
                }
            }
            
            if (contentLength < 1)
            {
                this.errorTitle = "Content-Header Missing";
                this.errorMessage = "The content header is missing for the scene " + this.entityID + ". Please notify customer service (custserv@usgs.gov) about this scene.";
                
                error();
                return;
            }
            
            /** 
             * Handle the EULA Stuff
             */
            // Create the EULA directory
            if (eulaCode.length() > 0)
            {
                File eulaDir = new File(directory + eulaCode + Settings.PATH_SEPARATOR);
                
                if (!eulaDir.exists())
                {
                    // Make the directory
                    eulaDir.mkdir();
                    
                    // TODO: Request the EULA information

                    String eulaText = "";
                                        
                    // Save the EULA text file
                    String eulaTxtPath = directory + eulaCode + Settings.PATH_SEPARATOR + "EULA.txt";
                    File eulaTxt = new File(eulaTxtPath);
                    
                    // Fix the newlines
                    eulaText = eulaText.replaceAll("\\r?\\n|\\n", "\\\r\\\n");
                    
                    try
                    {
                        eulaTxt.createNewFile();
                        FileWriter fstream = new FileWriter(eulaTxtPath);
                        BufferedWriter out = new BufferedWriter(fstream);
                        out.write(eulaText);
                        out.close();
                    }
                    catch (IOException e)
                    {
                        this.errorTitle = "Could Not Write EULA";
                        this.errorMessage = e.getMessage();
                        
                        error();
                        return;
                    }
                }
            }
            
            // Create the file name
            String filePath = directory + eulaCode + Settings.PATH_SEPARATOR;
            String fileName = getFileName(url);
            
            // Get the file name from the Content-Disposition header if necessary
            if (fileName.lastIndexOf('.') == -1)
            {
                /**
                 * any character any number of times, ignore case sensitive checking,
                 * filename=, start group #1, any character other than ';' any number
                 * of times, end group #1, ';' optional
                 */
                final String FILENAME_PATTERN = ".*(?i)filename=([^;]*);?";
                
                String contentDisposition = connection.getHeaderField("Content-Disposition");
                
                Pattern pattern = Pattern.compile(FILENAME_PATTERN);
                Matcher matcher = pattern.matcher(contentDisposition);

                if (matcher.find())
                {
                    // Get the file name from the match
                    fileName = matcher.group(1);
                }
                else
                {
                    fileName += ".jpg";
                }
            }
            
            fileName = filePath + fileName;
            
            while (downloaded == 0 && (new File(fileName).exists()))
            {
                // Create the new file name for the copy
                fileName = createCopyFileName(fileName);
            }
            
            // Open file and seek to the end of it.
            file = new RandomAccessFile(fileName, "rw");
            file.seek(downloaded);

            stream = connection.getInputStream();
            
            runDownloadTimer();
            
            // TODO: Update the database to show the file was downloaded
            
            while (status == 0) // TODO: Use a meaningful status value for "Active"
            {
                if ( size == downloaded)
                {
                    // Stop calculating time
                    stopDownloadTimer();
                    
                    // Change the description to reflect the file finished
                    downloadDescription = "Done";
                    
                    setStatus(0); // TODO: Use a meaningful status value for "Complete"
                    return;
                }
                
                /**
                 * Size buffer according to how much of the file is left to download.
                 */
                byte buffer[];
                if (size - downloaded > MAX_BUFFER_SIZE)
                {
                    buffer = new byte[MAX_BUFFER_SIZE];
                }
                else if ( downloaded > size )
                {
                    buffer = new byte[(int) size];
                }
                else
                {
                    buffer = new byte[(int) (size - downloaded)];
                }
                
                // Read from server into buffer.
                int read = stream.read(buffer);
                if (read == -1)
                {
                    break;
                }

                // Write buffer to file.
                file.write(buffer, 0, read);
                downloaded += read;
                stateChanged();
            }

            /**
            * Change status to complete if this point was reached because downloading
            * has finished.
            */
            if (status == 0) // TODO: Use a meaningful status value for "Active"
            {
                // Stop calculating time
                stopDownloadTimer();
                
                // Change the description to reflect the file finished
                downloadDescription = "Done";
                
                setStatus(0); // TODO: Use a meaningful status value for "Complete"
                return;
            }
        }
        catch (FileNotFoundException e)
        {
            this.errorTitle = "Could Not Locate File";
            this.errorMessage = e.getMessage();
            
            error();
            return;
        }
        catch (IOException e)
        {
            this.errorTitle = "Could Not Write File";
            this.errorMessage = e.getMessage();

            error();
            return;
        }
        finally
        {
            // Close file.
            if (file != null)
            {
                try 
                {
                    file.close();
                } 
                catch (IOException e) 
                {
                    this.errorTitle = "Could Not Close File";
                    this.errorMessage = e.getMessage();

                    error();
                    return;
                }
            }

            // Close connection to server.
            if (stream != null)
            {
                try 
                {
                    stream.close();
                } 
                catch (IOException e) 
                {
                    this.errorTitle = "Could Not Close Stream";
                    this.errorMessage = e.getMessage();
                    
                    error();
                    return;
                }
            }
            
            // Stop the download timer
            stopDownloadTimer();
        }
    }

    private String createCopyFileName(String fullName)
    {
        int splitPos = 0;
        String fileName = "";
        String fileExtension = "";

        /** 
         * start group #1, any character any number of times, start group #2, 
         * dot (.), ignore case sensitive checking, start group #3, tar.gz 
         * OR tar.bz2 OR opt.gz OR hgt.zip, end group #3, end group #2, 
         * end of string, end of group #1
         */
        final String EXTENSION_PATTERN = "(.*(\\.(?i)(tar\\.gz|tar\\.bz2|opt\\.gz|hgt\\.zip))$)";
        
        /**
         * any character other than '(' any number of times, parenthesis '(',
         * start group #1, a digit one or more times, parenthesis ')',
         * end group #1, end of string
         */
        final String NUMBER_PATTERN = "[^\\(]*\\((\\d+)\\)$";

        // If the extension is .tar.gz, .tar.bz2, .opt.gz, or .hgt.zip
        if (Pattern.compile(EXTENSION_PATTERN).matcher(fullName).matches())
        {
            splitPos = fullName.substring(0, fullName.lastIndexOf('.')).lastIndexOf('.');
        }
        else
        {
            splitPos = fullName.lastIndexOf('.');
        }

        fileName = fullName.substring(0, splitPos);

        // Check if the file name already has a number appended
        Pattern pattern = Pattern.compile(NUMBER_PATTERN);
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find())
        {
            // Obtain the copy number and add 1, then replace in fileName
            int copyNumber = Integer.parseInt(matcher.group(1)) + 1;
            String replaceStr = '(' + Integer.toString(copyNumber) + ')';
            fileName = Pattern.compile("\\(\\d+\\)").matcher(fileName).replaceAll(replaceStr);
        }
        else
        {
           // Append the copy number
           fileName += " (1)";
        }

        fileExtension = fullName.substring(splitPos);

        return fileName + fileExtension;
    }

    private void stateChanged()
    {
        setChanged();
        notifyObservers();
    }
}
