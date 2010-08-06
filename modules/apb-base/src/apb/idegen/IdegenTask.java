
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

package apb.idegen;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import apb.Apb;
import apb.BuildException;
import apb.Environment;
import apb.metadata.Library;
import apb.metadata.PackageType;

import static java.util.Arrays.asList;
//
// User: emilio
// Date: Sep 11, 2009
// Time: 3:12:32 PM

//

public abstract class IdegenTask
{
    //~ Instance fields ......................................................................................

    @NotNull protected Environment env;

    @NotNull protected final String id;
    protected long                  lastModified;
    @NotNull protected final File   modulesHome;
    @Nullable protected File        template;

    //~ Constructors .........................................................................................

    /**
     * Constructs a Idegen Task
     * @param id           The task Id
     * @param modulesHome  a path to the modules files
     */
    protected IdegenTask(String id, File modulesHome)
    {
        env = Apb.getEnv();
        this.id = id;
        this.modulesHome = modulesHome;
        lastModified = Long.MAX_VALUE;
    }

    //~ Methods ..............................................................................................

    /**
     * Runs the IdegenTask and create the IDE metadata files
     */
    public abstract void execute();

    /**
     * Creates a ModuleBuilder instance using the given id
     * @param moduleId  The Id of the ModuleBuilder
     * @return a new {@link ModuleBuilder} instance
     */
    public static ModuleBuilder generateModule(final String moduleId)
    {
        final ModuleBuilder result = new ModuleBuilder();
        result.id = moduleId;
        return result;
    }

    /**
     * Creates a ProjectBuilder instance
     * @param projectId         The Id of the project
     * @param projectDirectory  Source project directory
     * @return a new {@link ProjectBuilder} instance
     */
    public static ProjectBuilder generateProject(final String projectId, final File projectDirectory)
    {
        final ProjectBuilder result = new ProjectBuilder();
        result.projectId = projectId;
        result.projectDirectory = projectDirectory;
        return result;
    }

    /**
     * Updates the lastModified value and returns the IdegenTask instance
     * @param timeStamp   A <code>long</code> value representing the time it was
     *                    last modified, measured in milliseconds
     * @return the current instance
     */
    public IdegenTask ifOlder(long timeStamp)
    {
        lastModified = timeStamp;
        return this;
    }

    /**
     * Updates the template file name and returns the IdegenTask instance
     * @param templateFile    The name of the template file
     * @return the current instance
     *
     * @throws apb.BuildException if the template file does not exist in the base directory
     */
    public IdegenTask usingTemplate(@NotNull String templateFile)
    {
        if (templateFile.isEmpty()) {
            template = null;
        }
        else {
            final File file = env.fileFromBase(templateFile);

            if (!file.exists()) {
                throw new BuildException("Cannot find template file '" + file.getPath() + "'");
            }

            template = file;
        }

        return this;
    }

    protected boolean mustBuild(File file)
    {
        final long fileLastModified;
        return env.forceBuild() || (fileLastModified = file.lastModified()) == 0 ||
               lastModified > fileLastModified;
    }

    //~ Inner Classes ........................................................................................

