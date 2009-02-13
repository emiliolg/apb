

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

import apb.Environment;
import apb.ModuleHelper;
import apb.compiler.DiagnosticReporter;
import apb.compiler.JavaC;
import apb.metadata.CompileInfo;
import apb.metadata.LocalLibrary;
import apb.utils.DirectoryScanner;
import apb.utils.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//
// User: emilio
// Date: Oct 20, 2008
// Time: 4:15:59 PM

//
public class JavacTask
    extends Task
{
    //~ Instance fields ......................................................................................

    private Map<String, String>       annnotationOptions;
    @NotNull private final List<File> classPath;
    private boolean                   debug;
    private boolean                   deprecated;
    private List<String>              excludes;
    private boolean                   failOnWarning;
    private List<String>              includes;
    private boolean                   lint;
    private String                    lintOptions;
    private DiagnosticReporter        reporter;
    private String                    source;
    @NotNull private final List<File> sourceDirs;
    private String                    target;
    @NotNull private final File       targetDir;
    private boolean                   warn;

    //~ Constructors .........................................................................................

    public JavacTask(@NotNull Environment env, @NotNull List<File> sourceDirs, @NotNull File targetDir)
    {
        super(env);
        this.targetDir = targetDir;
        this.sourceDirs = sourceDirs;
        classPath = new ArrayList<File>();
        includes = Collections.singletonList("**/*.java");
        excludes = Collections.emptyList();
        reporter = new DiagnosticReporter(env);
    }

    //~ Methods ..............................................................................................

    public static void execute(Environment env)
    {
        ModuleHelper     module = env.getModuleHelper();
        CompileInfo      info = module.getCompileInfo();
        final List<File> sourceDirs = module.getSourceDirs();

        if (sourceDirs.isEmpty()) {
            env.handle(module.getSource() + " does not exists!");
        }

        final JavacTask javac = new JavacTask(env, sourceDirs, module.getOutput());
        javac.classPathAddAll(module.classPath(false, false));

        javac.addExtraLibraries(info.extraLibraries());

        javac.debug = info.debug;
        javac.deprecated = info.deprecated;
        javac.lint = info.lint;
        javac.source = info.source;
        javac.target = info.target;
        javac.warn = info.warn;
        javac.failOnWarning = info.failOnWarning;
        javac.annnotationOptions = info.annotationOptions();
        javac.lintOptions = info.lintOptions;
        javac.includes = info.includes();
        javac.excludes = info.excludes();

        if (info.defaultErrorFormatter) {
            javac.reporter = null;
        }
        else {
            javac.reporter.setExcludes(info.warnExcludes());
        }

        javac.execute();
    }

    public void execute()
    {
        validateDirectories(env, sourceDirs, targetDir);

        JavaC jc = new JavaC();

        List<File> files = findFiles();

        if (files.isEmpty()) {
            env.logVerbose("Nothing to compile\n");
        }
        else {
            env.logInfo("Compiling %3d file%s\n", files.size(), (files.size() > 1) ? "s" : "");

            if (env.isVerbose()) {
                for (File file : FileUtils.removePrefix(sourceDirs, files)) {
                    env.logVerbose("         %s\n", file);
                }

                env.logVerbose("ClassPath: \n");

                for (File file : classPath) {
                    env.logVerbose("         %s\n", file);
                }

                env.logVerbose("Source:\n");

                for (File dir : sourceDirs) {
                    env.logVerbose("         %s\n", dir);
                }

                env.logVerbose("Target directory: %s\n", targetDir);
            }

            classPath.add(0, targetDir);

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

            if (reporter == null && failOnWarning) {
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

            final boolean status =
                jc.compile(files, sourceDirs, targetDir, FileUtils.makePath(classPath), options, reporter);

            if (reporter != null) {
                reporter.flush();
                reporter.reportError(failOnWarning);
            }
            if (!status) {
                env.handle("Compilation failed");
            }
        }
    }

    private static void validateDirectories(Environment env, List<File> sourceDirs, File targetDir)
    {
        for (Iterator<File> it = sourceDirs.iterator(); it.hasNext();) {
            File sourceDir = it.next();

            if (sourceDir.exists()) {
                validateIsDirectory(env, sourceDir);
            }
            else {
                it.remove();
            }
        }

        if (targetDir.exists()) {
            validateIsDirectory(env, targetDir);
        }
        else if (!targetDir.mkdirs()) {
            env.handle("Can not create directory: " + targetDir);
        }
    }

    private static void validateIsDirectory(Environment env, File sourceDir)
    {
        if (!sourceDir.isDirectory()) {
            env.handle("Not a directory: " + sourceDir);
        }
    }

    private void addExtraLibraries(List<LocalLibrary> libraries)
    {
        for (LocalLibrary library : libraries) {
            classPath.add(library.getFile(env));
        }
    }

    private List<File> findFiles()
    {
        List<File> result = new ArrayList<File>();

        try {
            for (File dir : sourceDirs) {
                DirectoryScanner scanner = new DirectoryScanner(dir, includes, excludes);
                scanner.scan();
                filterByTimeStamp(result, dir, scanner.getIncludedFiles());
            }
        }
        catch (IOException e) {
            env.handle(e);
        }

        return result;
    }

    private void filterByTimeStamp(List<File> result, File dir, List<String> files)
    {
        for (String file : files) {
            File sourceFile = new File(dir, file);

            if (env.forceBuild()) {
                result.add(sourceFile);
            }
            else {
                File classFile = new File(targetDir, FileUtils.changeExtension(file, ".class"));

                if (!classFile.exists() || (classFile.lastModified() < sourceFile.lastModified())) {
                    result.add(sourceFile);
                }
            }
        }
    }

    private void classPathAddAll(List<File> files)
    {
        classPath.addAll(files);
    }
}
