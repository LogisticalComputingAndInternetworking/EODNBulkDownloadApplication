package bulkdownloadapplication;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.text.View;

@SuppressWarnings("serial")
public class MessageBox extends JPanel
{
    private static final JLabel resizer = new JLabel();
    
    public MessageBox(String message)
    {
        JLabel messageLabel = new JLabel("<html>" + message + "</html>");
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(messageLabel);
        
        Dimension prefSize = getPreferredSize(messageLabel.getText(), true, 640);
        this.setPreferredSize(prefSize);
    }
    
    public static Dimension getPreferredSize(String html, boolean width, int prefSize) 
    {
        resizer.setText(html);
    
        View view = (View) resizer.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);

        view.setSize(width ? prefSize : 0, width ? 0 : prefSize);

        float w = view.getPreferredSpan(View.X_AXIS);
        float h = view.getPreferredSpan(View.Y_AXIS);
        
        return new Dimension((int) Math.ceil(w), (int) Math.ceil(h));
    }
}
