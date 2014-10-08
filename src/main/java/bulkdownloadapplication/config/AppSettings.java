package bulkdownloadapplication.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "download"
})
@XmlRootElement(name = "AppSettings")
public class AppSettings {

    @XmlElement(name = "Download", required = true)
    protected Download download;

    /**
     * Gets the value of the download property.
     * 
     * @return
     *     possible object is
     *     {@link Download }
     *     
     */
    public Download getDownload() {
        return download;
    }

    /**
     * Sets the value of the download property.
     * 
     * @param value
     *     allowed object is
     *     {@link Download }
     *     
     */
    public void setDownload(Download value) {
        this.download = value;
    }
}
