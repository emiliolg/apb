
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

import javax.xml.stream.XMLStreamException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import apb.Apb;
import apb.BuildException;
import apb.utils.FileUtils;
import apb.utils.SchemaUtils;

/**
 * This task allow the invocation of the XML Binding Compiler (xjc)
 */
public class XjcTask
    extends Task
{
    //~ Instance fields ......................................................................................

    @NotNull private final List<File> externalBindings;

    private boolean               packageAnnotations;
    @NotNull private final File[] schemas;
    @NotNull private final File   targetDir;
    @NotNull private String       targetPackage;
    private boolean               timestamp;

    //~ Constructors .........................................................................................

    /**
     * Construt the XML Binding Compiler Task
     * @param schemaFiles  The xml schemas to generate the java bindings for, relative the source folder
     * @param target The directory where generated files will be placed
     */
    private XjcTask(@NotNull File[] schemaFiles, @NotNull File target)
    {
        schemas = schemaFiles;
        targetDir = target;
        targetPackage = "";
        externalBindings = new ArrayList<File>();
        packageAnnotations = true;
        timestamp = true;
    }

    //~ Methods ..............................................................................................

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
        catch (IOException e) {
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

    private File targetFile() {
        return new File(targetDir, schemas[0].getName() + ".touch");
    }

    private boolean mustBuild()
    {
        final File target = targetFile();
        final long ts;
        return env.forceBuild() || (ts = target.lastModified()) == 0 ||
               !FileUtils.uptodate(externalBindings, ts) || !FileUtils.uptodate(Arrays.asList(schemas), ts);
    }

    private void run()
    {
        File[] schemas = this.schemas;

        if (env.getBooleanProperty(XJC_SYMLINK_BUG, false) && isAnySymlink(schemas)) {
            try {
                schemas =
                    SchemaUtils.copySchema(schemas, env.fileFromBase("$output/" + schemas[0].getName()));
            }
            catch (XMLStreamException e) {
                env.handle(e);
            }
            catch (IOException e) {
                env.handle(e);
            }
        }

        //noinspection ResultOfMethodCallIgnored
        targetDir.mkdirs();

        //If there is a jaxb xjc jar in ext, use that
        String jar = findJar();

        List<String> args = new ArrayList<String>();
        args.add("-extension");

        if (!packageAnnotations) {
            args.add("-npa");
        }

        env.logInfo("Processing: %s\n", makeString(schemas));

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
        args.add(targetDir.getPath());

        if (!targetPackage.isEmpty()) {
            args.add("-p");
            args.add(targetPackage);
        }

        for (File binding : externalBindings) {
            args.add("-b");
            args.add(binding.getPath());
        }

        for (File schema : schemas) {
            args.add(schema.getPath());
        }

        final File target = targetFile();
        target.delete();

        final ExecTask command =
            jar == null ? CoreTasks.exec(env.getProperty(XJC_CMD, "xjc"), args)
                        : CoreTasks.javaJar(jar, args);
        command.execute();

        if (command.getExitValue() == 0) {
            touchFile(target);
        }
        else {
            env.handle("Xjc task failed");
        }
    }

    private static String makeString(File[] schemas) {
        final StringBuilder sb = new StringBuilder();

        for (File schema : schemas) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(schema.getName());
        }

        return sb.toString();
    }

    private void touchFile(File file)
    {
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

    //~ Inner Classes ........................................................................................

    public static class Builder
    {
        @NotNull private final File[] from;

        /**
         * Private constructor called from factory methods
         * @param schemas The Schema to generate files from.
         */

        Builder(@NotNull File[] schemas)
        {
            from = schemas;

            if (schemas.length == 0) {
                throw new BuildException("You must specify one schema file");
            }

            for (File file : schemas) {
                if (!file.exists()) {
                    throw new BuildException("Non existent schema: " + file);
                }
            }
        }

        /**
         * Specify the target file where sources will be generated
         * @param dirName The directory where to place generated files
         */
        @NotNull public XjcTask to(@NotNull String dirName)
        {
            return to(Apb.getEnv().fileFromBase(dirName));
        }

        /**
         * Specify the target file where sources will be generated
         * @param dir The directory where to place generated files
         */
        @NotNull public XjcTask to(@NotNull File dir)
        {
            if (dir.isFile()) {
                throw new BuildException("Not a directory: '" + dir.getPath() + "'.");
            }

            return new XjcTask(from, dir);
        }
    }
}
