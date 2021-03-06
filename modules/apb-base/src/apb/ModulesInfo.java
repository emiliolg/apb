

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


package apb;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jetbrains.annotations.NotNull;
// User: emilio
// Date: Apr 25, 2009
// Time: 11:14:57 AM

class ModulesInfo
    implements Serializable
{
    //~ Instance fields ......................................................................................

    @NotNull private final File path;

    private final long lastScanTime;

    @NotNull private final Map<String, ModuleInfo> modules;

    //~ Constructors .........................................................................................

    ModulesInfo(File dir, long lastModified)
    {
        path = dir;
        lastScanTime = lastModified;
        modules = new TreeMap<String, ModuleInfo>();
    }

    //~ Methods ..............................................................................................

    @NotNull Collection<ModuleInfo> getModules()
    {
        return modules.values();
    }

    long getLastScanTime()
    {
        return lastScanTime;
    }

    @NotNull File getPath()
    {
        return path;
    }

    void loadModulesInfo(Environment e, ProjectBuilder pb, List<File> files)
    {
        for (File file : files) {
            ProjectElementHelper element = pb.constructProjectElement(e, path, file);

            if (element != null) {
                ModuleInfo info = new ModuleInfo(element);
                modules.put(info.getName(), info);
            }
        }
    }

    void establishPath()
    {
        for (ModuleInfo moduleInfo : modules.values()) {
            moduleInfo.establishPath(path);
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static final long serialVersionUID = -421322822182064725L;
}
