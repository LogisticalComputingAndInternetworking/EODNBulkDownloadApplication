package bulkdownloadapplication.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "appPosition"
})
@XmlRootElement(name = "GUIConfigs")
public class GUIConfigs {

    @XmlElement(name = "AppPosition", required = true)
    protected AppPosition appPosition;

    /**
     * Gets the value of the appPosition property.
     * 
     * @return
     *     possible object is
     *     {@link AppPosition }
     *     
     */
    public AppPosition getAppPosition() {
        return appPosition;
    }

    /**
     * Sets the value of the appPosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link AppPosition }
     *     
     */
    public void setAppPosition(AppPosition value) {
        this.appPosition = value;
    }
}
