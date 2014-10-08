package bulkdownloadapplication;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.table.AbstractTableModel;

import bulkdownloadapplication.FileSizeFormat;

@SuppressWarnings("serial")
public class DownloadsTableModel extends AbstractTableModel implements Observer
{
    private static final String[] columnNames = { "Entity ID", "Data Set", "File Size", "Status" };
    @SuppressWarnings("rawtypes")
	private static final Class[] columnClasses = { String.class, String.class, String.class, String.class };
    private ArrayList<Download> downloadsList = new ArrayList<Download>();

    /**
     * Disable cell editing for all cells in the table.
     */
    public boolean isCellEditable(int row, int column)
    {
        return false;
    }

    /**
     * Add a Download to the table.
     */
    public void addDownload(Download download)
    {
        download.addObserver(this);
        downloadsList.add(download);
        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }

    /**
     * Retrieve a Download from the table.
     */
    public Download getDownload(int row)
    {
        return (Download) downloadsList.get(row);
    }

    /**
     * Remove a Download from the table based on its index in the table.
     */
    public void clearDownload(int row)
    {
        downloadsList.remove(row);
        fireTableRowsDeleted(row, row);
    }
    
    /**
     * Remove a Download from the table based on the Download object.
     */
    public void clearDownloadObject(Download download)
    {
        clearDownload(downloadsList.indexOf(download));
    }
    
    /**
     * Remove all Downloads from the table.
     */
    public void clearTable()
    {
        int size = downloadsList.size();
        
        downloadsList = new ArrayList<Download>();
        
        if (size > 0)
        {
            fireTableRowsDeleted(0, size - 1);
        }
    }
    
    /**
     * Move the selected Download up one row in the table.
     */
    public void moveUp(int row)
    {
        Download temp = downloadsList.remove(row);
        downloadsList.add(row - 1, temp);
        fireTableRowsUpdated(row - 1, row);
    }
    
    /**
     * Move the selected Download down one row in the table.
     */
    public void moveDown(int row)
    {
        Download temp = downloadsList.remove(row);
        downloadsList.add(row + 1, temp);
        fireTableRowsUpdated(row, row + 1);
    }
    
    /**
     * Update the GUI to reflect the changes to the passed row.
     */
    public void updateRow(int row)
    {
        fireTableRowsUpdated(row, row);
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    public String getColumnName(int col)
    {
        return columnNames[col];
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int col)
    {
        return columnClasses[col];
    }

    public int getRowCount()
    {
        return downloadsList.size();
    }

    public Object getValueAt(int row, int col) 
    {
        Download download = downloadsList.get(row);
        switch (col) 
        {
            case 0: // Entity ID
                return download.getEntityID();
            case 1: // Data Set
                return download.getDataSet();
            case 2: // File Size
                Long size = download.getSize();
                return (size == -1) ? "" : FileSizeFormat.format((double)size);
            case 3: // Status
                return Download.STATUSES[download.getStatus()];
        }
        return "";
    }

    public void update(Observable o, Object arg)
    {
        int index = downloadsList.indexOf(o);
        fireTableRowsUpdated(index, index);
    }
}
