

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


package apb.tests.tasks;

import java.io.File;
import java.io.IOException;

import apb.tasks.FileSet;

import apb.tests.testutils.FileAssert;

import static apb.tasks.CoreTasks.xslt;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class XsltTest
    extends TaskTestCase
{
    //~ Methods ..............................................................................................

    public void testSingleName()
        throws IOException
    {
        xslt(dataPath(SCHEMA)).to(TABLE)  //
                              .usingStyle(dataPath("xml/tablenameprovider.xsl"))  //
                              .withParameter("package", "apb.test")  //
                              .withParameter("file", "apb/schema.xml")  //
                              .withOutputProperty("method", "text")  //
                              .execute();

        FileAssert.assertFileEquals(dataFile(TABLE), env.fileFromBase(TABLE));
    }

    public void testSingleFile()
        throws IOException
    {
        File out = env.fileFromBase(FIELD);

        xslt(dataFile(SCHEMA)).to(out)  //
                              .usingStyle(dataFile("xml/fieldnameprovider.xsl"))  //
                              .withParameter("package", "apb.test")  //
                              .withParameter("file", "apb/schema.xml")  //
                              .withOutputProperty("method", "text")  //
                              .execute();

        FileAssert.assertFileEquals(dataFile(FIELD), out);
    }

    public void testDirXml()
        throws IOException
    {
        final File out = env.fileFromBase(OUTXML);
        xslt(FileSet.fromDir(dataFile("xml/persons/in"))).to(out)  //
                                                         .usingStyle(dataFile("xml/persons/toxml.xsl"))  //
                                                         .execute();

        FileAssert.assertDirEquals(new File(dataFile("xml/persons"), OUTXML), out);
    }

    public void testDirXhtml()
        throws IOException
    {
        final File out = env.fileFromBase(OUTXHTML);
        xslt(FileSet.fromDir(dataFile("xml/persons/in"))).to(out)  //
                                                         .usingStyle(dataFile("xml/persons/toxhtml.xsl"))  //
                                                         .usingExtension(".xhtml").execute();

        FileAssert.assertDirEquals(new File(dataFile("xml/persons"), OUTXHTML), out);
    }

    //~ Static fields/initializers ...........................................................................

    private static final String OUTXML = "outxml";
    private static final String OUTXHTML = "outxhtml";

    private static final String TABLE = "src/apb/test/TableNameProvider.java";
    private static final String FIELD = "src/apb/test/FieldNameProvider.java";

    private static final String SCHEMA = "xml/schema.xml";
}
