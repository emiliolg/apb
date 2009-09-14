

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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Collection;

import apb.Apb;
import apb.Environment;
import apb.ModuleHelper;

import apb.metadata.Library;
import apb.metadata.PackageType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    public IdegenTask(String id, File modulesHome)
    {
        env = Apb.getEnv();
        this.id = id;
        this.modulesHome = modulesHome;
        lastModified = Long.MAX_VALUE;
    }

    //~ Methods ..............................................................................................

    public abstract void execute();

    public static ModuleBuilder generateModule(final String moduleId)
    {
        final ModuleBuilder result = new ModuleBuilder();
        result.id = moduleId;
        return result;
    }

    public static ProjectBuilder generateProject(final String projectId, final File projectDirectory)
    {
        final ProjectBuilder result = new ProjectBuilder();
        result.projectId = projectId;
        result.projectDirectory = projectDirectory;
        return result;
    }

    public IdegenTask ifOlder(long timeStamp)
    {
        lastModified = timeStamp;
        return this;
    }

    public IdegenTask usingTemplate(@NotNull String t)
    {
        template = t.isEmpty() ? null : env.fileFromBase(t);
        return this;
    }

    protected boolean mustBuild(File file)
    {
        final long fileLastModified;
        return env.forceBuild() || (fileLastModified = file.lastModified()) == 0 ||
               lastModified > fileLastModified;
    }

    //~ Inner Classes ........................................................................................

    //
    public abstract static class Module
        extends IdegenTask
    {
        @NotNull protected final List<String> excludes;

        @NotNull protected final List<Library>      libraries;
        @NotNull protected final List<ModuleHelper> moduleDependencies;
        @Nullable protected File                    output;
        @NotNull protected PackageType              packageType;
        @NotNull protected final List<File>         sourceDirs;
        protected boolean                           testModule;
        boolean                                     includeEmptyDirs;

        public Module(String id, File modulesHome)
        {
            super(id, modulesHome);
            testModule = false;
            packageType = PackageType.NONE;
            excludes = new ArrayList<String>();
            libraries = new ArrayList<Library>();
            sourceDirs = new ArrayList<File>();

            moduleDependencies = new ArrayList<ModuleHelper>();
        }

        public abstract void execute();

        public Module usingSources(@NotNull List<File> sources)
        {
            sourceDirs.addAll(sources);
            return this;
        }

        public Module usingOutput(@NotNull File f)
        {
            output = f;
            return this;
        }

        public Module usingTemplate(@NotNull String t)
        {
            return (Module) super.usingTemplate(t);
        }

        public Module testModule(boolean b)
        {
            testModule = b;
            return this;
        }

        public Module withPackageType(@NotNull PackageType t)
        {
            packageType = t;
            return this;
        }

        public Module ifOlder(long timestamp)
        {
            return (Module) super.ifOlder(timestamp);
        }

        public Module includeEmptyDirs(boolean b)
        {
            includeEmptyDirs = b;
            return this;
        }

        public Module excluding(String... excludeDirectories)
        {
            excludes.addAll(java.util.Arrays.asList(excludeDirectories));
            return this;
        }

        public Module usingLibraries(Iterable<Library> libs)
        {
            apb.utils.CollectionUtils.addAll(libraries, libs);
            return this;
        }

        public Module usingModules(Iterable<ModuleHelper> modules)
        {
            apb.utils.CollectionUtils.addAll(moduleDependencies, modules);
            return this;
        }
    }

    //
    public abstract static class Project
        extends IdegenTask
    {
        @NotNull protected String            jdkName;
        @NotNull protected final Set<String> modules;
        @NotNull protected final File        projectDirectory;

        public Project(@NotNull String id, @NotNull File modulesHome, File projectDirectory)
        {
            super(id, modulesHome);
            jdkName = "";
            this.projectDirectory = projectDirectory;
            modules = new TreeSet<String>();
        }

        public Project usingModules(@NotNull Collection<String> moduleIds)
        {
            modules.addAll(moduleIds);
            return this;
        }

        @Override public Project usingTemplate(@NotNull String t)
        {
            return (Project) super.usingTemplate(t);
        }

        public Project useJdk(@NotNull String jdk)
        {
            jdkName = jdk;
            return this;
        }

        @Override public Project ifOlder(long timestamp)
        {
            return (Project) super.ifOlder(timestamp);
        }
    }

    public static class ModuleBuilder
    {
        @NotNull private String id;

        public Module on(File dir)
        {
            return new IdeaTask.Module(id, dir);
        }
    }

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
