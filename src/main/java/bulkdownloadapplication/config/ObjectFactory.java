package bulkdownloadapplication.config;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: config
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Download }
     * 
     */
    public Download createDownload() {
        return new Download();
    }

    /**
     * Create an instance of {@link AppConfig }
     * 
     */
    public AppConfig createAppConfig() {
        return new AppConfig();
    }

    /**
     * Create an instance of {@link GUIConfigs }
     * 
     */
    public GUIConfigs createGUIConfigs() {
        return new GUIConfigs();
    }

    /**
     * Create an instance of {@link AppSettings }
     * 
     */
    public AppSettings createAppSettings() {
        return new AppSettings();
    }

    /**
     * Create an instance of {@link AppPosition }
     * 
     */
    public AppPosition createAppPosition() {
        return new AppPosition();
    }
}
