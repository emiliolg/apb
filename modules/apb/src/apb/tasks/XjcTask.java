

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
import java.util.List;

import apb.Apb;
import apb.BuildException;

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This task allow the invocation of the XML Binding Compiler (xjc)
 */
public class XjcTask
    extends Task
{
    //~ Instance fields ......................................................................................

    private boolean             packageAnnotations;
    private boolean             timestamp;
    @NotNull private final File schema;
    @NotNull private final File targetDir;

    @NotNull private final List<File> externalBindings;
    @NotNull private String           targetPackage;

    //~ Constructors .........................................................................................

    /**
     * Construt the XML Binding Compiler Task
     * @param schemaFile  The xml schema to generate the java bindings for, relative the source folder
     * @param target The directory where generated files will be placed
     */
    private XjcTask(@NotNull File schemaFile, @NotNull File target)
    {
        super();
        schema = schemaFile;
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
    public void execute()
    {
        if (mustBuild()) {
            run();
        }
    }

    private boolean mustBuild()
    {
        final long ts;
        File       dir = new File(targetDir, targetPackage.replace('.', File.separatorChar));

        return env.forceBuild() || (ts = dir.lastModified()) == 0 || schema.lastModified() > ts ||
               !FileUtils.uptodate(externalBindings, ts);
    }

    private void run()
    {
        targetDir.mkdirs();

        //If there is a jaxb xjc jar in ext, use that
        String jar = findJar();

        List<String> args = new ArrayList<String>();
        args.add("-extension");

        if (!packageAnnotations) {
            args.add("-npa");
        }

        env.logInfo("Processing: %s\n", schema);

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

        args.add(schema.getPath());

        final ExecTask command = jar == null ? CoreTasks.exec("xjc", args) : CoreTasks.javaJar(jar, args);
        command.execute();
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

    //~ Inner Classes ........................................................................................

    public static class Builder
    {
        @NotNull private final File from;

        /**
         * Private constructor called from factory methods
         * @param schema The Schema to generate files from.
         */

        Builder(@NotNull File schema)
        {
            from = schema;

            if (!schema.exists()) {
                throw new BuildException("Non existent schema: " + schema);
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
