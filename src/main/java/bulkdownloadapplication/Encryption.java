package bulkdownloadapplication;

public class Encryption 
{
    private static Encryption _instance = null;

    public Encryption() throws EncryptionException
    {
        // TODO: Prepare an encryption algorithm
    }

    public static Encryption getInstance() throws EncryptionException
    {
        if(Encryption._instance == null)
        {
            Encryption._instance = new Encryption();
        }

        return Encryption._instance;
    }

    public String encrypt(String source) throws EncryptionException
    {
        // TODO: Encrypt

        return null;
    }

    public String decrypt(String source) throws EncryptionException
    {
    	// TODO: Decrypt
    	
    	return null;
    }
}
