

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

import apb.tasks.FileSet;
import apb.tasks.JarTask;

import apb.utils.CollectionUtils;
import apb.utils.DebugOption;
import apb.utils.FileUtils;
import apb.utils.IdentitySet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static apb.tasks.CoreTasks.*;

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

    @Nullable private File              generatedSource;
    @Nullable private File              output;
    @Nullable private File              packageFile;
    @Nullable private File              source;
    @Nullable private File              sourcePackageFile;
    @Nullable private Iterable<Library> allLibraries;

    @Nullable private Iterable<ModuleHelper>     dependencies;
    @Nullable private Iterable<ModuleHelper>     directDependencies;
    @Nullable private Iterable<Library>          libraries;
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

    @NotNull public Iterable<Library> getLibraries()
    {
        if (libraries == null) {
            List<Library> list = new ArrayList<Library>();

            for (Dependency dependency : getModule().dependencies()) {
                if (dependency.isLibrary()) {
                    list.add(dependency.asLibrary());
                }
            }

            libraries = list;
        }

        return libraries;
    }

    @NotNull public Iterable<Library> getAllLibraries()
    {
        if (allLibraries == null) {
            List<Library> list = new ArrayList<Library>();

            for (Library library : getLibraries()) {
                list.add(library);
            }

            for (Library l : getCompileInfo().extraLibraries()) {
                list.add(l);
            }

            allLibraries = list;
        }

        return allLibraries;
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
        for (Library library : getLibraries()) {
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

        if (getPackageInfo().addClassPath) {
            for (File file : classPath(true, false, false)) {
                files.add(file.getPath());
            }

            files.addAll(getPackageInfo().extraClassPathEntries());
        }

        return files;
    }

    @NotNull @Override public ModuleHelper getModuleHelper()
    {
        return this;
    }

    public void clean()
    {
        delete(getOutput()).execute();
        delete(getGeneratedSource()).execute();

        if (hasPackage()) {
            delete(getPackageFile()).execute();
        }
    }

    public void createPackage()
    {
        final PackageInfo packageInfo = getPackageInfo();

        if (hasPackage()) {
            final List<Module> additionalDeps = modulesToPackage();

            Map<String, Set<String>> services = mergeServices(additionalDeps);

            JarTask jarTask =
                jar(getPackageFile()).fromDir(getOutput())  //
                                     .mainClass(packageInfo.mainClass)  //
                                     .version(getModule().version)  //
                                     .manifestAttributes(packageInfo.attributes())  //
                                     .withClassPath(manifestClassPath())  //
                                     .withServices(services).excluding(packageInfo.excludes());

            // prepare dependencies included in package

            if (!additionalDeps.isEmpty()) {
                for (Module m : additionalDeps) {
                    logVerbose("Adding module '%s'.\n", m.toString());
                    jarTask.addDir(m.getHelper().getOutput());
                }
            }

            // run task
            jarTask.execute();

            // generate sources jar
            if (packageInfo.generateSourcesJar) {
                jar(getSourcePackageFile()).fromDir(getSource())  //
                                           .excluding(FileUtils.DEFAULT_EXCLUDES)  //
                                           .execute();
            }
        }
    }

    public void compile()
    {
        CompileInfo   info = getCompileInfo();
        List<FileSet> fileSets = getSourceFileSets(info);

        javac(fileSets).to(getOutput())  //
                       .withClassPath(compileClassPath())  //
                       .sourceVersion(info.source)  //
                       .targetVersion(info.target)  //
                       .withAnnotations(info.annotationOptions())  //
                       .withExtraLibraries(info.extraLibraries())  //
                       .debug(info.debug)  //
                       .deprecated(info.deprecated)  //
                       .lint(info.lint)  //
                       .showWarnings(info.warn)  //
                       .failOnWarning(info.failOnWarning)  //
                       .lintOptions(info.lintOptions)  //
                       .trackUnusedDependencies(info.validateDependencies)  //
                       .processing(info.getProcessingOption().paramValue())  //
                       .usingDefaultFormatter(info.defaultErrorFormatter)  //
                       .excludeFromWarning(info.warnExcludes())  //
                       .useName(getName())  //
                       .execute();
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

    private static void mergeServices(Map<String, Set<String>> mergedServices,
                                      Map<String, Set<String>> services)
    {
        for (Map.Entry<String, Set<String>> e : services.entrySet()) {
            String            service = e.getKey();
            Set<String>       providers = e.getValue();
            final Set<String> mergedProviders = mergedServices.get(service);

            if (mergedProviders == null) {
                mergedServices.put(service, new HashSet<String>(providers));
            }
            else {
                mergedProviders.addAll(providers);
            }
        }
    }

    private List<FileSet> getSourceFileSets(CompileInfo info)
    {
        List<FileSet> fileSets = new ArrayList<FileSet>();

        fileSets.add(FileSet.fromDir(getSource()).including(info.includes()).excluding(info.excludes()));

        if (getGeneratedSource().exists()) {
            fileSets.add(FileSet.fromDir(getGeneratedSource()));
        }

        return fileSets;
    }

    private Map<String, Set<String>> mergeServices(List<Module> additionalDeps)
    {
        final Map<String, Set<String>> services = getPackageInfo().services();

        if (additionalDeps.isEmpty()) {
            return services;
        }

        // add packaged dependencies
        Map<String, Set<String>> mergedServices = new HashMap<String, Set<String>>();

        for (Module m : additionalDeps) {
            mergeServices(mergedServices, m.pkg.services());
        }

        mergeServices(mergedServices, services);

        return mergedServices;
    }

    private List<Module> modulesToPackage()
    {
        List<Module>      result = new ArrayList<Module>();
        final PackageInfo packageInfo = getPackageInfo();

        switch (packageInfo.includeDependencies) {
        case DIRECT:

            for (ModuleHelper dep : getDirectDependencies()) {
                result.add(dep.getModule());
            }

            break;
        case ALL:

            for (ModuleHelper dep : getDependencies()) {
                result.add(dep.getModule());
            }

            break;
        }

        for (Dependency dep : packageInfo.additionalDependencies()) {
            if (dep.isModule()) {
                result.add(dep.asModule());
            }
        }

        return result;
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

        for (Library lib : getLibraries()) {
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
