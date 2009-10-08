

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import apb.tasks.ExecTask;

import apb.tests.testutils.FileAssert;

import static java.util.Arrays.asList;

import static apb.tasks.CoreTasks.exec;

import static apb.tests.testutils.FileAssert.assertDoesNotExist;
import static apb.tests.testutils.FileAssert.assertExists;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class ExecTest
    extends TaskTestCase
{
    //~ Instance fields ......................................................................................

    private File dir1;
    private File dir2;

    //~ Methods ..............................................................................................

    public void testRmDirectory()
        throws IOException
    {
        createFiles();

        exec("rm", "-rf", "dir1").onDir("$basedir").execute();
        assertExists(dir2);
        assertDoesNotExist(dir1);
        exec("rm", "-rf", "$basedir").call();
        assertDoesNotExist(dir2);
        assertDoesNotExist(basedir);
    }

    public void testRmIfReq()
        throws IOException
    {
        env.putProperty("source", "$module-source");
        createFiles();

        exec("rm", "-rf", "dir1").onDir("$basedir")  //
                                 .executeIfRequired(dataPath("."),
                                                    asList("dir1/- Not there -", "dir1/A.java",
                                                           "dir1/B.java"));
        assertDoesNotExist(dir1);

        createFiles();
        exec("rm", "-rf", "dir1").onDir("$basedir")  //
                                 .executeIfRequired("dir1/B.java", asList("dir1/A.java"));
        assertExists(dir1);

        exec("rm", "-rf", "dir1").onDir("$basedir")  //
                                 .executeIfRequired("dir2", "dir1", "A", "B", asList("A.java"));
        assertExists(dir1);

        exec("rm", "-rf", "dir1").onDir("$basedir")  //
                                 .executeIfRequired(dataPath("- Not There -"), asList("dir1/A.java"));
        assertDoesNotExist(dir1);

        createFiles();
        exec("rm", "-rf", "dir1").onDir("$basedir")  //
                                 .executeIfRequired("dir2", "dir1", "java", "txt",
                                                    asList("A.java", "C.java"));

        assertDoesNotExist(dir1);

        createFiles();
        exec("rm", "-rf", "dir1").onDir("$basedir")  //
                                 .executeIfRequired(dataPath("."), "dir1", "java", "txt",
                                                    asList("A.java", "B.java"));

        assertDoesNotExist(dir1);
    }

    public void testExpr()
        throws IOException
    {
        // Using outputTo
        List<String> output = new ArrayList<String>();
        exec("expr", "49", "/", "7").outputTo(output).execute();
        assertEquals("7", output.get(0));

        // Checking the standard output
        String result = invokeExec("expr", "49", "+", "7");

        // keep lastLine
        if (result.endsWith("\n")) {
            result = result.substring(0, result.length() - 1);
        }

        result = result.substring(result.lastIndexOf('\n') + 1);
        assertEquals("56", result);
    }

    public void testTest()
        throws IOException
    {
        ExecTask t = exec("test", "10", "-gt", "7");
        t.execute();
        assertEquals(0, t.getExitValue());

        t = exec("test", "10", "-gt", "17");
        t.execute();
        assertEquals(1, t.getExitValue());
    }

    public void testEnv()
        throws IOException
    {
        List<String>        output = new ArrayList<String>();
        Map<String, String> e = new HashMap<String, String>();
        e.put("AA", "aa");
        e.put("BB", "bb");
        exec("env").withEnvironment(e).outputTo(output).execute();

        assertTrue(output.contains("AA=aa"));
        assertTrue(output.contains("BB=bb"));
    }

    static String invokeExec(String cmd, String... args)
    {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        PrintStream           prev = System.out;

        try {
            PrintStream p = new PrintStream(b);
            System.setOut(p);
            exec(cmd, args).execute();
            p.close();
        }
        finally {
            System.setOut(prev);
        }

        return b.toString();
    }

    void createFiles()
        throws IOException
    {
        dir1 = mkdir("dir1");
        addFiles(dir1, "A.java", "B.java", "C.java");
        addFiles(dir1, "a.txt", "b.txt");
        dir2 = mkdir("dir2");
        addFiles(dir2, "A.java", "B.java", "C.java");
        addFiles(dir2, "a.txt", "b.txt");
    }

    private void addFiles(File dir, String... files)
        throws IOException
    {
        for (String file : files) {
            FileAssert.createFile(dir, file, DATA);
        }
    }

    private File mkdir(String name)
    {
        File result = new File(basedir, name);
        result.mkdir();
        return result;
    }

    //~ Static fields/initializers ...........................................................................

    private static final String[] DATA = new String[] { "// line 1", "// line 2", "// line 3" };
}
