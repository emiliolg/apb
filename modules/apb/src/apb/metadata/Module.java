
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

package apb.metadata;

import apb.Environment;
import apb.ModuleHelper;
import apb.tasks.CopyTask;
import apb.tasks.JarTask;
import apb.tasks.JavacTask;
import apb.tasks.JavadocTask;
import apb.tasks.RemoveTask;

import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.List;

/**
 * This class defines a Module for the building system
 * Every Module definition must inherit (directly or indirectly) from this class
 * This class have handy defaults for several attributes that assume the following layout:
 * <pre>
 *
 *  basedir/
 *         +--- project-definitions--+
 *                                   +-- Module1.java
 *                                   +-- Module2.java
 *                                   .
 *                                   .
 *                                   +-- ModuleN.java
 *
 *        +--- module1/
 *                    +-- src    --   <-- Sources here
 *                    +-- gsrc   --   <-- Generated Sources here
 *                    .
 *                    +-- output +-- classes --   <-- Compiled classes and resources here
 *                               +-- javadoc --   <-- javadoc here
 *        +--- module2/
 *        .
 *        .
 *        +--- moduleN/
 *        .
 *        +--- lib/
 *                +-- module1.jar
 *                +-- module2.jar
 *                .
 *                .
 *                +-- moduleN.jar
 * </pre>
 *
 */
public class Module
    extends ProjectElement
    implements Dependency
{
    //~ Instance fields ......................................................................................

    /**
     * Info for the compiler
     */
    @BuildProperty public final CompileInfo compiler = new CompileInfo();

    /**
     * The directory where the generated source files for this module are placed
     */
    @BuildProperty public String generatedSource = "$module-dir/gsrc";

    /**
     * All the info for Javadoc
     */
    @BuildProperty public JavadocInfo javadoc = new JavadocInfo();

    /**
     * The root directory of the module
     */
    @BuildProperty(order = 0)
    public String moduleDir = "$module";

    /**
     * The directory for the output classes
     */
    @BuildProperty public String output = "$module-dir/output/classes";

    /**
     * All the info for the package (Jar,War, etc)
     */
    @BuildProperty public final PackageInfo pkg = new PackageInfo();

    /**
     * All the info for processing resources
     */
    @BuildProperty public ResourcesInfo resources = new ResourcesInfo();

    /**
     * The directory where the source files for this module are placed
     */
    @BuildProperty public String source = "$module-dir/src";

    /**
     * The list of modules & libraries this module depends from
     */
    private final List<Dependency> dependencies = new ArrayList<Dependency>();

    /*
     * The list of test modules
     */
    private final List<TestModule> tests = new ArrayList<TestModule>();

    //~ Methods ..............................................................................................

    public List<Dependency> dependencies()
    {
        return dependencies;
    }

    public List<TestModule> tests()
    {
        return tests;
    }

    public void clean(Environment env)
    {
        ModuleHelper helper = env.getModuleHelper();
        RemoveTask.remove(env, helper.getOutput());
        RemoveTask.remove(env, helper.getPackageFile());
        RemoveTask.remove(env, helper.getGeneratedSource());
    }

    public void resources(Environment env)
    {
        CopyTask.copyResources(env);
    }

    public void compile(Environment env)
    {
        JavacTask.execute(env);
    }

    public void compileTests(Environment env)
    {
        env.forward("compile", tests);
    }

    public void runTests(Environment env)
    {
        env.forward("run", tests);
    }

    public void packageit(Environment env)
    {
        JarTask.execute(env);
    }

    @BuildTarget public void javadoc(Environment env)
    {
        JavadocTask.execute(env);
    }

    /**
     * Method used to set dependencies when defining a module
     * @param dependencyList The list of dependencies to be set
     */
    protected final void dependencies(Dependency... dependencyList)
    {
        dependencies.addAll(asList(dependencyList));
    }

    /**
     * Method used to associate tests when defining a module
     * @param testList The list of tests to be set
     */
    protected final void tests(TestModule... testList)
    {
        for (TestModule test : testList) {
            test.dependencies(this);
            tests.add(test);
        }
    }

    protected LocalLibrary localLibrary(String path)
    {
        return new LocalLibrary(path, false);
    }

    protected LocalLibrary optionalLibrary(String path)
    {
        return new LocalLibrary(path, true);
    }

    protected Dependency library(String g, String id)
    {
        return library(g, id, "");
    }

    protected Dependency library(String g, String id, String vers)
    {
        return Library.create(g, id, vers);
    }
}
