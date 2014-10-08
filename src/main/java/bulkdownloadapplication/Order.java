package bulkdownloadapplication;

public class Order
{
    public static final String STATUSES[] = {"TODO: List of statuses"};

    // TODO: Create statuses for an Order
    
    private int orderID;
    private long orderDate;
    private int status;

    // Constructor for Order.
    public Order(int orderID, long orderDate, int status)
    {
        this.orderID = orderID;
        this.orderDate = orderDate;
        this.status = status;
    }

    public int getOrderID()
    {
        return orderID;
    }

    public long getOrderDate()
    {
        return orderDate;
    }

    public int getStatus()
    {
        return status;
    }
}
