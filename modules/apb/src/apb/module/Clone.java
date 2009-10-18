

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


package apb.module;

import java.io.File;

import apb.BuildException;
import apb.Command;
import apb.ModuleHelper;

import apb.metadata.Module;
import apb.metadata.ProjectElement;

import apb.tasks.FilterTask;

import apb.utils.FileUtils;
import apb.utils.NameUtils;

import static apb.tasks.CoreTasks.copyFiltering;

import static apb.utils.Console.getString;
import static apb.utils.Console.printf;
import static apb.utils.FileUtils.makeRelative;
import static apb.utils.StringUtils.isNotEmpty;
//
// User: emilio
// Date: Mar 18, 2009
// Time: 4:11:52 PM

//
public class Clone
    extends Command
{
    //~ Constructors .........................................................................................

    public Clone()
    {
        super(Module.class, "module", "clone", "Generate a new Module based on a specified one.", false);
    }

    //~ Methods ..............................................................................................

    public void invoke(ProjectElement projectElement)
    {
        if (projectElement instanceof Module) {
            clone(((Module) projectElement).getHelper());
        }
        else {
            throw new BuildException("Clone project not supported");
        }
    }

    private static String asWord(String str)
    {
        return WORD_BOUNDARY + str + WORD_BOUNDARY;
    }

    private void clone(ModuleHelper module)
    {
        final Class<?> moduleClass = module.getModule().getClass();
        File           newDir = getNewProjectDir(module.getProjectDirectory());
        String         newModule = getNewModuleName(moduleClass);

        cloneDefinition(module.getSourceFile(), moduleClass, newDir, newModule);

        cloneContent(module, newDir, newModule);
    }

    private void cloneContent(ModuleHelper module, File newDir, String newModule)
    {
        String oldGroup = module.getModule().group;
        File   oldSources = module.getSourceDir();
        String topDir = FileUtils.topSingleDirectory(oldSources);
        String oldPackage = NameUtils.idFromDir(topDir);

        String newGroup = getNewGroupName(oldGroup);
        String newPackage = getNewTopPackage(oldPackage.replace(oldGroup, newGroup));

        File   oldDirFile = module.getDirFile();
        String newId = NameUtils.idFromJavaId(newModule);

        File newBaseDir =
            new File(newDir, makeRelative(module.getProjectDirectory(), module.getBaseDir()).getPath());
        File newDirFile = new File(newBaseDir, NameUtils.dirFromId(newId));

        File newSources = new File(newDirFile, FileUtils.makeRelative(oldDirFile, oldSources).getPath());

        if (!topDir.isEmpty()) {
            oldSources = new File(oldSources, topDir);
        }

        if (!newPackage.isEmpty()) {
            newSources = new File(newSources, NameUtils.dirFromId(newPackage));
        }

        copyFiltering(oldSources).to(newSources).replacing(asWord(oldPackage), newPackage).execute();
    }

    private void cloneDefinition(final File moduleSource, final Class<?> moduleClass, File newDir,
                                 String newModule)
    {
        File defFile = new File(newDir, NameUtils.dirFromId(newModule) + ".java");

        String oldClassName = NameUtils.simpleName(moduleClass);
        String newClassName = NameUtils.simpleName(newModule);

        String oldPackageName = NameUtils.packageName(moduleClass);
        String newPackageName = NameUtils.packageName(newModule);

        final FilterTask filterTask =
            copyFiltering(moduleSource).to(defFile).replacing(asWord(oldClassName), newClassName);

        if (oldPackageName.isEmpty() && !newPackageName.isEmpty()) {
            filterTask.insertingLine("package " + newPackageName);
        }
        else {
            filterTask.replacing("package\\s+" + oldPackageName, "package " + newPackageName);
        }

        filterTask.execute();
    }

    private File getNewProjectDir(File dir)
    {
        File result = null;

        while (result == null) {
            result = new File(getString("Project directory", dir.getPath()));
            final String msg = FileUtils.validateDir(result);

            if (isNotEmpty(msg)) {
                printf("%s\n", msg);
                result = null;
            }
        }

        return result;
    }

    private String getNewModuleName(Class<?> moduleClass)
    {
        String pkg = NameUtils.packageName(moduleClass);

        if (!pkg.isEmpty()) {
            pkg += ".";
        }

        String result = null;

        while (result == null) {
            result = getString("New module name", pkg + "NewModule");

            if (!NameUtils.isValidQualifiedClassName(result)) {
                printf("Invalid module name: %s\n", result);
                result = null;
            }
            else if (NameUtils.packageName(result).isEmpty() && !pkg.isEmpty()) {
                printf("Should enter a package name for the module\n");
                result = null;
            }
        }

        return result;
    }

    private String getNewGroupName(String groupName)
    {
        String result = null;

        while (result == null) {
            result = getString("New group name", groupName).trim();

            if (!result.isEmpty() && !NameUtils.isValidPackageName(result)) {
                printf("Invalid group name\n");
                result = null;
            }
        }

        return result;
    }

    private String getNewTopPackage(String packageName)
    {
        String result = null;

        while (result == null) {
            result = getString("New top package name", packageName).trim();

            if (!result.isEmpty() && !NameUtils.isValidPackageName(result)) {
                printf("Invalid package name\n");
                result = null;
            }
        }

        return result;
    }

    //~ Static fields/initializers ...........................................................................

    private static final String WORD_BOUNDARY = "\\b";
}
