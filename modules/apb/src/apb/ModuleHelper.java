

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
import apb.metadata.ResourcesInfo;
import apb.metadata.TestModule;

import apb.tasks.RemoveTask;

import apb.utils.CollectionUtils;
import apb.utils.DebugOption;
import apb.utils.FileUtils;
import apb.utils.IdentitySet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable private File generatedSource;
    @Nullable private File output;
    @Nullable private File packageFile;
    @Nullable private File source;
    @Nullable private File sourcePackageFile;

    @Nullable private Iterable<ModuleHelper>     dependencies;
    @Nullable private Iterable<ModuleHelper>     directDependencies;
    @Nullable private Iterable<Library>          localLibraries;
    @Nullable private Iterable<TestModuleHelper> testModules;

    //~ Constructors .........................................................................................

    ModuleHelper(ProjectBuilder pb, Module module)
    {
        super(pb, module);
        putProperty(MODULE_PROP_KEY, getName());
        putProperty(MODULE_PROP_KEY + ID_SUFFIX, getId());
        putProperty(MODULE_PROP_KEY + DIR_SUFFIX, module.getDir());
    }

    //~ Methods ..............................................................................................

    public Module getModule()
    {
        return (Module) getElement();
    }

    /**
     * Get current module output directory
     * @return current module output directory
     */
    @NotNull public File getOutput()
    {
        if (output == null) {
            output = fileFromBase(getModule().output);
        }

        return output;
    }

    /**
     * Get current module source directory
     * @return current module source directory
     */
    @NotNull public File getSource()
    {
        if (source == null) {
            source = fileFromBase(getModule().source);
        }

        return source;
    }

    /**
     * Get current module generated sources directory
     * @return current module generated sources directory
     */
    @NotNull public File getGeneratedSource()
    {
        if (generatedSource == null) {
            generatedSource = fileFromBase(getModule().generatedSource);
        }

        return generatedSource;
    }

    /**
     * Get current module package (.jar, .war etc) file
     * @return current module pacjage file
     */
    @NotNull public File getPackageFile()
    {
        if (packageFile == null) {
            if (!hasPackage()) {
                throw new IllegalArgumentException("Module: '" + getName() + "' does not have a package.");
            }

            File dir = FileUtils.normalizeFile(fileFromBase(getModule().pkg.dir));
            packageFile = new File(dir, getPackageName() + getPackageType().getExt());
        }

        return packageFile;
    }

    /**
     * Get current module sources package (xxxx-src.jar) file
     * @return current module sources package file
     */
    @NotNull public File getSourcePackageFile()
    {
        if (sourcePackageFile == null) {
            File dir = FileUtils.normalizeFile(fileFromBase(getModule().pkg.dir));
            sourcePackageFile = new File(dir, getPackageName() + SRC_JAR);
        }

        return sourcePackageFile;
    }

    @NotNull public PackageInfo getPackageInfo()
    {
        return getModule().pkg;
    }

    public boolean hasPackage()
    {
        return getPackageType() != PackageType.NONE;
    }

    @NotNull public PackageType getPackageType()
    {
        return getPackageInfo().type;
    }

    @NotNull public ResourcesInfo getResourcesInfo()
    {
        return getModule().resources;
    }

    @NotNull public Iterable<ModuleHelper> getDirectDependencies()
    {
        if (directDependencies == null) {
            ArrayList<ModuleHelper> list = new ArrayList<ModuleHelper>();

            for (Dependency dependency : getModule().dependencies()) {
                if (dependency.isModule()) {
                    list.add(dependency.asModule().getHelper());
                }
            }

            directDependencies = list;
        }

        return directDependencies;
    }

    @NotNull public Iterable<Library> getLocalLibraries()
    {
        if (localLibraries == null) {
            List<Library> list = new ArrayList<Library>();

            for (Dependency dependency : getModule().dependencies()) {
                if (dependency.isLibrary()) {
                    list.add(dependency.asLibrary());
                }
            }

            localLibraries = list;
        }

        return localLibraries;
    }

    @NotNull public Iterable<ModuleHelper> getDependencies()
    {
        if (dependencies == null) {
            // Topological Sort elements
            List<ModuleHelper> list = new ArrayList<ModuleHelper>();
            tsort(list, new IdentitySet<ModuleHelper>());

            if (mustShow(DebugOption.DEPENDENCIES)) {
                logVerbose("Dependencies for: %s = %s\n", getName(), list.toString());
            }

            dependencies = list;
        }

        return dependencies;
    }

    @NotNull public Iterable<TestModuleHelper> getTestModules()
    {
        if (testModules == null) {
            List<TestModuleHelper> list = new ArrayList<TestModuleHelper>();

            for (TestModule testModule : getModule().tests()) {
                list.add(testModule.getHelper());
            }

            testModules = list;
        }

        return testModules;
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
            addIfNotNull(result, library.getArtifact(this, PackageType.JAR));
        }

        return result;
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

        for (ModuleHelper mod : getDependencies()) {
            result.add(mod);
            CollectionUtils.addAll(result, mod.getTestModules());
        }

        result.add(this);
        CollectionUtils.addAll(result, getTestModules());

        return result;
    }

    public List<File> runtimePath()
    {
        return classPath(false, true, false);
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

    @NotNull @Override public ModuleHelper getModuleHelper()
    {
        return this;
    }

    public void clean()
    {
        RemoveTask.remove(this, getOutput());
        RemoveTask.remove(this, getPackageFile());
        RemoveTask.remove(this, getGeneratedSource());
    }

    protected void build(ProjectBuilder pb, String commandName)
    {
        Command command = findCommand(commandName);

        if (command == null) {
            throw new BuildException("Invalid command: " + commandName);
        }

        if (command.isRecursive() && !isNonRecursive()) {
            for (ModuleHelper dep : getDependencies()) {
                pb.execute(dep, commandName);
            }
        }
        else {
            for (Command cmd : command.getDirectDependencies()) {
                pb.build(this, cmd.getQName());
            }
        }

        pb.execute(this, commandName);
    }

    private static String trimDashes(String s)
    {
        int l = s.length();
        return s.substring(s.charAt(0) == '-' ? 1 : 0, s.charAt(l - 1) == '-' ? l - 1 : l);
    }

    private List<File> classPath(boolean useJars, boolean addModuleOutput, boolean compile)
    {
        List<File> result = new ArrayList<File>();

        // Add output dir
        if (addModuleOutput) {
            result.add(getOutput());
        }

        // Add dependencies from modules
        for (ModuleHelper module : getDirectDependencies()) {
            if (module.getModule().mustInclude(compile)) {
                result.add(useJars && module.hasPackage() ? module.getPackageFile() : module.getOutput());
            }
        }

        for (Library lib : getLocalLibraries()) {
            if (lib.mustInclude(compile)) {
                addIfNotNull(result, lib.getArtifact(this, PackageType.JAR));
            }
        }

        return result;
    }

    /**
     * Topological sort dependent modules using a Depth First Search
     * @param elements All descendant elements
     * @param visited  Already visited elements
     */
    private void tsort(List<ModuleHelper> elements, IdentitySet<ModuleHelper> visited)
    {
        visited.add(this);

        for (ModuleHelper dependency : getDirectDependencies()) {
            if (!visited.contains(dependency)) {
                dependency.tsort(elements, visited);
                elements.add(dependency);
            }
        }
    }

    //~ Static fields/initializers ...........................................................................

    public static final String  SRC_JAR = "-src.jar";
    private static final String MODULE_PROP_KEY = "module";
}
