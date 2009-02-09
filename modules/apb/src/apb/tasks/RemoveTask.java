
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

package apb.tasks;

import java.io.File;

import apb.Environment;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Oct 16, 2008
// Time: 4:42:50 PM

//
public class RemoveTask
    extends Task
{
    //~ Instance fields ......................................................................................

    private File file;

    //~ Constructors .........................................................................................

    public RemoveTask(@NotNull Environment env, @NotNull File file)
    {
        super(env);
        this.file = file;
    }

    //~ Methods ..............................................................................................

    public static void remove(@NotNull Environment env, File file)
    {
        if (file.exists()) {
            RemoveTask t = new RemoveTask(env, file);
            t.execute();
        }
    }

    public void execute()
    {
        if (file.exists()) {
            String type = file.isDirectory() ? "directory" : "file";
            env.logInfo("Deleting %s %s\n", type, file.getAbsolutePath());

            boolean ok = file.isDirectory() ? doRemoveDir(file) : file.delete();

            if (!ok) {
                env.handle("Unable to delete " + type + " " + file.getAbsolutePath());
            }
        }
    }

    private boolean doRemoveDir(File d)
    {
        String[] list = d.list();

        if (list == null) {
            list = new String[0];
        }

        for (String s : list) {
            File f = new File(d, s);

            if (f.isDirectory()) {
                doRemoveDir(f);
            }
            else {
                env.logVerbose("Deleting: %s\n", f.getAbsolutePath());

                if (!f.delete()) {
                    env.handle("Unable to delete file " + f.getAbsolutePath());
                }
            }
        }

        return d.delete();
    }
}
