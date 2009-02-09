
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

package apb.coverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import apb.BuildException;
import apb.Environment;
import apb.TestModuleHelper;

import apb.metadata.CoverageInfo;

import apb.utils.FileUtils;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * To be used from TestTask
 */
public class CoverageBuilder
{
    //~ Instance fields ......................................................................................

    @NotNull private final CoverageInfo coverage;

    @NotNull private final Environment      env;
    @NotNull private final List<File>       filesDoDelete;
    @NotNull private final TestModuleHelper helper;
    @NotNull private final File             outputDir;
    @Nullable private CoverageReport        textReport;

    //~ Constructors .........................................................................................

    public CoverageBuilder(@NotNull Environment env, @NotNull TestModuleHelper helper)
    {
        this.env = env;
        this.helper = helper;
        coverage = helper.getCoverageInfo();
        filesDoDelete = new ArrayList<File>();
        outputDir = helper.getCoverageDir();
    }

    //~ Methods ..............................................................................................

    @NotNull public List<String> addCommandLineArguments()
    {
        List<String> args = new ArrayList<String>();
        List<File>   cs = helper.getClassesToTest();

        if (coverage.enable) {
            if (env.isVerbose()) {
                args.add("-verbose");
            }
            else {
                args.add("-quiet");
            }

            for (CoverageReport report : processReports()) {
                args.add("-r");
                args.add(report.getType());
                args.addAll(report.defines(outputDir));
            }

            if (coverage.dumpData) {
                args.add("-sessiondata");
                args.add("-out");
                args.add(new File(outputDir, DATA_FILE).getAbsolutePath());
            }

            args.add("-f");
            args.add("-ix");
            args.add("@" + exclusionFileForTests());
            args.add("-sp");
            args.add(FileUtils.makePath(helper.getSourcesToTest()));
            args.add("-cp");
            args.add(makePath(env, helper.getOutput(), cs));
            args.add(TESTRUNNER_MAIN);
        }

        return args;
    }

    public void stopRun()
    {
        if (coverage.enable) {
            final CoverageReport report = textReport;

            if (report != null) {
                EnumMap<CoverageReport.Column, Integer> coverageInfo = loadCoverageInfo(report);

                if (!env.isQuiet()) {
                    env.logInfo("Coverage summary Information: %s\n", formatLine(coverageInfo));
                }

                final int min = coverage.ensure;

                if (min > 0 && Collections.min(coverageInfo.values()) < min) {
                    env.handle("Coverage below minimum value of: " + min + "% !!.");
                }
            }

            for (File file : filesDoDelete) {
                file.delete();
            }
        }
    }

    @NotNull public String runnerMainClass()
    {
        return coverage.enable ? EMMARUN : TESTRUNNER_MAIN;
    }

    @NotNull private static String makePath(Environment env, File testsDir, List<File> classesToTest)
    {
        List<File> cp = new ArrayList<File>();
        cp.add(env.applicationJarFile());
        cp.add(testsDir);
        cp.addAll(classesToTest);
        return FileUtils.makePath(cp);
    }

    private String formatLine(EnumMap<CoverageReport.Column, Integer> coverageInfo)
    {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<CoverageReport.Column, Integer> e : coverageInfo.entrySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(e.getKey()).append(" = ").append(e.getValue()).append("%");
        }

        return builder.toString();
    }

    private EnumMap<CoverageReport.Column, Integer> loadCoverageInfo(CoverageReport report)
    {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(report.getOutputFile(outputDir)));

            // Skip 5 lines
            String line = null;

            for (int i = 0; i < 6 && (line = reader.readLine()) != null; i++) {
                ;
            }

            EnumMap<CoverageReport.Column, Integer> coverageInfo =
                new EnumMap<CoverageReport.Column, Integer>(CoverageReport.Column.class);

            if (line != null) {
                for (CoverageReport.Column o : report.getColumns()) {
                    int p = line.indexOf('%');

                    if (p != -1) {
                        int val = Integer.parseInt(line.substring(0, p).trim());
                        coverageInfo.put(o, val);
                        p = line.indexOf(')');
                        line = line.substring(p + 2);
                    }
                }
            }

            return coverageInfo;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            FileUtils.close(reader);
        }
    }

    @NotNull private File createTempFile()
    {
        try {
            File file = File.createTempFile("emma", null);
            file.deleteOnExit();
            filesDoDelete.add(file);
            return file;
        }
        catch (IOException e) {
            throw new BuildException("Cannot create temporary file to store emma info", e);
        }
    }

    @NotNull private List<CoverageReport> processReports()
    {
        List<CoverageReport> reports = new ArrayList<CoverageReport>(coverage.reports());

        if (!env.isQuiet() || coverage.ensure > 0) {
            for (CoverageReport c : coverage.reports()) {
                if (c.getType().equals(CoverageReport.TEXT.getType())) {
                    textReport = c;
                }
            }

            if (textReport == null) {
                textReport = CoverageReport.TEXT.outputTo(createTempFile().getAbsolutePath());
                reports.add(textReport);
            }
        }

        return reports;
    }

    @NotNull private String exclusionFileForTests()
    {
        File testsDir = helper.getOutput();

        try {
            File        exclusionFile = createTempFile();
            PrintWriter ps = new PrintWriter(exclusionFile);

            for (String p : coverage.includes()) {
                ps.println("+" + p);
            }

            ps.println("-apb.*");
            ps.println("-junit.*");

            for (String p : coverage.excludes()) {
                ps.println("-" + p);
            }

            for (File dir : apb.utils.FileUtils.listDirsWithFiles(testsDir, ".class")) {
                ps.printf("-%s.*\n",
                          apb.utils.FileUtils.makeRelative(testsDir, dir).getPath().replace(File.separatorChar,
                                                                                            '.'));
            }

            ps.close();
            return exclusionFile.getAbsolutePath();
        }
        catch (IOException e) {
            throw new BuildException("Cannot create temporary file to store emma exclusions", e);
        }
    }

    //~ Static fields/initializers ...........................................................................

    @NonNls private static final String EMMARUN = "emmarun";

    @NonNls private static final String TESTRUNNER_MAIN = "apb.testrunner.Main";

    @NonNls private static final String DATA_FILE = "coverage.es";
}
