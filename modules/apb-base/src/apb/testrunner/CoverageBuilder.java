

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import apb.Apb;
import apb.BuildException;
import apb.Environment;
import apb.TestModuleHelper;

import apb.coverage.CoverageReport;

import apb.metadata.CoverageInfo;

import apb.tasks.CoreTasks;

import apb.testrunner.output.TestReport;

import apb.utils.StreamUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static apb.Constants.EMMA_JAR;

import static apb.utils.FileUtils.makePath;

/**
 * To be used from TestLauncher
 */
class CoverageBuilder
{
    //~ Instance fields ......................................................................................

    private boolean coverageEnabled;

    @NotNull private final CoverageInfo coverage;
    @Nullable private CoverageReport    textReport;

    @NotNull private final Environment env;
    @NotNull private final File        outputDir;
    @Nullable private File             saveCoverageEc;

    @NotNull private final List<File>                     classesToTest;
    @NotNull private final List<File>                     filesDoDelete = new ArrayList<File>();
    @NotNull private final List<File>                     sourcesToTest;
    @Nullable private Map<CoverageReport.Column, Integer> coverageInfo;
    @NotNull private final String                         workingDirectory;

    //~ Constructors .........................................................................................

    CoverageBuilder(@NotNull TestModuleHelper helper)
    {
        env = helper;
        coverage = helper.getModule().coverage;
        outputDir = helper.getCoverageDir();
        sourcesToTest = helper.getSourcesToTest();
        classesToTest = helper.getClassesToTest();
        workingDirectory = helper.getWorkingDirectory().getPath();
    }

    //~ Methods ..............................................................................................

    public void buildReport()
    {
        final List<String> args = new ArrayList<String>();

        args.addAll(Arrays.asList("-sp", makePath(sourcesToTest), "-in", COVERAGE_EM, "-in", COVERAGE_EC));

        for (CoverageReport report : processReports()) {
            args.add("-r");
            args.add(report.getType());
            args.addAll(report.defines(outputDir));
        }

        emma("report", args);
    }

    public void startRun()
    {
        if (coverageEnabled) {
            instrument();
            coverageInfo = null;
        }
    }

    public void stopRun(@NotNull final TestReport testReport)
    {
        if (!coverageEnabled) {
            return;
        }

        buildReport();

        restoreEmmaFiles();

        final CoverageReport report = textReport;

        if (report != null) {
            coverageInfo = loadCoverageInfo(report);

            testReport.coverage(getCoverageClass(), getCoverageMethod(), getCoverageBlock(),
                                getCoverageLine());

            if (!env.isQuiet()) {
                env.logInfo("Coverage summary Information: %s\n", formatLine(coverageInfo));
            }

            final int min = coverage.ensure;

            if (min > 0) {
                int cv = Collections.min(coverageInfo.values());

                if (cv < min) {
                    env.handle("Coverage (" + cv + "%) below minimum value of: " + min + "% !!");
                }
            }
        }

        for (File file : filesDoDelete) {
            file.delete();
        }
    }

    public void setEnabled(boolean b)
    {
        coverageEnabled = b;
    }

    public File emmaJar()
    {
        final File file = Apb.applicationJarFile();

        if (file != null) {
            final File result = new File(file.getParentFile(), EMMA_JAR);

            if (result.exists()) {
                return result;
            }
        }

        throw new BuildException("cannot find '" + EMMA_JAR + "'");
    }

    public Set<File> classPath()
    {
        return coverageEnabled ? Collections.singleton(getInstrDir()) : Collections.<File>emptySet();
    }

    public Set<File> runnerClassPath()
    {
        return coverageEnabled ? Collections.singleton(emmaJar()) : Collections.<File>emptySet();
    }

    private static String formatLine(Map<CoverageReport.Column, Integer> coverageInfo)
    {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<CoverageReport.Column, Integer> e : coverageInfo.entrySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(e.getKey()).append(" = ").append(e.getValue()).append('%');
        }

