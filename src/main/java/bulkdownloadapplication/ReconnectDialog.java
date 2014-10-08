package bulkdownloadapplication;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

@SuppressWarnings("serial")
public class ReconnectDialog extends JDialog implements Observer
{
    private JLabel messageLabel = new JLabel("Reconnecting.");
    
    private JButton retryButton = new JButton("Retry");
    private JButton cancelButton = new JButton("Cancel");
    
    private JFrame parentFrame = null;
    
    protected static final int DISCONNECTED = 0;
    protected static final int RECONNECTING = 1;
    protected static final int RECONNECTED = 2;
    protected static final int CANCELLED = 3;
    
    protected static final int MAX_ATTEMPTS = 5;
    protected static final int COUNTDOWN_TIME = 60;
    
    private DelegatedObservable obs = null;
    public Observable getObservable() { return obs; }
    
    private int status;
    private int attempts;
    private int seconds;
    ReconnectTask reconnectTask;
    private Timer reconnectTimer;
    private ReconnectTimerTask reconnectTimerTask;
    
    // ActionListener to click the "Cancel" button if the user hits ESC
    private ActionListener cancelListener = new ActionListener()
    {
        public void actionPerformed(ActionEvent e)
        {
            cancelButton.doClick();
        }
    };
    
    public void addObserver(Observer o)
    {
        obs.addObserver(o);
    }
    
    public void deleteObserver(Observer o)
    {
        obs.deleteObserver(o);
    }
    
    public void showDialog()
    {
        // Set the location to the center of the BDA window
        setLocation(parentFrame.getLocation().x + (parentFrame.getWidth() / 2) - (getWidth() / 2),
                parentFrame.getLocation().y + (parentFrame.getHeight() / 2) - (getHeight() / 2));
        
        setVisible(true);
    }
    
    public void hideDialog()
    {
        setVisible(false);
    }
    
    public void resetDialog()
    {
        attempts = 0;    
    }
    
    public void beginReconnect()
    {
        attempts = 0;
        actionRetry();
    }
    
    public void updateMessage(String message)
    {
        messageLabel.setText("Attempt " + attempts + " of " + MAX_ATTEMPTS + ": " + message);
    }
    
    public void updateButtons()
    {
        switch (this.status)
        {
            case DISCONNECTED : retryButton.setText("Retry (" + seconds-- + ")");
                retryButton.setEnabled(true);
                cancelButton.setText("Cancel");
                cancelButton.setActionCommand("cancel");
                break;
            case RECONNECTING : retryButton.setText("Retry");
                retryButton.setEnabled(false);
                cancelButton.setText("Cancel");
                cancelButton.setActionCommand("cancel");
                break;
            case RECONNECTED : retryButton.setText("Retry");
                retryButton.setEnabled(false);
                cancelButton.setText("Close (" + seconds-- + ")");
                cancelButton.setActionCommand("hide");
                break;
        }
    }
    
    public void stateChanged()
    {
        obs.setChanged();
        obs.notifyObservers();
    }
    
    public int getStatus()
    {
        return this.status;
    }
    
    public void setStatus(int status)
    {
        this.status = status;
        stateChanged();
    }
    
    public void runReconnectTimer()
    {
        stopReconnectTimer();
        
        seconds = COUNTDOWN_TIME;
        
        updateButtons();
        
        reconnectTimer = new Timer();
        reconnectTimerTask = new ReconnectTimerTask();
        reconnectTimer.schedule(reconnectTimerTask, 0, 1 * 1000);
    }
    
    public void stopReconnectTimer()
    {
        if (reconnectTimer != null)
        {
            reconnectTimer.cancel();
            reconnectTimer = null;
        }
        
        if (reconnectTimerTask != null)
        {
            reconnectTimerTask.cancel();
            reconnectTimerTask = null;
        }
    }
    
    class ReconnectTimerTask extends TimerTask
    {
        public void run()
        {
            if (attempts < MAX_ATTEMPTS)
            {
                if (seconds > 0)
                {
                    updateButtons();
                }
                else
                {
                    actionRetry();
                }
            }
            else
            {
                messageLabel.setText("Unable to reconnect to the Bulk Server in " + MAX_ATTEMPTS + " attempts. The application will close.");
                retryButton.setText("Retry");
                retryButton.setEnabled(false);
                cancelButton.setText("Close");
                cancelButton.setActionCommand("close");
            }
        }
    }
    
    public void runHideDialogTimerTask()
    {   
        seconds = COUNTDOWN_TIME;
        
        updateButtons();
        
        Timer hideDialogTimer = new Timer();
        HideDialogTimerTask hideDialogTimerTask = new HideDialogTimerTask();
        hideDialogTimer.schedule(hideDialogTimerTask, 0, 1 * 1000);
    }
    
