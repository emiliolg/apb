
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import apb.Apb;
import apb.Environment;
import apb.ModuleHelper;
import apb.ProjectBuilder;
import apb.ProjectElementHelper;
import apb.ProjectHelper;
import apb.TestModuleHelper;
import apb.metadata.Dependency;
import apb.metadata.Library;
import apb.metadata.PackageType;
import apb.metadata.ProjectElement;
import apb.metadata.ProjectElementList;

/**
 * @exclude
 */
@SuppressWarnings({"UnusedDeclaration"})
public class Idea
    extends IdegenCommand
{
    //~ Constructors .........................................................................................

    /**
     * Constructs an Idea IdegenCommand instance
     */
    public Idea()
    {
        super("idea");
    }

    //~ Methods ..............................................................................................

    /**
     * This is the method implementation that will be invoked when running this IdegenCommand over a Module or Project
     *
     * @param projectElement The Module or Project to be processes.
     */
    @Override public void invoke(ProjectElement projectElement)
    {
        final ProjectElementHelper helper = projectElement.getHelper();
        final IdeaInfo             info = helper.getInfoObject("idegen", IdeaInfo.class);
        final File                 targetDir = helper.fileFromBase(info.dir);

        if (helper instanceof ModuleHelper) {
            final ModuleHelper mod = (ModuleHelper) helper;

            if (mod.isTestModule()) {
                createTask(mod, info, targetDir).testModule(true).execute();
            }
            else {
                createTask(mod, info, targetDir).withPackageType(mod.getPackageType()).execute();
                ProjectBuilder.forward(getName(), mod.getTestModule());
            }
        }

        if (helper.isTopLevel()) {
            generateProject(helper, info, targetDir);
        }
    }

    @Nullable private static Boolean scope(Dependency d)
    {
        return d.mustInclude(true) == d.mustInclude(false) ? null : d.mustInclude(true);
    }

    private static IdegenTask.Module createTask(ModuleHelper mod, final IdeaInfo info, final File dir)
    {
        final List<String>          mods = new ArrayList<String>();
        final List<Library>         libs = new ArrayList<Library>();
        final Map<String, Boolean>  modsScope = new HashMap<String, Boolean>();
        final Map<Library, Boolean> libsScope = new HashMap<Library, Boolean>();

        getDependenciesInfo(mod, mods, libs, modsScope, libsScope);

        return IdegenTask.generateModule(mod.getId()).on(dir)  //
                         .usingSources(mod.getSourceDirs())  //
                         .usingOutput(mod.getOutput())  //
                         .includeEmptyDirs(info.includeEmptyDirs)  //
                         .usingTemplate(info.moduleTemplate)  //
                         .usingModules(mods)  //
                         .usingModulesScope(modsScope)  //
                         .usingLibraries(libs)  //
                         .usingLibsScope(libsScope)  //
                         .withContentDirs(buildContentDirs(info, mod))  //
                         .ifOlder(mod.getSourceFile().lastModified());
    }

    private static void getDependenciesInfo(ModuleHelper mod, List<String> mods, List<Library> libs,
                                            Map<String, Boolean> modsScope, Map<Library, Boolean> libsScope)
    {
        for (Dependency d : mod.getModule().dependencies()) {
            Boolean scope = scope(d);

            if (d.isModule()) {
                String modId = d.asModule().getId();
                mods.add(modId);

                if (scope != null) {
                    modsScope.put(modId, scope);
                }
            }
            else if (d.isLibrary()) {
                Library lib = d.asLibrary();
                libs.add(lib);

                if (scope != null) {
                    libsScope.put(lib, scope);
                }
            }
        }
    }

    private static List<String> buildContentDirs(IdeaInfo info, ModuleHelper mod)
    {
        final List<String> result = new ArrayList<String>();

        result.add(mod.getDirFile().getPath());
        result.add(mod.getModule().generatedSource);
        result.addAll(info.contentDirs());

        return result;
    }

    private static Collection<Library> getAllLibraries(ProjectElementHelper helper)
    {
        final Map<String, Library> allLibraries = new HashMap<String, Library>();

        final Queue<ProjectElementHelper> pending = new ArrayDeque<ProjectElementHelper>();

        pending.offer(helper);

        final Set<ProjectElementHelper> proccessed = new HashSet<ProjectElementHelper>();

        ProjectElementHelper current;

        while ((current = pending.poll()) != null) {
            if (proccessed.add(current)) {
                if (current instanceof ProjectHelper) {
                    ProjectHelper      project = (ProjectHelper) current;
                    ProjectElementList components = project.getProject().components();

                    for (ProjectElement component : components) {
                        pending.add(component.getHelper());
                    }
                }
                else {
                    assert current instanceof ModuleHelper;

                    ModuleHelper mod = (ModuleHelper) current;

                    addLibraries(allLibraries, mod);

                    TestModuleHelper test = mod.getTestModule();

                    if (test != null) {
                        addLibraries(allLibraries, test);
                    }
                }
            }
        }

        final Collection<Library> result = allLibraries.values();

        checkDuplicates(result);

        return result;
    }

    private static void checkDuplicates(Collection<Library> allLibs)
    {
        final Map<File, Set<String>> dups = new HashMap<File, Set<String>>();
        final Environment            env = Apb.getEnv();

        for (Library library : allLibs) {
            File jar = library.getArtifact(env, PackageType.JAR);

            if (jar != null) {
                Set<String> libs = dups.get(jar);

                if (libs == null) {
                    dups.put(jar, (libs = new HashSet<String>()));
                }

                libs.add(library.getClass().getName());
            }
        }

        for (Set<String> names : dups.values()) {
            if (names.size() > 1) {
                env.logWarning("Warning duplicate libraries: %s%n", names);
            }
        }
    }

    private static void addLibraries(Map<String, Library> allLibraries, ModuleHelper dep)
    {
        for (Library library : dep.getAllLibraries()) {
            String id = library.getName() + ';' + library.getClass().getName();
            allLibraries.put(id, library);
        }
    }

    private static void generateProject(ProjectElementHelper helper, final IdeaInfo info, final File dir)
    {
        IdegenTask.generateProject(helper.getId(), helper.getProjectDirectory()).on(dir)  //
                  .usingModules(helper.listAllModules())  //
                  .usingLibraries(getAllLibraries(helper))  //
                  .usingTemplate(info.projectTemplate)  //
                  .useJdk(info.jdkName)  //
                  .ifOlder(helper.getSourceFile().lastModified())  //
                  .execute();
    }
}