    /**
     * This class represents a java module for IDE metadata generation
     *
     */
    public abstract static class Module
        extends IdegenTask
    {
        @NotNull protected final List<String>          contentDirs = new ArrayList<String>();
        @NotNull protected final List<String>          excludes = new ArrayList<String>();
        @NotNull protected final List<Library>         libraries = new ArrayList<Library>();
        @NotNull protected final Map<Library, Boolean> libsScope = new HashMap<Library, Boolean>();
        @NotNull protected final Map<String, Boolean>  modsScope = new HashMap<String, Boolean>();
        @NotNull protected final List<String>          moduleDependencies = new ArrayList<String>();
        @Nullable protected File                       output;
        @NotNull protected PackageType                 packageType = PackageType.NONE;
        @NotNull protected final List<File>            sourceDirs = new ArrayList<File>();
        protected boolean                              testModule;
        boolean                                        includeEmptyDirs;

        /**
         * Constructs a Module
         * @param id           The module Id
         * @param modulesHome  a path to the modules files
         */
        protected Module(String id, File modulesHome)
        {
            super(id, modulesHome);
        }

        /**
         * Updates the source directories and returns the Module instance
         *
         * @param sources    A list of source directories
         * @return the current instance
         */
        public Module usingSources(@NotNull List<File> sources)
        {
            sourceDirs.addAll(sources);
            return this;
        }

        /**
         * Updates the output file returns the Module instance
         *
         * @param outputFile  A list of source directories
         * @return the current instance
         */
        public Module usingOutput(@NotNull File outputFile)
        {
            output = outputFile;
            return this;
        }

        /**
         * Updates the template file name and returns the Module instance
         * @param templateFile    The name of the template file
         *
         * @throws apb.BuildException if the template file does not exist in the base directory
         */
        @Override public Module usingTemplate(@NotNull String templateFile)
        {
            return (Module) super.usingTemplate(templateFile);
        }

        /**
         * Updates the testModule flag and returns the Module instance
         *
         * @param b  true or false
         * @return the current instance
         */
        public Module testModule(boolean b)
        {
            testModule = b;
            return this;
        }

        /**
         * Updates the packageType value and returns the Module instance
         *
         * @param pkgType  A {@link apb.metadata.PackageType}.  Ie. WAR, EAR.
         * @return the current instance
         */
        public Module withPackageType(@NotNull PackageType pkgType)
        {
            packageType = pkgType;
            return this;
        }

        /**
         * Updates the lastModified value and returns the Module instance
         * @param timeStamp   A <code>long</code> value representing the time it was
         *                    last modified, measured in milliseconds
         */
        @Override public Module ifOlder(long timeStamp)
        {
            return (Module) super.ifOlder(timeStamp);
        }

        /**
         * Updates the includeEmptyDirs flag and returns the Module instance
         *
         * @param b  true or false
         * @return the current instance
         */
        public Module includeEmptyDirs(boolean b)
        {
            includeEmptyDirs = b;
            return this;
        }

        /**
         * Updates the exclude folder list and returns the Module instance
         *
         * @param excludeDirectories  a exclude folder list
         * @return the current instance
         */
        public Module excluding(String... excludeDirectories)
        {
            excludes.addAll(java.util.Arrays.asList(excludeDirectories));
            return this;
        }

        /**
        * Updates the libraries and returns the Module instance
        *
        * @param libs  a {@link apb.metadata.Library} list
        * @return the current instance
        */
        public Module usingLibraries(Iterable<? extends Library> libs)
        {
            apb.utils.CollectionUtils.addAll(libraries, libs);
            return this;
        }

        public Module usingLibsScope(Map<Library, Boolean> libs)
        {
            libsScope.putAll(libs);
            return this;
        }

        /**
         * Updates the module dependencies and returns the Module instance
         *
         * @param modules  a modules list
         * @return the current instance
         */
        public Module usingModules(String... modules)
        {
            return usingModules(asList(modules));
        }

        /**
         * Updates the module dependencies and returns the Module instance
         *
         * @param modules  a modules List
         * @return the current instance
         */
        public Module usingModules(List<String> modules)
        {
            moduleDependencies.addAll(modules);
            return this;
        }

        public Module usingModulesScope(Map<String, Boolean> modules)
        {
            modsScope.putAll(modules);
            return this;
        }

        /**
         * Updates the module dependencies and returns the Module instance
         *
         * @param dirs  a modules List
         * @return the current instance
         */
        public Module withContentDirs(List<String> dirs)
        {
            contentDirs.addAll(dirs);
            return this;
        }
    }

    /**
     * This class represents a java project for IDE metadata generation
     *
     */
    public abstract static class Project
        extends IdegenTask
    {
        @NotNull protected String             jdkName;
        @NotNull protected final Set<Library> libraries;
        @NotNull protected final Set<String>  modules;
        @NotNull protected final File         projectDirectory;

        /**
         * Constructs a Project
         * @param id                 The module Id
         * @param modulesHome        a path to the modules files
         * @param projectDirectory   a path to a project directory
         */
        protected Project(@NotNull String id, @NotNull File modulesHome, File projectDirectory)
        {
            super(id, modulesHome);
            jdkName = "";
            this.projectDirectory = projectDirectory;
            modules = new TreeSet<String>();
            libraries =
                new TreeSet<Library>(new Comparator<Library>() {
                        @Override public int compare(Library o1, Library o2)
                        {
                            final int c = o1.getClass().getName().compareTo(o2.getClass().getName());
                            return c != 0 ? c : o1.getName().compareTo(o2.getName());
                        }
                    });
        }

        /**
         * Updates the modules and returns the Project instance
         *
         * @param moduleIds  a moduleId Collection
         * @return the current instance
         */
        public Project usingModules(@NotNull Collection<String> moduleIds)
        {
            modules.addAll(moduleIds);
            return this;
        }

        public Project usingLibraries(@NotNull Collection<? extends Library> libs)
        {
            libraries.addAll(libs);
            return this;
        }

        /**
         * Updates the template file name and returns the IdegenTask instance
         * @param templateFile    The name of the template file
         *
         * @throws apb.BuildException if the template file does not exist in the base directory
         */
        @Override public Project usingTemplate(@NotNull String templateFile)
        {
            return (Project) super.usingTemplate(templateFile);
        }

        public Project useJdk(@NotNull String jdk)
        {
            jdkName = jdk;
            return this;
        }

        /**
         * Updates the lastModified value and returns the Project instance
         * @param timeStamp   A <code>long</code> value representing the time it was
         *                    last modified, measured in milliseconds
         */
        @Override public Project ifOlder(long timeStamp)
        {
            return (Project) super.ifOlder(timeStamp);
        }
    }

    /**
    * ModuleBuilder class
    */
    public static class ModuleBuilder
    {
        @NotNull private String id;

        public Module on(File dir)
        {
            return new IdeaTask.Module(id, dir);
        }
    }

    /**
    * ProjectBuilder class
    */
    public static class ProjectBuilder
    {
        @NotNull private File   projectDirectory;
        @NotNull private String projectId;

        public Project on(File dir)
        {
            return new IdeaTask.Project(projectId, dir, projectDirectory);
        }
    }
}
