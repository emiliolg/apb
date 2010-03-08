

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


package apb.tasks;

import java.io.File;

import apb.Apb;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Sep 9, 2008
// Time: 3:01:10 PM

//
public class WarTask
    extends Task
{
    //~ Instance fields ......................................................................................

    private final File warFile;

    /**
     * The directory where the webapp is built.
     *
     * @parameter expression="${project.build.directory}/${project.build.finalName}"
     */
    private File webAppDir;

    //~ Constructors .........................................................................................

    private WarTask(@NotNull File warFile, @NotNull File webAppDir)
    {
        this.warFile = warFile;
        this.webAppDir = webAppDir;
    }

    //~ Methods ..............................................................................................

    public void execute()
    {
        CoreTasks.jar(warFile).from(webAppDir).execute();
    }

    //~ Inner Classes ........................................................................................

    public static class Builder
    {
        @NotNull private final File warFile;

        /**
         * Private constructor called from factory methods
         * @param warFile The warfile to be created
         */

        Builder(@NotNull File warFile)
        {
            this.warFile = warFile;
        }

        /**
        * @param webAppDir The directory where the webapp will be built
        */
        public WarTask fromWebApp(@NotNull File webAppDir)
        {
            return new WarTask(warFile, webAppDir);
        }

        /**
        * @param webAppDir The directory where the webapp will be built
        */
        public WarTask fromWebApp(@NotNull String webAppDir)
        {
            return fromWebApp(Apb.getEnv().fileFromBase(webAppDir));
        }
    }
}
