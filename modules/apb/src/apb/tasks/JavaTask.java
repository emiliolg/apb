

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

import apb.Environment;
import apb.ModuleHelper;

import apb.metadata.Dependency;
import apb.metadata.DependencyList;
import apb.metadata.PackageType;

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;

import static apb.utils.CollectionUtils.addIfNotNull;
//
// User: emilio
// Date: Oct 22, 2008
// Time: 10:07:04 AM

//
public class JavaTask
    extends ExecTask
{
    //~ Instance fields ......................................................................................

    private final boolean executeJar;

    /**
     * Max memory in megabytes used by the Java command
     */
    private int memory = 256;

    @NotNull private final List<File>          classPath;
    @NotNull private final List<String>        javaArgs;
    @NotNull private final Map<String, String> properties;
    private final String                       jarOrClass;

    //~ Constructors .........................................................................................

    JavaTask(boolean executeJar, @NotNull String className, List<String> arguments)
    {
        super(FileUtils.findJavaExecutable("java"), arguments);
        this.executeJar = executeJar;
        jarOrClass = className;
        properties = new HashMap<String, String>();
        javaArgs = new ArrayList<String>();
        memory = 256;
        classPath = new ArrayList<File>();
    }

    //~ Methods ..............................................................................................

    public JavaTask withClassPath(@NotNull String... fileNames)
    {
        for (String fileName : fileNames) {
            classPath.add(env.fileFromBase(fileName));
        }

        return this;
    }

    public JavaTask withClassPath(@NotNull Dependency... dependencies)
    {
        classPath.addAll(JavaTask.fileList(env, DependencyList.create(dependencies)));
        return this;
    }

    public JavaTask withModuleClassPath(@NotNull ModuleHelper helper)
    {
        classPath.addAll(helper.runtimePath());
        return this;
    }

    public void execute()
    {
        List<String> argList = new ArrayList<String>();
        argList.add("-Xmx" + memory + "m");

        // Pass properties
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            argList.add("-D" + entry.getKey() + "=" + entry.getValue());
        }

        argList.addAll(javaArgs);

        // add classpath
        if (!classPath.isEmpty()) {
            argList.add("-classpath");
            argList.add(FileUtils.makePath(classPath));
        }

        if (executeJar) {
            argList.add("-jar");
        }

        argList.add(jarOrClass);
        insertArguments(argList);
        super.execute();
    }

    /**
     * Add an argument to java executable
     * @param arg the argument to be added
     */
    public void addJavaArg(String arg)
    {
        javaArgs.add(arg);
    }

    /**
     * Set all properties to the ones specified
     * @param ps The set of properties
     */
    public JavaTask withProperties(@NotNull Map<String, String> ps)
    {
        properties.clear();
        properties.putAll(ps);
        return this;
    }

    /**
     * Set the wnvironment to the one specified
     * @param e The environment to be set
     */
    @Override public JavaTask withEnvironment(@NotNull Map<String, String> e)
    {
        return (JavaTask) super.withEnvironment(e);
    }

    @Override public JavaTask redirectErrorStream(boolean b)
    {
        return (JavaTask) super.redirectErrorStream(b);
    }

    /**
     * Specify the max memory to be used
     * @param mb The maximum number of megabytes to use
     */
    public JavaTask maxMemory(int mb)
    {
        memory = mb;
        return this;
    }

    @Override public JavaTask outputTo(@NotNull List<String> list)
    {
        return (JavaTask) super.outputTo(list);
    }

    @Override public JavaTask onDir(@NotNull String directory)
    {
        return (JavaTask) super.onDir(directory);
    }

    private static List<File> fileList(Environment env, DependencyList dependencies)
    {
        List<File> result = new ArrayList<File>();

        for (Dependency dependency : dependencies) {
            if (dependency.isLibrary()) {
                addIfNotNull(result, dependency.asLibrary().getArtifact(env, PackageType.JAR));
            }
            else if (dependency.isModule()) {
                result.addAll(dependency.asModule().getHelper().deepClassPath(false, true));
            }
        }

        return result;
    }
}
