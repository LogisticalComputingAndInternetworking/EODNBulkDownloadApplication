package bulkdownloadapplication;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.View;

public class Growl extends Observable implements Runnable
{
    private JDialog frame;
    private JLabel titleLabel;
    private String title;
    private String message;
    private int opacityLevel = 80;
    private static final Font titleFont = new Font("SansSerif", Font.BOLD, 24);
    private static final Font messageFont = new Font("SansSerif", Font.PLAIN, 14);
    private Timer growlDisplayTimer;
    private Timer growlFadeTimer;
    private Timer growlDropTimer;
    private GrowlDisplayTimerTask growlDisplayTimerTask;
    private GrowlFadeTimerTask growlFadeTimerTask;
    private GrowlDropTimerTask growlDropTimerTask;
    private JLabel closeButton = new JLabel();
    protected static ImageIcon closeIcon = null;
    protected static ImageIcon closeIconOver = null;
    protected static ImageIcon infoIcon = null;
    
    private JLabel resizer = new JLabel();
    
    public static final String STATUSES[] = {"Displayed", "Fading", "Deleted", "Dropping"};
    public static final int DISPLAYED = 0;
    public static final int FADING = 1;
    public static final int DELETED = 2;
    public static final int DROPPING = 3;
    private int status;
    private int verticalLocationLimit;
    
