
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

package apb.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import apb.BuildException;

import org.jetbrains.annotations.NotNull;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import org.xml.sax.SAXException;

public class XmlUtils
{
    //~ Constructors .........................................................................................

    private XmlUtils() {}

    //~ Methods ..............................................................................................

    public static Document readDocument(@NotNull File file)
    {
        try {
            DocumentBuilder reader = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            InputStream is = new FileInputStream(file);
            return reader.parse(is);
        }
        catch (ParserConfigurationException e) {
            throw new BuildException(e);
        }
        catch (IOException e) {
            throw new BuildException(e);
        }
        catch (SAXException e) {
            throw new BuildException(e);
        }
    }

    public static void writeDocument(@NotNull Document document, @NotNull File file)
    {
        try {
            final OutputStream output = FileUtils.createOutputStream(file);

            try {
                serialize(document, output);
            }
            finally {
                output.close();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Element findChildElement(Element parent, String name)
    {
        Element  result = null;
        NodeList nodes = parent.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node.getNodeName().equals(name)) {
                result = (Element) node;
                break;
            }
        }

        return result;
    }

    public static void removeAllChildren(Element deps)
    {
        NodeList childNodes = deps.getChildNodes();

        for (int i = childNodes.getLength() - 1; i >= 0; i--) {
            Node item = childNodes.item(i);
            deps.removeChild(item);
        }
    }

    public static void addValuedElement(Document document, Element parent, String elementName, String content)
    {
        Element name = document.createElement(elementName);

        Text text = document.createTextNode(content);
        name.appendChild(text);
        parent.appendChild(name);
    }

    private static void serialize(Document document, OutputStream output)
        throws LSException
    {
        final DOMImplementationLS ls =
            (DOMImplementationLS) document.getImplementation().getFeature("LS", "3.0");

        final LSOutput out = ls.createLSOutput();
        out.setByteStream(output);

        final LSSerializer serializer = ls.createLSSerializer();

        serializer.getDomConfig().setParameter("format-pretty-print", true);
        serializer.write(document, out);
    }
}
