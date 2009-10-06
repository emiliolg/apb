

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


package apb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import apb.metadata.CompileInfo;
import apb.metadata.Dependency;
import apb.metadata.JavadocInfo;
import apb.metadata.Library;
import apb.metadata.Module;
import apb.metadata.PackageInfo;
import apb.metadata.PackageType;
import apb.metadata.ProjectElement;
import apb.metadata.ResourcesInfo;
import apb.metadata.TestModule;

import apb.utils.DebugOption;
import apb.utils.FileUtils;
import apb.utils.IdentitySet;

import org.jetbrains.annotations.NotNull;

import static apb.utils.CollectionUtils.addIfNotNull;
//
// User: emilio
// Date: Sep 15, 2008
// Time: 2:03:05 PM

//
public class ModuleHelper
    extends ProjectElementHelper
{
    //~ Instance fields ......................................................................................

    private final List<ModuleHelper>          dependencies;
    @NotNull private final List<ModuleHelper> directDependencies;

    private File                         generatedSource;
    @NotNull private final List<Library> localLibraries;
    private File                         output;
    private File                         packageDir;
    private File                         source;

    //~ Constructors .........................................................................................

    ModuleHelper(Module module, Environment env)
    {
        super(module, env);

        dependencies = new ArrayList<ModuleHelper>();

        directDependencies = new ArrayList<ModuleHelper>();
        localLibraries = new ArrayList<Library>();

        // Add Direct Dependencies & local libraries
        for (Dependency dependency : module.dependencies()) {
            if (dependency.isModule()) {
                directDependencies.add((ModuleHelper) env.getHelper(dependency.asModule()));
            }
            else if (dependency.isLibrary()) {
                localLibraries.add(dependency.asLibrary());
            }
        }
    }

    //~ Methods ..............................................................................................

    public Module getModule()
    {
        return (Module) getElement();
    }

    /**
     * Get current module output directory
     * @return current module output directory
     * @throws IllegalStateException If there is no current module
     */
    @NotNull public File getOutput()
    {
        if (output == null) {
            throw new IllegalStateException("Module:'" + getName() + "' not initialized");
        }

        return output;
    }

    public File getSource()
    {
        return source;
    }

    public File getPackageFile()
    {
        return new File(packageDir, getPackageName() + getPackageInfo().type.getExt());
    }

    public File getSourcePackageFile()
    {
        return new File(packageDir, getPackageName() + SRC_JAR);
    }

    public PackageInfo getPackageInfo()
    {
        return getModule().pkg;
    }

    @NotNull public List<Library> getLocalLibraries()
    {
        return localLibraries;
    }

    public ResourcesInfo getResourcesInfo()
    {
        return getModule().resources;
    }

    @NotNull public Iterable<ModuleHelper> getDirectDependencies()
    {
        return directDependencies;
    }
    
    @NotNull public Iterable<ModuleHelper> getDependencies() {
        return dependencies;
    }


    public List<File> compileClassPath()
    {
        return classPath(false, false, true);
    }

    public boolean isTestModule()
    {
        return false;
    }

    public Collection<File> deepClassPath(boolean useJars, boolean addModuleOutput)
    {
        Set<File> result = new HashSet<File>();

        if (addModuleOutput) {
            result.add(useJars && hasPackage() ? getPackageFile() : getOutput());
        }

        // First classpath for module dependencies
        for (ModuleHelper dependency : getDirectDependencies()) {
            result.addAll(dependency.deepClassPath(useJars, true));
        }

        // The classpath for libraries
        for (Library library : getLocalLibraries()) {
            addIfNotNull(result, library.getArtifact(env, PackageType.JAR));
        }

        return result;
    }

    public File getGeneratedSource()
    {
        return generatedSource;
    }

    public List<File> getSourceDirs()
    {
        List<File> sourceDirs = new ArrayList<File>();

        sourceDirs.add(getSource());
        sourceDirs.add(getGeneratedSource());
        return sourceDirs;
    }

    public JavadocInfo getJavadocInfo()
    {
        return getModule().javadoc;
    }

    public CompileInfo getCompileInfo()
    {
        return getModule().compiler;
    }

    public String getPackageName()
    {
        return trimDashes(getModule().pkg.name);
    }

    public Set<ModuleHelper> listAllModules()
    {
        Set<ModuleHelper> result = new LinkedHashSet<ModuleHelper>();

        for (ModuleHelper mod : dependencies) {
            mod.addTo(result);
        }

        addTo(result);

        return result;
    }

    public List<File> runtimePath()
    {
        return classPath(false, true, false);
    }

    public boolean hasPackage()
    {
        return getPackageInfo().type != PackageType.NONE;
    }

    public List<String> manifestClassPath()
    {
        List<String> files = new ArrayList<String>();

        // Make the files relative to the jarfile
        File jarFileDir = getPackageFile().getParentFile();

        for (File file : classPath(true, false, false)) {
            files.add(FileUtils.makeRelative(jarFileDir, file).getPath());
        }

        files.addAll(getPackageInfo().extraClassPathEntries());
        return files;
    }

    protected void initDependencyGraph()
    {
        // Topological Sort elements
        tsort(dependencies, new IdentitySet<ModuleHelper>());

        if (env.mustShow(DebugOption.DEPENDENCIES)) {
            env.logVerbose("Dependencies for: %s = %s\n", getName(), dependencies.toString());
        }
    }

    protected void doBuild(String commandName)
    {
        Command command = findCommand(commandName);

        if (command == null) {
            throw new BuildException("Invalid command: " + commandName);
        }

        if (command.isRecursive() && !env.isNonRecursive()) {
            for (ModuleHelper dep : dependencies) {
                dep.execute(commandName);
            }
        }
        else {
            for (ModuleHelper dep : dependencies) {
                dep.activate();
            }

            for (Command cmd : command.getDirectDependencies()) {
                build(cmd.getQName());
            }
        }

        execute(commandName);
    }

    void activate(@NotNull ProjectElement activatedModule)
    {
        super.activate(activatedModule);

        Module module = getModule();
        output = env.fileFromBase(module.output);
        source = env.fileFromBase(module.source);
        generatedSource = env.fileFromBase(module.generatedSource);

        packageDir = FileUtils.normalizeFile(env.fileFromBase(module.pkg.dir));

        for (TestModule testModule : module.tests()) {
            final TestModuleHelper helper = (TestModuleHelper) env.getHelper(testModule);
            helper.setModuleToTest(this);
        }
    }

    private static String trimDashes(String s)
    {
        int l = s.length();
        return s.substring(s.charAt(0) == '-' ? 1 : 0, s.charAt(l - 1) == '-' ? l - 1 : l);
    }

    protected List<File> classPath(boolean useJars, boolean addModuleOutput, boolean compile)
    {
        List<File> result = new ArrayList<File>();

        // Add output dir
        if (addModuleOutput) {
            result.add(getOutput());
        }

        for (Dependency dependency : getModule().dependencies()) {
            if (dependency.mustInclude(compile)) {
                if (dependency.isModule()) {
                    ModuleHelper hlp = (ModuleHelper) env.getHelper(dependency.asModule());
                    result.add(useJars && hlp.hasPackage() ? hlp.getPackageFile() : hlp.getOutput());
                }
                else if (dependency.isLibrary()) {
                    addIfNotNull(result, dependency.asLibrary().getArtifact(env, PackageType.JAR));
                }
            }
        }

        return result;
    }

    private void addTo(Set<ModuleHelper> result)
    {
        result.add(this);

        for (TestModule testModule : getModule().tests()) {
            result.add((ModuleHelper) env.getHelper(testModule));
        }
    }

    /**
     * Topological sort dependent modules using a Depth First Search
     * @param elements All descendant elements
     * @param visited  Already visited elements
     */
    private void tsort(List<ModuleHelper> elements, IdentitySet<ModuleHelper> visited)
    {
        visited.add(this);

        for (ModuleHelper dependency : directDependencies) {
            if (!visited.contains(dependency)) {
                dependency.tsort(elements, visited);
                elements.add(dependency);
            }
        }
    }

    //~ Static fields/initializers ...........................................................................

    public static final String SRC_JAR = "-src.jar";
}
