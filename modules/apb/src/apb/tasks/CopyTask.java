
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import apb.Environment;
import apb.ModuleHelper;

import apb.metadata.ResourcesInfo;

import apb.utils.DirectoryScanner;
import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;

import static apb.utils.StringUtils.isEmpty;
//
// User: emilio
// Date: Oct 1, 2008
// Time: 4:58:01 PM

//
public class CopyTask
    extends Task
{
    //~ Instance fields ......................................................................................

    @NotNull private Set<String> doNotFilter;

    @NotNull private String encoding;

    @NotNull private List<String> excludes;
    private boolean               filtering;
    @NotNull private List<String> includes;
    @NotNull private final File   outputDir;
    @NotNull private final File   sourceDir;

    //~ Constructors .........................................................................................

    public CopyTask(@NotNull Environment env, @NotNull File sourceDir, @NotNull File outputDir)
    {
        super(env);
        this.sourceDir = sourceDir;
        this.outputDir = outputDir;
        includes = Collections.emptyList();
        excludes = Collections.emptyList();
        encoding = ResourcesInfo.DEFAULT_ENCODING;
        setDoNotFilter(ResourcesInfo.DEFAULT_DO_NOT_FILTER);
    }

    //~ Methods ..............................................................................................

    /**
     * Utility method
     */
    public static void copy(@NotNull Environment e, @NotNull String dirFrom, @NotNull String dirTo,
                            String... includes)
    {
        CopyTask copy = new CopyTask(e, e.fileFromBase(dirFrom), e.fileFromBase(dirTo));

        copy.setIncludes(Arrays.asList(includes));
        copy.execute();
    }

    public static void copyResources(Environment env)
    {
        ModuleHelper  helper = env.getModuleHelper();
        ResourcesInfo resources = helper.getResourcesInfo();

        CopyTask copy = new CopyTask(env, env.fileFromBase(resources.dir), helper.getOutput());

        if (!isEmpty(resources.encoding)) {
            copy.setEncoding(resources.encoding);
        }

        copy.setDoNotFilter(resources.doNotFilter());
        copy.setFiltering(resources.filtering);
        copy.setIncludes(resources.includes());
        copy.setExcludes(resources.excludes());
        copy.execute();
    }

    public void setDoNotFilter(@NotNull List<String> patterns)
    {
        doNotFilter = new HashSet<String>(patterns);
    }

    public void setFiltering(boolean b)
    {
        filtering = b;
    }

    public void execute()
    {
        if (includes.isEmpty()) {
            // Set defaults
            includes = Arrays.asList("**/**");
            excludes = Arrays.asList("**/*.java");
        }

        if (!sourceDir.exists()) {
            env.logInfo("Skip non existing resourceDirectory: %s\n", sourceDir.getPath());
            return;
        }

        if (!outputDir.exists() && !outputDir.mkdirs()) {
            env.handle("Cannot create resource output directory: " + outputDir);
            return;
        }

        if (env.isVerbose()) {
            env.logVerbose("Copying resources from: %s\n", sourceDir);
            env.logVerbose("                    to: %s\n", outputDir);
            env.logVerbose("              includes: %s\n", includes);

            if (!excludes.isEmpty()) {
                env.logVerbose("              excludes: %s\n", excludes);
            }
        }

        Map<File, File> includedFiles = findFiles(sourceDir, outputDir);

        if (includedFiles.isEmpty()) {
            return;
        }

        env.logInfo("Copying %2d resource%s\nto %s\n", includedFiles.size(),
                    includedFiles.size() > 1 ? "s" : "", outputDir);

        List<FileUtils.Filter> filters = new ArrayList<FileUtils.Filter>();
        filters.add(new FileUtils.Filter() {
                public String filter(String str)
                {
                    return env.expand(str);
                }
            });

        for (Map.Entry<File, File> entry : includedFiles.entrySet()) {
            File source = entry.getKey();
            File to = entry.getValue();

            try {
                if (filtering && !doNotFilter.contains(FileUtils.extension(source).toLowerCase())) {
                    env.logVerbose("Filtering %s\n", source);
                    env.logVerbose("       to %s\n", to);
                    FileUtils.copyFileFiltering(source, to, encoding, filters);
                }
                else {
                    env.logVerbose("Copy %s\n", source);
                    env.logVerbose("  to %s\n", to);
                    FileUtils.copyFile(source, to);
                }
            }
            catch (IOException e) {
                env.handle(e);
            }
        }
    }

    public void setEncoding(@NotNull final String e)
    {
        encoding = e;
    }

    public void setIncludes(@NotNull List<String> patterns)
    {
        includes = patterns;
    }

    public void setExcludes(@NotNull List<String> patterns)
    {
        excludes = patterns;
    }

    private Map<File, File> findFiles(File resourceDirectory, File outputDirectory)
    {
        DirectoryScanner scanner = new DirectoryScanner(resourceDirectory, includes, excludes);

        try {
            scanner.scan();
        }
        catch (IOException e) {
            env.handle(e);
        }

        Map<File, File> files = new LinkedHashMap<File, File>();

        for (String name : scanner.getIncludedFiles()) {
            File from = new File(resourceDirectory, name);
            File to = new File(outputDirectory, name);

            if (env.forceBuild() || !to.exists() || from.lastModified() > to.lastModified()) {
                files.put(from, to);
            }
        }

        return files;
    }
}
