

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

import apb.Main;

import apb.tests.testutils.CheckOutput;

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

    //~ Methods ..............................................................................................

    public void testHello()
        throws Throwable
    {
        invokeMain("HelloWorld.hello");
        checkOutput("[HelloWorld.hello]              Hello World !",  //
                    "[HelloWorld]                    ",  //
                    "[HelloWorld]                    BUILD COMPLETED in \\+");
    }

    public void testIdegen()
        throws Throwable
    {
        invokeMain("Math.idegen:idea");
        checkOutput(IDEGEN_WRITING,  //
                    IDEGEN_WRITING,  //   Test
                    IDEGEN_WRITING,  //
                    IDEGEN_WRITING,  //
                    "[Math]                          ",  //
                    "[Math]                          BUILD COMPLETED in \\+");
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
                    "[HelloWord]                     Cause: HelloWord.java",  //
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
                    "[HelloWorld]                    ",  //
                    "[HelloWorld]                    BUILD COMPLETED in \\+");
    }

    void invokeMain(String... args)
    {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        PrintStream           prev = System.out;

        try {
            PrintStream p = new PrintStream(b);
            System.setOut(p);

            try {
                String       path = System.getProperty("datadir") + "/projects/DEFS";
                String       tmp = new File("tmp").getAbsolutePath();
                List<String> cmd = new ArrayList<String>(asList(args));
                cmd.add(0, "-Dcolor=false");
                cmd.add(0, "-Dproject.path=" + path);
                cmd.add(0, "-Dtmpdir=" + tmp);
                cmd.add(0, "-f");
                Main.main(cmd.toArray(new String[cmd.size()]));
            }
            catch (Throwable throwable) {
                throwable.printStackTrace(p);
            }

            p.close();
        }
        finally {
            System.setOut(prev);
        }

        output = new ArrayList<String>();

        for (String string : b.toString().split("\n")) {
            output.add(string + "\n");
        }
    }

    private void checkOutput(String... expected)
    {
        CheckOutput.checkOutput(output, expected);
    }

    //~ Static fields/initializers ...........................................................................

    private static final String IDEGEN_WRITING = "[Math.idegen:idea]              Writing: \\+";
}
