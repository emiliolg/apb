

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


package apb.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.tools.Diagnostic;
import static javax.tools.Diagnostic.Kind.ERROR;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

import apb.Environment;
import apb.tasks.JavacTask;
import static apb.utils.ColorUtils.*;
import apb.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Diagnostic Reporter for the Java Compiler
 * This class implements the DiagnosticListener interface for the JavaCompiler
 * It allows to specify files for which warnings will be excluded.
 */
public class DiagnosticReporter
    implements DiagnosticListener<JavaFileObject>
{
    //~ Instance fields ......................................................................................

    @NotNull private final Environment env;
    private int                        warns, errors;
    private final JavacTask            javacTask;
    @NotNull private final List<File>  directories;

    @NotNull private final List<Diagnostic<? extends JavaFileObject>> ds;
    @NotNull private List<String>                                     excludes;
    @Nullable private String                                          lastFile;

    //~ Constructors .........................................................................................

    /**
     * Cosntruct a DiagnosticReporter
     * @param javacTask The original JavaTask
     */
    public DiagnosticReporter(JavacTask javacTask)
    {
        this.javacTask = javacTask;
        env = javacTask.getEnv();
        lastFile = null;
        ds = new LinkedList<Diagnostic<? extends JavaFileObject>>();
        excludes = new ArrayList<String>();
        directories = new ArrayList<File>();
        errors = warns = 0;
    }

    //~ Methods ..............................................................................................

    /**
     * This is the implementation of the method that is invoked
     * when a problem is found.
     * Messages are grouped by file, so this method just add them to the 'ds' list
     * and then they are output by the  {@link #doReport } method
     *
     * @param diagnostic  a diagnostic representing the problem that was found
     */
    public void report(@Nullable Diagnostic<? extends JavaFileObject> diagnostic)
    {
        if (diagnostic != null) {
            final String fileName =
                diagnostic.getSource() == null ? GLOBAL_ERRORS : diagnostic.getSource().toString();

            if (diagnostic.getKind() == ERROR || !isExcluded(fileName)) {
                count(diagnostic);

                if (!fileName.equals(lastFile)) {
                    doReport();
                    lastFile = fileName;
                }

                ds.add(diagnostic);
            }
        }
    }

    /**
     * Add a pattern to the list to be excluded from warnings
     * @param pattern The pattern to be added
     */
    public void addExcludePattern(@NotNull String pattern)
    {
        excludes.add(StringUtils.normalizePath(pattern));
    }

    /**
     * Add a whole directory to the list to be excluded from warnings
     * @param directory The directory to be added
     */
    public void addExcludeDirectory(@NotNull File directory)
    {
        directories.add(directory);
    }

    /**
     * Check if compilation must failed.
     * Report the count of errors and warnings if so.
     */
    public void reportSumary()
    {
        // Ensure the last messages are output
        doReport();

        // Report failure with summary information
        if (errors > 0 || javacTask.failOnWarning() && warns > 0) {
            StringBuilder msg = new StringBuilder();
            msg.append("Compilation failed: ");

            if (errors != 0) {
                msg.append(colorize(RED, errors == 1 ? "1 error" : errors + " errors"));

                if (warns > 0) {
                    msg.append(" and ");
                }
            }

            if (warns > 0) {
                msg.append(colorize(YELLOW, warns == 1 ? "1 warning" : warns + " warnings"));
            }

            msg.append(".");

            env.handle(msg.toString());
        }
    }

    /**
     * Count the number of error & warnings
     * @param diagnostic  a diagnostic representing the problem that was found
     */
    private void count(@NotNull Diagnostic<? extends JavaFileObject> diagnostic)
    {
        switch (diagnostic.getKind()) {
        case ERROR:
            errors++;
            break;
        case MANDATORY_WARNING:
        case WARNING:
            warns++;
            break;
        }
    }

    /**
     * Output the list of messages for a given file.
     */
    private void doReport()
    {
        if (lastFile != null) {
            env.logSevere("%s:\n", lastFile);

            for (Diagnostic d : ds) {
                env.logSevere("%s\n", new DiagnosticFormatter(d).format());
            }

            ds.clear();
            javacTask.markAsFail(lastFile);
        }
    }

    /**
     * Verify if this fileObject must be excluded from the output
     * @param fileName
     * @return true if the file must be excluded, false otherwise
     */
    private boolean isExcluded(final String fileName)
    {
        if (fileName.equals(GLOBAL_ERRORS)) {
            return true;
        }

        if (!directories.isEmpty()) {
            for (File directory : directories) {
                if (fileName.startsWith(directory.getPath() + File.separator)) {
                    return true;
                }
            }
        }

        if (!excludes.isEmpty()) {
            String name = javacTask.removeSourceDir(fileName);

            if (name != null) {
                for (String exclude : excludes) {
                    if (StringUtils.matchPath(exclude, name, true)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    //~ Static fields/initializers ...........................................................................

    private static final String GLOBAL_ERRORS = "<CONFIGURATION>";
}
