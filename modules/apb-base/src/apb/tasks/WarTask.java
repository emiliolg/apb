

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
import java.util.ArrayList;
import java.util.List;

import apb.Apb;

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;

import static java.util.Arrays.asList;

import static apb.tasks.CoreTasks.jar;

import static apb.utils.FileUtils.validateDirectory;
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
     * The directory where the web application will be built.
     * For example: target/projet-1.0/
     */
    private File webAppBuildDir;

    /**
     * The source web application directory.
     * For example src/main/webapp
     */
    @NotNull private final FileSet webAppDir;

    /**
     * The classes to package
     * For example: target/classes
     */
    private List<FileSet> classesToPackage;

    /**
     * Jars to package
     */
    private List<File> jarsToPackage;

    //~ Constructors .........................................................................................

    private WarTask(@NotNull File warFile, @NotNull FileSet webAppDir, @NotNull File webAppBuildDir)
    {
        validateDirectory(webAppBuildDir);
        this.warFile = warFile;
        this.webAppDir = webAppDir;
        this.webAppBuildDir = webAppBuildDir;
        classesToPackage = new ArrayList<FileSet>();
        jarsToPackage = new ArrayList<File>();
    }

    //~ Methods ..............................................................................................

    public void execute()
    {
        buildWebApp();
        jar(warFile).from(webAppBuildDir).execute();
    }

    public void buildWebApp()
    {
        performPackaging();

        performPostPackaging();
    }

    public WarTask includeJars(File... file)
    {
        jarsToPackage.addAll(asList(file));
        return this;
    }

    public WarTask includeClasses(FileSet... sets)
    {
        return includeClasses(asList(sets));
    }

    public WarTask includeClasses(List<FileSet> sets)
    {
        classesToPackage.addAll(sets);
        return this;
    }

    protected void copyExtraWebResources()
    {
        // This can be used to copy additional resources to the web application....
    }

    protected void copyWebApp()
    {
        final File dir = webAppDir.getDir();

        if (!dir.exists()) {
            env.logWarning("Source web application directory '%s' does not exist\n", dir.getAbsolutePath());
        }
        else if (!FileUtils.sameFile(dir, webAppBuildDir)) {
            CoreTasks.copy(webAppDir).to(webAppBuildDir).execute();
        }
    }

    private void performPackaging()
    {
        env.logInfo("Processing war project\n");

        copyExtraWebResources();

        copyWebApp();

        handleDeploymentDescriptors();

        copyClasses();
        copyJars();
    }

    private void handleDeploymentDescriptors()
    {
        // Here we can copy web.xml & context.xml files if they are not in the default location
        // Also filter them if requested
        File webInfDir = new File(webAppBuildDir, WEB_INF_PATH);
        File metaInfDir = new File(webAppBuildDir, META_INF_PATH);
        validateDirectory(webInfDir);
        validateDirectory(metaInfDir);
    }

    private void copyClasses()
    {
        File classesDir = new File(webAppBuildDir, CLASSES_PATH);

        for (FileSet fileSet : classesToPackage) {
            File dir = fileSet.getDir();

            if (dir.exists() && !FileUtils.sameFile(dir, classesDir)) {
                validateDirectory(classesDir);
                CoreTasks.copy(fileSet).to(classesDir).execute();
            }
        }
    }

    private void copyJars()
    {
        File jarsDir = new File(webAppBuildDir, LIB_PATH);

        for (File jar : jarsToPackage) {
            if (jar.exists() && !FileUtils.sameFile(jar.getParentFile(), jarsDir)) {
                validateDirectory(jarsDir);
                CoreTasks.copy(jar).to(jarsDir).execute();
            }
        }
    }

    private void performPostPackaging() {}

    //~ Static fields/initializers ...........................................................................

    private static final String WEB_INF_PATH = "WEB-INF";

    private static final String META_INF_PATH = "META-INF";

    private static final String CLASSES_PATH = "WEB-INF/classes/";
    public static final String  LIB_PATH = "WEB-INF/lib/";

    //~ Inner Classes ........................................................................................

    public static class Builder
    {
        @NotNull private final File warFile;
        @NotNull private FileSet    webAppDir;

        /**
         * Private constructor called from factory methods
         *
         * @param warFile The war file to be created
         */

        Builder(@NotNull File warFile)
        {
            this.warFile = warFile;
        }

        /**
         * @param webAppTargetDir The directory where the web application will be built
         */
        public WarTask usingBuildDirectory(@NotNull String webAppTargetDir)
        {
            return usingBuildDirectory(Apb.getEnv().fileFromBase(webAppTargetDir));
        }

        /**
         * @param webAppTargetDir The directory where the web application will be built
         */
        public WarTask usingBuildDirectory(@NotNull File webAppTargetDir)
        {
            return new WarTask(warFile, webAppDir, webAppTargetDir);
        }

        public Builder from(@NotNull String webAppDirectory)
        {
            return from(FileSet.fromDir(webAppDirectory));
        }

        public Builder from(@NotNull FileSet webAppDirectory)
        {
            webAppDir = webAppDirectory;
            return this;
        }
    }
}
