
// ...........................................................................................................
// Copyright (c) 1993, 2009, Oracle and/or its affiliates. All rights reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Oracle Corp.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// Last changed on 2009-04-27 14:58:12 (-0300), by: emilio. $Revision$
// ...........................................................................................................

package apb.index;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import apb.Environment;
import apb.ProjectElementHelper;
import org.jetbrains.annotations.NotNull;
// User: emilio
// Date: Apr 25, 2009
// Time: 11:14:57 AM

class ModulesInfo
    implements Serializable
{
    //~ Instance fields ......................................................................................

    private final long lastScanTime;

    @NotNull private final Map<String, ModuleInfo> modules;

    @NotNull private final File path;

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

    void loadModulesInfo(Environment env, List<File> files)
    {
        for (File file : files) {
            ProjectElementHelper element = loadElement(env, file);

            if (element != null) {
                ModuleInfo info = new ModuleInfo(element);
                modules.put(info.getName(), info);
            }
        }
        env.resetJavac();
    }

    void establishPath()
    {
        for (ModuleInfo moduleInfo : modules.values()) {
            moduleInfo.establishPath(path);
        }
    }

    private ProjectElementHelper loadElement(Environment env, File file)
    {
        try {
            return env.constructProjectElement(file);
        }
        catch (Exception e) {
            return null;
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static final long serialVersionUID = -421322822182064725L;
}
