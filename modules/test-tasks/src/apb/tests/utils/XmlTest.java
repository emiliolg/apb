

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


package apb.tests.utils;

import java.io.File;
import java.io.IOException;

import apb.Apb;
import apb.Environment;

import apb.tests.testutils.FileAssert;

import apb.utils.XmlUtils;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class XmlTest
    extends TestCase
{
    //~ Instance fields ......................................................................................

    private File datadir;

    //~ Methods ..............................................................................................

    public void test()
        throws IOException
    {
        final File file = dataFile("xml/schema.xml");
        Document   d = XmlUtils.readDocument(file);
        assertEquals("UTF-8", d.getInputEncoding());

        Element e = XmlUtils.findChildElement(d.getDocumentElement(), "table");
        assertEquals("OUs", e.getAttribute("name"));

        assertEquals(11, e.getChildNodes().getLength());

        XmlUtils.removeAllChildren(e);
        assertEquals(0, e.getChildNodes().getLength());

        XmlUtils.addValuedElement(d, d.getDocumentElement(), "test", "test-content");
        e = XmlUtils.findChildElement(d.getDocumentElement(), "test");
        assertEquals("test-content", e.getTextContent());

        File basedir = new File("tmp");
        basedir.mkdirs();

        final File file1 = new File(basedir, "modified-schema.xml");
        XmlUtils.writeDocument(d, file1);
        FileAssert.assertFileEquals(dataFile("xml/modified-schema.xml"), file1);
    }

    @Override protected void setUp()
        throws IOException
    {
        Environment env = Apb.createBaseEnvironment();
        datadir = new File(env.expand("$datadir"));
    }

    protected File dataFile(String name)
    {
        return new File(datadir, name);
    }
}
