

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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

    @NotNull private final List<ModuleHelper> directDependencies;
    private File                              generatedSource;
    @NotNull private final List<LocalLibrary> localLibraries;
    private File                              moduledir;
    private File                              output;
    private File                              packageDir;
    private File                              source;

    //~ Constructors .........................................................................................

    ModuleHelper(Module module, Environment env)
    {
        super(module, env);

        // Add Direct Dependencies & local libraries
        directDependencies = new ArrayList<ModuleHelper>();
        localLibraries = new ArrayList<LocalLibrary>();

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

    public File getModuledir()
    {
        return moduledir;
    }

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
            final File f = library.getFile(env);

            if (f != null) {
                result.add(f);
            }
        }

        return result;
    }

    public Collection<File> deepClassPath(boolean useJars)
    {
        Set<File> result = new HashSet<File>();

        // First classpath for module dependencies
        for (ModuleHelper dependency : getDirectDependencies()) {
            result.add(useJars ? dependency.getPackageFile() : dependency.getOutput());
            result.addAll(dependency.deepClassPath(useJars));
        }

        // The classpath for libraries
        for (LocalLibrary library : getLocalLibraries()) {
            final File f = library.getFile(env);

            if (f != null) {
                result.add(f);
            }
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

    protected Iterable<? extends ProjectElementHelper> getChildren()
    {
        return directDependencies;
    }

    void activate(@NotNull ProjectElement activatedModule)
    {
        super.activate(activatedModule);

        Module module = getModule();
        moduledir = env.fileFromBase(module.getDir());
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

    //~ Static fields/initializers ...........................................................................

    public static final String SRC_JAR = "-src.jar";
}
