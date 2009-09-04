

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
import java.io.PrintStream;

import apb.Apb;
import apb.Environment;

import apb.tasks.CoreTasks;

import apb.utils.StringUtils;

import junit.framework.Assert;
import junit.framework.TestCase;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class PrintfTest
    extends TestCase
{
    //~ Instance fields ......................................................................................

    private Environment env;

    //~ Methods ..............................................................................................

    public void testPlain()
    {
        Assert.assertEquals(invokePrintf("hello"), "hello");
        String str = StringUtils.nChars(8192, 'a');
        String s1 = str + "\r\n" + str + "\n" + str + "\r\n";
        String s2 = str + LINE_SEPARATOR + str + LINE_SEPARATOR + str + LINE_SEPARATOR;
        Assert.assertEquals(invokePrintf(s1), s2);
    }

    public void testArgs()
    {
        Assert.assertEquals(invokePrintf("hello %s", "world"), "hello world");
        Assert.assertEquals(invokePrintf("%d/%d = %.1f", 5, 2, 5.0 / 2), "5/2 = 2.5");
    }

    public void testProperties()
    {
        env.putProperty("p1", "10");
        env.putProperty("p2", "5");

        Assert.assertEquals(invokePrintf("hello:\n   $user.name"),
                            "hello:\n   " + System.getProperty("user.name"));
        Assert.assertEquals(invokePrintf("(${p1}${p2}-$p2)/$p1=$p1"), "(105-5)/10=10");
    }

    @Override protected void setUp()
        throws Exception
    {
        env = Apb.createBaseEnvironment();
    }

    static String invokePrintf(String format, Object... args)
    {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        PrintStream           prev = System.out;

        try {
            PrintStream p = new PrintStream(b);
            System.setOut(p);
            CoreTasks.printf(format, args);
            p.close();
        }
        finally {
            System.setOut(prev);
        }

        return b.toString();
    }

    //~ Static fields/initializers ...........................................................................

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
}