        return builder.toString();
    }

    private static void restoreFile(File saveFile, File file)
    {
        file.delete();

        if (saveFile != null) {
            saveFile.renameTo(file);
        }
    }

    @Nullable private static File moveOut(File file)
    {
        if (!file.exists()) {
            return null;
        }

        final File tempFile = tempFileName(file.getName(), file.getParentFile());
        file.renameTo(tempFile);
        return tempFile;
    }

    @NotNull private static File tempFileName(String prefix, File dir)
    {
        File tempFile;

        try {
            tempFile = File.createTempFile(prefix, null, dir);
        }
        catch (IOException e) {
            throw new BuildException("Cannot create temporary file", e);
        }

        tempFile.delete();
        return tempFile;
    }

    private int getCoverageLine()
    {
        return getCoverage(CoverageReport.Column.LINE);
    }

    private int getCoverageBlock()
    {
        return getCoverage(CoverageReport.Column.BLOCK);
    }

    private int getCoverageMethod()
    {
        return getCoverage(CoverageReport.Column.METHOD);
    }

    private int getCoverageClass()
    {
        return getCoverage(CoverageReport.Column.CLASS);
    }

    private int getCoverage(CoverageReport.Column column)
    {
        final Map<CoverageReport.Column, Integer> info = coverageInfo;

        if (info != null) {
            final Integer value = info.get(column);

            if (value != null) {
                return value;
            }
        }

        return -1;
    }

    private void backupEmmaFiles()
    {
        saveCoverageEc = moveOut(coverageEc());
    }

    private void restoreEmmaFiles()
    {
        restoreFile(saveCoverageEc, coverageEc());
    }

    private File coverageEc()
    {
        return new File(workingDirectory, COVERAGE_EC);
    }

    private void instrument()
    {
        backupEmmaFiles();
        final File instrDir = getInstrDir();
        instrDir.mkdirs();

        final List<String> args = new ArrayList<String>();

        args.addAll(Arrays.asList("-d", instrDir.getPath(), "-ip", makePath(classesToTest)));

        File exclusionFile = exclusionFileForTests();

        if (exclusionFile != null) {
            args.add("-ix");
            args.add('@' + exclusionFile.getAbsolutePath());
        }

        emma("instr", args);

        if (exclusionFile != null) {
            exclusionFile.delete();
        }
    }

    private void emma(String command, List<String> args)
    {
        List<String> emmaArgs = new ArrayList<String>(args.size() + 2);
        emmaArgs.add(command);
        emmaArgs.add(env.isVerbose() ? "-verbose" : "-quiet");
        emmaArgs.addAll(args);
        CoreTasks.java("emma", emmaArgs).withClassPath(emmaJar().getPath()).onDir(workingDirectory).execute();
    }

    private File getInstrDir()
    {
        return new File(outputDir, "instr");
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

            EnumMap<CoverageReport.Column, Integer> info =
                new EnumMap<CoverageReport.Column, Integer>(CoverageReport.Column.class);

            if (line != null) {
                for (CoverageReport.Column o : report.getColumns()) {
                    int p = line.indexOf('%');

                    if (p != -1) {
                        int val = Integer.parseInt(line.substring(0, p).trim());
                        info.put(o, val);
                        p = line.indexOf(')');
                        line = line.substring(p + 2);
                    }
                }
            }

            return info;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            StreamUtils.close(reader);
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

    @Nullable private File exclusionFileForTests()
    {
        final Set<String> includes = coverage.includes();
        final Set<String> excludes = coverage.excludes();

        if (excludes.isEmpty() && includes.isEmpty()) {
            return null;
        }

        try {
            File        exclusionFile = createTempFile();
            PrintWriter ps = new PrintWriter(exclusionFile);

            for (String p : includes) {
                ps.println("+" + p);
            }

            // Exclusion set
            for (String p : excludes) {
                ps.println("-" + p);
            }

            ps.close();
            return exclusionFile;
        }
        catch (IOException e) {
            throw new BuildException("Cannot create temporary file to store emma exclusions", e);
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static final String COVERAGE_EM = "coverage.em";
    private static final String COVERAGE_EC = "coverage.ec";
}
