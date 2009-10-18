

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
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import apb.Apb;
import apb.Proxy;

import apb.metadata.JavadocInfo;
import apb.metadata.ResourcesInfo;

import apb.processors.ExcludeDoclet;

import apb.utils.FileUtils;
import apb.utils.StringUtils;

import org.jetbrains.annotations.NotNull;

import static apb.tasks.CoreTasks.*;

import static apb.utils.FileUtils.makePath;
import static apb.utils.StringUtils.nChars;
//
// User: emilio
// Date: Oct 27, 2008
// Time: 12:35:05 PM

//
public class JavadocTask
    extends Task
{
    //~ Instance fields ......................................................................................

    //
    @NotNull private final List<String> additionalOptions;
    @NotNull private final List<String> args;

    private boolean author;

    @NotNull private String bottom = "";
    @NotNull private String classPath = "";
    private boolean         deprecated;
    private boolean         deprecatedList;

    @NotNull private String                        doclet = "";
    @NotNull private String                        doctitle = "";
    @NotNull private String                        encoding = "";
    private boolean                                excludeDoclet;
    @NotNull private final List<String>            excludes;
    @NotNull private String                        footer = "";
    @NotNull private final List<JavadocInfo.Group> groups;
    @NotNull private String                        header = "";
    private boolean                                help;
    private boolean                                index;
    @NotNull private final List<String>            links;
    private boolean                                linkSource;

    private Locale locale = null;

    /**
     * Max memory in megabytes
     */
    private int                                          maxMemory = 256;
    @NotNull private final List<JavadocInfo.OfflineLink> offlineLinks;

    private File                        outputDirectory;
    @NotNull private String             overview = "";
    @NotNull private final List<String> packages;
    private boolean                     since;
    @NotNull private final List<File>   sourcesDir;
    private boolean                     splitindex;
    private boolean                     tree;
    private boolean                     use;
    private boolean                     version;

    @NotNull private JavadocInfo.Visibility visibility;
    @NotNull private String                 windowTitle = "";

    //~ Constructors .........................................................................................

    public JavadocTask(@NotNull List<File> sourceDirs, @NotNull File outputDir)
    {
        super(Apb.getEnv());
        sourcesDir = sourceDirs;
        outputDirectory = outputDir;
        encoding = ResourcesInfo.DEFAULT_ENCODING;
        visibility = JavadocInfo.Visibility.PROTECTED;

        additionalOptions = new ArrayList<String>();
        args = new ArrayList<String>();
        links = new ArrayList<String>();
        offlineLinks = new ArrayList<JavadocInfo.OfflineLink>();
        groups = new ArrayList<JavadocInfo.Group>();
        excludes = new ArrayList<String>();
        packages = new ArrayList<String>();
    }

    //~ Methods ..............................................................................................

    public JavadocTask maxMemory(int memory)
    {
        maxMemory = memory;
        return this;
    }

    public JavadocTask withEncoding(@NotNull String value)
    {
        encoding = value;
        return this;
    }

    public JavadocTask withLocale(@NotNull Locale l)
    {
        locale = l;
        return this;
    }

    public JavadocTask withOverview(@NotNull String overviewFile)
    {
        overview = "";

        if (!overview.isEmpty()) {
            File f = env.fileFromBase(overviewFile);

            if (!f.exists()) {
                env.handle("Not existing overview file: " + f);
            }
            else {
                overview = f.getPath();
            }
        }

        return this;
    }

    public JavadocTask withVisibility(@NotNull JavadocInfo.Visibility level)
    {
        visibility = level;
        return this;
    }

    public JavadocTask useExcludeDoclet(boolean b)
    {
        excludeDoclet = b;
        return this;
    }

    public JavadocTask including(@NotNull List<String> packageList)
    {
        packages.addAll(packageList);
        return this;
    }

    public JavadocTask excluding(@NotNull List<String> list)
    {
        excludes.addAll(list);
        return this;
    }

    public JavadocTask additionalOptions(List<String> list)
    {
        additionalOptions.addAll(list);
        return this;
    }

    public JavadocTask usingDoclet(@NotNull String s)
    {
        doclet = s;
        return this;
    }

    public JavadocTask withTitle(@NotNull String s)
    {
        doctitle = env.expand(s);
        return this;
    }

    public JavadocTask withHeader(@NotNull String s)
    {
        header = env.expand(s);
        return this;
    }

    public JavadocTask withFooter(@NotNull String s)
    {
        footer = env.expand(s);
        return this;
    }

    public JavadocTask withBottom(@NotNull String s)
    {
        bottom = env.expand(s);
        return this;
    }

    public JavadocTask withWindowTitle(@NotNull String s)
    {
        windowTitle = env.expand(s);
        return this;
    }

    public JavadocTask withLinks(@NotNull List<String> list)
    {
        links.addAll(list);
        return this;
    }

    public JavadocTask withOfflineLinks(@NotNull List<JavadocInfo.OfflineLink> linkList)
    {
        offlineLinks.addAll(linkList);
        return this;
    }

    public JavadocTask withGroups(@NotNull List<JavadocInfo.Group> list)
    {
        groups.addAll(list);
        return this;
    }

    public JavadocTask withClassPath(List<File> files)
    {
        classPath = makePath(files);
        return this;
    }

    public JavadocTask includeAuthorInfo(boolean b)
    {
        author = b;
        return this;
    }

    public JavadocTask includeDeprecatedInfo(boolean b)
    {
        deprecated = b;
        return this;
    }

    public JavadocTask includeVersionInfo(boolean b)
    {
        version = b;
        return this;
    }

    public JavadocTask includeSinceInfo(boolean b)
    {
        since = b;
        return this;
    }

    public JavadocTask includeHelpLinks(boolean b)
    {
        help = b;
        return this;
    }

    public JavadocTask generateIndex(boolean b)
    {
        index = b;
        return this;
    }

    public JavadocTask generateHtmlSource(boolean b)
    {
        linkSource = b;
        return this;
    }

    public JavadocTask generateClassHierarchy(boolean b)
    {
        tree = b;
        return this;
    }

    public JavadocTask generateDeprecatedList(boolean b)
    {
        deprecatedList = b;
        return this;
    }

    public JavadocTask splitIndexPerLetter(boolean b)
    {
        splitindex = b;
        return this;
    }

    public JavadocTask createUsePages(boolean b)
    {
        use = b;
        return this;
    }

    public void execute()
    {
        List<File> sources = new ArrayList<File>();

        for (File file : sourcesDir) {
            if (file.exists()) {
                sources.add(file);
            }
            else {
                env.logInfo("Skipping empty directory: %s\n", file.getPath());
            }
        }

        if (!sources.isEmpty()) {
            final List<String> subpackages = packages.isEmpty() ? collectPackages() : packages;

            if (!subpackages.isEmpty()) {
                FileUtils.validateDirectory(outputDirectory);

                if (!uptodate(sources)) {
                    run(sources, subpackages);
                }
            }
        }
    }

    private boolean uptodate(List<File> sources)
    {
        if (env.forceBuild()) {
            return false;
        }

        long target = new File(outputDirectory, "index.html").lastModified();

        if (target == 0) {
            return false;
        }

        for (File source : sources) {
            if (!FileUtils.uptodate(source, ".java", target)) {
                return false;
            }
        }

        return true;
    }

    private void run(final List<File> sources, final List<String> subpackages)
    {
        for (int i = 0; i < sources.size(); i++) {
            File         file = sources.get(i);
            final String msg = (i == 0 ? GENERATING_DOC : nChars(GENERATING_DOC.length(), ' ')) + "%s\n";
            env.logInfo(msg, file.getPath());
        }

        copyAllResources();

        addMemoryArg(args, maxMemory);
        addProxyArg(args);

        if (locale != null) {
            addArgument("-locale", locale.toString());
        }

        if (!classPath.isEmpty()) {
            addArgument("-classpath", classPath);
        }

        if (excludeDoclet) {
            addArgument("-doclet", ExcludeDoclet.class.getName());
            addArgument("-docletpath", Apb.applicationJarFile().getPath());
        }
        else if (!doclet.isEmpty()) {
            addArgument("-doclet", doclet);
        }

        addArgument("-overview", overview);

        args.add("-" + visibility.toString().toLowerCase());

        addArgument("-sourcepath", makePath(sources));

        args.add("-subpackages");
        args.add(FileUtils.makePathFromStrings(subpackages));

        if (!excludes.isEmpty()) {
            args.add("-exclude");
            args.add(FileUtils.makePathFromStrings(excludes));
        }

        if (!env.isVerbose()) {
            args.add("-quiet");
        }

        if (!additionalOptions.isEmpty()) {
            args.addAll(additionalOptions);
        }

        if (doclet.isEmpty()) {
            addStandardDocletOptions();
        }

        final String javadoc = FileUtils.findJavaExecutable("javadoc");
        exec(javadoc, args).onDir(outputDirectory.getAbsolutePath()).execute();
    }

    private List<String> collectPackages()
    {
        final List<String> result = new ArrayList<String>();

        for (File source : sourcesDir) {
            source.listFiles(new FileFilter() {
                    public boolean accept(File file)
                    {
                        if (file.isDirectory()) {
                            final String pkg = file.getName();

                            if (StringUtils.isJavaId(pkg)) {
                                result.add(pkg);
                            }
                        }

                        return false;
                    }
                });
        }

        return result;
    }

    private void copyAllResources()
    {
        InputStream is = getStream("resources/javadoc/stylesheet.css");

        if (is == null) {
            env.handle("Cannot find the default stylesheet");
        }
        else {
            try {
                FileUtils.copyFile(is, new File(outputDirectory, "stylesheet.css"));
            }
            catch (IOException e) {
                env.handle("Error copying the default stylesheet");
            }
        }

        // Copy additional resources ???

    }

    private InputStream getStream(String resource)
    {
        return getClass().getClassLoader().getResourceAsStream(resource);
    }

    private void addStandardDocletOptions()
    {
        addArgument("-d", outputDirectory.getPath());

        addArgument("-docencoding", encoding);
        addArgument("-charset", encoding);

        addArgument("-doctitle", doctitle);

        addArgument("-header", header);

        addArgument("-footer", footer);

        addArgument("-bottom", bottom);
        addArgument("-windowtitle", windowTitle);

        addArgument("-use", use);

        addArgument("-version", version);

        addArgument("-author", author);

        addArgument("-splitindex", splitindex);
        addArgument("-nodeprecated", !deprecated);
        addArgument("-nodeprecatedlist", !deprecatedList);
        addArgument("-notree", !tree);
        addArgument("-nosince", !since);
        addArgument("-noindex", !index);
        addArgument("-nohelp", !help);
        addArgument("-linksource", linkSource);

        for (String link : links) {
            addArgument("-link", link);
        }

        for (JavadocInfo.OfflineLink link : offlineLinks) {
            addArgument("-linkoffline", link.url);
            args.add(link.location);
        }

        for (JavadocInfo.Group group : groups) {
            addArgument("-group", group.title);
            args.add(FileUtils.makePathFromStrings(Arrays.asList(group.packages)));
        }
    }

    private void addArgument(String argName, String arg)
    {
        if (!arg.isEmpty()) {
            args.add(argName);
            args.add(arg);
        }
    }

    private void addArgument(String argName, boolean arg)
    {
        if (arg) {
            args.add(argName);
        }
    }

    private void addMemoryArg(List<String> cmd, int memory)
    {
        cmd.add("-J-Xmx" + memory + "m");
    }

    private void addProxyArg(List<String> cmd)
    {
        Proxy activeProxy = Proxy.getDefaultProxy(env);

        if (activeProxy != null && !activeProxy.getHost().isEmpty()) {
            String protocol = activeProxy.getProtocol().isEmpty() ? "" : activeProxy.getProtocol() + ".";
            cmd.add("-J-D" + protocol + "proxySet=true");
            cmd.add("-J-D" + protocol + "proxyHost=" + activeProxy.getHost());

            if (activeProxy.getPort() > 0) {
                cmd.add("-J-D" + protocol + "proxyPort=" + activeProxy.getPort());
            }

            if (!activeProxy.getNonProxyHosts().isEmpty()) {
                cmd.add("-J-D" + protocol + "nonProxyHosts=\"" + activeProxy.getNonProxyHosts() + "\"");
            }

            if (!activeProxy.getUsername().isEmpty()) {
                cmd.add("-J-Dhttp.proxyUser=\"" + activeProxy.getUsername() + "\"");

                if (!activeProxy.getPassword().isEmpty()) {
                    cmd.add("-J-Dhttp.proxyPassword=\"" + activeProxy.getPassword() + "\"");
                }
            }
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static final String GENERATING_DOC = "Generating documentation for: ";

    //~ Inner Classes ........................................................................................

    public static class Builder
    {
        @NotNull private final List<File> sourceDirs;

        /**
         * Private constructor called from factory methods
         * @param sourceDirs The directories containing source classes to document
         */
        Builder(@NotNull List<File> sourceDirs)
        {
            this.sourceDirs = sourceDirs;
        }

        /**
        * Specify the target(output) directory.
        * That is the directory where "html" files will be placed
        * @param target The output directory
        * @throws IllegalArgumentException if file exists and it is not a directory.
        */
        @NotNull public JavadocTask to(@NotNull String target)
        {
            return to(Apb.getEnv().fileFromBase(target));
        }

        /**
        * Specify the target(output) directory.
        * That is the directory where ".html" files will be placed
        * @param target The output directory
         * @throws IllegalArgumentException if file exists and it is not a directory.
         */
        @NotNull public JavadocTask to(@NotNull File target)
        {
            if (target.exists()) {
                if (!target.isDirectory()) {
                    throw new IllegalArgumentException(target.getPath() + " is not a directory");
                }
            }
            else if (!target.mkdirs()) {
                throw new IllegalArgumentException("Can not create directory: " + target.getPath());
            }

            return new JavadocTask(sourceDirs, target);
        }
    }
}
