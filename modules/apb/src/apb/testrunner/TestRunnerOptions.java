

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


package apb.testrunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import apb.Messages;

import apb.metadata.TestModule;

import apb.utils.ClassUtils;
import apb.utils.OptionParser;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import static apb.utils.StringUtils.isEmpty;
//
// User: emilio
// Date: Sep 3, 2008
// Time: 6:53:36 PM

class TestRunnerOptions
    extends OptionParser
{
    //~ Instance fields ......................................................................................

    @NotNull private File                         basedir;
    private final Option<String>                  classpath;
    private final Option<String>                  creator;
    private final Option<String>                  excludes;
    private final Option<Boolean>                 failEmpty;
    private final Option<String>                  includes;
    private final Option<String>                  output;
    private final Option<Boolean>                 quiet;
    private final Option<String>                  reports;
    private final Option<String>                  reportSpecs;
    private final Option<String>                  singleTest;
    private final Option<String>                  suite;
    private final Option<String>                  testGroups;
    private final Option<String>                  type;
    private final Option<Boolean>                 verbose;
    @NotNull private final TestSetCreator.Factory factory;

    //~ Constructors .........................................................................................

    TestRunnerOptions(String[] ops)
    {
        super(ops, "testrunner");
        factory = new TestSetCreator.Factory();

        quiet = addBooleanOption('q', "quiet", Messages.QUIET_OUTPUT);
        verbose = addBooleanOption('v', "verbose", Messages.VERBOSE);
        failEmpty = addBooleanOption('f', "fail-empty", "Fail if no tests.");
        suite = addOption('s', "suite", "Run the specified test suite", "<test name>");
        includes = addOption('i', "includes", Messages.SET_TO_INCLUDE, Messages.COLON_SEPARATED_PATTERNS);
        includes.setValue(asString(TestModule.DEFAULT_INCLUDES));
        excludes = addOption('e', "excludes", Messages.SET_TO_EXCLUDE, Messages.COLON_SEPARATED_PATTERNS);
        excludes.setValue(asString(TestModule.DEFAULT_EXCLUDES));
        testGroups = addOption('g', "testGroups", Messages.TEST_GROUPS, Messages.COLON_SEPARATED_PATTERNS);
        type = addOption('t', "type", "The test type", "<type>");
        type.setValue("junit");
        classpath = addOption('c', "classpath", "The classpath used to load the test classes", "<classpath>");
        reports = addOption('r', "reports", "The reports to be generated", "<reports>");
        reportSpecs = addOption("report-specs-file", Messages.REPORT_SPEC_FILE, "<file name>");
        output = addOption('o', "output", Messages.OUTPUT_FOR_REPORTS, "<directory>");
        creator = addOption("creator", "A class defining a creator for a test type.", "<class>");
        singleTest = addOption("single-test", "Define a single test to be run.", "<test-name>");

        for (String testType : factory.names()) {
            type.addValidValue(testType);
        }
    }

    //~ Methods ..............................................................................................

    public String getArgShortDescription()
    {
        return "test-dir";
    }

    public String[] getArgFullDescription()
    {
        return new String[] { "test-dir: The base directory where the test classes are located." };
    }

    @NotNull public List<String> parse()
    {
        final List<String> result = super.parse();

        if (result.isEmpty()) {
            printHelp();
        }

        // Initialize base dir

        // Use current working directory
        basedir = new File(result.get(0));

        if (!basedir.isDirectory()) {
            System.err.println("Invalid base directory: " + basedir);
            System.exit(TestRunner.ERROR);
        }

        return result;
    }

    @NotNull public File getOutputDir()
    {
        String out = output.getValue();
        return isEmpty(out) ? basedir : new File(out);
    }

    @NotNull public List<String> getIncludes()
    {
        return asStringList(includes);
    }

    @NotNull public List<String> getExcludes()
    {
        return asStringList(excludes);
    }

    public boolean getFailEmpty()
    {
        return failEmpty.getValue();
    }

    @NotNull public List<String> getClassPath()
    {
        List<String> result = new ArrayList<String>();

        // Add the test directory to the classpath
        result.add(basedir.getAbsolutePath());

        if (classpath.getValue() != null) {
            result.addAll(asStringList(classpath));
        }

        return result;
    }

    public boolean isVerbose()
    {
        return verbose.getValue();
    }

    public boolean isQuiet()
    {
        return quiet.getValue();
    }

    public String getSuite()
    {
        String result = suite.getValue();

        if (!isEmpty(result) && !result.endsWith(".class")) {
            result += ".class";
        }

        return result;
    }

    public String getSingleTest()
    {
        return singleTest.getValue();
    }

    @NotNull public List<String> getTestGroups()
    {
        return asStringList(testGroups);
    }

    @NotNull public List<String> getReports()
    {
        return asStringList(reports);
    }

    public String getReportSpecFile()
    {
        return reportSpecs.getValue();
    }

    protected void printVersion()
    {
        System.err.println(getAppName() + " version: 1.0");
        System.exit(0);
    }

    File getBaseDir()
    {
        return basedir;
    }

    @NotNull TestSetCreator<?> findCreator(ClassLoader classloader)
        throws TestSetFailedException
    {
        final String      creatorClass = creator.getValue();
        TestSetCreator<?> result = null;

        if (!isEmpty(creatorClass)) {
            try {
                Object o = ClassUtils.newInstance(classloader, creatorClass);

                if (o instanceof TestSetCreator) {
                    result = (TestSetCreator) o;
                }
            }
            catch (Exception e) {}

            if (result == null) {
                System.err.println("Invalid creator class: " + creatorClass);
                System.exit(TestRunner.ERROR);
            }
        }
        else {
            result = factory.fromName(type.getValue());
        }

        return result;
    }

    @NotNull private List<String> asStringList(@NotNull Option<String> option)
    {
        List<String> result = new ArrayList<String>();

        final String value = option.getValue();

        if (!isEmpty(value)) {
            StringTokenizer tok = new StringTokenizer(value, ":");

            while (tok.hasMoreTokens()) {
                result.add(tok.nextToken());
            }
        }

        return result;
    }

    @NotNull private String asString(@NotNull List<String> list)
    {
        StringBuilder result = new StringBuilder();

        for (String s : list) {
            if (result.length() > 0) {
                result.append(':');
            }

            result.append(s);
        }

        return result.toString();
    }

    //~ Static fields/initializers ...........................................................................

    @NonNls public static final String EMMARUN = "--emmarun";
}
