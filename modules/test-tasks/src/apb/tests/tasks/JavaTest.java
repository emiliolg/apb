

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import apb.tasks.FileSet;
import apb.tasks.JavaTask;

import apb.tests.testutils.FileAssert;

import static java.util.Arrays.asList;

import static apb.tasks.CoreTasks.*;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class JavaTest
    extends TaskTestCase
{
    //~ Methods ..............................................................................................

    public void testSum()
        throws IOException
    {
        javac(FileSet.fromDir("$module-src").including(SUM_ARGS + ".java")).to("$basedir")  //
                                                                           .withClassPath("$apb-jar")  //
                                                                           .execute();
        FileAssert.assertExists(new File(basedir, SUM_ARGS + ".class"));

        List<String> output = new ArrayList<String>();
        JavaTask     j = java(makeClassName(SUM_ARGS), "1", "2", "3").outputTo(output);
        j.execute();

        assertEquals("6", output.get(0));
        assertEquals(1, j.getExitValue());

        output.clear();
        j = java(makeClassName(SUM_ARGS), "-1", "2", "-3").outputTo(output)  //
                                                          .maxMemory(64)  //
                                                          .onDir("$basedir/..")  //
                                                          .withClassPath("$basedir");
        j.execute();

        assertEquals("-2", output.get(0));
        assertEquals(2, j.getExitValue());
    }

    public void testNotNull()
        throws IOException
    {
        final FileSet set = FileSet.fromDir("$module-src").including(SUM_ARGS + ".java");
        final File    classFile = new File(basedir, SUM_ARGS + ".class");

        javac(set).to("$basedir")  //
                  .withClassPath("$apb-jar")  //
                  .instrumentNotNull(true)  //
                  .execute();

        FileAssert.assertExists(classFile);

        List<String> output = new ArrayList<String>();
        java(makeClassName(SUM_ARGS), "1", "2", "null").outputTo(output).redirectErrorStream(true).execute();

        assertTrue("Exception not thrown", output.get(0).contains("IllegalArgumentException"));

        delete(classFile).execute();

        // now do not instrument

        javac(set).to("$basedir")  //
                  .withClassPath("$apb-jar")  //
                  .instrumentNotNull(false)  //
                  .execute();

        FileAssert.assertExists(classFile);

        output.clear();
        java(makeClassName(SUM_ARGS), "1", "2", "null").outputTo(output).redirectErrorStream(true).execute();

        assertEquals("3", output.get(0));
    }

    public void testTouch()
        throws IOException
    {
        // Do twice to check uptodate mechanism
        compile();
        compile();

        jar(TOUCH_JAR).fromDir(basedir)  //
                      .including(TOUCH + ".class")  //
                      .mainClass(makeClassName(TOUCH))  //
                      .withClassPath(asList("$apb-jar"))  //
                      .execute();

        FileAssert.assertExists(new File(basedir, TOUCH_JAR));

        final long          ts = System.currentTimeMillis();
        Map<String, String> ps = new HashMap<String, String>();
        ps.put("timestamp", String.valueOf(ts));
        javaJar(TOUCH_JAR, "$basedir/f1").withProperties(ps)  //
                                         .withClassPath("$basedir", "$apb-jar")  //
                                         .execute();

        File f = new File(basedir, "f1");
        assertEquals(ts / 1000, f.lastModified() / 1000);
    }

    private static String makeClassName(String classFile)
    {
        return classFile.replace('/', '.');
    }

    private void compile()
    {
        javac(FileSet.fromDir("$module-src").including(TOUCH + ".java")).to("$basedir")  //
                                                                        .withClassPath("$basedir", "$apb-jar")  //
                                                                        .lint(true)  //
                                                                        .debug(true)  //
                                                                        .sourceVersion("1.5")  //
                                                                        .targetVersion("1.5")  //
                                                                        .deprecated(true)  //
                                                                        .trackUnusedDependencies(true)  //
                                                                        .failOnWarning(true)  //
                                                                        .showWarnings(true)  //
                                                                        .execute();

        FileAssert.assertExists(new File(basedir, TOUCH + ".class"));
    }

    //~ Static fields/initializers ...........................................................................

    private static final String TOUCH_JAR = "touch.jar";

    private static final String SUM_ARGS = "apb/tests/javas/SumArgs";
    private static final String TOUCH = "apb/tests/javas/Touch";
}