    class HideDialogTimerTask extends TimerTask
    {
        public void run()
        {
            if (seconds > 0)
            {
                updateButtons();
            }
            else
            {
                Thread.currentThread().interrupt();
                this.cancel();
                hideDialog();
            }
        }
    }
    
    public void runReconnectTask()
    {
        stopReconnectTimer();
        
        setStatus(RECONNECTING);
        
        updateButtons();
        
        if (reconnectTask == null)
        {
            reconnectTask = null;
        }
        
        reconnectTask = new ReconnectTask();
        reconnectTask.addObserver(this);
        reconnectTask.beginReconnect();
    }
    
    class ReconnectTask extends Observable implements Runnable
    {
        protected static final int DISCONNECTED = 0;
        protected static final int CONNECTED = 1;
        
        private int status = DISCONNECTED;
        
        public void beginReconnect()
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
        
        public void cancel()
        {
            Thread.currentThread().interrupt();
            setStatus(DISCONNECTED);
        }
        
        public void run()
        {
            if (ServerConnection.getInstance().isServerAvailable())
            {
                boolean reconnected = ServerConnection.getInstance().reconnect();
                
                if (reconnected)
                {
                    boolean authenticated = ServerConnection.getInstance().authenticate();
                    
                    if (authenticated)
                    {
                        setStatus(CONNECTED);
                    }
                    else
                    {
                        setStatus(DISCONNECTED);
                    }
                }
                else
                {
                    setStatus(DISCONNECTED);
                }
            }
            else
            {
                setStatus(DISCONNECTED);
            }
            
            this.cancel();
        }
        
        private void stateChanged()
        {
            setChanged();
            notifyObservers();
        }
    }
    
    public ReconnectDialog(JFrame frame, boolean modal, String title)
    {
        super(frame, modal);
        
        obs = new DelegatedObservable();
        parentFrame = frame;
        
        setTitle(title);
        setMinimumSize(new Dimension(
                (int) new JLabel("Successfully reconnected to the Bulk Server. Dialog will close in 60 seconds.").getPreferredSize().getWidth() + 48, 
                getHeight()
        ));
        
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                cancelButton.doClick();
            }
        });
        
        // Set up the message panel
        JPanel messagePanel = new JPanel();
        messagePanel.add(messageLabel);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        
        // Set up the button panel
        JPanel buttonsPanel = new JPanel();
        
        retryButton.setEnabled(false);
        retryButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                actionRetry();
            }
        });
        retryButton.setPreferredSize(new JButton("Retry (60)").getPreferredSize());
        buttonsPanel.add(retryButton);
        
        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                actionCancel();
            }
        });
        cancelButton.setPreferredSize(new JButton("Cancel (60)").getPreferredSize());
        buttonsPanel.add(cancelButton);
        
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(messagePanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
        pack();
        getRootPane().registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    private void actionRetry()
    {
        stopReconnectTimer();
        
        attempts++;
        
        setStatus(RECONNECTING);

        updateMessage("Reconnecting to the Bulk Server.");
        updateButtons();
        
        // Send the request
        runReconnectTask();
    }
    
    private void actionCancel()
    {
        stopReconnectTimer();
        
        toggleCancel();
    }
    
    private void toggleCancel()
    {
        if ("close".equals(cancelButton.getActionCommand()))
        {
            System.exit(1);
        }
        else
        {
            // Set status to disconnected
            setStatus(DISCONNECTED);
            
            if (reconnectTask != null)
            {
                reconnectTask.deleteObservers();
                reconnectTask.cancel();
                reconnectTask = null;
            }
            
            this.hideDialog();
        }
    }
    
    @Override
    public void update(Observable arg0, Object arg1)
    {
        if (reconnectTask != null && this.status == ReconnectDialog.RECONNECTING)
        {
            // Delete observers
            reconnectTask.deleteObservers();
            
            switch (reconnectTask.getStatus())
            {
                case ReconnectTask.DISCONNECTED :
                    updateMessage("Connection was unsuccessful. Retrying in " + COUNTDOWN_TIME + " seconds.");
                    setStatus(DISCONNECTED);
                    updateButtons();
                    runReconnectTimer();
                    break;
                case ReconnectTask.CONNECTED :
                    messageLabel.setText("Successfully reconnected to the Bulk Server. Dialog will close in " + COUNTDOWN_TIME + " seconds.");
                    setStatus(RECONNECTED);
                    updateButtons();
                    runHideDialogTimerTask();
                    break;
            }
            
            reconnectTask.cancel();
            reconnectTask = null;
        }
    }
}
