

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import apb.BuildException;

import apb.utils.FileUtils;
import apb.utils.SchemaUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static apb.utils.FileUtils.validateDirectory;

/**
 * This task allow the invocation of the XML Binding Compiler (xjc)
 */
public class XjcTask
    extends Task
{
    //~ Instance fields ......................................................................................

    private boolean               packageAnnotations;
    private boolean               timestamp;
    @NotNull private final File[] schemas;

    @NotNull private final List<File> externalBindings;
    @NotNull private String           targetDir;
    @NotNull private String           targetPackage;

    //~ Constructors .........................................................................................

    /**
     * Construt the XML Binding Compiler Task
     * @param schemaFiles  The xml schemas to generate the java bindings for, relative the source folder
     */
    XjcTask(@NotNull File[] schemaFiles)
    {
        schemas = schemaFiles;
        targetDir = "$generated-source";
        targetPackage = "";
        externalBindings = new ArrayList<File>();
        packageAnnotations = true;
        timestamp = true;
    }

    //~ Methods ..............................................................................................

    /**
     * @param target The directory where generated files will be placed
     */
    public final XjcTask to(@NotNull String target)
    {
        targetDir = target;
        return this;
    }

    /**
     * Specify an external binding file
     */
    public XjcTask withBinding(@NotNull String bindingFileName)
    {
        return withBinding(env.fileFromBase(bindingFileName));
    }

    /**
     * Specify the name of the package to for generated classes
     * @param packageName the name of the stylesheet to use
     */
    @NotNull public XjcTask usingPackage(@NotNull String packageName)
    {
        targetPackage = packageName;
        return this;
    }

    /**
     * Specify an external binding file
     * @param bindingFile The binding file
     */
    public XjcTask withBinding(@NotNull File bindingFile)
    {
        if (!bindingFile.exists()) {
            throw new BuildException("Non existent binding: " + bindingFile);
        }

        externalBindings.add(bindingFile);
        return this;
    }

    /**
     * Wheter to generate  package level annotations (package-info.java)
     * Default to true
     */
    public XjcTask usePackageAnnotations(boolean b)
    {
        packageAnnotations = b;
        return this;
    }

    /**
     *  Wether to generate a file header with timestamp info or not
     */
    public XjcTask useTimestamp(boolean b)
    {
        timestamp = b;
        return this;
    }

    /**
     * Execute the task
     */
    @Override public void execute()
    {
        if (mustBuild()) {
            run();
        }
    }

    private static boolean isSymbolicLink(File file)
    {
        try {
            return !file.getAbsolutePath().equals(file.getCanonicalPath());
        }
        catch (IOException ignore) {
            return true;
        }
    }

    private static boolean isAnySymlink(File[] files)
    {
        for (File file : files) {
            if (isSymbolicLink(file)) {
                return true;
            }
        }

        return false;
    }

    private static String makeString(File[] schemas)
    {
        final StringBuilder sb = new StringBuilder();

        for (File schema : schemas) {
            if (sb.length() > 0) {
                sb.append(", ");
            }

            sb.append(schema.getName());
        }

        return sb.toString();
    }

    @NotNull private File getTargetDir()
    {
        return env.fileFromBase(targetDir);
    }

    private File touchFile()
    {
        return new File(getTargetDir(), schemas[0].getName() + ".touch");
    }

    private boolean mustBuild()
    {
        if (env.forceBuild()) {
            return true;
        }

        final long ts = touchFile().lastModified();
        return ts == 0 || !FileUtils.uptodate(externalBindings, ts) ||
               !FileUtils.uptodate(Arrays.asList(schemas), ts);
    }

    private void run()
    {
        File[] schemas = this.schemas;

        final File[] bindings = getBindings();

        env.logInfo("Processing: %s\n", makeString(schemas));

        if (env.getBooleanProperty(XJC_SYMLINK_BUG, false) && (isAnySymlink(schemas) || isAnySymlink(bindings))) {
            try {
                SchemaUtils.copySchema(schemas, bindings,
                                       env.fileFromBase("$output/" + schemas[0].getName()));
            }
            catch (IOException e) {
                env.handle(e);
            }
            env.logInfo("xjcSymlinkBug is true");
        }

        final File target = getTargetDir();

        validateDirectory(target);

        //If there is a jaxb xjc jar in ext, use that
        String jar = findJar();

        List<String> args = new ArrayList<String>();
        args.add("-extension");

        if (!packageAnnotations) {
            args.add("-npa");
        }

        if (jar != null) {
            env.logInfo("Using     : %s\n", jar);
        }

        if (!env.isVerbose()) {
            args.add("-quiet");
        }

        if (!timestamp) {
            args.add("-no-header");
        }

        args.add("-d");
        args.add(target.getPath());

        if (!targetPackage.isEmpty()) {
            args.add("-p");
            args.add(targetPackage);
        }

        for (File binding : bindings) {
            args.add("-b");
            args.add(binding.getPath());
        }

        for (File schema : schemas) {
            args.add(schema.getPath());
        }

        final File touchFile = touchFile();

        //noinspection ResultOfMethodCallIgnored
        touchFile.delete();

        final ExecTask command =
            jar == null ? CoreTasks.exec(env.getProperty(XJC_CMD, "xjc"), args)
                        : CoreTasks.javaJar(jar, args);
        command.execute();

        if (command.getExitValue() == 0) {
            touch(touchFile);
        }
        else {
            env.handle("Xjc task failed");
        }
    }

    private File[] getBindings()
    {
        final List<File> bindingsList = externalBindings;
        final File[]     bindings = new File[bindingsList.size()];
        bindingsList.toArray(bindings);
        return bindings;
    }

    private void touch(File file)
    {
        //noinspection ResultOfMethodCallIgnored
        file.delete();

        try {
            new FileOutputStream(file).close();
        }
        catch (IOException e) {
            env.handle(e);
        }
    }

    @Nullable private String findJar()
    {
        for (File file : env.getExtClassPath()) {
            if (file.getName().equals(JAXB_XJC_JAR_FILENAME)) {
                return FileUtils.normalizePath(file.getAbsoluteFile());
            }
        }

        return null;
    }

    //~ Static fields/initializers ...........................................................................

    private static final String JAXB_XJC_JAR_FILENAME = "jaxb-xjc.jar";

    public static final String XJC_CMD = "xjc-cmd";
    public static final String XJC_SYMLINK_BUG = "xjc-symlink-bug";
}
