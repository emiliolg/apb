

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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the apb.po package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 *
 */
@XmlRegistry public class ObjectFactory
{
    //~ Constructors .........................................................................................

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: apb.po
     *
     */
    public ObjectFactory() {}

    //~ Methods ..............................................................................................

    /**
     * Create an instance of {@link Items.Item }
     *
     */
    public Items.Item createItemsItem()
    {
        return new Items.Item();
    }

    /**
     * Create an instance of {@link PurchaseOrderType }
     *
     */
    public PurchaseOrderType createPurchaseOrderType()
    {
        return new PurchaseOrderType();
    }

    /**
     * Create an instance of {@link USAddress }
     *
     */
    public USAddress createUSAddress()
    {
        return new USAddress();
    }

    /**
     * Create an instance of {@link Items }
     *
     */
    public Items createItems()
    {
        return new Items();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PurchaseOrderType }{@code >}}
     *
     */
    @XmlElementDecl(
                    namespace = "",
                    name = "purchaseOrder"
                   )
    public JAXBElement<PurchaseOrderType> createPurchaseOrder(PurchaseOrderType value)
    {
        return new JAXBElement<PurchaseOrderType>(_PurchaseOrder_QNAME, PurchaseOrderType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(
                    namespace = "",
                    name = "comment"
                   )
    public JAXBElement<String> createComment(String value)
    {
        return new JAXBElement<String>(_Comment_QNAME, String.class, null, value);
    }

    //~ Static fields/initializers ...........................................................................

    private static final QName _PurchaseOrder_QNAME = new QName("", "purchaseOrder");
    private static final QName _Comment_QNAME = new QName("", "comment");
}
