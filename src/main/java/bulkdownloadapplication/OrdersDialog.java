package bulkdownloadapplication;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JDialog;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class OrdersDialog extends JDialog implements ActionListener
{
    private JLabel messageLabel = new JLabel("Please select an order.");

    private JButton selectButton = new JButton("Select Order");
    private JButton deleteButton = new JButton("Delete Order");
    private JButton cancelButton = new JButton("Cancel");
    
    private Order selectedOrder = null;
    private JTable table;
    private OrdersTableModel tableModel = new OrdersTableModel();
    private JFrame parentFrame = null;
    
    protected static final int WAITING = 0;
    protected static final int SELECTED = 1;
    protected static final int CANCELLED = 2;
    
    private Request ordersRequest = null;
    private DelegatedObservable obs = null;
    public Observable getObservable() { return obs; }
    
    private int status;
    
    // ActionListener to click the "Cancel" button if the user hits ESC
    private ActionListener cancelListener = new ActionListener () {
        public void actionPerformed(ActionEvent e)
        {
            cancelButton.doClick();
        }
    };
    
    // ActionListener to click the "Select" button if the user hits ENTER
    private ActionListener selectListener = new ActionListener() {
        public void actionPerformed(ActionEvent e)
        {
            selectButton.doClick();
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
        return this.ordersRequest;
    }
    
    public void showDialog()
    {
        // Center the dialog within the main window
        setLocation(parentFrame.getLocation().x + (parentFrame.getWidth() / 2) - (getWidth() / 2), 
                parentFrame.getLocation().y + (parentFrame.getHeight() / 2) - (getHeight() / 2));
        
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
    
    public Order getOrder()
    {
        return this.selectedOrder;
    }

    public OrdersDialog(JFrame frame, boolean modal, String title)
    {
        super(frame, modal);

        obs = new DelegatedObservable();
        
        setTitle(title);
        setSize(460, 320);

        parentFrame = frame;
        
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                cancelButton.doClick();
            }
            
            public void windowActivated(WindowEvent e)
            {
                if ((table == null) || (table != null && table.getSelectedRow() < 0))
                {
                    selectButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                }
            }
        });

        // Set up message panel.
        JPanel messagePanel = new JPanel();
        messagePanel.add(messageLabel);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        // Set up Orders table.
        table = new JTable(tableModel) 
        {
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) 
            {
                Component c = super.prepareRenderer(tcr, row, column);
                if (isRowSelected(row)) 
                {
                    c.setForeground(getSelectionForeground());
                    c.setBackground(getSelectionBackground());
                }
                else
                {
                    c.setForeground(getForeground());
                    c.setBackground((row%2==0)?BulkDownloadApplication.evenColor:getBackground());
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
        table.setRowHeight(24);
        
        // Set up orders panel.
        JPanel ordersPanel = new JPanel();
        ordersPanel.setBorder(BorderFactory.createTitledBorder("Orders"));
        ordersPanel.setLayout(new BorderLayout());
        ordersPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Set up button panel.
        JPanel buttonsPanel = new JPanel();

        selectButton.setEnabled(false);
        selectButton.addActionListener(this);
        buttonsPanel.add(selectButton);
        
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(this);
        buttonsPanel.add(deleteButton);

        cancelButton.setEnabled(true);
        cancelButton.addActionListener(this);
        buttonsPanel.add(cancelButton);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(messagePanel, BorderLayout.NORTH);
        getContentPane().add(ordersPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
        getRootPane().registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(selectListener, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    public void buildOrdersTable(ArrayList<Order> ordersList)
    {
        // Clear the table model
        tableModel.clearTable();
        
        // Add the orders to the table model
        for (Order i : ordersList)
        {
            tableModel.addOrder(i);
        }
    }
    
    public void actionPerformed(ActionEvent e) 
    {
        if (selectButton == e.getSource())
        {
            if (table.getSelectedRow() > -1)
            {
                // Get the selected row's orderID
                this.selectedOrder = tableModel.getOrder(table.getSelectedRow());
                
                // Set the status to selected
                setStatus(SELECTED);
            }
        }
        else if (cancelButton == e.getSource())
        {
            // Set the status to CANCELLED
            setStatus(CANCELLED);
        }
        else if (deleteButton == e.getSource())
        {
            if (table.getSelectedRow() > -1)
            {
            	// TODO: Update the database to show the order was deleted
                
                tableModel.clearOrder(table.getSelectedRow());
                
                // Reset selectedOrder to null
                selectedOrder = null;
                
                // Update the buttons to reflect the new state
                updateButtons();
            }
        }
    }

    private void tableSelectionChanged()
    {
        if ( table.getSelectedRow() > -1 )
        {
            selectedOrder = tableModel.getOrder ( table.getSelectedRow() );
            
            updateButtons();
        }
    }
    
    /**
     * Enable/disable the buttons to reflect the operations allowed for a
     * selected row. 
     */
    private void updateButtons()
    {
        if (tableModel.getRowCount() == 0)
        {
            selectButton.setEnabled(false);
            deleteButton.setEnabled(false);
            cancelButton.setEnabled(true);
            
            // Tell the user there are no orders left and exit
            JOptionPane.showMessageDialog(this, "There are no orders remaining, Click OK to close the application.", "No Orders Remain", JOptionPane.WARNING_MESSAGE);
            
            // Exit
            System.exit(1);
        }
        else if (selectedOrder == null)
        {
            selectButton.setEnabled(false);
            deleteButton.setEnabled(false);
            cancelButton.setEnabled(true);
        }
        else
        {
            selectButton.setEnabled(true);
            deleteButton.setEnabled(true);
            cancelButton.setEnabled(true);
        }
    }
}
