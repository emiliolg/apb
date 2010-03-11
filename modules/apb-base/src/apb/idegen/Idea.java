

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import apb.ModuleHelper;
import apb.ProjectBuilder;
import apb.ProjectElementHelper;

import apb.metadata.Dependency;
import apb.metadata.ProjectElement;
//
// User: emilio
// Date: Mar 18, 2009
// Time: 4:11:52 PM

//
/**
 * @exclude
 */
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
    public void invoke(ProjectElement projectElement)
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

    private void generateProject(ProjectElementHelper helper, final IdeaInfo info, final File dir)
    {
        IdegenTask.generateProject(helper.getId(), helper.getProjectDirectory()).on(dir)  //
                  .usingModules(helper.listAllModules())  //
                  .usingTemplate(info.projectTemplate)  //
                  .useJdk(info.jdkName)  //
                  .ifOlder(helper.getSourceFile().lastModified())  //
                  .execute();
    }

    private static IdegenTask.Module createTask(ModuleHelper mod, final IdeaInfo info, final File dir)
    {
        final List<String> deps = new ArrayList<String>();

        for (Dependency d : mod.getModule().dependencies()) {
            if (d.isModule()) {
                deps.add(d.asModule().getId());
            }
        }

        return IdegenTask.generateModule(mod.getId()).on(dir)  //
                         .usingSources(mod.getSourceDirs())  //
                         .usingOutput(mod.getOutput())  //
                         .includeEmptyDirs(info.includeEmptyDirs)  //
                         .usingTemplate(info.moduleTemplate)  //
                         .usingModules(deps)  //
                         .usingLibraries(mod.getAllLibraries())  //
                         .withContentDirs(buildContentDirs(info, mod))  //
                         .ifOlder(mod.getSourceFile().lastModified());
    }

    private static List<String> buildContentDirs(IdeaInfo info, ModuleHelper mod)
    {
        final List<String> result = new ArrayList<String>();

        result.add(mod.getDirFile().getPath());
        result.add(mod.getModule().outputBase);
        result.addAll(info.contentDirs());

        return result;
    }
}
