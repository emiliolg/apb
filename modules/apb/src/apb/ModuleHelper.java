
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

    private CompileInfo compileInfo;

    private List<ModuleHelper> directDependencies;
    private File               generatedSource;
    private JavadocInfo        javadocInfo;
    private File               output;
    private File               packageDir;
    private PackageInfo        packageInfo;
    private String             packageName;
    private ResourcesInfo      resourcesInfo;
    private File               source;
    private File moduledir;

    //~ Constructors .........................................................................................

    ModuleHelper(Module module, Environment env)
    {
        super(module, env);
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
            throw new IllegalStateException("Module not initialized");
        }

        return output;
    }

    public File getSource()
    {
        return source;
    }

    public File getPackageFile()
    {
        return new File(packageDir, packageName + getPackageInfo().type.getExt());
    }

    public File getSourcePackageFile()
    {
        return new File(packageDir, packageName + SRC_JAR);
    }

    public String getClassPath()
    {
        return FileUtils.makePath(classPath(false, true));
    }

    public PackageInfo getPackageInfo()
    {
        return packageInfo;
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

    public List<LocalLibrary> getLocalLibraries()
    {
        List<LocalLibrary> result = new ArrayList<LocalLibrary>();

        for (Dependency dependency : getModule().dependencies()) {
            if (dependency instanceof LocalLibrary) {
                result.add((LocalLibrary) dependency);
            }
        }

        return result;
    }

    public ResourcesInfo getResourcesInfo()
    {
        return resourcesInfo;
    }

    public Iterable<ModuleHelper> getDirectDependencies()
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
        return javadocInfo;
    }

    public CompileInfo getCompileInfo()
    {
        return compileInfo;
    }

    public String getPackageName()
    {
        return packageName;
    }

    protected List<ModuleHelper> addChildren()
    {
        directDependencies = new ArrayList<ModuleHelper>();

        for (Dependency dependency : getModule().dependencies()) {
            if (dependency instanceof Module) {
                directDependencies.add((ModuleHelper) env.getHelper((Module) dependency));
            }
        }

        return directDependencies;
    }

    void activate()
    {
        Module module = getModule();
        moduledir = env.fileFromBase(env.getProperty(Environment.MODULE_DIR_PROP_KEY));
        output = env.fileFromBase(module.output);
        source = env.fileFromBase(module.source);
        generatedSource = env.fileFromBase(module.generatedSource);
        resourcesInfo = module.resources;
        javadocInfo = module.javadoc;
        compileInfo = module.compiler;
        packageInfo = module.pkg;
        packageName = env.expand(packageInfo.name);

        try {
            packageDir = env.fileFromBase(packageInfo.dir).getCanonicalFile();
        }
        catch (IOException e) {
            throw new BuildException(e);
        }

        for (TestModule testModule : module.tests()) {
            final TestModuleHelper helper = (TestModuleHelper) env.getHelper(testModule);
            helper.setModuleToTest(this);
        }
    }


    //~ Static fields/initializers ...........................................................................

    public static final String SRC_JAR = "-src.jar";
}
