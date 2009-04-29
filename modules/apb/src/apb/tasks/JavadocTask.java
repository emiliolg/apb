
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
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import apb.Environment;
import apb.ModuleHelper;
import apb.Proxy;

import apb.metadata.JavadocInfo;
import apb.metadata.ResourcesInfo;

import apb.utils.FileUtils;
import apb.utils.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//
// User: emilio
// Date: Oct 27, 2008
// Time: 12:35:05 PM

//
public class JavadocTask
    extends Task
{
    //~ Instance fields ......................................................................................

    /**
     * A set of additional Javadoc Options separated by ':'
     */
    @NotNull private String additionalJavadocOptions = "";
    private List<String>    additionalParams;
    private List<String>    args;
    private boolean         author;
    @NotNull private String bottom = "";
    @NotNull private String classPath;
    private boolean         deprecated;
    private boolean         deprecatedList;

    @NotNull private String                  doclet = "";
    @NotNull private String                  doctitle = "";
    @NotNull private String                  encoding = "";
    @NotNull private List<String>            excludes;
    @NotNull private String                  footer = "";
    @NotNull private List<JavadocInfo.Group> groups;
    @NotNull private String                  header = "";
    private boolean                          help;
    private boolean                          index;
    @NotNull private List<String>            links;
    private boolean                          linkSource;

    private Locale locale = null;

    /**
     * Max memory in megabytes
     */
    private int                                    maxMemory = 256;
    @NotNull private List<JavadocInfo.OfflineLink> offlineLinks;

    private File                  outputDirectory;
    @NotNull private String       overview = "";
    @NotNull private List<String> packages;
    private boolean               since;
    @NotNull private String       sourcePath;
    private boolean               splitindex;
    private boolean               tree;
    private boolean               use;
    private boolean               version;

    @NotNull private JavadocInfo.Visibility visibility;
    @NotNull private String                 windowTitle;

    //~ Constructors .........................................................................................

    public JavadocTask(@NotNull Environment env, @NotNull File outputDir, @NotNull String classPath,
                       @NotNull String sourcePath)
    {
        super(env);
        this.sourcePath = sourcePath;
        this.classPath = classPath;
        outputDirectory = outputDir;
        encoding = ResourcesInfo.DEFAULT_ENCODING;
        visibility = JavadocInfo.Visibility.PROTECTED;
        packages = Collections.emptyList();
        additionalParams = Collections.emptyList();
        args = new ArrayList<String>();
        links = Collections.emptyList();
        offlineLinks = Collections.emptyList();
        groups = Collections.emptyList();
    }

    //~ Methods ..............................................................................................

    public static void execute(Environment env)
    {
        ModuleHelper module = env.getModuleHelper();
        JavadocInfo  javadoc = module.getJavadocInfo();

        String sourcePath = module.getSource().getPath();
        String classPath = FileUtils.makePath(module.compileClassPath());

        JavadocTask t = new JavadocTask(env, env.fileFromBase(javadoc.output), classPath, sourcePath);

        t.initParameters(javadoc);

        t.execute();
    }

    public void setMaxMemory(int memory)
    {
        maxMemory = memory;
    }

    public void setExcludes(@NotNull List<String> excludes)
    {
        this.excludes = excludes;
    }

    public void setEncoding(@NotNull String value)
    {
        encoding = value;
    }

    public void setLocale(@NotNull Locale locale)
    {
        this.locale = locale;
    }

    public void setOverview(@Nullable File file)
    {
        if (file != null) {
            if (!file.exists()) {
                env.handle("Not existing overview file: " + file);
            }
            else {
                overview = file.getPath();
            }
        }
        else {
            overview = "";
        }
    }

    public void setVisibility(@NotNull JavadocInfo.Visibility level)
    {
        visibility = level;
    }

    public void execute()
    {
        FileUtils.validateDirectory(outputDirectory);

        copyAllResources();

        ExecTask execTask = new ExecTask(env, args);

        args.add(FileUtils.findJavaExecutable("javadoc", env));

        execTask.setCurrentDirectory(outputDirectory.getAbsolutePath());

        addMemoryArg(args, maxMemory);
        addProxyArg(args);

        if (!additionalJavadocOptions.isEmpty()) {
            args.addAll(Arrays.asList(additionalJavadocOptions.split(":")));
        }

        if (locale != null) {
            addArgument("-locale", locale.toString());
        }

        addArgument("-classpath", classPath);

        addArgument("-doclet", doclet);

        addArgument("-overview", overview);

        args.add("-" + visibility.toString().toLowerCase());

        addArgument("-sourcepath", sourcePath);

        args.add("-subpackages");
        args.add(FileUtils.makePathFromStrings(packages.isEmpty() ? collectPackages() : packages));

        if (!excludes.isEmpty()) {
            args.add("-exclude");
            args.add(FileUtils.makePathFromStrings(excludes));
        }

        args.add(env.isVerbose() ? "-verbose" : "-quiet");

        if (!additionalParams.isEmpty()) {
            args.addAll(additionalParams);
        }

        if (doclet.isEmpty()) {
            addStandardDocletOptions();
        }

        execTask.execute();
    }

    public Proxy getActiveProxy()
    {
        return null;
    }

    public void setAdditionalJavadocOptions(@NotNull String additionalJavadocOptions)
    {
        this.additionalJavadocOptions = additionalJavadocOptions;
    }

    public void setAdditionalParams(List<String> additionalParams)
    {
        this.additionalParams = additionalParams;
    }

    public void setPackages(@NotNull List<String> packages)
    {
        this.packages = packages;
    }

    public void setDoclet(@NotNull String doclet)
    {
        this.doclet = doclet;
    }

    public void setDoctitle(@NotNull String doctitle)
    {
        this.doctitle = doctitle;
    }

    public void setHeader(@NotNull String header)
    {
        this.header = header;
    }

    public void setFooter(@NotNull String footer)
    {
        this.footer = footer;
    }

    public void setBottom(@NotNull String bottom)
    {
        this.bottom = bottom;
    }

    public void setWindowTitle(@NotNull String value)
    {
        windowTitle = value;
    }

    public void setLinks(@NotNull List<String> value)
    {
        links = value;
    }

    public void setOfflineLinks(@NotNull List<JavadocInfo.OfflineLink> offlineLinks)
    {
        this.offlineLinks = offlineLinks;
    }

    public void setGroups(@NotNull List<JavadocInfo.Group> groupList)
    {
        groups = groupList;
    }

    private List<String> collectPackages()
    {
        final List<String> result = new ArrayList<String>();

        for (String source : sourcePath.split(":")) {
            new File(source).listFiles(new FileFilter() {
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

        if (activeProxy != null) {
            String protocol =
                    activeProxy.getProtocol().isEmpty() ? "" : activeProxy.getProtocol() + ".";

            if (!activeProxy.getHost().isEmpty()) {
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
    }

    private void initParameters(JavadocInfo info)
    {
        setEncoding(info.encoding);

        setOverview(info.overview.isEmpty() ? null : env.fileFromBase(info.overview));

        setVisibility(info.visibility);
        setPackages(info.includes());
        setExcludes(info.excludes());
        setDoclet(info.doclet);
        setLocale(info.locale);
        setMaxMemory(info.memory);
        setDoctitle(env.expand(info.title));
        setHeader(env.expand(info.header));
        setFooter(env.expand(info.footer));
        setBottom(env.expand(info.bottom));
        setWindowTitle(env.expand(info.windowTitle));

        setLinks(info.links());
        setOfflineLinks(info.offlineLinks());
        setGroups(info.groups());

        splitindex = info.splitIndex;
        use = info.use;
        version = info.version;
        author = info.author;
        deprecated = info.deprecated;
        deprecatedList = info.deprecatedList;
        since = info.since;
        tree = info.tree;
        index = info.index;
        help = info.help;
        linkSource = info.linkSource;
    }
}
