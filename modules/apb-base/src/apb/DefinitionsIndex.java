

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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static apb.Constants.*;

import static apb.utils.CollectionUtils.stringToList;
import static apb.utils.FileUtils.normalizeFile;
import static apb.utils.FileUtils.uptodate;
import static apb.utils.FileUtils.validateDir;

/**
 * This class manages an index of Definitions for Module/Project data
 * So it can be used for command completion and tools
 * The index is cached in a serialized file under $HOME/.apb/.definitions.idx
 * to avoid construction every time.
 * @exclude
 */
public class DefinitionsIndex
    implements Iterable<ModuleInfo>
{
    //~ Instance fields ......................................................................................

    /**
     * The list of Modules
     */
    @NotNull private final Collection<ModuleInfo> modules;

    //~ Constructors .........................................................................................

    public DefinitionsIndex(@NotNull Environment e, Set<File> projectPath)
    {
        boolean b = Apb.failOnAbsentProperty();
        Apb.setFailOnAbsentProperty(false);
        Loader loader = new Loader(e, projectPath);
        modules = loader.buildModulesList();
        Apb.setFailOnAbsentProperty(b);
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

    //~ Inner Classes ........................................................................................

    static class Loader
    {
        @NotNull private final Environment  e;
        @NotNull private final File         cacheFile;
        @Nullable private final PrintStream debugFile;
        @NotNull private final Set<String>  excludeDirs;
        @NotNull private final Set<File>    projectPath;

        public Loader(Environment e, Iterable<File> path)
        {
            debugFile = initDebugFile();
            this.e = e;
            projectPath = normalize(path);
            cacheFile = definitionsCacheFile(e);
            excludeDirs = excludeDirectories(e);
            debug("About to build index for: %s\n", projectPath);
        }

        private static PrintStream initDebugFile()
        {
            try {
                if (System.getenv("APB_DEBUG_COMPLETION") != null) {
                    return new PrintStream(new File(FileUtils.getApbDir(), "debug"));
                }
            }
            catch (FileNotFoundException e1) {}

            return null;
        }

        private static File definitionsCacheFile(Environment e)
        {
            final String d = e.getProperty(DEFINITIONS_CACHE_PROPERTY, "");
            return d.isEmpty() ? new File(FileUtils.getApbDir(), DEFINITIONS_CACHE) : new File(d);
        }

        private static Set<String> excludeDirectories(Environment e)
        {
            final Set<String> excludeDirs =
                new HashSet<String>(stringToList(e.getProperty(PROJECT_PATH_EXCLUDE_PROPERTY, ""), ","));
            excludeDirs.addAll(FileUtils.DEFAULT_DIR_EXCLUDES);
            return excludeDirs;
        }

        private static void storeEntries(Map<File, ModulesInfo> indexByPath,
                                         @NotNull final File    definitionsCacheFile)
        {
            try {
                final File dir = definitionsCacheFile.getParentFile();

                String msg = validateDir(dir);

                if (!msg.isEmpty()) {
                    throw new IOException(msg);
                }

                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(definitionsCacheFile));

                oos.writeInt(indexByPath.size());

                for (ModulesInfo item : indexByPath.values()) {
                    oos.writeObject(item);
                }

                oos.close();
            }
            catch (IOException ioe) {
                throw new BuildException("Cannot write definitions cache. Cause: " + ioe.getMessage());
            }
        }

        private Set<File> normalize(Iterable<File> path)
        {
            Set<File> result = new LinkedHashSet<File>();

            for (File file : path) {
                if (file.isDirectory()) {
                    result.add(normalizeFile(file.getAbsoluteFile()));
                }
            }

            return result;
        }

        private List<File> listDefinitionFiles(File pdir)
        {
            long       ts = System.currentTimeMillis();
            List<File> files = new ArrayList<File>();
            addFiles(pdir, files);
            debug("Dir: %s, Scanned in %d ms\n", pdir, System.currentTimeMillis() - ts);
            return files;
        }

        private void debug(final String msg, Object... args)
        {
            if (debugFile != null) {
                debugFile.printf(msg, args);
                debugFile.flush();
            }
        }

        private void addFiles(File dir, final List<File> files)
        {
            dir.listFiles(new FileFilter() {
                    public boolean accept(File pathname)
                    {
                        if (pathname.isDirectory()) {
                            if (isNotExcluded(pathname.getName())) {
                                addFiles(pathname, files);
                            }
                        }
                        else if (pathname.getName().endsWith(".java")) {
                            files.add(pathname);
                        }

                        return true;
                    }
                });
        }

        private boolean isNotExcluded(String dirName)
        {
            return dirName == null || !excludeDirs.contains(dirName);
        }

        private Collection<ModuleInfo> buildModulesList()
        {
            Collection<ModuleInfo> result = new TreeSet<ModuleInfo>();

            for (ModulesInfo modulesInfo : loadCache().values()) {
                if (projectPath.contains(modulesInfo.getPath())) {
                    debug("Path %s. Modules: %s\n", modulesInfo.getPath(), modulesInfo.getModules());

                    for (ModuleInfo module : modulesInfo.getModules()) {
                        result.add(module);
                    }
                }
            }

            return result;
        }

        private Map<File, ModulesInfo> loadCache()
        {
            final Map<File, ModulesInfo> result = loadEntries();

            boolean mustStore = false;

            for (File pdir : projectPath) {
                ModulesInfo info = result.get(pdir);

                List<File> files = listDefinitionFiles(pdir);

                if (info == null || !uptodate(files, info.getLastScanTime())) {
                    mustStore = true;
                    info = rebuild(pdir, files);
                    result.put(pdir, info);
                }

                info.establishPath();
            }

            if (mustStore) {
                storeEntries(result, cacheFile);
            }

            return result;
        }

        private ModulesInfo rebuild(File pdir, List<File> files)
        {
            long           ts = System.currentTimeMillis();
            ModulesInfo    info = new ModulesInfo(pdir, System.currentTimeMillis());
            ProjectBuilder pb = new ProjectBuilder(e, projectPath);
            info.loadModulesInfo(e, pb, files);
            debug("Dir: %s, Info build in %d ms\n", pdir, System.currentTimeMillis() - ts);
            return info;
        }

        private Map<File, ModulesInfo> loadEntries()
        {
            Map<File, ModulesInfo> result = new TreeMap<File, ModulesInfo>();

            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cacheFile));
                int               size = ois.readInt();

                for (int i = 0; i < size; i++) {
                    Object item = ois.readObject();

                    if (item instanceof ModulesInfo) {
                        ModulesInfo info = (ModulesInfo) item;
                        result.put(info.getPath(), info);
                    }
                }

                ois.close();
            }
            catch (IOException ignore) {}
            catch (ClassNotFoundException ignore) {}

            return result;
        }
    }
}
