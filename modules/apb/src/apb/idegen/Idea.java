

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

import apb.ModuleHelper;
import apb.ProjectElementHelper;
import apb.TestModuleHelper;

import apb.metadata.ProjectElement;
//
// User: emilio
// Date: Mar 18, 2009
// Time: 4:11:52 PM

//
public class Idea
    extends IdegenCommand
{
    //~ Constructors .........................................................................................

    public Idea()
    {
        super("idea");
    }

    //~ Methods ..............................................................................................

    public void invoke(ProjectElement element)
    {
        final ProjectElementHelper helper = element.getHelper();
        IdeaInfo                   info = helper.getInfoObject("idegen", IdeaInfo.class);
        File                       dir = helper.fileFromBase(info.dir);

        if (helper instanceof ModuleHelper) {
            final ModuleHelper mod = (ModuleHelper) helper;

            createTask(mod, dir, info).withPackageType(mod.getPackageType()).execute();

            for (TestModuleHelper testModule : mod.getTestModules()) {
                createTask(testModule, dir, info).testModule(true).execute();
            }
        }

        if (helper.isTopLevel()) {
            IdegenTask.generateProject(helper.getId(), helper.getProjectDirectory()).on(dir)  //
                      .usingModules(helper.listAllModules())  //
                      .usingTemplate(info.projectTemplate)  //
                      .useJdk(info.jdkName)  //
                      .ifOlder(helper.lastModified())  //
                      .execute();
        }
    }

    private IdegenTask.Module createTask(ModuleHelper mod, File dir, IdeaInfo info)
    {
        return IdegenTask.generateModule(mod.getId()).on(dir)  //
                         .usingSources(mod.getSourceDirs())  //
                         .usingOutput(mod.getOutput())  //
                         .includeEmptyDirs(info.includeEmptyDirs)  //
                         .usingTemplate(info.moduleTemplate)  //
                         .usingModules(mod.getDirectDependencies())  //
                         .usingLibraries(mod.getAllLibraries())  //
                         .ifOlder(mod.lastModified());
    }
}