    public Growl(String title, String message)
    {
        this.title = title;
        this.message = message;
        this.status = DISPLAYED;
        this.verticalLocationLimit = -1;
        
        initComponents();
        Thread thread = new Thread(this);
        thread.start();
        runGrowlDisplayTimer();
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                frame.repaint();
            }
        });
    }
    
    public static void loadImages()
    {
        closeIcon = BulkDownloadApplication.createImageIcon("images/todo.png"); // TODO: Create a Close icon (22x22)
        closeIconOver = BulkDownloadApplication.createImageIcon("images/todo.png");;	// TODO: Create a Close hover-over icon (22x22)
        infoIcon = BulkDownloadApplication.createImageIcon("images/todo.png");	// TODO: Create an Info icon (48x48)
    }
    
    private void initComponents()
    {
        // Create the frame
        frame = new JDialog();
        
        // Set up the labels
        titleLabel = new JLabel(this.title);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(new Color(239, 239, 239));
        titleLabel.setVerticalAlignment(JLabel.TOP);
        
        JLabel messageLabel = new JLabel("<html>" + this.message + "</html>");
        messageLabel.setFont(messageFont);
        messageLabel.setOpaque(true);
        messageLabel.setBackground(new Color(0f, 0f, 0f, 0.0f));
        messageLabel.setForeground(new Color(239, 239, 239));
        
        Container content = frame.getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(0f, 0f, 0f, 0.80f));
        
        content.add(Box.createRigidArea(new Dimension(0, 8)));
        Box container = Box.createHorizontalBox();
        container.add(Box.createRigidArea(new Dimension(6, 0)));
        
        // Create the info box
        Box infoBox = Box.createVerticalBox();
        
        // Add the info icon
        JLabel infoLabel = new JLabel(infoIcon);
        infoLabel.setVerticalAlignment(JLabel.TOP);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        
        infoBox.add(infoLabel);
        infoBox.add(Box.createVerticalGlue());
        
        container.add(infoBox);
        
        container.add(Box.createRigidArea(new Dimension(8, 0)));
        
        // NEW LABEL BOX
        JPanel labelBox = new JPanel();
        labelBox.setLayout(new BorderLayout());
        
        Box topBox = Box.createHorizontalBox();
        topBox.add(titleLabel);
        topBox.add(Box.createHorizontalGlue());
        
        closeButton = new JLabel(closeIcon);
        MouseListener closeListener = new CloseListener();
        closeButton.addMouseListener(closeListener);
        
        topBox.add(closeButton);
                
        labelBox.add(topBox, BorderLayout.NORTH);
        
        labelBox.add(messageLabel, BorderLayout.CENTER);
        labelBox.setBackground(new Color(0f, 0f, 0f, 0.0f));
        
        container.add(labelBox);
        container.add(Box.createRigidArea(new Dimension(6, 0)));
        content.add(container);
        content.add(Box.createRigidArea(new Dimension(0, 8)));
        
        Dimension prefSize = getPreferredSize(messageLabel.getText(), true, 400);
        
        frame.setPreferredSize(prefSize);
        frame.setUndecorated(true);
        frame.setLocationRelativeTo(null);
        frame.pack();
        
        AWTUtilitiesWrapper.setWindowOpacity(frame, 0.80f);
        AWTUtilitiesWrapper.setWindowShape(frame, new RoundRectangle2D.Float(0, 0, frame.getWidth(), frame.getHeight(), 16, 16));
        frame.setFocusable(false);
        frame.setFocusableWindowState(false);
        frame.setAlwaysOnTop(false);
        
        if (BulkDownloadApplication.getWindowFrame().getState() != Frame.ICONIFIED)
        {
            frame.setVisible(true);
            frame.repaint();
        }
    }
    
    public void setLocation(int x, int y)
    {
        frame.setLocation(x, y);
        setVerticalLocationLimit(y);
        frame.repaint();
    }
    
    public Point getLocation()
    {
        return frame.getLocation();
    }
    
    public int getHeight()
    {
        return frame.getHeight();
    }
    
    public int getWidth()
    {
        return frame.getWidth();
    }
    
    public void setStatus(int status)
    {
        this.status = status;
        stateChanged();
    }
    
    public int getStatus()
    {
        return status;
    }
    
    public void setVerticalLocationLimit(int y)
    {
        verticalLocationLimit = y;
    }
    
    public int getVerticalLocationLimit()
    {
        return verticalLocationLimit;
    }
    
    public void decreaseVerticalLocationLimit(int y)
    {
        verticalLocationLimit += y;
    }
    
    public Dimension getPreferredSize(String html, boolean width, int prefSize) 
    {
        resizer.setText(html);
        resizer.setFont(messageFont);
        
        View view = (View) resizer.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
        view.setSize(width ? prefSize : 0, width ? 0 : prefSize);

        float w = view.getPreferredSpan(View.X_AXIS);
        float h = view.getPreferredSpan(View.Y_AXIS) + 76;
        
        view = null;
        
        return new Dimension((int) Math.ceil(w), (int) Math.ceil(h));
    }
    
    class GrowlDisplayTimerTask extends TimerTask
    {
        public void run()
        {
            runGrowlFadeTimer();
            this.cancel();
        }
    }
    
    class GrowlFadeTimerTask extends TimerTask
    {
        public void run()
        {
            setStatus(FADING);
            
            if (opacityLevel >= 5)
            {
                opacityLevel -= 5;
                AWTUtilitiesWrapper.setWindowOpacity(frame, (float)opacityLevel / 100.0f);
                frame.repaint();
            }
            else
            {
                if (growlDropTimerTask != null)
                {
                    growlDropTimerTask.cancel();
                }
                frame.dispose();
                this.cancel();
                setStatus(DELETED);
            }
        }
    }
    
    class GrowlDropTimerTask extends TimerTask
    {
        public void run()
        {
            setStatus(DROPPING);
            
            if (frame.getY() + 6 < verticalLocationLimit)
            {
                frame.setLocation(frame.getX(), frame.getY() + 6);
            }
            else if (frame.getY() < verticalLocationLimit)
            {
                frame.setLocation(frame.getX(), frame.getY() + 1);
            }
            else
            {
                this.cancel();
                setStatus(DISPLAYED);
            }
        }
    }
    
    private void runGrowlDisplayTimer()
    {
        growlDisplayTimer = new Timer();
        growlDisplayTimerTask = new GrowlDisplayTimerTask();
        growlDisplayTimer.schedule(growlDisplayTimerTask, 6 * 1000);
    }
    
    public void runGrowlFadeTimer()
    {
        growlFadeTimer = new Timer();
        growlFadeTimerTask = new GrowlFadeTimerTask();
        growlFadeTimer.scheduleAtFixedRate(growlFadeTimerTask, 0, 25);
    }
    
    public void runGrowlDropTimer()
    {
        growlDropTimer = new Timer();
        growlDropTimerTask = new GrowlDropTimerTask();
        growlDropTimer.schedule(growlDropTimerTask, 0, 5);
    }

    @Override
    public void run()
    {
        while (status == DISPLAYED)
        {
            // Check if this is the only Growl
            if (verticalLocationLimit != -1 && frame.getLocation().y != verticalLocationLimit)
            {
                this.runGrowlDropTimer();
            }
            
            try
            {
                Thread.currentThread();
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
            }
        }
    }
    
    private void stateChanged()
    {
        setChanged();
        notifyObservers();
    }
    
    private class CloseListener extends MouseAdapter
    {
        public void mouseClicked(MouseEvent e)
        {
            close();
        }
        
        public void mousePressed(MouseEvent e)
        {
        }
        
        public void mouseReleased(MouseEvent e)
        {
        }
        
        public void mouseEntered(MouseEvent e)
        {
            closeButton.setIcon(closeIconOver);
            frame.repaint();
        }
        
        public void mouseExited(MouseEvent e)
        {
            closeButton.setIcon(closeIcon);
            frame.repaint();
        }
    }
    
    public void hide()
    {
        frame.setVisible(false);
    }
    
    public void show()
    {
        frame.setVisible(true);
        frame.repaint();
    }
    
    private void close()
    {
        frame.dispose();
        
        if (growlFadeTimerTask != null)
        {
            growlFadeTimerTask.cancel();
        }
        setStatus(DELETED);
    }
}
