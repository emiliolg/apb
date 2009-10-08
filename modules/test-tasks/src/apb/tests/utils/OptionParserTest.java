

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import apb.Apb;
import apb.Environment;

import apb.utils.ClassUtils;
import apb.utils.OptionParser;

import junit.framework.TestCase;

import static java.util.Arrays.asList;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class OptionParserTest
    extends TestCase
{
    //~ Instance fields ......................................................................................

    private File datadir;

    //~ Methods ..............................................................................................

    public void testAhHoc()
        throws IOException
    {
        final List<String> args = asList("-aa1", "--no-flag", "--", "test1");
        MyOptionParser     op = new MyOptionParser(args);
        op.addOption('a', "aoption", "the a option.", "<some a>");
        OptionParser.Option<Integer> opt = op.addIntegerOption('n', "noption", "the n option.", "<some n>");
        opt.setCanRepeat(true);
        opt.addValidValue(1);
        opt.addValidValue(3);
        opt.addValidValue(5);
        op.addBooleanOption('f', "flag", "A flag");

        List<String> result = op.parse();
        assertEquals(asList("test1"), result);

        assertEquals(args, op.getArguments());

        String[] help = invoke(op, "printHelp");

        for (int i = 0; i < help.length; i++) {
            assertEquals(AD_HOC_HELP[i], help[i]);
        }
    }

    @SuppressWarnings({ "unchecked" })
    public void testApbOptions()
        throws Exception
    {
        OptionParser op = (OptionParser) ClassUtils.newInstance("apb.ApbOptions", APB_ARGS);
        String[]     help = invoke(op, "printHelp");

        for (int i = 0; i < help.length; i++) {
            assertEquals(APB_HELP[i], help[i]);
        }

        List<String> args = op.parse();
        assertEquals(asList("X.y"), args);

        String[] version = invoke(op, "printVersion");
        assertEquals("apb", version[0].substring(0, 3));

        Environment env = Apb.createBaseEnvironment();
        ClassUtils.invoke(op, "initEnv", env);

        assertEquals(true, env.isVerbose());
        Map<String, String> ps = (Map<String, String>) ClassUtils.invoke(op, "definedProperties");
        assertEquals("v1", ps.get("prop1"));
        assertEquals("true", ps.get("bprop"));

        boolean b = (Boolean) ClassUtils.invoke(op, "showStackTrace");
        assertTrue(b);
    }

    static String[] invoke(OptionParser op, String method)
    {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        PrintStream           prev = System.err;
        PrintStream           p = new PrintStream(b);

        try {
            System.setErr(p);
            ClassUtils.invoke(op, method);
        }
        catch (Exception e) {
            // ignore
        }
        finally {
            p.close();
            System.setErr(prev);
        }

        return b.toString().split("\n");
    }

    //~ Static fields/initializers ...........................................................................

    private static final List<String> APB_ARGS =
        asList("-s", "-Dprop1=v1", "-Dbprop", "-v", "--debug", "properties", "X.y");

    private static final String[] AD_HOC_HELP = {
            "test [options]  ",  //
            "",  //
            "Where:",  //
            "    A long",  //
            "    description",  //
            "",  //
            "Options: ",  //
            "    -h, --help            : Display command line options help. ",  //
            "        --version         : Print version information and exit.",  //
            "    -a, --aoption <some a>: the a option.",  //
            "    -n, --noption <some n>: the n option. [1|3|5]",  //
            "    -f, --flag            : A flag",  //
            "",
        };
    private static final String[] APB_HELP = {
            "apb [options]  Mod.command ...", "", "Where:",
            "    Mod     : module or project specification defined as 'Mod.java' in the project path.",
            "    command : help and others. (Execute the help command over a module to get the actual list).",
            "", "Options: ", "    -h, --help                 : Display command line options help. ",
            "        --version              : Print version information and exit.",
            "    -s, --show-stack-trace     : Show stack trace for build exception.",
            "    -q, --quiet                : Quiet output - only show errors.",
            "    -v, --verbose              : Be extra verbose.",
            "    -c, --continue             : Continue after error.",
            "    -f, --force-build          : Force build (Do not check timestamps).",
            "    -n, --non-recursive        : Do not recurse over module dependencies.",
            "    -D, --define <name>=<value>: Define a property.",
            "    -t, --track-execution      : Track execution statistics.",
            "    -d, --debug <info type>    : What to show when doing verbose output. [all|dependencies|properties|task_info|track]",
        };
    private static final String[] LONG_DESCR = { "A long", "description" };

    //~ Inner Classes ........................................................................................

    private static class MyOptionParser
        extends OptionParser
    {
        public MyOptionParser(List<String> ops)
        {
            super(ops, "test");
        }

        @Override public List<String> parse()
        {
            return super.parse();
        }

        @Override public String[] getArgFullDescription()
        {
            return LONG_DESCR;
        }

        @Override public void printVersion() {}
    }
}
