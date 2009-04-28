
// ...........................................................................................................
// Copyright (c) 1993, 2009, Oracle and/or its affiliates. All rights reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Oracle Corp.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// Last changed on 2009-04-27 17:48:17 (-0300), by: emilio. $Revision$
// ...........................................................................................................

package apb.index;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import apb.Environment;
import apb.utils.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class manages an index of Definitions for Module/Project data
 * So it can be used for command completion and tools
 * The index is cached in a serialized file under $HOME/.apb/.definitions.idx
 * to avoid construction every time.
 */
public class DefinitionsIndex
    implements Iterable<ModuleInfo>
{
    //~ Instance fields ......................................................................................

    @NotNull private final Environment            env;
    private String[]                              excludeDirs;
    @NotNull private final Map<File, ModulesInfo> mdir;
    @NotNull private final Collection<ModuleInfo> modules;

    //~ Constructors .........................................................................................

    public DefinitionsIndex(@NotNull Environment e)
    {
        env = e;
        modules = new TreeSet<ModuleInfo>();
        mdir = new TreeMap<File, ModulesInfo>();
        String excludes = env.getProperty("project.path.exclude",DEFAULT_EXCLUDES);
        if (!excludes.equals(DEFAULT_EXCLUDES))
            excludes += "," + DEFAULT_EXCLUDES;
        excludeDirs = excludes.split(",");
        loadCache();
    }

    //~ Methods ..............................................................................................

    public Iterator<ModuleInfo> iterator()
    {
        return modules.iterator();
    }

    @Nullable public ModuleInfo searchCurrentDirectory()
    {
        return searchByDirectory(FileUtils.getCurrentWorkingDirectory());
    }

    @Nullable public ModuleInfo searchByDirectory(@NotNull String dir)
    {
        for (ModuleInfo info : this) {
            String p = info.getContentDir().getPath();

            if (dir.startsWith(p)) {
                return info;
            }
        }

        return null;
    }

    @NotNull public List<ModuleInfo> findAllByName(@NotNull String name)
    {
        List<ModuleInfo> result = new ArrayList<ModuleInfo>();
        for (ModuleInfo moduleInfo : env.getDefinitionsIndex()) {
            final String m = moduleInfo.getName();
            int          lastDot = m.lastIndexOf('.');

            if (m.startsWith(name) || lastDot != -1 && m.substring(lastDot + 1).startsWith(name)) {
                result.add(moduleInfo);
            }
        }
        return result;
    }

    private void storeEntries()
    {
        try {
            File       modulesDir = getModulesDirFile();
            final File dir = modulesDir.getParentFile();

            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("Cannot create directory: " + dir);
            }

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modulesDir));

            oos.writeInt(mdir.size());

            for (ModulesInfo item : mdir.values()) {
                oos.writeObject(item);
            }

            oos.close();
        }
        catch (IOException e) {
            env.logSevere("Cannot write definitions cache. Cause: %s\n", e.getMessage());
        }
    }

    private void loadCache()
    {
        loadEntries();

        boolean mustStore = false;

        for (File pdir : env.getProjectPath()) {
            ModulesInfo info = mdir.get(pdir);
            List<File>  files = new ArrayList<File>();
            listDefinitionFiles(pdir, files);
            final long  lastModified = FileUtils.lastModified(files);

            if (info == null || lastModified > info.getLastScanTime()) {
                if (!mustStore) {
                    env.logVerbose("Regenerating definitions cache");
                }

                mustStore = true;
                info = new ModulesInfo(pdir, lastModified);
                info.loadModulesInfo(env, files);
                mdir.put(pdir, info);
            }

            info.establishPath();
        }

        if (mustStore) {
            storeEntries();
        }

        for (ModulesInfo modulesInfo : mdir.values()) {
            for (ModuleInfo module : modulesInfo.getModules()) {
                modules.add(module);
            }
        }
    }

    private void listDefinitionFiles(File dir, final List<File> files)
    {
        dir.listFiles(new FileFilter() {
                public boolean accept(File file)
                {
                    if (file.isDirectory()) {
                        if (isNotExcluded(file.getName()))
                            listDefinitionFiles(file, files);
                    }
                    else if (file.getName().endsWith(".java")) {
                        files.add(file);
                    }

                    return true;
                }
            });
    }

    private boolean isNotExcluded(String dir)
    {
        if (dir != null) {
            for (String excludeDir : excludeDirs) {
                if (excludeDir.equals(dir)) {
                    return false;
                }
            }
        }

        return true;
    }

    @NotNull private File getModulesDirFile()
    {
        return new File(env.getApbDir(), DEFINITIONS_IDX);
    }

    private void loadEntries()
    {
        File modulesDir = getModulesDirFile();

        if (modulesDir.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modulesDir));
                int               size = ois.readInt();

                for (int i = 0; i < size; i++) {
                    Object item = ois.readObject();

                    if (item instanceof ModulesInfo) {
                        ModulesInfo info = (ModulesInfo) item;
                        mdir.put(info.getPath(), info);
                    }
                }

                ois.close();
            }
            catch (IOException ignore) {}
            catch (ClassNotFoundException ignore) {}
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static final String DEFAULT_EXCLUDES = ".svn";

    public static final String DEFINITIONS_IDX = ".definitions.idx";
}
