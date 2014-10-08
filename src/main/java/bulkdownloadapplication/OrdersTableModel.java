package bulkdownloadapplication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class OrdersTableModel extends AbstractTableModel implements Observer
{
    private static final String[] columnNames = { "Order ID", "Date Ordered", "Status" };
    @SuppressWarnings("rawtypes")
	private static final Class[] columnClasses = { String.class, String.class, String.class };
    private ArrayList<Order> ordersList = new ArrayList<Order>();

    public boolean isCellEditable ( int row, int column )
    {
        return false;
    }

    public void addOrder (Order order)
    {
        ordersList.add ( order );
        fireTableRowsInserted ( getRowCount() - 1, getRowCount() - 1);
    }

    public Order getOrder ( int row )
    {
        return (Order) ordersList.get(row);
    }

    public void clearOrder ( int row )
    {
        ordersList.remove ( row );
        fireTableRowsDeleted ( row, row );
    }
    
    public void clearTable()
    {
        int size = ordersList.size();
        
        ordersList = new ArrayList<Order>();
        
        if(size > 0)
        {
            fireTableRowsDeleted(0, size - 1);
        }
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    public String getColumnName ( int col )
    {
        return columnNames[col];
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass ( int col )
    {
        return columnClasses[col];
    }

    public int getRowCount()
    {
        return ordersList.size();
    }

    public Object getValueAt ( int row, int col )
    {
        Order order = ordersList.get ( row );
        switch (col)
        {
            case 0: // Order ID
                return Integer.toString(order.getOrderID());
            case 1: // Date Ordered
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                Date d = new Date();
                d.setTime(order.getOrderDate());
                return formatter.format(d);
            case 2: // Status
                return Order.STATUSES[order.getStatus()];
        }
        return "";
    }

    public void update(Observable o, Object arg) 
    {
        int index = ordersList.indexOf(o);
        fireTableRowsUpdated(index, index);
    }
}
