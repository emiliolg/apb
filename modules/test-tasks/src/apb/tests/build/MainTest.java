

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


package apb.tests.build;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import apb.Apb;
import apb.Main;

import apb.tests.testutils.CheckOutput;
import apb.tests.testutils.FileAssert;

import junit.framework.TestCase;

import static java.util.Arrays.asList;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class MainTest
    extends TestCase
{
    //~ Instance fields ......................................................................................

    List<String> output;
    private File tmpFile;

    //~ Methods ..............................................................................................

    public void testHello()
        throws Throwable
    {
        invokeMain("HelloWorld.hello");
        checkOutput("[HelloWorld.hello]              Hello World !",  //
                    "",  //
                    BUILD_COMPLETED);
    }

    public void testIdegen()
        throws Throwable
    {
        invokeMain("Math.idegen:idea");
        checkOutput("[Math.idegen:idea]              Writing: " + ideaFile("math.iml"),  //
                    "[tests.Math.idegen:idea]        Writing: " + ideaFile("tests.math.iml"),  //
                    "[Math.idegen:idea]              Writing: " + ideaFile("DEFS.iml"),  //
                    "[Math.idegen:idea]              Writing: " + ideaFile("math.ipr"),  //
                    "",  //
                    BUILD_COMPLETED);
        FileAssert.assertExists(ideaFile("math.iml"));
        FileAssert.assertExists(ideaFile("tests.math.iml"));
        FileAssert.assertExists(ideaFile("DEFS.iml"));
        FileAssert.assertExists(ideaFile("math.ipr"));
    }

    public void testWrongCommand()
        throws Throwable
    {
        invokeMain("HelloWorld.helo");
        checkOutput("[HelloWorld.helo]               Invalid command: helo",  //
                    "[HelloWorld.helo]               ",  //
                    "[HelloWorld.helo]               BUILD FAILED !!");
    }

    public void testWrongModule()
        throws Throwable
    {
        invokeMain("HelloWord.hello");
        checkOutput("[HelloWord]                     Cannot load definition for: HelloWord",  //
                    "[HelloWord]                     Cause: File not found: HelloWord.java",  //
                    "[HelloWord]                     ",  //
                    "[HelloWord]                     BUILD FAILED !!");
    }

    public void testWrongProperty()
        throws Throwable
    {
        invokeMain("HelloWorld.hellowho");
        checkOutput("[HelloWorld.hellowho]           apb.PropertyException: Undefined Property: who",  //
                    "[HelloWorld.hellowho]           ",  //
                    "[HelloWorld.hellowho]           ",  //
                    "[HelloWorld.hellowho]           BUILD FAILED !!");
    }

    public void testOkProperty()
        throws Throwable
    {
        invokeMain("-Dwho=John", "HelloWorld.hellowho");
        checkOutput("[HelloWorld.hellowho]           Hello John !",  //
                    "",  //
                    BUILD_COMPLETED);
    }

    public void testHelp()
        throws Throwable
    {
        invokeMain("Math.help");
        checkOutput("Commands for 'Math' : ",  //
                    "    clean\\+",  //
                    "    compile\\+",  //
                    "   \\+\\+");
    }

    public void testGlobalHelp()
        throws Throwable
    {
        invokeMain("--help");
        checkOutput("apb [options]  Mod.command ...",  //
                    "",  //
                    "Where:",  //
                    "    Mod     : module or project specification defined as 'Mod.java' in the project path.",  //
                    "    command : help and others. (Execute the help command over a module to get the actual list).",  //
                    "",  //
                    "Options: ",  //
                    "  \\+\\+"  //
                   );
    }

    public void testVersion()
        throws Throwable
    {
        invokeMain("--version");
        checkOutput("apb \\+",  //
                    "java \\+",  //
                    "OS \\+",  //
                    "Memory \\+"  //
                   );
    }

    void invokeMain(String... args)
    {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        PrintStream           prev = System.out;
        PrintStream           prevErr = System.err;

        try {
            Apb.setAvoidSystemExit(true);
            PrintStream p = new PrintStream(b);
            System.setOut(p);
            System.setErr(p);

            try {
                String path = System.getProperty("datadir") + "/projects/DEFS";
                tmpFile = new File("tmp").getAbsoluteFile();
                List<String> cmd = new ArrayList<String>(asList(args));
                cmd.add(0, "-Dcolor=false");
                cmd.add(0, "-Dproject.path=" + path);
                cmd.add(0, "-Dtmpdir=" + tmpFile.getPath());
                cmd.add(0, "-f");
                Main.main(cmd.toArray(new String[cmd.size()]));
            }
            catch (Apb.ExitException ignore) {}
            catch (Throwable throwable) {
                throwable.printStackTrace(p);
            }

            p.close();
        }
        finally {
            System.setOut(prev);
            System.setErr(prevErr);
            Apb.setAvoidSystemExit(false);
        }

        output = new ArrayList<String>();

        for (String string : b.toString().split("\n")) {
            output.add(string + "\n");
        }
    }

    private File ideaFile(String name)
    {
        return new File(new File(tmpFile, "IDEA"), name);
    }

    private void checkOutput(String... expected)
    {
        CheckOutput.checkOutput(output, expected);
    }

    //~ Static fields/initializers ...........................................................................

    private static final String BUILD_COMPLETED = "BUILD COMPLETED in \\+";
}
