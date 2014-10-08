package bulkdownloadapplication;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import bulkdownloadapplication.config.AppConfig;
import bulkdownloadapplication.config.AppPosition;
import bulkdownloadapplication.config.AppSettings;
import bulkdownloadapplication.config.GUIConfigs;

public class BulkDownloadApplication extends JFrame implements Observer
{
    private JLabel destinationLabel, fileNameLabel, progressDescriptionLabel, downloadListDescriptionLabel;
    protected int contactId, orderNumber;
    private JLabel freeSpaceLabel = new JLabel("");
    private DownloadsTableModel tableModel = new DownloadsTableModel();
    private JTable table;
    private JButton playPauseButton, skipButton, upButton, downButton, deleteButton;
    private JButton downloadButton, clearCompleteButton;
    private JProgressBar progressBar;
    private ImageIcon playIcon, pauseIcon;
    private Download selectedDownload;
    private boolean clearing;
    private String directory;
    private ArrayList<Download> downloadList;
    private OrdersDialog ordersDialog;
    private LoadingDialog loadingDialog;
    private double freeSpace = 0.0;
    private static final long serialVersionUID = 1;
    public static final Color evenColor = new Color(240, 240, 240);
    private boolean downloading = false;
    
    protected boolean recurringDownload = false;
    protected String identifier = "bda";
    
    JPanel downloadsPanel = new JPanel();
    
    /**
     * Timers
     */
    private RecurringDownloadTimerTask recurringDownloadTimerTask;
    private Timer recurringDownloadTimer;
    private KeepAliveTimerTask keepAliveTimerTask;
    private Timer keepAliveTimer;   
 
    private JPopupMenu popupMenu;
    private JMenuItem errorMenuItem;
    
    private LoginDialog loginDialog;
    protected Request currentRequest = null;
    
    @SuppressWarnings("serial")
    public BulkDownloadApplication()
    {
        loadXmlConfigurationFile();
        
        // Listen for the window closing
        addWindowListener(new WindowAdapter()
        {
             public void windowClosing(WindowEvent e)
             {
                 getContentPane().setVisible(false);
                 
                 // TODO: Close the connection to the server
                 
                 // Save the application settings to the config file
                 try
                 {
                     saveXmlConfigurationFile();
                 }
                 catch (JAXBException e1)
                 {
                 }
                 catch (IOException e1)
                 {
                 }
                 System.exit(0);
             }
             
             public void windowIconified(WindowEvent e)
             {
                 MessageManager.getInstance().hideGrowlMessages();
             }
             
             public void windowDeiconified(WindowEvent e)
             {
                 MessageManager.getInstance().showGrowlMessages();
             }
             
             public void windowActivated(WindowEvent e)
             {
                 MessageManager.getInstance().showGrowlMessages();
             }
        });
        
        // Listen for window resizing
        addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                // Move any growl messages that are currently displayed
                MessageManager.getInstance().moveMessages();
                
                // Add ellipsis to the destination path if it is too long
                createDirectoryLabel();
            }
            
