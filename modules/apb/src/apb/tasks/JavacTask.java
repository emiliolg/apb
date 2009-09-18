

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


package apb.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import apb.Apb;
import apb.BuildException;

import apb.compiler.DiagnosticReporter;
import apb.compiler.JavaC;

import apb.metadata.Library;
import apb.metadata.PackageType;

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Collections.singletonList;

import static apb.utils.CollectionUtils.addIfNotNull;
import static apb.utils.FileUtils.makePath;
import static apb.utils.StringUtils.appendIndenting;

//
// User: emilio
// Date: Oct 20, 2008
// Time: 4:15:59 PM

//
public class JavacTask
    extends Task
{
    //~ Instance fields ......................................................................................

    private boolean            debug;
    private boolean            deprecated;
    private boolean            failOnWarning;
    private boolean            lint;
    private boolean            trackUnusedDependencies;
    private boolean            warn;
    private DiagnosticReporter reporter;

    @NotNull private final File          targetDir;
    @NotNull private final List<File>    classPath;
    @NotNull private final List<File>    extraLibraries;
    @NotNull private final List<FileSet> fileSets;
    @NotNull private final List<File>    sourceDirs;

    @NotNull private final Map<String, String> annnotationOptions;

    @NotNull private String lintOptions;
    @NotNull private String name;
    @NotNull private String processing;
    @NotNull private String source;
    @NotNull private String target;

    //~ Constructors .........................................................................................

    private JavacTask(@NotNull List<FileSet> fileSets, @NotNull File targetDir)
    {
        super();
        this.targetDir = targetDir;
        this.fileSets = fileSets;
        classPath = new ArrayList<File>();
        extraLibraries = new ArrayList<File>();
        sourceDirs = new ArrayList<File>();
        reporter = new DiagnosticReporter(this);
        source = "";
        target = "";
        lintOptions = "";
        processing = "";
        name = "";
        annnotationOptions = new HashMap<String, String>();
    }

    //~ Methods ..............................................................................................

    /**
     * Add the specified files to the classpath
     * @param files the files to add to the classpath
     */
    public JavacTask withClassPath(List<File> files)
    {
        classPath.addAll(files);
        return this;
    }

    /**
     * Add the specified files to the classpath
     * @param fileNames the files to add to the classpath
     */
    public JavacTask withClassPath(String... fileNames)
    {
        for (String fileName : fileNames) {
            classPath.add(env.fileFromBase(fileName));
        }

        return this;
    }

    public JavacTask withAnnotations(Map<String, String> map)
    {
        annnotationOptions.putAll(map);
        return this;
    }

    public JavacTask sourceVersion(String v)
    {
        source = v;
        return this;
    }

    public JavacTask targetVersion(String v)
    {
        target = v;
        return this;
    }

    public JavacTask withExtraLibraries(List<Library> libraries)
    {
        for (Library library : libraries) {
            addIfNotNull(extraLibraries, library.getArtifact(env, PackageType.JAR));
        }

        return this;
    }

    /**
     * Which files eclude from warning analysis
     */
    public JavacTask excludeFromWarning(List<String> patterns)
    {
        if (reporter != null) {
            reporter.setExcludes(patterns);
        }

        return this;
    }

    public void execute()
    {
        JavaC jc = new JavaC(reporter);

        List<File> files = filterByTimeStamp();

        if (files.isEmpty()) {
            logVerbose("Nothing to compile\n");
        }
        else {
            env.logInfo("Compiling %3d file%s\n", files.size(), (files.size() > 1) ? "s" : "");

            if (env.isVerbose()) {
                for (File file : FileUtils.removePrefix(sourceDirs, files)) {
                    logVerbose("         %s\n", file);
                }

                logVerbose("ClassPath: \n");

                for (File file : classPath) {
                    logVerbose("         %s\n", file);
                }

                logVerbose("Extra Libraries: \n");

                for (File file : extraLibraries) {
                    logVerbose("         %s\n", file);
                }

                logVerbose("Source:\n");

                for (FileSet dir : fileSets) {
                    logVerbose("         %s\n", dir.getDir());
                }

                logVerbose("Target directory: %s\n", targetDir);
            }

            List<String> options = new ArrayList<String>();

            if (debug) {
                options.add("-g");
            }

            if (lint) {
                if (lintOptions.isEmpty()) {
                    options.add("-Xlint");
                }
                else {
                    options.add("-Xlint:" + lintOptions);
                }
            }

            if (deprecated) {
                options.add("-deprecation");
            }

            if (!warn) {
                options.add("-nowarn");
            }

            if (failOnWarning && reporter == null) {
                options.add("-Werror");
            }

            if (!source.isEmpty()) {
                options.add("-source");
                options.add(source);
            }

            if (!target.isEmpty()) {
                options.add("-target");
                options.add(target);
            }

            for (Map.Entry<String, String> entry : annnotationOptions.entrySet()) {
                options.add("-A" + entry.getKey() + "=" + entry.getValue());
            }

            if (!processing.isEmpty()) {
                options.add("-proc:" + processing);
            }

            final boolean status =
                jc.compile(files, sourceDirs, targetDir, classPath, extraLibraries, options,
                           trackUnusedDependencies);

            if (reporter != null) {
                reporter.reportSumary();
            }

            if (!status) {
                env.handle("Compilation failed");
            }
            else if (trackUnusedDependencies) {
                final List<File> unused = jc.unusedPathElements(classPath);

                if (!unused.isEmpty()) {
                    throw new UnusedLibrariesException(name, unused);
                }
            }
        }
    }

    public boolean failOnWarning()
    {
        return failOnWarning;
    }

    /**
     * Mark this source file as failed
     * In the case that fail on Warning is true this causes the deletion of the class file
     * so compilation is retried
     * @param file The offending file
     */
    public void markAsFail(String file)
    {
        // If fail on warning delete the class file to ensure that compilation is retried
        if (failOnWarning && file != null) {
            // Look for the target class file
            file = removeSourceDir(file);

            if (file != null) {
                File classFile = FileUtils.changeExtension(new File(targetDir, file), ".class");

                if (classFile.exists()) {
                    logVerbose("Removing class: %s\n", classFile);
                    classFile.delete();
                }
            }
        }
    }

    @Nullable public String removeSourceDir(@NotNull String file)
    {
        for (File dir : sourceDirs) {
            final String d = dir.getPath();

            if (file.startsWith(d)) {
                return file.substring(d.length() + 1);
            }
        }

        return null;
    }

    /**
     * Indicates whether source should be compiled with debug information
     */
    public JavacTask debug(boolean b)
    {
        debug = b;
        return this;
    }

    /**
     * Indicates whether source should be compiled with deprecation information; defaults to false
     */
    public JavacTask deprecated(boolean b)
    {
        deprecated = b;
        return this;
    }

    /**
     * Indicates whether to enable recommended warnings
     */
    public JavacTask lint(boolean b)
    {
        lint = b;
        return this;
    }

    /**
     * Indicates whether to enable warnings
     */
    public JavacTask showWarnings(boolean b)
    {
        warn = b;
        return this;
    }

    /**
     * Specify wheter to fail when any warning is issued or not
     */
    public JavacTask failOnWarning(boolean b)
    {
        failOnWarning = b;
        return this;
    }

    /**
     * Enable or disable specific warnings
     * all,
     * cast,deprecation,divzero,empty,unchecked,fallthrough,path,serial,finally,overrides,
     * -cast,-deprecation,-divzero,-empty,-unchecked,-fallthrough,-path,-serial,-finally,-overrides,
     * none
     */
    public JavacTask lintOptions(String options)
    {
        lintOptions = options;
        return this;
    }

    /**
     * Specify wheter to generate a warning if unused dependencies are found
     */
    public JavacTask trackUnusedDependencies(boolean b)
    {
        trackUnusedDependencies = b;
        return this;
    }

    /**
     * Control whether annotation processing and/or compilation is done. {none,only}
     */
    public JavacTask processing(String options)
    {
        processing = options;
        return this;
    }

    /**
     * Wheter to use the default Error formatter or the one provided by APB
     */
    public JavacTask usingDefaultFormatter(boolean b)
    {
        if (b) {
            reporter = null;
        }

        return this;
    }

    /**
     * Define a name to be used in diagnostic information
     */
    public JavacTask useName(String s)
    {
        name = s;
        return this;
    }

    private List<File> filterByTimeStamp()
    {
        List<File> result = new ArrayList<File>();

        for (FileSet fileset : fileSets) {
            final List<String> fileNames = fileset.list();

            final File sourceDir = fileset.getDir();

            if (fileNames.isEmpty()) {
                Apb.getEnv().logInfo("Skipping empty directory: %s\n", sourceDir.getPath());
            }
            else {
                sourceDirs.add(sourceDir);

                for (String f : fileNames) {
                    final File sourceFile = new File(sourceDir, f);
                    final File classFile = FileUtils.changeExtension(new File(targetDir, f), ".class");

                    if (env.forceBuild()) {
                        result.add(sourceFile);
                    }
                    else {
                        final long classLastModified;

                        if ((classLastModified = classFile.lastModified()) == 0 ||
                                sourceFile.lastModified() > classLastModified) {
                            result.add(sourceFile);
                        }
                        else if (trackUnusedDependencies) {
                            logVerbose("Not tracking dependencies because some files will not be compiled\n");
                            trackUnusedDependencies = false;
                        }
                    }
                }
            }
        }

        return result;
    }

    //~ Inner Classes ........................................................................................

    public static class Builder
    {
        @NotNull private final List<FileSet> fileSets;

        /**
         * Private constructor called from factory methods
         * @param sourceDirectory The directory containing source classes to compile.
         */
        Builder(@NotNull String sourceDirectory)
        {
            this(singletonList(FileSet.fromDir(sourceDirectory)));
        }

        Builder(@NotNull List<FileSet> fileSets)
        {
            this.fileSets = fileSets;
        }

        /**
        * Specify the target(output) directory.
        * That is the directory where ".class" files will be placed
        * @param target The output directory
        * @throws IllegalArgumentException if file exists and it is not a directory.
        */
        @NotNull public JavacTask to(@NotNull String target)
        {
            return to(new File(Apb.getEnv().expand(target)));
        }

        /**
        * Specify the target(output) directory.
        * That is the directory where ".class" files will be placed
        * @param target The output directory
         * @throws IllegalArgumentException if file exists and it is not a directory.
         */
        @NotNull public JavacTask to(@NotNull File target)
        {
            if (target.exists()) {
                if (!target.isDirectory()) {
                    throw new IllegalArgumentException(target.getPath() + " is not a directory");
                }
            }
            else if (!target.mkdirs()) {
                throw new IllegalArgumentException("Can not create directory: " + target.getPath());
            }

            // If not includes specified add all java file
            for (FileSet fileSet : fileSets) {
                if (!fileSet.isFile() && fileSet.getIncludes().isEmpty()) {
                    fileSet.including("**/*.java");
                }
            }

            return new JavacTask(fileSets, target);
        }
    }

    static class UnusedLibrariesException
        extends BuildException
    {
        @NotNull private final List<File> libraries;
        @NotNull private final String     moduleName;

        public UnusedLibrariesException(@NotNull String name, @NotNull List<File> unused)
        {
            moduleName = name;
            libraries = unused;
        }

        @Override public String getMessage()
        {
            String msgHeader = "Unused Dependencies";

            if (!moduleName.isEmpty()) {
                msgHeader += " (Module: " + moduleName + ")";
            }

            msgHeader += ": ";
            return appendIndenting(msgHeader, makePath(libraries, "\n"));
        }

        private static final long serialVersionUID = -8622820733815646515L;
    }
}
