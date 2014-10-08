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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

@SuppressWarnings("serial")
public class LoginDialog extends JDialog implements Observer
{
    protected static final int DISCONNECTED = 0;
    protected static final int CONNECTED = 1;
    protected static final int CONNECTING = 2;
    
    private JLabel messageLabel = new JLabel("Please enter your login credentials.");
    private JLabel usernameLabel = new JLabel("Username: ");
    private JTextField usernameField = new JTextField(20);
    private JLabel passwordLabel = new JLabel("Password: ");
    private JPasswordField passwordField = new JPasswordField(20);
    private JButton loginButton = new JButton("Login");
    private JButton cancelButton = new JButton("Close");
    
    private Request loginRequest = null;
    private DelegatedObservable obs = null;
    public Observable getObservable() { return obs; }
    
    private int status;
    
    private ActionListener cancelListener = new ActionListener () {
        public void actionPerformed(ActionEvent e)
        {
            cancelButton.doClick();
        }
    };
    
    private ActionListener loginListener = new ActionListener () {
        public void actionPerformed(ActionEvent e)
        {
            loginButton.doClick();
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
    
    public Request getRequest()
    {
        return this.loginRequest;
    }
    
    public void showDialog()
    {
        setVisible(true);
    }
    
    public void hideDialog()
    {
        setVisible(false);
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
    
    public LoginDialog(JFrame frame, boolean modal, String title)
    {
        super(frame, modal);
        
        obs = new DelegatedObservable();
        
        setTitle(title);
        
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        
        // Set up the message panel
        JPanel messagePanel = new JPanel();
        messagePanel.add(messageLabel);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        
        // Set up the username panel
        JPanel usernamePanel = new JPanel();
        usernamePanel.setLayout(new BoxLayout(usernamePanel, BoxLayout.X_AXIS));
        usernamePanel.add(Box.createRigidArea(new Dimension(32, 0)));
        usernamePanel.add(usernameLabel);
        usernamePanel.add(Box.createHorizontalGlue());
        usernamePanel.add(usernameField);
        usernamePanel.add(Box.createRigidArea(new Dimension(32, 0)));
        
        // Set up the password panel
        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.X_AXIS));
        passwordPanel.add(Box.createRigidArea(new Dimension(32, 0)));
        passwordPanel.add(passwordLabel);
        passwordPanel.add(Box.createHorizontalGlue());
        passwordPanel.add(passwordField);
        passwordPanel.add(Box.createRigidArea(new Dimension(32, 0)));
        
        // Set up the input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.add(usernamePanel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        inputPanel.add(passwordPanel);
        
        int usernameLabelWidth = (int) usernameLabel.getPreferredSize().getWidth();
        int passwordLabelWidth = (int) passwordLabel.getPreferredSize().getWidth();
        
        int preferredWidth = (usernameLabelWidth > passwordLabelWidth)? usernameLabelWidth : passwordLabelWidth;
        
        usernameLabel.setPreferredSize(new Dimension(preferredWidth, (int) usernameLabel.getPreferredSize().getHeight()));
        passwordLabel.setPreferredSize(new Dimension(preferredWidth, (int) passwordLabel.getPreferredSize().getHeight()));
        
        // Set up the buttons panel
        JPanel buttonsPanel = new JPanel();
        
        loginButton.setEnabled(true);
        loginButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                actionLogin();
            }
        });
        buttonsPanel.add(loginButton);
        
        JButton cancelSizeButton = new JButton("Cancel");
        int cancelButtonWidth = (int) cancelSizeButton.getPreferredSize().getWidth();
        cancelButton.setPreferredSize(new Dimension(cancelButtonWidth, (int) cancelButton.getPreferredSize().getHeight()));
        
        cancelButton.setActionCommand("close");
        cancelButton.setEnabled(true);
        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                new SwingWorker<Boolean, Void>()
                {   
                    @Override
                    protected Boolean doInBackground() throws Exception 
                    {
                        actionCancel();
                        
                        return true;
                    }
                }.execute();
            }
        });
        buttonsPanel.add(cancelButton);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(messagePanel, BorderLayout.NORTH);
        getContentPane().add(inputPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
        getRootPane().registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(loginListener, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        // Center the dialog within the main window
        setLocation(frame.getLocation().x + (frame.getWidth() / 2) - (getWidth() / 2), 
                frame.getLocation().y + (frame.getHeight() / 2) - (getHeight() / 2));
    }
    
    public JButton getLoginButton()
    {
        return this.loginButton;
    }
    
    public JLabel getMessageLabel()
    {
        return this.messageLabel;
    }
    
    public String getUsername()
    {
        String usernameEncrypted = "";
        
        try
        {
            usernameEncrypted = Encryption.getInstance().encrypt(usernameField.getText());
        }
        catch (EncryptionException e)
        {
        }
        
        return usernameEncrypted;
    }
    
    public String getPassword()
    {
        String passwordEncrypted = "";
        
        try
        {
            passwordEncrypted = Encryption.getInstance().encrypt(new String(passwordField.getPassword()));
        }
        catch (EncryptionException e)
        {
        }
        
        return passwordEncrypted;
    }
    
    public void toggleCancel()
    {
        if ("cancel".equals(cancelButton.getActionCommand()))
        {   
            // Set the status to disconnected
            setStatus(DISCONNECTED);
            
            // Delete the observers
            loginRequest.deleteObservers();
            
            // Cancel the request
            loginRequest.cancel();
            
            loginRequest = null;
            
            resetDialog();
            
            // Set the message label text
            messageLabel.setText("Connection Aborted.");
            
            // Make a new clean connection
            ServerConnection.getInstance().reconnect();
        }
        else
        {
            // Close the application
            System.exit(0);
        }
    }
    
    public void resetDialog()
    {
        loginButton.setEnabled(true);
        
        cancelButton.setText("Close");
        cancelButton.setActionCommand("close");
        
        usernameField.setEnabled(true);
        passwordField.setEnabled(true);
        
        passwordField.setText("");
        passwordField.requestFocus();
    }
    
    private void actionLogin()
    {
        setStatus(CONNECTING);
        
        loginButton.setEnabled(false);
        cancelButton.setActionCommand("cancel");
        cancelButton.setText("Cancel");
        
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        
        messageLabel.setText("Logging In.");
        
        // Send the request
        sendLoginRequest(false);
    }
    
    private void actionCancel()
    {
        toggleCancel();
    }
    
    private void sendLoginRequest(boolean disconnect)
    {
    	// TODO: Send the credentials to the server after encrypting them to authenticate
        
        // The update() will receive the response, then handle the connection
    }

    @Override
    public void update(Observable arg0, Object arg1)
    {
        if (loginRequest != null && this.status == LoginDialog.CONNECTING)
        {
            switch (loginRequest.getStatus())
            {
                // TODO: Handle the update from the authentication request
            }
        }
    }
}
