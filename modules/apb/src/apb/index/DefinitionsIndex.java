

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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import apb.BuildException;
import apb.Environment;
import apb.ProjectBuilder;

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

    @NotNull private final Collection<ModuleInfo> modules;
    private final File                            definitionsDir;
    @NotNull private final Map<File, ModulesInfo> mdir;

    @NotNull private final String[] excludeDirs;

    //~ Constructors .........................................................................................

    public DefinitionsIndex(@NotNull Environment e, Set<File> projectPath)
    {
        modules = new TreeSet<ModuleInfo>();
        mdir = new TreeMap<File, ModulesInfo>();
        String excludes = e.getProperty("project.path.exclude", DEFAULT_EXCLUDES);

        if (!excludes.equals(DEFAULT_EXCLUDES)) {
            excludes += "," + DEFAULT_EXCLUDES;
        }

        excludeDirs = excludes.split(",");
        final String d = e.getProperty("definitions.dir", "");
        definitionsDir = d.isEmpty() ? new File(FileUtils.getApbDir(), DEFINITIONS_IDX) : new File(d);
        loadCache(e, projectPath);
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
        for (ModuleInfo info : modules) {
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

        for (ModuleInfo moduleInfo : modules) {
            String m = moduleInfo.getName();
            int    lastDot = m.lastIndexOf('.');
            m += ".";

            if (m.startsWith(name) || lastDot != -1 && m.substring(lastDot + 1).startsWith(name)) {
                result.add(moduleInfo);
            }
        }

        return result;
    }

    @Override public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append("[");

        for (ModuleInfo info : this) {
            if (result.length() > 1) {
                result.append(", ");
            }

            result.append(String.valueOf(info));
        }

        result.append("]");
        return result.toString();
    }

    private void storeEntries()
    {
        try {
            File       modulesDir = getDefinitionsDir();
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
            throw new BuildException("Cannot write definitions cache. Cause: " + e.getMessage());
        }
    }

    private void loadCache(Environment e, final Set<File> projectPath)
    {
        loadEntries();

        boolean mustStore = false;

        for (File pdir : projectPath) {
            ModulesInfo info = mdir.get(pdir);
            List<File>  files = new ArrayList<File>();
            listDefinitionFiles(pdir, files);
            final long lastModified = FileUtils.lastModified(files);

            if (info == null || lastModified > info.getLastScanTime()) {
                if (!mustStore) {
                    e.logVerbose("Regenerating definitions cache");
                }

                mustStore = true;
                info = new ModulesInfo(pdir, lastModified);
                ProjectBuilder pb = new ProjectBuilder(e, projectPath);
                info.loadModulesInfo(e, pb, files);
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
                public boolean accept(File pathname)
                {
                    if (pathname.isDirectory()) {
                        if (isNotExcluded(pathname.getName())) {
                            listDefinitionFiles(pathname, files);
                        }
                    }
                    else if (pathname.getName().endsWith(".java")) {
                        files.add(pathname);
                    }

                    return true;
                }

                private boolean isNotExcluded(String dirName)
                {
                    if (dirName != null) {
                        for (String excludeDir : excludeDirs) {
                            if (excludeDir.equals(dirName)) {
                                return false;
                            }
                        }
                    }

                    return true;
                }
            });
    }

    @NotNull private File getDefinitionsDir()
    {
        return definitionsDir;
    }

    private void loadEntries()
    {
        File modulesDir = getDefinitionsDir();

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

    //~ Static fields/initializers ...........................................................................

    private static final String DEFAULT_EXCLUDES = ".svn";

    public static final String DEFINITIONS_IDX = ".definitions.idx";
}
