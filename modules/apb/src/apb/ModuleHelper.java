
// ...........................................................................................................
// Copyright (c) 1993, 2009, Oracle and/or its affiliates. All rights reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Oracle Corp.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// Last changed on 2009-04-24 11:36:53 (-0300), by: emilio. $Revision$
// ...........................................................................................................

package apb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import apb.metadata.CompileInfo;
import apb.metadata.Dependency;
import apb.metadata.JavadocInfo;
import apb.metadata.LocalLibrary;
import apb.metadata.Module;
import apb.metadata.PackageInfo;
import apb.metadata.ProjectElement;
import apb.metadata.ResourcesInfo;
import apb.metadata.TestModule;

import apb.utils.FileUtils;
import apb.utils.IdentitySet;

import org.jetbrains.annotations.NotNull;
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

    @NotNull private final Set<Command>       executedCommands;
    private File                              generatedSource;
    @NotNull private final List<LocalLibrary> localLibraries;
    private File                              output;
    private File                              packageDir;
    private File                              source;

    //~ Constructors .........................................................................................

    ModuleHelper(Module module, Environment env)
    {
        super(module, env);

        executedCommands = new HashSet<Command>();

        dependencies = new ArrayList<ModuleHelper>();

        directDependencies = new ArrayList<ModuleHelper>();
        localLibraries = new ArrayList<LocalLibrary>();

        // Add Direct Dependencies & local libraries
        for (Dependency dependency : module.dependencies()) {
            if (dependency instanceof Module) {
                directDependencies.add((ModuleHelper) env.getHelper((Module) dependency));
            }
            else if (dependency instanceof LocalLibrary) {
                localLibraries.add((LocalLibrary) dependency);
            }
        }
    }

    //~ Methods ..............................................................................................

    public Module getModule()
    {
        return (Module) getElement();
    }

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

    public String getClassPath()
    {
        return FileUtils.makePath(classPath(false, true));
    }

    public PackageInfo getPackageInfo()
    {
        return getModule().pkg;
    }

    public String classFileForManifest()
    {
        List<File> files = new ArrayList<File>();

        // Make the files relative to the jarfile
        File jarFileDir = getPackageFile().getParentFile();

        for (File file : classPath(true, false)) {
            files.add(FileUtils.makeRelative(jarFileDir, file));
        }

        String result = FileUtils.makePath(files, " ");

        if (File.separatorChar != '/') {
            result = result.replace(File.separatorChar, '/');
        }

        return result;
    }

    @NotNull public Iterable<LocalLibrary> getLocalLibraries()
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

    public List<File> classPath(boolean useJars, boolean addModuleOutput)
    {
        List<File> result = new ArrayList<File>();

        // Add output dir
        if (addModuleOutput) {
            result.add(getOutput());
        }

        // First classpath for module dependencies
        for (ModuleHelper dependency : getDirectDependencies()) {
            result.add(useJars ? dependency.getPackageFile() : dependency.getOutput());
        }

        // The classpath for libraries
        for (LocalLibrary library : getLocalLibraries()) {
            result.addAll(library.getFiles(env));
        }

        return result;
    }

    public Collection<File> deepClassPath(boolean useJars, boolean addModuleOutput)
    {
        Set<File> result = new HashSet<File>();

        if (addModuleOutput) {
            result.add(useJars ? getPackageFile() : getOutput());
        }

        // First classpath for module dependencies
        for (ModuleHelper dependency : getDirectDependencies()) {
            result.addAll(dependency.deepClassPath(useJars, true));
        }

        // The classpath for libraries
        for (LocalLibrary library : getLocalLibraries()) {
            result.addAll(library.getFiles(env));
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

    protected void initDependencyGraph()
    {
        // Topological Sort elements
        tsort(dependencies, new IdentitySet<ModuleHelper>());

        if (env.isVerbose()) {
            env.logVerbose("Dependencies for: %s = %s\n", getName(), dependencies.toString());
        }
    }

    void build(String commandName)
    {
        Command command = findCommand(commandName);

        if (command != null) {
            build(command);
        }
        else {
            env.handle("Invalid command: " + commandName);
        }
    }

    void activate(@NotNull ProjectElement activatedModule)
    {
        super.activate(activatedModule);

        Module module = getModule();
        output = env.fileFromBase(module.output);
        source = env.fileFromBase(module.source);
        generatedSource = env.fileFromBase(module.generatedSource);

        try {
            packageDir = env.fileFromBase(module.pkg.dir).getCanonicalFile();
        }
        catch (IOException e) {
            throw new BuildException(e);
        }

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

    private void build(Command command)
    {
        final String commandName = command.getQName();

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
                build(cmd);
            }
        }

        execute(commandName);
    }

    private void execute(@NotNull String commandName)
    {
        Command command = findCommand(commandName);

        if (command != null && notExecuted(command)) {
            ProjectElement projectElement = activate();
            long           ms = startExecution(command);

            for (Command cmd : command.getDependencies()) {
                if (notExecuted(cmd)) {
                    env.setCurrentCommand(cmd);
                    markExecuted(cmd);
                    cmd.invoke(projectElement, env);
                }
            }

            env.setCurrentCommand(null);
            endExecution(command, ms);
            env.deactivate();
        }
    }

    private void addTo(Set<ModuleHelper> result)
    {
        result.add(this);

        for (TestModule testModule : getModule().tests()) {
            result.add((ModuleHelper) env.getHelper(testModule));
        }
    }

    private void endExecution(Command command, long ms)
    {
        if (env.isVerbose()) {
            ms = System.currentTimeMillis() - ms;
            long free = Runtime.getRuntime().freeMemory() / MB;
            long total = Runtime.getRuntime().totalMemory() / MB;
            env.logVerbose("Execution of '%s'. Finished in %d milliseconds. Memory usage: %dM of %dM\n",
                           command, ms, total - free, total);
        }
    }

    private long startExecution(Command command)
    {
        long result = 0;

        if (env.isVerbose()) {
            env.logVerbose("About to execute '%s'\n", command);
            result = System.currentTimeMillis();
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
        for (ModuleHelper dependency : directDependencies) {
            if (!visited.contains(dependency)) {
                visited.add(dependency);
                dependency.tsort(elements, visited);
                elements.add(dependency);
            }
        }
    }

    private void markExecuted(Command cmd)
    {
        executedCommands.add(cmd);
    }

    private boolean notExecuted(Command cmd)
    {
        return !executedCommands.contains(cmd);
    }

    //~ Static fields/initializers ...........................................................................

    private static final long MB = (1024 * 1024);

    public static final String SRC_JAR = "-src.jar";
}
