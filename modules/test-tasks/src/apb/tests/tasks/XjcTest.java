

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

import apb.Environment;

import apb.tests.utils.FileAssert;

import static apb.tasks.CoreTasks.*;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class XjcTest
    extends TaskTestCase
{
    //~ Methods ..............................................................................................

    public void testBin()
        throws IOException
    {
        final File schema = new File(dataFile(SCHEMADIR), "po.xsd");
        xjc(schema).to(SRCDIR)  //
                   .usingPackage("apb.po")  //
                   .useTimestamp(false)  //
                   .execute();

        final String dir = SRCDIR + "/apb/po";
        FileAssert.assertDirEquals(dataFile(dir), env.fileFromBase(dir), false);
    }

    public void testJar()
        throws IOException
    {
        env.putProperty(Environment.EXT_PATH_PROPERTY, dataPath("jaxb/lib"));

        final File schema = new File(dataFile(SCHEMADIR), "po.xsd");
        xjc(schema.getPath()).to(SRCDIR)  //
                             .usingPackage("apb.po")  //
                             .useTimestamp(false)  //
                             .execute();

        final String dir = SRCDIR + "/apb/po";
        FileAssert.assertDirEquals(dataFile(dir), env.fileFromBase(dir), false);
    }

    //~ Static fields/initializers ...........................................................................

    private static final String SCHEMADIR = "jaxb/schemas";
    private static final String SRCDIR = "jaxb/src";
}
