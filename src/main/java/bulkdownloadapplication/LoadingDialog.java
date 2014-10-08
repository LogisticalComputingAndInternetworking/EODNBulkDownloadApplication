package bulkdownloadapplication;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class LoadingDialog extends JDialog
{
    private JFrame parentFrame = null;
    private JLabel messageLabel;
    
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
    
    public LoadingDialog(JFrame frame, boolean modal, String message)
    {
        super(frame, modal);
        
        parentFrame = frame;
        
        setUndecorated(true);
        setFocusable(false);
        setFocusableWindowState(false);
        
        JPanel loadingPanel = new JPanel();
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.X_AXIS));
        
        ImageIcon loadingIcon = BulkDownloadApplication.createImageIcon("images/todo.gif");	// TODO: Create a Loading GIF
        JLabel loadingIconLabel = new JLabel(loadingIcon);
        
        messageLabel = new JLabel(message);
        Float fontSize = messageLabel.getFont().getSize2D();
        messageLabel.setFont(messageLabel.getFont().deriveFont(fontSize + 3.0f));
        
        loadingPanel.add(loadingIconLabel);
        loadingPanel.add(Box.createRigidArea(new Dimension(8, 0)));
        loadingPanel.add(messageLabel);
        
        JPanel mainPanel = new JPanel();
        mainPanel.add(loadingPanel);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        pack();
    }
}
