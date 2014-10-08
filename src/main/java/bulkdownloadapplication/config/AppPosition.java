package bulkdownloadapplication.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "AppPosition")
public class AppPosition {

    @XmlAttribute(required = true)
    protected int height;
    @XmlAttribute(required = true)
    protected int width;
    @XmlAttribute(required = true)
    protected int x;
    @XmlAttribute(required = true)
    protected int y;
    @XmlAttribute(required = true)
    protected boolean isMaximized;

    /**
     * Gets the value of the height property.
     * 
     * @return
     *     possible object is
     *     {@link int }
     *     
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the value of the height property.
     * 
     * @param value
     *     allowed object is
     *     {@link int }
     *     
     */
    public void setHeight(int value) {
        this.height = value;
    }

    /**
     * Gets the value of the width property.
     * 
     * @return
     *     possible object is
     *     {@link int }
     *     
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     * @param value
     *     allowed object is
     *     {@link int }
     *     
     */
    public void setWidth(int value) {
        this.width = value;
    }

    /**
     * Gets the value of the x property.
     * 
     * @return
     *     possible object is
     *     {@link int }
     *     
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the value of the x property.
     * 
     * @param value
     *     allowed object is
     *     {@link int }
     *     
     */
    public void setX(int value) {
        this.x = value;
    }

    /**
     * Gets the value of the y property.
     * 
     * @return
     *     possible object is
     *     {@link int }
     *     
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the value of the y property.
     * 
     * @param value
     *     allowed object is
     *     {@link int }
     *     
     */
    public void setY(int value) {
        this.y = value;
    }

    /**
     * Gets the value of the isMaximized property.
     * 
     */
    public boolean isIsMaximized() {
        return isMaximized;
    }

    /**
     * Sets the value of the isMaximized property.
     * 
     */
    public void setIsMaximized(boolean value) {
        this.isMaximized = value;
    }

}
