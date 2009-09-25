

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import apb.Apb;
import apb.DefinitionException;
import apb.Environment;
import apb.ProjectBuilder;

import apb.tests.testutils.TestLogger;

import junit.framework.ComparisonCompactor;
import junit.framework.TestCase;

import org.jetbrains.annotations.NotNull;

import static java.util.Collections.singleton;

import static apb.tasks.CoreTasks.delete;

import static apb.utils.StringUtils.truncateTo;

public abstract class ApbTestCase
    extends TestCase
{
    //~ Instance fields ......................................................................................

    protected Environment  env;
    protected File         dataDir;
    protected File         tmpdir;
    protected List<String> output;
    protected Set<File>    projectPath;

    //~ Constructors .........................................................................................

    public ApbTestCase() {}

    //~ Methods ..............................................................................................

    protected void setUp()
        throws Exception
    {
        output = new ArrayList<String>();
        tmpdir = new File("tmp");
        final String path = tmpdir.getAbsolutePath();

        Map<String, String> props = new HashMap<String, String>();
        props.put("tmpdir", path);

        if (tmpdir.exists()) {
            delete(tmpdir).execute();
        }

        if (!tmpdir.mkdirs()) {
            throw new IOException("Cannot create temporary directory: '" + path + "' for tests.");
        }

        createEnv(props);
        dataDir = new File(env.expand("$datadir"));
        projectPath = singleton(new File(dataDir, "projects/DEFS"));
    }

    protected void build(final String module, final String command)
        throws DefinitionException
    {
        output.clear();
        ProjectBuilder builder = new ProjectBuilder(env, projectPath);
        builder.build(env, module, command);
    }

    protected void createEnv(Map<String, String> properties)
    {
        env = Apb.createBaseEnvironment(new TestLogger(output), properties);
    }

    @NotNull protected String getOutputLine(int index)
    {
        return index >= output.size() ? "" : output.get(index);
    }

    protected void checkOutput(String... expectedLines)
    {
        StringBuilder msg = new StringBuilder();
        boolean       many = false;

        for (int i = 0, j = 0; i < expectedLines.length || j < output.size(); i++, j++) {
            String current = output(j);
            String expected = i < expectedLines.length ? expectedLines[i] : "";

            //Check if partial match
            if (expected.endsWith("\\+")) {
                expected = expected.substring(0, expected.length() - 2);

                // Check if many
                if (expected.endsWith("\\+")) {
                    expected = expected.substring(0, expected.length() - 2);
                    many = true;
                }

                current = truncateTo(current, expected);
            }

            if (!expected.equals(current)) {
                msg.append(new ComparisonCompactor(20, expected, current).compact("Line (" + i + "):"));
                msg.append("\n");
            }

            if (many) {
                while (expected.equals(truncateTo(output(++j), expected))) {
                    ;
                }

                --j;
                many = false;
            }
        }

        if (msg.length() > 0) {
            fail(msg.toString());
        }
    }

    protected String dataFile(String fileName)
    {
        return new File(dataDir, fileName).getAbsolutePath();
    }

    protected String tmpFile(String fileName)
    {
        return new File(tmpdir, fileName).getAbsolutePath();
    }

    private String output(int i)
    {
        String current = i < output.size() ? output.get(i) : "\n";
        return current.substring(0, current.length() - 1);
    }

    //~ Static fields/initializers ...........................................................................

    protected static final String SOME_TESTS_HAVE_FAILED = "Some tests have failed.";
    protected static final String CHECK = "                 Check: ";

    protected static final String DELETING_DIRECTORY = "Deleting directory ";
    protected static final String DELETING_FILE = "Deleting file ";
    protected static final String COMPILING_1_FILE = "Compiling   1 file";
    protected static final String BUILDING = "Building: ";
    protected static final String SKIPPING_EMPTY_DIR = "Skipping empty directory: ";
}
