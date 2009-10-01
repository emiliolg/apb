

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

import apb.tests.testutils.CheckOutput;
import apb.tests.testutils.TestLogger;

import junit.framework.TestCase;

import org.jetbrains.annotations.NotNull;

import static java.util.Collections.singleton;

import static apb.tasks.CoreTasks.delete;

public abstract class ApbTestCase
    extends TestCase
{
    //~ Instance fields ......................................................................................

    protected Environment env;

    protected File         dataDir;
    protected File         fractionClass;
    protected File         fractionTestClass;
    protected File         mathClasses;
    protected File         mathJar;
    protected File         outputDir;
    protected File         playClass;
    protected File         playClasses;
    protected File         playJar;
    protected File         playSrcJar;
    protected File         testMathClasses;
    protected File         testOutputDir;
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
        outputDir = new File(tmpdir, "output").getAbsoluteFile();
        testOutputDir = new File(outputDir, "tests/math");
        mathClasses = new File(new File(outputDir, "math"), "classes");
        fractionClass = new File(mathClasses, "math/Fraction.class");
        testMathClasses = new File(testOutputDir, "classes");
        fractionTestClass = new File(testMathClasses, "math/test/FractionTest.class");
        mathJar = new File(outputDir, "lib/tests-math-1.0.jar");
        playJar = new File(outputDir, "lib/tests-play-with-math-1.0.jar");
        playSrcJar = new File(outputDir, "lib/tests-play-with-math-1.0-src.jar");
        playClasses = new File(new File(outputDir, "play-with-math"), "classes");
        playClass = new File(playClasses, "Play.class");
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

    protected String dataFile(String fileName)
    {
        return new File(dataDir, fileName).getAbsolutePath();
    }

    protected String tmpFile(String fileName)
    {
        return new File(tmpdir, fileName).getAbsolutePath();
    }

    protected void checkOutput(String... expected)
    {
        CheckOutput.checkOutput(output, expected);
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
