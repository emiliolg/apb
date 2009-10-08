

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>Java class for Items complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Items">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="item" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="productName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="quantity">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}positiveInteger">
 *                         &lt;maxExclusive value="100"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="USPrice" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *                   &lt;element ref="{}comment" minOccurs="0"/>
 *                   &lt;element name="shipDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="partNum" use="required" type="{}SKU" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
         name = "Items",
         propOrder = { "item" }
        )
public class Items
{
    //~ Instance fields ......................................................................................

    @XmlElement(required = true)
    protected List<Items.Item> item;

    //~ Methods ..............................................................................................

    /**
     * Gets the value of the item property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the item property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getItem().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Items.Item }
     *
     *
     */
    public List<Items.Item> getItem()
    {
        if (item == null) {
            item = new ArrayList<Items.Item>();
        }

        return this.item;
    }

    //~ Inner Classes ........................................................................................

    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="productName" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="quantity">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}positiveInteger">
     *               &lt;maxExclusive value="100"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="USPrice" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
     *         &lt;element ref="{}comment" minOccurs="0"/>
     *         &lt;element name="shipDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="partNum" use="required" type="{}SKU" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(
             name = "",
             propOrder = { "productName", "quantity", "usPrice", "comment", "shipDate" }
            )
    public static class Item
    {
        @XmlElement(
                    name = "USPrice",
                    required = true
                   )
        protected BigDecimal usPrice;
        protected int        quantity;
        protected String     comment;
        @XmlAttribute(required = true)
        protected String     partNum;

        @XmlElement(required = true)
        protected String               productName;
        @XmlSchemaType(name = "date")
        protected XMLGregorianCalendar shipDate;

        /**
         * Gets the value of the productName property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getProductName()
        {
            return productName;
        }

        /**
         * Sets the value of the productName property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setProductName(String value)
        {
            this.productName = value;
        }

        /**
         * Gets the value of the quantity property.
         *
         */
        public int getQuantity()
        {
            return quantity;
        }

        /**
         * Sets the value of the quantity property.
         *
         */
        public void setQuantity(int value)
        {
            this.quantity = value;
        }

        /**
         * Gets the value of the usPrice property.
         *
         * @return
         *     possible object is
         *     {@link BigDecimal }
         *
         */
        public BigDecimal getUSPrice()
        {
            return usPrice;
        }

        /**
         * Sets the value of the usPrice property.
         *
         * @param value
         *     allowed object is
         *     {@link BigDecimal }
         *
         */
        public void setUSPrice(BigDecimal value)
        {
            this.usPrice = value;
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
         * Gets the value of the shipDate property.
         *
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *
         */
        public XMLGregorianCalendar getShipDate()
        {
            return shipDate;
        }

        /**
         * Sets the value of the shipDate property.
         *
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *
         */
        public void setShipDate(XMLGregorianCalendar value)
        {
            this.shipDate = value;
        }

        /**
         * Gets the value of the partNum property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getPartNum()
        {
            return partNum;
        }

        /**
         * Sets the value of the partNum property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setPartNum(String value)
        {
            this.partNum = value;
        }
    }
}