            public void componentMoved(ComponentEvent e)
            {
                MessageManager.getInstance().moveMessages();
            }
        });
        
        // Set a minimum size for the window
        setMinimumSize(new Dimension(640, 480));
        
        // Menu Bar
        setJMenuBar(buildMenuBar());
        
        // Popup Menu
        popupMenu = new JPopupMenu();
        errorMenuItem = new JMenuItem("View Error");
        
        errorMenuItem.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) 
            {
                MessageManager.getInstance().displayGrowlMessage(MessageManager.getInstance().getMessage(selectedDownload.getFileID()), true);
            }
        });

        /** 
         * ====================================================================
         * = Destination Panel
         * ====================================================================
         */
        JPanel destinationPanel = new JPanel();
        destinationPanel.setBorder(BorderFactory.createTitledBorder("Destination"));
        destinationPanel.setLayout(new BoxLayout(destinationPanel, BoxLayout.Y_AXIS));
        
        ImageIcon hddIcon = createImageIcon("images/todo.png"); // TODO: Create a Storage icon (16x16)
        JLabel hddLabel = new JLabel(hddIcon);
        hddLabel.setVerticalAlignment(Label.CENTER);
        hddLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        
        // If the config file somehow did not set directory, use "user.dir"
        directory = (directory != null)? directory : System.getProperty("user.dir");
        
        // If the directory path does not include an ending separator, add one
        if (!Character.toString(directory.charAt(directory.length() - 1)).equals(Settings.PATH_SEPARATOR))
        {
            directory = directory + Settings.PATH_SEPARATOR;
        }
        
        createDirectoryLabel();
        
        ImageIcon folderIcon = createImageIcon("images/todo.png"); // TODO: Create a Folder icon (16x16)
        JButton openButton = new JButton(folderIcon);
        openButton.setVerticalTextPosition(AbstractButton.CENTER);
        openButton.setHorizontalTextPosition(AbstractButton.CENTER);
        openButton.setActionCommand("pause");
        openButton.setPreferredSize(new Dimension(32, 24));
        openButton.setToolTipText("Choose a directory to save downloads");
        
        openButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                actionChooseDestination();
            }
        });
        
        Box box = Box.createHorizontalBox();
        box.add(hddLabel);
        box.add(Box.createRigidArea(new Dimension(5,0)));
        box.add(destinationLabel);
        box.add(Box.createHorizontalGlue());
        box.add(Box.createRigidArea(new Dimension(5,0)));
        box.add(openButton);
        destinationPanel.add(box);
        
        // Calculate free space of destination
        calculateFreeSpace();
        
        JLabel freeSpaceTextLabel = new JLabel("Free Space:");
        
        freeSpaceLabel.setHorizontalAlignment(JLabel.RIGHT);
        
        box = Box.createHorizontalBox();
        box.add(freeSpaceTextLabel);
        box.add(Box.createRigidArea(new Dimension(10, 0)));
        box.add(freeSpaceLabel);
        box.add(Box.createHorizontalGlue());
        
        destinationPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        destinationPanel.add(box);
        
        /** 
         * ====================================================================
         * = Downloads Panel
         * ====================================================================
         */

        // Set up Downloads table.
        table = new JTable(tableModel) 
        {
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) 
            {
                Component c = super.prepareRenderer(tcr, row, column);
                if(isRowSelected(row)) 
                {
                    c.setForeground(getSelectionForeground());
                    c.setBackground(getSelectionBackground());
                }
                else
                {
                    c.setForeground(getForeground());
                    c.setBackground((row%2==0)?evenColor:getBackground());
                }
                return c;
            }
        };
        
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (e.getValueIsAdjusting())
                {
                    tableSelectionChanged();
                }
            }
        });
        
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        table.setFillsViewportHeight(true);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0,0));
        table.getTableHeader().setReorderingAllowed(false);

        table.setDefaultRenderer(String.class, new StringRenderer());
        
        int columns = table.getColumnModel().getColumnCount();
        for (int i = 0; i < columns; i++)
        {
            table.getColumnModel().getColumn(i).setHeaderRenderer(new HeaderRenderer());
        }

        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.setRowHeight(24);
        
        // Mouse listener for the table
        MouseListener popupListener = new PopupListener();
        table.addMouseListener(popupListener);

        downloadsPanel.setBorder(BorderFactory.createTitledBorder("Downloads"));
        downloadsPanel.setLayout(new BorderLayout());
        
        if (downloadListDescriptionLabel == null)
        {
            downloadListDescriptionLabel = new JLabel(" ");
        }
        
        downloadsPanel.add(downloadListDescriptionLabel, BorderLayout.NORTH);
        
        JPanel downloadsScrollPanel = new JPanel();
        downloadsScrollPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        downloadsScrollPanel.setLayout(new BorderLayout());
        
        downloadsScrollPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        Box downloadsControlPanel = Box.createHorizontalBox();
        downloadsControlPanel.add(Box.createRigidArea(new Dimension(5,0)));
        
        ImageIcon upIcon = createImageIcon("images/todo.png"); // TODO: Create an Up icon (16x16)
        upButton = new JButton(upIcon);
        upButton.setVerticalTextPosition(AbstractButton.CENTER);
        upButton.setHorizontalTextPosition(AbstractButton.CENTER);
        upButton.setActionCommand("moveup");
        upButton.setPreferredSize(new Dimension(28, 28));
        upButton.setToolTipText("Move selected download up");
        
        upButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {
                int row = table.getSelectedRow();
                
                if (row > 0)
                {
                    tableModel.moveUp(row);
                    selectRow(row - 1);
                }
            }
        });
        
        ImageIcon downIcon = createImageIcon("images/todo.png"); // TODO: Create a Down icon (16x16)
        downButton = new JButton(downIcon);
        downButton.setVerticalTextPosition(AbstractButton.CENTER);
        downButton.setHorizontalTextPosition(AbstractButton.CENTER);
        downButton.setActionCommand("movedown");
        downButton.setPreferredSize(new Dimension(28, 28));
        downButton.setToolTipText("Move selected download down");
        
        downButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int row = table.getSelectedRow();
                
                // Check that this is not the last row
                if (row < (tableModel.getRowCount() - 1))
                {
                    tableModel.moveDown(row);
                    selectRow(row + 1);
                }
            }
        });
        
        ImageIcon deleteIcon = createImageIcon("images/todo.png"); // TODO: Create a Delete icon (16x16)
        deleteButton = new JButton(deleteIcon);
        deleteButton.setVerticalTextPosition(AbstractButton.CENTER);
        deleteButton.setHorizontalTextPosition(AbstractButton.CENTER);
        deleteButton.setActionCommand("delete");
        deleteButton.setPreferredSize(new Dimension(28, 28));
        deleteButton.setToolTipText("Remove selected download");
        
        deleteButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // TODO: Delete the record from the database

                // Remove the Download from the table
                tableModel.clearDownload(table.getSelectedRow());
                
                // Set the selected Download to null as nothing is selected
                selectedDownload = null;
                
                updateButtons();
  
                resetProgressPanel();
                
                // Reset selectedDownload to null
                selectedDownload = null;
                
                /**
                 * If this was the last Download, set this order to complete in 
                 * the order dialog and make them select another order 
                 */
                if (table.getRowCount() == 0)
                {
                    // Tell the user about the situation
                    JOptionPane.showMessageDialog(getWindowFrame(), "You have processed all downloads in this order. Please select another.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Rebuild the orders table and load the dialog
                    sendOrdersListRequest();
                }
            }
        });
        
        box = Box.createVerticalBox();
        box.add(Box.createRigidArea(new Dimension(0, 20)));
        box.add(upButton);
        box.add(downButton);
        box.add(Box.createRigidArea(new Dimension(0, 5)));
        box.add(deleteButton);
        box.add(Box.createVerticalGlue());
        
        downloadsControlPanel.add(box);
        downloadsScrollPanel.add(downloadsControlPanel, BorderLayout.EAST);
        
        downloadsPanel.add(downloadsScrollPanel, BorderLayout.CENTER);
        
        /** 
         * ====================================================================
         * = Progress Panel
         * ====================================================================
         */
        JPanel progressPanel = new JPanel();
        progressPanel.setBorder(BorderFactory.createTitledBorder("Progress"));
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        
        fileNameLabel = new JLabel(" ");
        
        JPanel progressBarPanel = new JPanel();
        progressBarPanel.setLayout(new BoxLayout(progressBarPanel, BoxLayout.X_AXIS));
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setVisible(true);
        progressBar.setStringPainted(true);
        
        playIcon = createImageIcon("images/todo.png");	// TODO: Create a Play icon (16x16)
        pauseIcon = createImageIcon("images/todo.png");	// TODO: Create a Pause icon (16x16)
        ImageIcon skipIcon = createImageIcon("images/todo.png");	// TODO: Create a Skip icon (16x16)
        
        playPauseButton = new JButton(pauseIcon);
        playPauseButton.setVerticalTextPosition(AbstractButton.CENTER);
        playPauseButton.setHorizontalTextPosition(AbstractButton.CENTER);
        playPauseButton.setActionCommand("pause");
        playPauseButton.setPreferredSize(new Dimension(32, 24));
        playPauseButton.setToolTipText("Pause current download");
        playPauseButton.setEnabled(false);
        
        playPauseButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                new SwingWorker<Boolean, Void>()
                {   
                    @Override
                    protected Boolean doInBackground() throws Exception 
                    {
                        togglePlayPause();
                        
                        return true;
                    }
                    
                    @Override
                    protected void done() 
                    {
                        try
                        {
                            if (get() == true && selectedDownload != null)
                            {
                                if ("play".equals(playPauseButton.getActionCommand()))
                                {
                                    playPauseButton.setIcon(pauseIcon);
                                    playPauseButton.setActionCommand("pause");
                                    playPauseButton.setToolTipText("Pause current download");
                                }
                                else
                                {
                                    playPauseButton.setIcon(playIcon);
                                    playPauseButton.setActionCommand("play");
                                    playPauseButton.setToolTipText("Resume current download");
                                }
                            }
                        }
                        catch (InterruptedException e)
                        {
                        }
                        catch (ExecutionException e)
                        {
                        }
                    }
                }.execute();
            }
        });
        
        skipButton = new JButton(skipIcon);
        skipButton.setVerticalAlignment(AbstractButton.CENTER);
        skipButton.setHorizontalAlignment(AbstractButton.CENTER);
        skipButton.setActionCommand("skip");
        skipButton.setPreferredSize(new Dimension(32, 24));
        skipButton.setToolTipText("Skip current download");
        skipButton.setEnabled(false);
        
        skipButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                new SwingWorker<Boolean, Void>()
                {   
                    @Override
                    protected Boolean doInBackground() throws Exception 
                    {
                        switch (selectedDownload.getStatus())
                        {
                        	// TODO: Handle various download statuses
                        }
                        
                        selectedDownload.skip();
                        
                        return true;
                    }
                    
                    @Override
                    protected void done() 
                    {
                        try
                        {
                            if (get() == true)
                            {   
                                // Reset the Play/Pause button state
                                playPauseButton.setIcon(pauseIcon);
                                playPauseButton.setActionCommand("pause");
                                playPauseButton.setToolTipText("Pause current download");
                            }
                        }
                        catch (InterruptedException e)
                        {
                        }
                        catch (ExecutionException e)
                        {
                        }
                    }
                }.execute();    
            }
        });
        
        progressBarPanel.add(progressBar);
        progressBarPanel.add(Box.createRigidArea(new Dimension(5,0)));
        progressBarPanel.add(playPauseButton);
        progressBarPanel.add(Box.createRigidArea(new Dimension(2,0)));
        progressBarPanel.add(skipButton);
        
        progressDescriptionLabel = new JLabel(" ");
        
        box = Box.createHorizontalBox();
        box.add(fileNameLabel);
        box.add(Box.createHorizontalGlue());
        progressPanel.add(box);
        progressPanel.add(progressBarPanel);
        box = Box.createHorizontalBox();
        box.add(progressDescriptionLabel);
        box.add(Box.createHorizontalGlue());
        progressPanel.add(box);
        
        /** 
         * ====================================================================
         * = Buttons Panel
         * ====================================================================
         */
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(4,8,8,8));
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.add(Box.createHorizontalGlue());
        downloadButton = new JButton("Begin Download");
        downloadButton.setActionCommand("begindownload");
        downloadButton.setToolTipText("Begin Downloading Scenes");
        downloadButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                new SwingWorker<Boolean, Void>() {
                    
                    @Override
                    protected Boolean doInBackground() throws Exception 
                    {
                        if ("stopdownload".equals(downloadButton.getActionCommand()))
                        {
                            actionStopDownload();
                        }
                        else
                        {
                            actionStartDownload();
                        }
                        
                        return true;
                    }
                    
                    @Override
                    protected void done() 
                    {
                        try
                        {
                            if (get() == true)
                            {
                                toggleDownloadButton();
                            }
                        }
                        catch (InterruptedException e)
                        {
                        }
                        catch (ExecutionException e)
                        {
                        }
                    }
                }.execute();
            }
        });
        
        clearCompleteButton = new JButton("Clear Completed Downloads");
        clearCompleteButton.setToolTipText("Clear completed downloads from the list.");
        clearCompleteButton.setEnabled(false);
        clearCompleteButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                clearCompleteDownloads();
            }
        });
        
        buttonsPanel.add(downloadButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(3,0)));
        buttonsPanel.add(clearCompleteButton);

        // Set the initial state of the buttons to false
        resetButtons();
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(destinationPanel);
        getContentPane().add(downloadsPanel);
        getContentPane().add(progressPanel);
        getContentPane().add(buttonsPanel);
        
        // Login
        loginDialog = new LoginDialog(this, true, "Login");
        loginDialog.addObserver(BulkDownloadApplication.this);
        
        // Order selection
        ordersDialog = new OrdersDialog(this, true, "Orders");
        ordersDialog.addObserver(BulkDownloadApplication.this);
        
        // Load the growl images
        Growl.loadImages();
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                loginDialog.showDialog();
            }
        });
    }
    
    public boolean isDownloading()
    {
        return this.downloading;
    }
    
    public boolean getRecurring()
    {
        return this.recurringDownload;
    }
    
    public void setRecurring(boolean value)
    {
        this.recurringDownload = value;
    }
    
    /**
     * Timer to periodically process the downloads list again after a file was
     * unavailable on a previous iteration.
     */
    private class RecurringDownloadTimerTask extends TimerTask
    {
        public void run()
        {
            processDownloads();
        }
    }
    
    /**
     * Instantiate the Timer and TimerTask and schedule them to run at a
     * specific time.
     */
    private void runRecurringDownloadTimer()
    {
        recurringDownloadTimer = new Timer();
        recurringDownloadTimerTask = new RecurringDownloadTimerTask();
        
        // Present the user with a message telling them it will try again
        MessageManager.getInstance().displayGrowlMessage(
                new Message("Recurrence Scheduled", 
                        "One or more downloads did not complete. They will be attempted again in ten minutes."), false);
        
        // Schedule the task for 10 minutes from now
        recurringDownloadTimer.schedule(recurringDownloadTimerTask, 600000);
    }
    
    private void sendKeepAliveMessage()
    {
        // TODO: Send a KEEP_ALIVE message to the server to keep the connection alive
        
    	// If the user is still downloading, start another KeepAliveTimerTask
        if (this.downloading)
        {
            runKeepAliveTimer();
        }
    }
    
    private class KeepAliveTimerTask extends TimerTask
    {
        public void run()
        {
            sendKeepAliveMessage();
        }
    }
    
    private void runKeepAliveTimer()
    {
        keepAliveTimer = new Timer();
        keepAliveTimerTask = new KeepAliveTimerTask();
        
        // Schedule the timer for 10 minutes
        keepAliveTimer.schedule(keepAliveTimerTask, 10 * 60 * 1000);
    }
    
    private void stopKeepAliveTimer()
    {
        if (keepAliveTimer != null)
        {
            keepAliveTimer.cancel();
            keepAliveTimer = null;
        }
        
        if (keepAliveTimerTask != null)
        {
            keepAliveTimerTask.cancel();
            keepAliveTimerTask = null;
        }
    }
 
    private void togglePlayPause()
    {
        if ("play".equals(playPauseButton.getActionCommand()))
        {
            // Resume the current download
            selectedDownload.resume();
        }
        else
        {
            // Pause the current download
            selectedDownload.pause();
        }
    }
    
    protected void actionStopDownload()
    {
        // Stop the current Download
        if (selectedDownload != null)
        {
            if (selectedDownload.getStatus() != 0) // TODO: Use a meaningful status value representing "Complete"
            {
                selectedDownload.stop();
                
                // If there is a current request waiting, interrupt and cancel it
                if (currentRequest != null)
                {
                    currentRequest.cancel();
                }
                
                // TODO: Update database to show file was stopped
            }
        }
        
        // Set the Downloading flag
        downloading = false;
        
        // Stop sending KeepAlive
        ServerConnection.getInstance().setKeepAlive(false);
        stopKeepAliveTimer();
        
        // Stop the recurring Download timer if it is running
        if (recurringDownloadTimerTask != null)
        {
            recurringDownloadTimerTask.cancel();
        }
        
        // Enable the table
        table.setEnabled(true);
    }
    
    private void actionStartDownload()
    {
        if (downloadList.size() == 0)
        {
            return;
        }
        
        // Check if the user has write privileges to the download destination
        if (!canWrite(this.directory))
        {
            MessageBox messageBox = new MessageBox("Could not write files to directory \"" + this.directory + "\" due to insufficient privileges. " +
                    "Please select a destination for which you have read and write privileges by clicking the folder icon in the upper right of the application.");
            
            JOptionPane.showMessageDialog(BulkDownloadApplication.getWindowFrame(), messageBox, "Insufficient Privileges", JOptionPane.ERROR_MESSAGE);
            
            return;
        }
        
        // Set the downloading flag
        downloading = true;
        
        // Set KeepAlive to the server connection
        ServerConnection.getInstance().setKeepAlive(true);
        runKeepAliveTimer();
        
        // Disable the table
        table.setEnabled(false);
        
        // Start the download process
        processDownloads();
    }
    
    protected void stopDownload()
    {
        actionStopDownload();
        
        toggleDownloadButton();
    }
    
    protected void startDownload()
    {
        actionStartDownload();
        
        toggleDownloadButton();
    }
    
    public void resetDownloadButton()
    {
        downloadButton.setText("Begin Download");
        downloadButton.setActionCommand("begindownload");
        downloadButton.setToolTipText("Begin Downloading Scenes");
    }
    
    public void toggleDownloadButton()
    {
        if (!downloading)
        {
            // Toggle the button            
            downloadButton.setText("Begin Download");
            downloadButton.setActionCommand("begindownload");
            downloadButton.setToolTipText("Begin Downloading Scenes");
        }
        else
        {
            // Toggle the button
            downloadButton.setText("Stop Download");
            downloadButton.setActionCommand("stopdownload");
            downloadButton.setToolTipText("Stop Downloading Scenes");
        }
        
        // Update the buttons
        updateButtons();
    }
    
    public void resetButtons()
    {
        upButton.setEnabled(false);
        downButton.setEnabled(false);
        deleteButton.setEnabled(false);
        
        downloadButton.setEnabled(false);
        clearCompleteButton.setEnabled(false);
    }
    
    public void resetProgressPanel()
    {
        fileNameLabel.setText(" ");
        progressDescriptionLabel.setText(" ");
        progressBar.setValue(0);
    }
    
    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) 
    {
        java.net.URL imgURL = BulkDownloadApplication.class.getResource(path);
        
        if (imgURL != null) 
        {
            return new ImageIcon(imgURL);
        } 
        else 
        {
            return null;
        }
    }
    
    public void buildDownloadsTable(ArrayList<Download> downloadsList)
    {
        // Clear our the Table Model
        tableModel.clearTable();
        
        selectedDownload = null;
        
        this.downloadList = downloadsList;
        
        for (Download i : this.downloadList)
        {
            tableModel.addDownload(i);
        }
        
        // Update the Downloads list description
        updateDownloadListDescription();
        
        // Hide the loading message if it is still visible
        if (loadingDialog != null)
        {
            loadingDialog.hideDialog();
        }
        
        // Clear out the progress description stuff
        progressDescriptionLabel.setText(" ");
        fileNameLabel.setText(" ");
        progressBar.setValue(0);
        
        updateButtons();
        
        downloadButton.setEnabled(true);
    }
    
    private void sendFileListRequest(Order order)
    {
        // TODO: Retrieve a list of files for this order
        
        // The update() will receive the response, then open the dialog
    }
    
    protected void sendOrdersListRequest()
    {
        // TODO: Retrieve a list of orders for this user
        
        // The update() will receive the response, then open the dialog
    }
    
    private JMenuBar buildMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem orderMenuItem = new JMenuItem("Open Order", KeyEvent.VK_O);
        orderMenuItem.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) 
            {
                new SwingWorker<Boolean, Void>()
                {   
                    @Override
                    protected Boolean doInBackground() throws Exception 
                    {
                        if (downloading)
                        {
                            int option = JOptionPane.showConfirmDialog(getWindowFrame(), "You are currently downloading scenes from the active order. Would you like to stop the current download to view other orders?", "Download In Progress", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);                    
                            
                            if (option == JOptionPane.YES_OPTION)
                            {
                                // Stop the current Download and open the orders dialog
                                actionStopDownload();                           
                            }
                            else
                            {
                                return false;
                            }
                        }
                        
                        return true;
                    }
                    
                    @Override
                    protected void done() 
                    {
                        try
                        {
                            if (get() == true)
                            {
                                resetDownloadButton();
                                
                                sendOrdersListRequest();
                            }
                        }
                        catch (InterruptedException e)
                        {
                        }
                        catch (ExecutionException e)
                        {
                        }
                    }
                }.execute();
            }
        });
        fileMenu.add(orderMenuItem);
        fileMenu.addSeparator();
        
        JMenuItem fileExitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        fileExitMenuItem.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) 
            {
                System.exit(0);
            }
        });
        fileMenu.add(fileExitMenuItem);
        
        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.setMnemonic(KeyEvent.VK_S);
        JMenuItem recurringDownloadCheckbox = new JCheckBoxMenuItem("Recurring Download");
        recurringDownloadCheckbox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // Recurring download setting
                AbstractButton button = (AbstractButton) e.getSource();
                boolean selected = button.getModel().isSelected();
                
                recurringDownload = selected;
                
                // Save the recurring setting
                try
                {
                    saveXmlConfigurationFile();
                }
                catch (JAXBException ex)
                {
                }
                catch (IOException ex)
                {
                }
            }
        });
        recurringDownloadCheckbox.setSelected(recurringDownload);
        settingsMenu.add(recurringDownloadCheckbox);
        
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        JMenuItem aboutMenuItem = new JMenuItem("About Bulk Download", KeyEvent.VK_A);
        aboutMenuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {   
                JOptionPane.showMessageDialog(getWindowFrame(), "Bulk Download Application v" + Version.version, "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        helpMenu.add(aboutMenuItem);
        
        menuBar.add(fileMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    private void createDirectoryLabel()
    {
        if (destinationLabel == null)
        {
            destinationLabel = new JLabel("");
        }
        
        // Determine the width of the string
        FontMetrics metrics = new FontMetrics(destinationLabel.getFont()) {
            private static final long serialVersionUID = 1L;};
        Rectangle2D bounds = metrics.getStringBounds(directory, null);
        int labelWidth = (int) bounds.getWidth() + 100;
        
        // Determine if the size of the label will be too great
        if (labelWidth > this.getWidth())
        {
            // Get the folder name
            String folder = (new File(directory)).getName();
            
            Rectangle2D charBounds = metrics.getStringBounds("A", null);
            int charWidth = (int) charBounds.getWidth();
            
            // Determine where the path needs to be truncated
            int truncatePos = ((this.getWidth() - 100) / charWidth) - (folder.length() + 5);
            
            String destination = "";
            
            if (truncatePos < 1)
            {
                destination = "(Folder name is too long to display)";
            }
            else
            {
                // Concatenate the string parts
                destination = directory.substring(0, truncatePos) + "..." +
                    Settings.PATH_SEPARATOR + folder + Settings.PATH_SEPARATOR;
            }
            
            // Set the text of the destination label
            destinationLabel.setText(destination);
            destinationLabel.setToolTipText(directory);
        }
        else
        {
            destinationLabel.setText(directory);
        }
    }
    
    private boolean canWrite(String path)
    {
        File file = new File(path);
        
        if (file.canWrite() == false)
        {
            return false;
        }
        
        // If the path does not include an ending separator, add one
        if (!Character.toString(path.charAt(path.length() - 1)).equals(Settings.PATH_SEPARATOR))
        {
            path = path + Settings.PATH_SEPARATOR;
        }
        
        file = new File(path + ".tmp");
        
        try
        {
            file.createNewFile();
        }
        catch (IOException e)
        {
            return false;
        }
        finally
        {
            file.delete();
        }
        
        return true;
    }
    
    private String openFileChooser()
    {
        String destination = "";
        
        if (System.getProperty("os.name").startsWith("Mac"))
        {
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            JFrame chooserFrame = new JFrame();
            FileDialog chooserDialog = new FileDialog(chooserFrame);
            chooserDialog.setVisible(true);
            
            if (chooserDialog.getFile() != null)
            {
                destination = chooserDialog.getDirectory() + chooserDialog.getFile();
            }
        }
        else
        {
            JFileChooser chooser = new JFileChooser(directory);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    
            int rc = chooser.showDialog(null, "Choose Destination");
    
            if (rc == JFileChooser.APPROVE_OPTION)
            {
                destination = chooser.getSelectedFile().getAbsolutePath();
            }
        }
        
        return destination;
    }
    
    private void actionChooseDestination()
    {
        // Do not allow if they are currently downloading
        if (downloading)
        {
            return;
        }
        
        String destination = openFileChooser();
        
        if (destination.equals(""))
        {
            return;
        }

        // Check if there was an error
        if (!canWrite(destination))
        {
            JOptionPane.showMessageDialog(getWindowFrame(), "The destination is not valid. You do not have permission to write to this directory.", "Invalid Destination", JOptionPane.WARNING_MESSAGE);
        }
        else
        {
            // Set the system setting for directory
            directory = destination;
            
            // If the directory path does not include an ending separator, add one
            if (!Character.toString(directory.charAt(directory.length() - 1)).equals(Settings.PATH_SEPARATOR))
            {
                directory = directory + Settings.PATH_SEPARATOR;
            }
    
            File temp = new File(directory);
            long freeSpace = temp.getFreeSpace();
            freeSpaceLabel.setText(FileSizeFormat.format(freeSpace, FileSizeFormat.KB) + " (" + FileSizeFormat.format(freeSpace) + ")");
    
            createDirectoryLabel();
    
            if (downloadList != null)
            {
                for (Download i : downloadList)
                {
                    i.setDirectory(directory);
                }
            }
            
            try
            {
                saveXmlConfigurationFile();
            }
            catch (JAXBException e)
            {
            }
            catch (IOException e)
            {
            }
        }
    }

    private void tableSelectionChanged()
    {   
        if ( selectedDownload != null )
        {
            selectedDownload.deleteObserver(this);
        }

        if (!clearing && table.getSelectedRow() > -1)
        {
            selectedDownload = null;
            selectedDownload = tableModel.getDownload(table.getSelectedRow());
            selectedDownload.addObserver(this);
            updateButtons();
            updateProgressPanel();
        }
    }
    
    /**
     * Iterate through the downloads list checking for completed downloads. If
     * the download is complete, remove the row from the table.
     */
    private void clearCompleteDownloads()
    {
        if (!downloading && downloadList != null)
        {
            for (Download iterator : downloadList)
            {
                if (iterator.getStatus() == 0) // TODO: Use a meaningful status that represents "Complete"
                {
                    if (selectedDownload != null && iterator.equals(selectedDownload))
                    {
                        selectedDownload = null;
                        resetButtons();
                    }
                    tableModel.clearDownloadObject(iterator);
                }
            }
            
            // Rebuild the downloadList
            downloadList.clear();
            
            for (int i = 0; i < tableModel.getRowCount(); i++)
            {
                downloadList.add(tableModel.getDownload(i));
            }
            
            updateButtons();
            updateProgressPanel();
            updateDownloadListDescription();
        }
    }
    
    /**
     * Iterate through the file list to determine if all files have been
     * completed, if there are any unavailable or errored files, and determine
     * the next step to take.
     */
    private void processListCompletion()
    {
        int files = tableModel.getRowCount();
        int completed = 0;
        Download iterator;
        
        for (int i = 0; i < files; i++)
        {
            iterator = tableModel.getDownload(i);
            
            if (iterator.getStatus() == 0) // TODO: Use a meaningful status that represents "Complete"
            {
                completed++;
            }
        }
        
        if (files == completed)
        {
            // Reset selectedDownload as it is no longer relevant
            selectedDownload = null;
            
            // Stop the downloading process
            stopDownload();
            
            // All downloads complete -- send user back to order dialog with confirmation
            int response = JOptionPane.showConfirmDialog(getWindowFrame(), "You have completed all downloads in this order. Would you like to select another order?", "Notice", JOptionPane.YES_NO_OPTION);
            
            if (response == JOptionPane.YES_OPTION)
            {
                // Rebuild the orders table and load the dialog
                sendOrdersListRequest();
            }
        }
        else
        {
            if (recurringDownload)
            {
                // Start timer to run processDownloads() again
                runRecurringDownloadTimer();
            }
            else
            {
                stopDownload();
            }
        }
    }
    
    /**
     * Iterate through the downloads list and download any relevant scenes.
     */
    private void processDownloads()
    {
        @SuppressWarnings("unused")
		int completed = 0;
        
        // Begin process on row 0
        int currentRow = 0;
        
        // Select the first row!
        selectRow(currentRow);
        
        boolean processRow = false;
        
        // Find the first scene that is available to be processed
        while (currentRow < tableModel.getRowCount() && downloading == true && processRow == false)
        {
            processRow = false;
            
            switch (selectedDownload.getStatus())
            {
            	// TODO: Handle various download statuses
            }
        }
        
        // We either found a row to process or reached the end
        if (processRow)
        {
            processRow(currentRow);
        }
        else
        {
            processListCompletion();
        }
    }

    /**
     * Process a row to determine the status of the order from the server and,
     * if necessary, begin downloading the file. 
     */
    private boolean processRow(int currentRow)
    {   
        // Check if the Download was already completed
        if (selectedDownload.getStatus() == 0) // TODO: Use a meaningful status value representing "Complete"
        {
            if (table.getSelectedRow() == tableModel.getRowCount() - 1 )
            {
                processListCompletion();
            }
            else
            {
                // Select the next row
                selectRow(++currentRow);
                
                // Process the row
                processRow(currentRow);
            }
            
            return true;
        }
        
        // Recalculate the free space in the download folder
        calculateFreeSpace();
                
        // TODO: Retrieve the file information
        
        return true;
    }

    /**
     * Enable/Disable the buttons to reflect operations allowed for a selected 
     * row.
     */
    private void updateButtons()
    {
        clearCompleteButton.setEnabled(!downloading);
        skipButton.setEnabled(downloading);
        downloadButton.setEnabled(true);
        
        if (selectedDownload == null)
        {
            upButton.setEnabled(false);
            downButton.setEnabled(false);
            deleteButton.setEnabled(false);
            playPauseButton.setEnabled(false);
        }
        else if (tableModel.getRowCount() == 1)
        {
            upButton.setEnabled(false);
            downButton.setEnabled(false);
            deleteButton.setEnabled(true);
            playPauseButton.setEnabled(false);
        }
        else if (selectedDownload == tableModel.getDownload(0))
        {
            upButton.setEnabled(false);
            downButton.setEnabled(true);
            deleteButton.setEnabled(true);
            playPauseButton.setEnabled(false);
        }
        else if (selectedDownload == tableModel.getDownload(tableModel.getRowCount() - 1))
        {
            upButton.setEnabled(true);
            downButton.setEnabled(false);
            deleteButton.setEnabled(true);
            playPauseButton.setEnabled(false);
        }
        else
        {
            upButton.setEnabled(true);
            downButton.setEnabled(true);
            deleteButton.setEnabled(true);
            playPauseButton.setEnabled(false);
        }
    }
    
    /**
     * Set the progress bar value along with the text descriptions.
     */
    private void updateProgressPanel()
    {
        if (tableModel.getRowCount() == 0 || table.getSelectedRow() == -1)
        {
            resetProgressPanel();
        }
        else
        {
            fileNameLabel.setText(selectedDownload.getEntityID());
            progressDescriptionLabel.setText(selectedDownload.getDownloadDescription());
            progressBar.setValue((int) selectedDownload.getProgress());
        }
    }
    
    /**
     * Set the text for the label describing the order number and total size.
     */
    private void updateDownloadListDescription()
    {
        if (downloadListDescriptionLabel == null)
        {
            downloadListDescriptionLabel = new JLabel();
            downloadListDescriptionLabel.setFont(downloadListDescriptionLabel.getFont().deriveFont(13f));
            downloadListDescriptionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        }
        
        // Set the text
        downloadListDescriptionLabel.setText(
                (this.orderNumber == -1? "" : "Order " + orderNumber + " (" + FileSizeFormat.format(calculateOrderSize()) + " Remaining)")
                );
    }

    /**
     * Update certain aspects of the user interface when an observable sends
     * a stateChanged() update.
     */
    public void update(Observable o, Object arg)
    {
        if ( selectedDownload != null && selectedDownload.equals(o) && downloading == true )
        {
            // Update the row in the table
            tableModel.updateRow(table.getSelectedRow());
            
            // Update the progress bar and description labels
            updateProgressPanel();
            
            // Update the buttons
            updateButtons();
            
            switch (selectedDownload.getStatus())
            {
            	// TODO: Handle each update from the selected download
            }
        }
        else if (loginDialog != null && loginDialog.getObservable() != null && loginDialog.getObservable().equals(o))
        {
            switch (loginDialog.getStatus())
            {
                case LoginDialog.DISCONNECTED :
                    break;
                case LoginDialog.CONNECTING :
                    break;
                case LoginDialog.CONNECTED :
                    loginDialog.hideDialog();
                    
                    // Tell the Garbage Collection the login dialog is okay to remove from memory
                    loginDialog = null;
                    
                    // Make the order list request!
                    sendOrdersListRequest();
                    
                    break;
            }
        }
        else if (ordersDialog != null && ordersDialog.getObservable() != null && ordersDialog.getObservable().equals(o))
        {
    		switch (ordersDialog.getStatus())
            {
                case OrdersDialog.CANCELLED :
                    ordersDialog.hideDialog();
                    break;
                case OrdersDialog.SELECTED :
                    // Retrieve the Order from the Orders Dialog
                    Order order = ordersDialog.getOrder();
                    
                    // Set the Order Number
                    orderNumber = order.getOrderID();
                    
                    // Hide the dialog
                    ordersDialog.hideDialog();
                    
                    resetButtons();
                    resetProgressPanel();
                    tableModel.clearTable();
                    
                    // Create the loading dialog
                    if (loadingDialog == null)
                    {
                        loadingDialog = new LoadingDialog(this, true, "Loading Scene List");
                    }
                    
                    // Send the file list request
                    sendFileListRequest(order);
                    
                    if (tableModel.getRowCount() < 1)
                    {
                        loadingDialog.showDialog();
                    }
                    
                    break;
            }
        }
        else if (currentRequest != null && currentRequest.equals(o))
        {   
            switch (currentRequest.getStatus())
            {
                // TODO: Handle each update from a request that is sent
            }
        }
    }
    
    /**
     * Select a row in the downloads table.
     */
    private void selectRow(int row)
    {
        ListSelectionModel selectionModel = table.getSelectionModel();
        
        selectionModel.setSelectionInterval(row, row);
        
        scrollToRow(row);
        
        tableSelectionChanged();
    }
    
    /**
     * Scroll the downloads table to the selected row
     */
    private void scrollToRow(int row)
    {
        try
        {
            // Make sure the row exists
            if (!(table.getParent() instanceof JViewport))
            {
                return;
            }
            
            JViewport viewport = (JViewport)table.getParent();
            
            // This rectangle is relative to the table where the northwest corner of cell (0,0) is always (0,0).
            Rectangle rect = table.getCellRect(row, 0, true);
            
            // The location of the viewport relative to the table
            Point pt = viewport.getViewPosition();
            
            // Translate the cell location so that it is relative to the view
            rect.setLocation(rect.x - pt.x, rect.y - pt.y);
            
            // Scroll the area into view
            viewport.scrollRectToVisible(rect);
            
            // Repaint the window
            this.repaint();
        }
        catch (Exception e)
        {
        }
    }
    
    /**
     * Calculate the free space available for the user's download folder.
     */
    private void calculateFreeSpace()
    {
        File temp = new File(directory);

        freeSpace = temp.getFreeSpace();
        freeSpaceLabel.setText(FileSizeFormat.format(freeSpace, FileSizeFormat.KB) + " (" + FileSizeFormat.format(freeSpace) + ")");
    }
    
    /**
     * Calculate the total hard disk space the order will consume.
     */
    private double calculateOrderSize()
    {
        double size = 0.0;
        
        if (downloadList != null)
        {
            for (Download i : downloadList)
            {
                if (i.getStatus() != 0) // TODO: Use a meaningful status that represents "Complete"
                {
                    size += i.getSize();
                }
            }
            
            return size;
        }
        
        return -1;
    }
        
    /**
     * Save the current application settings to the configuration file.
     * @throws JAXBException 
     * @throws IOException 
     */
    protected void saveXmlConfigurationFile() throws JAXBException, IOException
    {
        JAXBContext context = JAXBContext.newInstance(AppConfig.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
        AppConfig bda = new AppConfig();
        GUIConfigs guiConfigs = new GUIConfigs();
        AppPosition appPosition = new AppPosition();
            appPosition.setHeight(getHeight());
            appPosition.setWidth(getWidth());
            appPosition.setX(getX());
            appPosition.setY(getY());
            appPosition.setIsMaximized((getExtendedState() == MAXIMIZED_BOTH));
        guiConfigs.setAppPosition(appPosition);
        
        AppSettings appSettings = new AppSettings();
        bulkdownloadapplication.config.Download download = new bulkdownloadapplication.config.Download();
            download.setPath(directory);
            download.setRecurring(recurringDownload);
        appSettings.setDownload(download);
        
        bda.setGUIConfigs(guiConfigs);
        bda.setAppSettings(appSettings);
        
        marshaller.marshal(bda, new FileWriter("config.xml"));
    }
    
    /**
     * Load the application settings from the configuration file.
     * @throws FileNotFoundException 
     */
    private void loadXmlConfigurationFile()
    {
        // Load the defaults in case something is wrong with the xml file
        int width = Settings.WIDTH;
        int height = Settings.HEIGHT;
        int x = Settings.X;
        int y = Settings.Y;
        boolean isMaximized = Settings.IS_MAXIMIZED;
        
        try
        {
            JAXBContext context = JAXBContext.newInstance(AppConfig.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            AppConfig appConfig = (AppConfig)unmarshaller.unmarshal(
                    new FileReader("config.xml"));
            
            bulkdownloadapplication.config.Download downloadConfig = appConfig.getAppSettings().getDownload();
            bulkdownloadapplication.config.AppPosition appPositionConfig = appConfig.getGUIConfigs().getAppPosition();
            
            directory = downloadConfig.getPath();
            recurringDownload = downloadConfig.isRecurring();
            width = appPositionConfig.getWidth();
            height = appPositionConfig.getHeight();
            x = appPositionConfig.getX();
            y = appPositionConfig.getY();
            isMaximized = appPositionConfig.isIsMaximized();
        }
        catch(JAXBException jaxbException)
        {
        }
        catch(FileNotFoundException fileException)
        {
        }
        
        if (directory == null || !(new File(directory).exists()))
        {
            directory = System.getProperty("user.dir");
        }
        
        setTitle("Bulk Download Application");
        
        setSizeAndLocation(new Dimension(width, height), new Point(x, y));
        
        if (isMaximized)
        {
            setExtendedState(MAXIMIZED_BOTH);
        }
    }
    
    private void setSizeAndLocation(Dimension size, Point location)
    {
        java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        java.awt.GraphicsDevice[] screenDevices = ge.getScreenDevices();
        
        int expandedWidth = 0, expandedHeight = 0;
        
        // Get the full resolution
        for (int monitor = 0; monitor < screenDevices.length; monitor++)
        {
            expandedWidth += screenDevices[monitor].getDisplayMode().getWidth();
            expandedHeight += screenDevices[monitor].getDisplayMode().getHeight();
        }
        
        // Check that the width is set to a positive value
        if (size.width < 0)
        {
            size.width = Settings.WIDTH;
        }
        
        // Check that the height is set to a positive value
        if (size.height < 0)
        {
            size.height = Settings.HEIGHT;
        }
        
        // Check that the window fits within the extended width
        if (size.width + location.x > expandedWidth)
        {
            // Check if the window can fit the area
            if (expandedWidth - size.width < 0)
            {
                size.width = expandedWidth;
            }
            
            location.x = expandedWidth - size.width;
        }
        
        // Check that the window fits within the extended height
        if (size.height + location.y > expandedHeight)
        {
            // Check if the window can fit the area
            if (expandedHeight - size.height < 0)
            {
                size.height = expandedHeight;
            }
            
            location.y = expandedHeight - size.height;
        }
        
        setSize(size);
        setLocation(location);
    }
    
    /**
     * Create the application configuration file and fill all the settings in
     * with default values.
     * @throws IOException 
     * @throws JAXBException 
     */
    protected static void createXMLConfigurationFile() throws JAXBException, IOException
    {
        JAXBContext context = JAXBContext.newInstance(AppConfig.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
        AppConfig bda = new AppConfig();
        GUIConfigs guiConfigs = new GUIConfigs();
        AppPosition appPosition = new AppPosition();
            appPosition.setHeight(Settings.HEIGHT);
            appPosition.setWidth(Settings.WIDTH);
            appPosition.setX(Settings.X);
            appPosition.setY(Settings.Y);
            appPosition.setIsMaximized(Settings.IS_MAXIMIZED);
        guiConfigs.setAppPosition(appPosition);
        
        AppSettings appSettings = new AppSettings();
        bulkdownloadapplication.config.Download download = new bulkdownloadapplication.config.Download();
            download.setPath(System.getProperty("user.dir"));
            download.setRecurring(Settings.RECURRING);
        appSettings.setDownload(download);
        
        bda.setGUIConfigs(guiConfigs);
        bda.setAppSettings(appSettings);
        
        marshaller.marshal(bda, new FileWriter("config.xml"));
    }
    
    protected static JFrame getWindowFrame()
    {
        java.awt.Frame[] frames = BulkDownloadApplication.getFrames();
        
        // Find the Bulk Download Application JFrame and return it
        for (java.awt.Frame frame : frames)
        {
            if ("Bulk Download Application".equals(frame.getTitle()))
            {
                return (JFrame) frame;
            }
        }
        
        // Otherwise, return null
        return null;
    }
    
    /**
     * Prepare the look and feel to match the look and feel of the user's
     * operating system. Also set the progress bar color.
     */
    protected static void prepareLookAndFeel()
    {
        try 
        {
            // Set System L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } 
        catch (UnsupportedLookAndFeelException e) 
        {
        }
        catch (ClassNotFoundException e) 
        {
        }
        catch (InstantiationException e) 
        {
        }
        catch (IllegalAccessException e) 
        {
        }
        
        // Set the fill color for the bar 
        UIManager.put("ProgressBar.selectionBackground",
                new javax.swing.plaf.ColorUIResource(new Color(0, 0, 0)));
        // Set the font's color to white when the background is behind it
        UIManager.put("ProgressBar.selectionForeground",
                new javax.swing.plaf.ColorUIResource(new Color(255, 255, 255)));
    }
    
    /**
     * Check to ensure there is no other instance of the application running
     * before attempting to start a new instance.
     */
    protected static void checkInstances()
    {
        try
        {
            new Socket("localhost", InstanceServer.PORT);
            JOptionPane.showMessageDialog(getWindowFrame(), "Another instance is already running.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        catch (Exception e)
        {
            InstanceServer instanceServer = new InstanceServer();
            instanceServer.start();
        }
    }

    /**
     * Check to ensure the server is running and accepting connections.
     */
    private static void checkServerStatus()
    {
        if (!ServerConnection.getInstance().isServerAvailable())
        {
            URL bulkStatusUrl = null;
            String bulkStatus = null;
            
            try
            {
            	bulkStatusUrl = new URL("http://example.com"); // TODO: Use a status URL to check if the appropriate interfaces are working
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                bulkStatusUrl.openStream()));
                
                bulkStatus = in.readLine();
                
                in.close();
            }
            catch (Exception e1)
            {
            }
            
            if ("1".equals(bulkStatus))
            {
                MessageBox messageBox = new MessageBox("Unable to access the bulk download service due to network settings. " +
                        "Please verify hostname \"" + ServerConnection.getInstance().getHostName() + "\" on port " + 
                        ServerConnection.getInstance().getPort() + " is open on any personal or company firewalls and try again.");
                
                JOptionPane.showMessageDialog(getWindowFrame(), messageBox, "Unable To Connect", JOptionPane.ERROR_MESSAGE);
                
                System.exit(1);
            }
            else
            {
                JOptionPane.showMessageDialog(getWindowFrame(), "The bulk download service is unavailable at this time. Please try again later.", "Server Unavailable", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }
    }
    
    /**
     * Create the GUI and show it. For thread safety, this method is invoked 
     * from the event-dispatching thread.
     */
    private static void createAndShowGUI()
    {
        // Set up the look and feel
        prepareLookAndFeel();
        
        // Check if the application is already running
        checkInstances();
        
        // Set some variables for the ServerConnection
        ServerConnection.getInstance().setHostName(Version.host);
        ServerConnection.getInstance().setPort(Version.port);
        
        // Check that the server is running and accepting connections
        checkServerStatus();
        
        // Establish a connection with the server
        ServerConnection.getInstance().start();
        
        // Check to ensure the application configuration file exists
        File configFile = new File("config.xml");
        
        if (!configFile.exists())
        {
            try
            {
                createXMLConfigurationFile();
            }
            catch (JAXBException e)
            {
            }
            catch (IOException e)
            {
            }
        }
        
        // Check that the user has access to edit the configuration file
        if (!configFile.exists() && !configFile.canWrite())
        {
            MessageBox messageBox = new MessageBox("Could not access configuration file due to insufficient privileges. Configuration changes will not be saved. " +
                    "Please install the Bulk Download Application to a directory for which you have read and write privileges to take advantage of application settings.");
            
            JOptionPane.showMessageDialog(getWindowFrame(), messageBox, "Insufficient Privileges", JOptionPane.ERROR_MESSAGE);
        }
        
        // Create and set up the window
        BulkDownloadApplication downloadManager = new BulkDownloadApplication();
        
        // Display the window
        downloadManager.setVisible(true);
    }
    
    // Run the Bulk Download Application.
    public static void main(String[] args)
    {
        // Schedule a job for the event-dispatching thread: create and show GUI
        javax.swing.SwingUtilities.invokeLater(new Runnable() 
        {
            public void run()
            {
                createAndShowGUI();
            }
        });
    }
    
    private class PopupListener extends MouseAdapter
    {
        public void mousePressed(MouseEvent e)
        {
            showPopup(e);
        }
        
        public void mouseReleased(MouseEvent e)
        {
            showPopup(e);
        }
        
        public void showPopup(MouseEvent e)
        {
            if (downloading || table.getRowCount() < 1)
            {
                return;
            }
            
            if (e.isPopupTrigger())
            {
                // Select the row being right-clicked
                selectRow(table.rowAtPoint(e.getPoint()));
                
                // Get the status of the download
                int status = selectedDownload.getStatus();
                
                if (status == 0) // TODO: Use a meaningful status to represent "Error"
                {
                    // Build the popup menu with the proper abilities
                    popupMenu.removeAll();
                    errorMenuItem.setEnabled(true);
                    popupMenu.add(errorMenuItem);
                    
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
    }
}

@SuppressWarnings("serial")
class StringRenderer extends JLabel implements TableCellRenderer
{
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, final int row, int column)
    {
        JLabel cellSpacingLabel= (JLabel) (this);
        int rowSpacing = 0;

        if (!hasFocus)
        {
            setBackground(table.getBackground());
            setBorder(null);
        }

        if (isSelected)
        {
            setBackground(table.getSelectionBackground());
            setBorder(null);
        }
        else
        {
            setBackground(table.getBackground());
            setBorder(null);
        }

        if (cellSpacingLabel != null)
        {
            if (row == 0)
            {
                rowSpacing = 2;
            }
            else
            {
                rowSpacing = 1;
            }

            cellSpacingLabel.setBorder(new CompoundBorder(new EmptyBorder(new Insets(rowSpacing, 6, 1, 6)), cellSpacingLabel.getBorder()));
        }

        Font style = cellSpacingLabel.getFont();
        cellSpacingLabel.setFont(new Font("Arial", Font.PLAIN, style.getSize()));

        if (column == 2)
        {
            cellSpacingLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        }
        else
        {
            cellSpacingLabel.setHorizontalAlignment(SwingConstants.LEFT);
        }

        this.setOpaque(true);
        setText((String) value);

        return this;
    }
}

@SuppressWarnings("serial")
class HeaderRenderer extends JLabel implements TableCellRenderer
{
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        JLabel headerLabel= (JLabel) (this);

        Border lineBorder = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 0, 1, 1), BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        Border emptyBorder = BorderFactory.createEmptyBorder(2, 6, 2, 6);
        headerLabel.setBorder(BorderFactory.createCompoundBorder(lineBorder, emptyBorder));

        if (column == 2)
        {
            headerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        }
        else
        {
            headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
        }

        setText((String) value);

        return this;
    }
}
