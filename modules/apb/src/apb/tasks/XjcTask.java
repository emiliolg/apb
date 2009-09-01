

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

import apb.Environment;

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

    @NotNull private final List<File> externalBindings;
    private boolean                   packageAnnotations;
    @NotNull private final String     schema;
    @NotNull private final String           targetPackage;

    //~ Constructors .........................................................................................

    /**
     * Construt the XML Binding Compiler Task
     * @param env The apb Environment
     * @param schema  The xml schema to generate the java bindings for, relative the source folder
     * @param targetPackage specifies the target package
     */
    public XjcTask(@NotNull Environment env, @NotNull String schema, @NotNull String targetPackage)
    {
        super(env);
        this.schema = schema;
        externalBindings = new ArrayList<File>();
        packageAnnotations = true;
        this.targetPackage = targetPackage.trim();
    }

    //~ Methods ..............................................................................................

    /**
     * Specify an external binding file
     * @param bindingFile The binding file
     */
    public void addBinding(@NotNull String bindingFile)
    {
        File f = env.fileFromSource(bindingFile);

        if (f.exists()) {
            externalBindings.add(f);
        }
        else {
            env.handle("Non existent binding: " + f);
        }
    }

    /**
     * Wheter to generate  package level annotations (package-info.java)
     */
    public void setPackageAnnotations(boolean b)
    {
        packageAnnotations = b;
    }

    /**
     * Execute the task
     */
    public void execute()
    {
        final File schemaFile = env.fileFromSource(schema);

        if (!schemaFile.exists()) {
            env.handle("Non existent schema: " + schemaFile);
        }

        final File targetDir = env.fileFromGeneratedSource(targetPackage.replace('.', '/'));

        if (mustBuild(targetDir, schemaFile)) {
            execute(targetDir, schemaFile);
        }
    }

    private boolean mustBuild(@NotNull final File targetDir, @NotNull final File schemaFile)
    {
        final long ts;

        return env.forceBuild() || (ts = targetDir.lastModified()) == 0 || schemaFile.lastModified() > ts ||
               FileUtils.uptodate(externalBindings, ts);
    }

    private void execute(@NotNull final File targetDir, @NotNull final File schemaFile)
    {
        targetDir.mkdirs();

        //If there is a jaxb xjc jar in ext, use that
        String jar = findJar();

        final CommandTask command = jar == null ? new ExecTask(env, "xjc") : new JavaTask(env, true, jar);

        command.addArguments("-extension");

        if (!packageAnnotations) {
            command.addArguments("-npa");
        }

        env.logInfo("Processing: %s\n", schemaFile);

        if (jar != null) {
            env.logInfo("Using     : %s\n", jar);
        }

        if (!env.isVerbose()) {
            command.addArguments("-quiet");
        }

        command.addArguments("-d", env.fileFromGeneratedSource("").getPath());

        if (!targetPackage.isEmpty()) {
            command.addArguments("-p", targetPackage);
        }

        for (File binding : externalBindings) {
            command.addArguments("-b", binding.getPath());
        }

        command.addArguments(schemaFile.getPath());

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
}
