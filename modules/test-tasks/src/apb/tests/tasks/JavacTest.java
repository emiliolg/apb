

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

import apb.BuildException;

import apb.tests.testutils.FileAssert;

import static apb.tasks.CoreTasks.javac;

//
public class JavacTest
    extends TaskTestCase
{
    //~ Methods ..............................................................................................

    public void testFail()
        throws IOException
    {
        boolean exceptionThrown;

        try {
            compile("unchecked");
            exceptionThrown = false;
        }
        catch (BuildException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
        FileAssert.assertDoesNotExist(new File(basedir, "Hello.class"));
    }

    public void testOk()
        throws IOException
    {
        compile("-unchecked");
        FileAssert.assertExists(new File(basedir, "Hello.class"));
    }

    private void compile(final String lintOptions)
    {
        javac(dataPath("src/hello")).to("$basedir")  //
                                    .lint(true)  //
                                    .lintOptions(lintOptions)  //
                                    .debug(true)  //
                                    .sourceVersion("1.6")  //
                                    .targetVersion("1.6")  //
                                    .deprecated(true)  //
                                    .trackUnusedDependencies(true)  //
                                    .failOnWarning(true)  //
                                    .execute();
    }
}
