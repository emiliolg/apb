

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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import apb.metadata.*;

import apb.tasks.FileSet;
import apb.tasks.JavacTask;
import apb.tasks.WarTask;

import apb.utils.ClassUtils;
import apb.utils.DebugOption;
import apb.utils.IdentitySet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static apb.tasks.CoreTasks.*;

import static apb.utils.CollectionUtils.addIfNotNull;
//
// User: emilio
// Date: Sep 15, 2008
// Time: 2:03:05 PM

/**
 * Provides additional functionality for {@link apb.metadata.Module} objects
 */
public class ModuleHelper
    extends ProjectElementHelper
{
    //~ Instance fields ......................................................................................

    @Nullable private File generatedSource;
    @Nullable private File output;
    @Nullable private File packageFile;
    @Nullable private File source;
    @Nullable private File sourcePackageFile;

    @Nullable private Iterable<Library> allLibraries;

    @Nullable private Iterable<ModuleHelper> dependencies;
    @Nullable private TestModuleHelper       testModule;

    //~ Constructors .........................................................................................

    ModuleHelper(ProjectBuilder pb, Module module)
    {
        super(pb, module);
    }

    //~ Methods ..............................................................................................

    /**
     * Returns the Module associated to this helper
     */
    public Module getModule()
    {
        return (Module) getElement();
    }

    /**
     * Get current module output directory
     *
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
     *
     * @return current module source directory
     */
    @NotNull public File getSourceDir()
    {
        if (source == null) {
            source = fileFromBase(getModule().source);
        }

        return source;
    }

    /**
     * Get current module generated sources directory
     *
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
     * Get current module package name
     *
     * @return current module package name
     */
    public String getPackageName()
    {
        return trimDashes(getModule().pkg.name);
    }

    /**
     * Get current module package (.jar, .war etc) file
     *
     * @return current module package file
     */
    @NotNull public File getPackageFile()
    {
        if (packageFile == null) {
            if (!hasPackage()) {
                throw new IllegalArgumentException("Module: '" + getName() + "' does not have a package.");
            }

            File dir = fileFromBase(getModule().pkg.dir);
            packageFile = new File(dir, getPackageName() + getPackageType().getExt());
        }

        return packageFile;
    }

    /**
     * Get current module sources package (xxxx-src.jar) file
     *
     * @return current module sources package file
     */
    @NotNull public File getSourcePackageFile()
    {
        if (sourcePackageFile == null) {
            File dir = fileFromBase(getModule().pkg.dir);
            sourcePackageFile = new File(dir, getPackageName() + SRC_JAR);
        }

        return sourcePackageFile;
    }

    /**
     * Get the {@link apb.metadata.PackageInfo} object for this Module
     */
    @NotNull public PackageInfo getPackageInfo()
    {
        return getModule().pkg;
    }

    /**
     * Returns true if this module must produce a package (jar, war, etc) file
     */
    public boolean hasPackage()
    {
        return getPackageType() != PackageType.NONE;
    }

    /**
     * Returns the type of package file this module has to produce
     */
    @NotNull public PackageType getPackageType()
    {
        return getPackageInfo().type;
    }

    /**
     * Get the {@link apb.metadata.ResourcesInfo} object for this Module
     */
    @NotNull public ResourcesInfo getResourcesInfo()
    {
        return getModule().resources;
    }

    /**
     * Get the {@link apb.metadata.JavadocInfo} object for this Module
     */
    public JavadocInfo getJavadocInfo()
    {
        return getModule().javadoc;
    }

    /**
     * Get the {@link apb.metadata.CompileInfo} object for this Module
     */
    public CompileInfo getCompileInfo()
    {
        return getModule().compiler;
    }

    /**
     * Returns true if this is a test module, false otherwise
     */
    public boolean isTestModule()
    {
        return false;
    }

    /**
     * All libraries used by this Module
     */
    @NotNull public Iterable<Library> getAllLibraries()
    {
        if (allLibraries == null) {
            List<Library> list = new ArrayList<Library>();

            for (Dependency dependency : getDirectDependencies()) {
                if (dependency.isLibrary()) {
                    list.add(dependency.asLibrary());
                }
            }

            allLibraries = list;
        }

        return allLibraries;
    }

    /**
     * All (direct and indirect) Modules this Module depends on
     */
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

    /**
     * The TestModule for this Module
     */
    @Nullable public TestModuleHelper getTestModule()
    {
        return testModule;
    }

    /**
     * The classpath needed to compile this module
     */
    public List<File> compileClassPath()
    {
        return classPath(false, false, true);
    }

    /**
     * The classpath needed to run this module
     */
    public List<File> runtimePath()
    {
        return classPath(false, true, false);
    }

    /**
     * The classpath that mut be included in the jar manifest
     */
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

    /**
     * The list of directories whre source files for this Module can be found
     */
    public List<File> getSourceDirs()
    {
        List<File> sourceDirs = new ArrayList<File>();

        sourceDirs.add(getSourceDir());
        sourceDirs.add(getGeneratedSource());
        return sourceDirs;
    }

    public Set<String> listAllModules()
    {
        Set<String> result = new TreeSet<String>();

        for (ModuleHelper mod : getDependencies()) {
            result.add(mod.getId());

            final TestModuleHelper tm = mod.getTestModule();

            if (tm != null) {
                result.add(tm.getId());
            }
        }

        result.add(getId());

        final TestModuleHelper tm = getTestModule();

        if (tm != null) {
            result.add(tm.getId());
        }

        return result;
    }

    /**
     * Default implementation of a clean action for the current module
     */
    public void clean()
    {
        delete(getOutput()).execute();
        delete(getGeneratedSource()).execute();
        delete(getJavadocInfo().output).execute();

        if (hasPackage()) {
            delete(getPackageFile()).execute();

            if (getPackageInfo().generateSourcesJar) {
                delete(getSourcePackageFile()).execute();
            }
        }
    }

    /**
     * Default implementation of a package action for the current module
     */
    public void createPackage()
    {
        final PackageType type = getPackageType();

        if (type == PackageType.NONE) {
            return;
        }

        final PackageInfo  packageInfo = getPackageInfo();
        final List<Module> modules = modulesToPackage();

        switch (type) {
        case JAR:
            createJar(getPackageFile(), packageInfo, modules);

            // generate sources jar
            if (packageInfo.generateSourcesJar) {
                jar(getSourcePackageFile()).from(sourceFileSets(modules))  //
                                           .execute();
            }

            break;

        case WAR:

            final File    buildDir = fileFromBase(packageInfo.webAppBuildDir);
            final WarTask war =
                war(getPackageFile()).from(packageInfo.webAppDir).usingBuildDirectory(buildDir);

            if (packageInfo.archiveClasses) {
                final File jarFile =
                    new File(new File(buildDir, WarTask.LIB_PATH),
                             getPackageName() + PackageType.JAR.getExt());
                createJar(jarFile, packageInfo, modules);
                war.includeJar(jarFile);
            }
            else {
                war.includeClasses(outputFileSets(modules));
            }

            war.execute();
            break;
        }
    }

    /**
     * Default implementation of a compile action for the current module
     */
    public void compile()
    {
        CompileInfo   info = getCompileInfo();
        List<FileSet> fileSets = getSourceFileSets(info);

        JavacTask javac =
            javac(fileSets).to(getOutput())  //
                           .withClassPath(compileClassPath())  //
                           .sourceVersion(info.source)  //
                           .targetVersion(info.target)  //
                           .withAnnotationOptions(info.annotationOptions())  //
                           .withExtraLibraries(getExtraLibraries())  //
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
                           .instrumentNotNull(info.instrumentNotNull)  //
                           .useName(getName()).encoding(info.encoding);

        if (!info.warnGenerated) {
            javac.excludeFromWarning(getGeneratedSource());
        }

        javac.execute();
    }

    /**
     * Default implementation of a javadoc action for the current module
     */
    public void generateJavadoc()
    {
        JavadocInfo info = getJavadocInfo();

        List<File> sources = new ArrayList<File>();
        sources.add(getSourceDir());
        
        if (info.includeGeneratedSource){
            sources.add(getGeneratedSource());
        }

        for (Module module : modulesToInclude(info.includeDependencies)) {
            sources.add(module.getHelper().getSourceDir());
        }

        javadoc(sources).to(info.output)  //
                        .maxMemory(info.memory)  //
                        .withClassPath(compileClassPath())  //
                        .withEncoding(info.encoding)  //
                        .withVisibility(info.visibility)  //
                        .withLocale(info.locale)  //
                        .withOverview(info.overview)  //
                        .withTitle(info.title)  //
                        .withHeader(info.header)  //
                        .withFooter(info.footer)  //
                        .withBottom(info.bottom)  //
                        .withWindowTitle(info.windowTitle)  //
                        .withLinks(info.links())  //
                        .withOfflineLinks(info.offlineLinks())  //
                        .withGroups(info.groups())  //
                        .useExcludeDoclet(info.useExcludeDoclet)  //
                        .includeAuthorInfo(info.author)  //
                        .includeDeprecatedInfo(info.deprecated)  //
                        .includeVersionInfo(info.version)  //
                        .includeSinceInfo(info.since)  //
                        .includeHelpLinks(info.help)  //
                        .additionalOptions(info.additionalOptions())  //
                        .splitIndexPerLetter(info.splitIndexPerLetter)  //
                        .generateIndex(info.index)  //
                        .generateHtmlSource(info.linkSource)  //
                        .generateClassHierarchy(info.tree)  //
                        .generateDeprecatedList(info.generateDeprecatedList)  //
                        .createUsePages(info.use)  //
                        .usingDoclet(info.doclet)  //
                        .including(info.includes())  //
                        .excluding(info.excludes())  //
                        .execute();
    }

    /**
     * Get the direct (first level) Module dependencies
     */
    public Iterable<Dependency> getDirectDependencies()
    {
        return getModule().dependencies();
    }

    protected List<File> classPath(boolean useJars, boolean addModuleOutput, boolean compile)
    {
        List<File> result = new ArrayList<File>();

        // Add output dir
        if (addModuleOutput) {
            result.add(getOutput());
        }

        // Add dependencies from modules
        for (Dependency dependency : getDirectDependencies()) {
            if (dependency.mustInclude(compile)) {
                if (dependency.isModule()) {
                    ModuleHelper m = dependency.asModule().getHelper();

                    if (!useJars) {
                        result.add(m.getOutput());
                    }
                    else if (m.hasPackage()) {
                        result.add(m.getPackageFile());
                    }
                }
                else if (dependency.isLibrary()) {
                    addIfNotNull(result, dependency.asLibrary().getArtifact(this, PackageType.JAR));
                }
            }
        }

        return result;
    }

    @Override void init()
    {
        super.init();
        Class<? extends TestModule> tmClass = getModule().test;

        if (tmClass != null) {
            try {
                Constructor<? extends TestModule> c = tmClass.getDeclaredConstructor();
                c.setAccessible(true);
                testModule = c.newInstance().getHelper();
                testModule.setModuleToTest(this);
            }
            catch (InstantiationException e) {
                throw new BuildException(e);
            }
            catch (IllegalAccessException e) {
                throw new BuildException(e);
            }
            catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            catch (InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        }
    }

    @NotNull @Override ModuleHelper getModuleHelper()
    {
        return this;
    }

    /**
     * todo this should be replaced by runtimepath or compileclasspath....
     */
    Collection<File> deepClassPath(Iterable<Dependency> dependencyList, boolean useJars)
    {
        Set<File> result = new HashSet<File>();

        for (Dependency dependency : dependencyList) {
            if (dependency.isModule()) {
                result.addAll(dependency.asModule().getHelper().deepClassPath(useJars, true));
            }
            else if (dependency.isLibrary()) {
                addIfNotNull(result, dependency.asLibrary().getArtifact(this, PackageType.JAR));
            }
        }

        return result;
    }

    /**
     * todo this should be replaced by runtimepath or compileclasspath....
     */
    Collection<File> deepClassPath(boolean useJars, boolean addModuleOutput)
    {
        Collection<File> result = deepClassPath(getDirectDependencies(), useJars);

        if (addModuleOutput) {
            result.add(useJars && hasPackage() ? getPackageFile() : getOutput());
        }

        return result;
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

    private void createJar(final File jarFile, PackageInfo packageInfo, List<Module> modules)
    {
        Map<String, Set<String>> services = mergeServices(modules);

        jar(jarFile).from(outputFileSets(modules))  //
                    .mainClass(packageInfo.mainClass)  //
                    .version(getModule().version)  //
                    .manifestAttributes(packageInfo.attributes())  //
                    .withClassPath(manifestClassPath())  //
                    .withServices(services)  //
                    .execute();
    }

    private List<FileSet> outputFileSets(List<Module> modules)
    {
        final List<FileSet> result = new ArrayList<FileSet>();

        for (Module m : modules) {
            final ModuleHelper h = m.getHelper();
            result.add(FileSet.fromDir(h.getOutput()).excluding(h.getPackageInfo().excludes()));
        }

        return result;
    }

    private List<FileSet> sourceFileSets(List<Module> modules)
    {
        final List<FileSet> result = new ArrayList<FileSet>();

        for (Module m : modules) {
            result.add(FileSet.fromDir(m.getHelper().getSourceDir()).excluding(Constants.DEFAULT_EXCLUDES));
        }

        return result;
    }

    private List<Library> getExtraLibraries()
    {
        final List<Library> result = new ArrayList<Library>(getCompileInfo().extraLibraries());

        if (getCompileInfo().useTools) {
            File toolsJar = ClassUtils.toolsJar();

            if (toolsJar != null) {
                result.add(new LocalLibrary(toolsJar.getPath()));
            }
        }

        if (getCompileInfo().instrumentNotNull) {
            File notNullAnnotations = Apb.applicationJarFile();
            result.add(new LocalLibrary(notNullAnnotations.getPath()));
        }

        return result;
    }

    private List<FileSet> getSourceFileSets(CompileInfo info)
    {
        List<FileSet> fileSets = new ArrayList<FileSet>();

        fileSets.add(FileSet.fromDir(getSourceDir()).including(info.includes()).excluding(info.excludes()));

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
        final PackageInfo packageInfo = getPackageInfo();

        List<Module> result = modulesToInclude(packageInfo.includeDependencies);

        for (Dependency dep : packageInfo.additionalDependencies()) {
            if (dep.isModule()) {
                result.add(dep.asModule());
            }
        }

        return result;
    }

    private List<Module> modulesToInclude(IncludeDependencies d)
    {
        List<Module> result = new ArrayList<Module>();

        // Add this module
        result.add(getModule());

        switch (d) {
        case DIRECT:

            for (Dependency dep : getDirectDependencies()) {
                if (dep.isModule()) {
                    result.add(dep.asModule());
                }
            }

            break;
        case ALL:

            for (ModuleHelper dep : getDependencies()) {
                result.add(dep.getModule());
            }

            break;
        }

        return result;
    }

    /**
     * Topological sort dependent modules using a Depth First Search
     *
     * @param elements All descendant elements
     * @param visited  Already visited elements
     */
    private void tsort(List<ModuleHelper> elements, IdentitySet<ModuleHelper> visited)
    {
        visited.add(this);

        for (Dependency dependency : getDirectDependencies()) {
            if (dependency.isModule()) {
                final ModuleHelper m = dependency.asModule().getHelper();

                if (!visited.contains(m)) {
                    m.tsort(elements, visited);
                    elements.add(m);
                }
            }
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static final String SRC_JAR = "-src.jar";
}
