package bulkdownloadapplication;

@SuppressWarnings("serial")
public class EncryptionException extends Exception
{
    public EncryptionException(String exception)
    {
        throw new RuntimeException(exception);
    }
}
