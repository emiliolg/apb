

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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

import apb.Environment;

import apb.tasks.JavacTask;

import apb.utils.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static apb.utils.ColorUtils.*;

/**
 * A Diagnostic Reporter for the Java Compiler
 * This class implements the DiagnosticListener interface for the JavaCompiler
 * It allows to specify files for which warnings will be excluded.
 */
public class DiagnosticReporter
    implements DiagnosticListener<JavaFileObject>
{
    //~ Instance fields ......................................................................................

    @NotNull private final List<Diagnostic<? extends JavaFileObject>> ds;
    @NotNull private final Environment                                env;
    @NotNull private List<String>                                     excludes;
    private JavacTask                                                 javacTask;
    @Nullable private String                                          lastFile;
    private int                                                       warns, errors;

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
        excludes = Collections.emptyList();
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
        final JavaFileObject source = diagnostic == null ? null : diagnostic.getSource();

        if (source != null && !isExcluded(source)) {
            count(diagnostic);

            String fileName = source.toString();

            if (!fileName.equals(lastFile)) {
                doReport();
                lastFile = fileName;
            }

            ds.add(diagnostic);
        }
    }

    /**
     * Set the patterns that defines which files to exclude warnings from
     * @param patterns The patterns
     */
    public void setExcludes(@NotNull List<String> patterns)
    {
        excludes = StringUtils.normalizePaths(patterns);
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
     * @param fileObject The file to check
     * @return true if the file must be excluded, false otherwise
     */
    private boolean isExcluded(@NotNull JavaFileObject fileObject)
    {
        if (!excludes.isEmpty()) {
            String name = javacTask.removeSourceDir(fileObject.toString());

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
}
