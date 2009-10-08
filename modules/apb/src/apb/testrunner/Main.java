

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import apb.Apb;

import apb.testrunner.output.TestReport;

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Arrays.asList;

import static apb.Apb.exit;

import static apb.utils.StringUtils.isEmpty;
//
// User: emilio
// Date: Nov 12, 2008
// Time: 7:00:13 PM

//
public class Main
{
    //~ Methods ..............................................................................................

    public static void main(String[] args)
        throws TestSetFailedException
    {
        Apb.createBaseEnvironment();

        TestRunnerOptions options = new TestRunnerOptions(asList(args));
        options.parse();

        TestRunner runner =
            new TestRunner(options.getBaseDir(), options.getOutputDir(), options.getIncludes(),
                           options.getExcludes(), options.getTestGroups());
        runner.setVerbose(options.isVerbose());
        runner.setFailEmpty(options.getFailEmpty());

        run(runner, options);
    }

    private static void run(TestRunner runner, TestRunnerOptions options)
        throws TestSetFailedException
    {
        ClassLoader classloader = createClassLoader(options);

        final TestSetCreator<?> creator = options.findCreator(classloader);
        final String            suite = options.getSuite();
        final String            singleTest = options.getSingleTest();

        final String     reportSpecFile = options.getReportSpecFile();
        final TestReport report = restoreOutput(reportSpecFile);

        int r;

        if (isEmpty(suite)) {
            r = runner.run(creator, report, classloader, singleTest);
        }
        else {
            r = runner.runOne(suite, creator, classloader, report, singleTest);
            saveOutput(report, reportSpecFile);
        }

        exit(r);
    }

    private static ClassLoader createClassLoader(TestRunnerOptions options)
        throws TestSetFailedException
    {
        try {
            final List<String> path = options.getClassPath();
            final List<File>   files = new ArrayList<File>();

            for (String s : path) {
                files.add(new File(s));
            }

            return new URLClassLoader(FileUtils.toURLArray(files),
                                      Thread.currentThread().getContextClassLoader());
        }
        catch (MalformedURLException e) {
            throw new TestSetFailedException(e);
        }
    }

    @NotNull private static TestReport restoreOutput(@Nullable String file)
    {
        TestReport result = null;

        if (file != null) {
            try {
                ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
                result = (TestReport) is.readObject();
                is.close();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        return result == null ? TestReport.SIMPLE.build(Apb.getEnv()) : result;
    }

    private static void saveOutput(@NotNull TestReport report, @Nullable String reportSpecFile)
    {
        if (reportSpecFile != null) {
            try {
                ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(reportSpecFile));
                os.writeObject(report);
                os.close();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
