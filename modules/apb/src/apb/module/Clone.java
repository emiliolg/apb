

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

import apb.Command;
import apb.ProjectElementHelper;

import apb.metadata.ProjectElement;

import static apb.utils.Console.getString;
import static apb.utils.Console.printf;
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
        super("module", "clone", "Generate a new Module based on a specified one.", false);
    }

    //~ Methods ..............................................................................................

    public void invoke(ProjectElement projectElement)
    {
        ProjectElementHelper helper = projectElement.getHelper();

        File   dir = helper.getProjectDirectory();
        File   newDir = getNewProjectDir(dir);
        String newModule = getString("New module name", "NewModule");
        System.out.println("newDir = " + newDir);
        System.out.println("newModule = " + newModule);
    }

    private File getNewProjectDir(File dir)
    {
        File    result;
        boolean ok;

        do {
            result = new File(getString("Project directory", dir.getPath()));

            if (!(ok = result.isDirectory())) {
                if (result.exists()) {
                    printf("Not a directory: '%s'.\n", result.getPath());
                }
                else if (!(ok = result.mkdirs())) {
                    printf("Cannot create directory: '%s'.\n", result.getPath());
                }
            }
        }
        while (!ok);

        return result;
    }
}
