package bulkdownloadapplication.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "guiConfigs",
    "appSettings"
})
@XmlRootElement(name = "AppConfig")
public class AppConfig {

    @XmlElement(name = "GUIConfigs", required = true)
    protected GUIConfigs guiConfigs;
    @XmlElement(name = "AppSettings", required = true)
    protected AppSettings appSettings;

    /**
     * Gets the value of the guiConfigs property.
     * 
     * @return
     *     possible object is
     *     {@link GUIConfigs }
     *     
     */
    public GUIConfigs getGUIConfigs() {
        return guiConfigs;
    }

    /**
     * Sets the value of the guiConfigs property.
     * 
     * @param value
     *     allowed object is
     *     {@link GUIConfigs }
     *     
     */
    public void setGUIConfigs(GUIConfigs value) {
        this.guiConfigs = value;
    }

    /**
     * Gets the value of the appSettings property.
     * 
     * @return
     *     possible object is
     *     {@link AppSettings }
     *     
     */
    public AppSettings getAppSettings() {
        return appSettings;
    }

    /**
     * Sets the value of the appSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link AppSettings }
     *     
     */
    public void setAppSettings(AppSettings value) {
        this.appSettings = value;
    }
}
