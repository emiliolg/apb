

// Copyright 2008-2009 Emilio Lopez-Gabeiras
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License
//


package apb.po;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>Java class for PurchaseOrderType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PurchaseOrderType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="shipTo" type="{}USAddress"/>
 *         &lt;element name="billTo" type="{}USAddress"/>
 *         &lt;element ref="{}comment" minOccurs="0"/>
 *         &lt;element name="items" type="{}Items"/>
 *       &lt;/sequence>
 *       &lt;attribute name="orderDate" type="{http://www.w3.org/2001/XMLSchema}date" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
         name = "PurchaseOrderType",
         propOrder = { "shipTo", "billTo", "comment", "items" }
        )
public class PurchaseOrderType
{
    //~ Instance fields ......................................................................................

    @XmlElement(required = true)
    protected Items     items;
    protected String    comment;
    @XmlElement(required = true)
    protected USAddress billTo;

    @XmlElement(required = true)
    protected USAddress                          shipTo;
    @XmlAttribute @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar               orderDate;

    //~ Methods ..............................................................................................

    /**
     * Gets the value of the shipTo property.
     *
     * @return
     *     possible object is
     *     {@link USAddress }
     *
     */
    public USAddress getShipTo()
    {
        return shipTo;
    }

    /**
     * Sets the value of the shipTo property.
     *
     * @param value
     *     allowed object is
     *     {@link USAddress }
     *
     */
    public void setShipTo(USAddress value)
    {
        this.shipTo = value;
    }

    /**
     * Gets the value of the billTo property.
     *
     * @return
     *     possible object is
     *     {@link USAddress }
     *
     */
    public USAddress getBillTo()
    {
        return billTo;
    }

    /**
     * Sets the value of the billTo property.
     *
     * @param value
     *     allowed object is
     *     {@link USAddress }
     *
     */
    public void setBillTo(USAddress value)
    {
        this.billTo = value;
    }

    /**
     * Gets the value of the comment property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getComment()
    {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setComment(String value)
    {
        this.comment = value;
    }

    /**
     * Gets the value of the items property.
     *
     * @return
     *     possible object is
     *     {@link Items }
     *
     */
    public Items getItems()
    {
        return items;
    }

    /**
     * Sets the value of the items property.
     *
     * @param value
     *     allowed object is
     *     {@link Items }
     *
     */
    public void setItems(Items value)
    {
        this.items = value;
    }

    /**
     * Gets the value of the orderDate property.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getOrderDate()
    {
        return orderDate;
    }

    /**
     * Sets the value of the orderDate property.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setOrderDate(XMLGregorianCalendar value)
    {
        this.orderDate = value;
    }
}
