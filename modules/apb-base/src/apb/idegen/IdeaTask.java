

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


package apb.idegen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import apb.Apb;
import apb.BuildException;
import apb.Environment;

import apb.metadata.Library;
import apb.metadata.LocalLibrary;
import apb.metadata.PackageType;

import apb.utils.FileUtils;
import apb.utils.XmlUtils;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import static apb.utils.FileUtils.makeRelative;
//
// User: emilio
// Date: Oct 7, 2008
// Time: 12:50:45 PM

/**
 * @exclude
 */
public class IdeaTask
{
    //~ Methods ..............................................................................................

    // Common utility methods

    private static Element createElement(Element module, String name)
    {
        final Element result = module.getOwnerDocument().createElement(name);
        module.appendChild(result);
        return result;
    }

    private static Element findComponent(Element module, String name)
    {
        NodeList children = module.getElementsByTagName("component");

        Element result = null;

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                result = (Element) child;

                if (result.getAttribute(NAME_ATTRIBUTE).equals(name)) {
                    break;
                }
            }
        }

        if (result == null) {
            result = createElement(module, "component");
            result.setAttribute(NAME_ATTRIBUTE, name);
        }

        return result;
    }

    private static Element getElementByName(Element element, String name)
    {
        Element  result = null;
        NodeList children = element.getElementsByTagName(name);

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(name)) {
                result = (Element) node;
                break;
            }
        }

        return result;
    }

    private static void removeOldElements(Element element, String name)
    {
        NodeList   children = element.getElementsByTagName(name);
        List<Node> toBeRemoved = new ArrayList<Node>();

        for (int i = 0; i < children.getLength(); i++) {
            toBeRemoved.add(children.item(i));
        }

        for (Node node : toBeRemoved) {
            element.removeChild(node);
        }
    }

    private static Element findElement(Element component, String name)
    {
        Element element = getElementByName(component, name);

        if (element == null) {
            element = createElement(component, name);
        }

        return element;
    }

    private static void writeDocument(Environment env, File ideaFile, Document document)
    {
        env.logInfo("Writing: %s\n", ideaFile);
        XmlUtils.writeDocument(document, ideaFile);
    }

    // Is there a simpler way to accomplish this ?
    private static boolean inside(@NotNull File directory, @NotNull File descendant)
    {
        File file = descendant;

        do {
            if (file.equals(directory)) {
                return true;
            }

            file = file.getParentFile();
        }
        while (file != null);

        return false;
    }

    //~ Static fields/initializers ...........................................................................

    private static final String DEFAULT_PROJECT_TEMPLATE = "/resources/templates/project.xml";

    private static final Set<String> EMPTY_STRING_SET = Collections.emptySet();

    @NonNls private static final String WILDCARD_RESOURCE_PATTERNS = "wildcardResourcePatterns";
    private static final String         DEFAULT_MODULE_TEMPLATE = "/resources/templates/module.xml";

    @NonNls private static final String MODULE_ROOT = "NewModuleRootManager";

    @NonNls private static final String EXCLUDE_FOLDER = "excludeFolder";

    @NonNls private static final String OUTPUT_FOLDER = "output";
    @NonNls private static final String EXCLUDE_OUTPUT = "exclude-output";

    @NonNls private static final String NAME_ATTRIBUTE = "name";
    @NonNls private static final String MODULE_NAME_ATTRIBUTE = "module-name";
    @NonNls private static final String MODULE_LIBRARY_TYPE = "module-library";
    @NonNls private static final String MODULE_TYPE = "module";

    @NonNls private static final String TYPE_ATTRIBUTE = "type";
    @NonNls private static final String URL_ATTRIBUTE = "url";

    @NonNls private static final String LIBRARY_CLASSES_TAG = "CLASSES";
    @NonNls private static final String LIBRARY_SOURCES_TAG = "SOURCES";

    @NonNls private static final String LIBRARY_JAVADOC_TAG = "JAVADOC";
    @NonNls private static final String MODULE_ENTRY = "orderEntry";
    @NonNls private static final String SOURCE_FOLDER = "sourceFolder";

    //~ Inner Classes ........................................................................................

    /**
     * @exclude
     */
    public static class Project
        extends IdegenTask.Project
    {
        Project(@NotNull String id, @NotNull final File modulesHome, @NotNull File projectDirectory)
        {
            super(id, modulesHome, projectDirectory);
        }

        public void execute()
        {
            final File ideaFile = ideaFile(id, ".ipr");

            if (mustBuild(ideaFile)) {
                Document document = readIdeaFile(ideaFile, DEFAULT_PROJECT_TEMPLATE);

                Element docElement = document.getDocumentElement();

                if (jdkName.isEmpty()) {
                    jdkName = System.getProperty("java.specification.version");
                    env.logInfo("jdkName is not set, using [java version %s] as default.\n", jdkName);
                }

                setJdkName(docElement, jdkName);

                setWildcardResourcePatterns(docElement, "!?*.java");

                Element modulesElement =
                    findElement(findComponent(docElement, "ProjectModuleManager"), "modules");

                removeOldElements(modulesElement, "module");

                // Get the set of moduleGroups
                Set<String> groups = calculateGroups();

                for (String mod : modules) {
                    addModuleToProject(mod, modulesElement, groups);
                }

                generateProjectDefinitionsModule(modulesElement, projectDirectory,
                                                 projectDirectory.getName());
                writeDocument(env, ideaFile, document);
            }
        }

        private static String moduleGroup(final String moduleId, @NotNull Set<String> groups)
        {
            String s = moduleId.replace('.', '/');

            String result;

            if (groups.contains(s)) {
                result = s;
            }
            else {
                int slash = s.lastIndexOf('/');
                result = slash == -1 ? "" : s.substring(0, slash);
            }

            return result;
        }

        private static List<Library> librariesForDef(@NotNull Environment env, @Nullable File jarFile)
            throws IOException
        {
            List<Library> libraries = new ArrayList<Library>();
            final File    base = env.getBaseDir();

            for (File f : env.getExtClassPath()) {
                libraries.add(new LocalLibrary(makePath(base, f), false));
            }

            if (jarFile != null) {
                final String       path = jarFile.getPath();
                File               srcFile = new File(path.substring(0, path.length() - 4) + "-src.jar");
                final LocalLibrary library = new LocalLibrary(makePath(base, jarFile), false);

                if (srcFile.exists()) {
                    library.setSources(makePath(base, srcFile));
                }

                libraries.add(library);
            }

            return libraries;
        }

        private static String makePath(final File baseDir, File f)
            throws IOException
        {
            return makeRelative(baseDir, f).getPath();
        }

        private Set<String> calculateGroups()
        {
            Set<String> groups = new HashSet<String>();

            for (String mod : modules) {
                groups.add(moduleGroup(mod, EMPTY_STRING_SET));
            }

            return groups;
        }

        private Document readIdeaFile(@NotNull File ideaFile, final String defaultTemplate)
        {
            Document result = null;

            try {
                DocumentBuilder reader = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                InputStream     is;

                if (!env.forceBuild() && ideaFile.exists()) {
                    is = new FileInputStream(ideaFile);
                }
                else if (template != null && template.exists()) {
                    is = new FileInputStream(template);
                }
                else {
                    is = getClass().getResourceAsStream(defaultTemplate);
                }

                result = reader.parse(is);
            }
            catch (ParserConfigurationException e) {
                env.handle(e);
            }
            catch (IOException e) {
                env.handle(e);
            }
            catch (SAXException e) {
                env.handle(e);
            }

            return result;
        }

        private void generateProjectDefinitionsModule(Element modulesElement, final File defDir,
                                                      final String moduleId)
        {
            try {
                File     apbJarFile = null;
                String[] mods = new String[0];

                if (modules.contains(APB_MODULE)) {
                    mods = new String[] { APB_MODULE, APB_BASE_MODULE };
                }
                else {
                    apbJarFile = Apb.applicationJarFile();
                }

                final Module task = new IdeaTask.Module(moduleId, modulesHome);

                task.usingSources(java.util.Collections.singletonList(defDir))  //
                    .usingLibraries(librariesForDef(env, apbJarFile))  //
                    .usingModules(mods)  //
                    .excluding("file://$MODULE_DIR$/")  //
                    .execute();
                addModuleToProject(moduleId, modulesElement, EMPTY_STRING_SET);
            }
            catch (IOException e) {
                throw new BuildException(e);
            }
        }

        private void addModuleToProject(final String moduleId, Element modulesElement, Set<String> groups)
        {
            final Element moduleElement = createElement(modulesElement, "module");

            String filePath = makeRelative(modulesHome, ideaFile(moduleId, ".iml")).getPath();

            moduleElement.setAttribute("filepath", "$PROJECT_DIR$/" + filePath);
            String group = moduleGroup(moduleId, groups);

            if (!group.isEmpty()) {
                moduleElement.setAttribute("group", group);
            }
        }

        private void setWildcardResourcePatterns(@NotNull Element content,
                                                 @NotNull String  wildcardResourcePatterns)
        {
            Element compilerConfigurationElement = findComponent(content, "CompilerConfiguration");

            if (wildcardResourcePatterns.isEmpty()) {
                removeOldElements(compilerConfigurationElement, WILDCARD_RESOURCE_PATTERNS);
                Element         wildcardResourcePatternsElement =
                    createElement(compilerConfigurationElement, WILDCARD_RESOURCE_PATTERNS);
                StringTokenizer wildcardResourcePatternsTokenizer =
                    new StringTokenizer(wildcardResourcePatterns, ";");

                while (wildcardResourcePatternsTokenizer.hasMoreTokens()) {
                    String  wildcardResourcePattern = wildcardResourcePatternsTokenizer.nextToken();
                    Element entryElement = createElement(wildcardResourcePatternsElement, "entry");
                    entryElement.setAttribute("name", wildcardResourcePattern);
                }
            }
        }

        private File ideaFile(@NotNull final String name, @NotNull String ext)
        {
            return new File(modulesHome, name + ext);
        }

        private void setJdkName(@NotNull Element content, @NotNull String jdkName)
        {
            Element component = findComponent(content, "ProjectRootManager");

            component.setAttribute("project-jdk-name", jdkName);

            boolean jdk_15 = jdkName.compareTo("1.5") >= 0;
            boolean assertKeyword = jdk_15 || jdkName.startsWith("1.4");
            component.setAttribute("jdk-15", String.valueOf(jdk_15));
            component.setAttribute("assert-keyword", String.valueOf(assertKeyword));
        }

        private static final String APB_MODULE = "apb";
        private static final String APB_BASE_MODULE = "apb-base";
    }

    /**
     * Generate the module (.iml) file
     * @exclude
     */
    static class Module
        extends IdegenTask.Module
    {
        Module(@NotNull String id, @NotNull final File modulesHome)
        {
            super(id, modulesHome);
        }

        public void execute()
        {
            final File ideaFile = ideaFile(id, ".iml");

            if (mustBuild(ideaFile)) {
                Document document = readIdeaFile(ideaFile, DEFAULT_MODULE_TEMPLATE);
                FileUtils.validateDirectory(modulesHome);

                if (document != null) {
                    Element docElement = document.getDocumentElement();

                    addPackageSpecificInfo(docElement);

                    Element component = findComponent(docElement, MODULE_ROOT);

                    assignOutputFolder(component);

                    Set<File> contents = new LinkedHashSet<File>();

                    for (String c : contentDirs) {
                        final File contentDir = env.fileFromBase(c);

                        if (includeEmptyDirs || !contents.contains(contentDir) && contentDir.exists()) {
                            contents.add(contentDir);
                        }
                    }

                    final List<File> sources = new ArrayList<File>(sourceDirs);

                    for (File contentDir : contents) {
                        Element element =
                            findContent(component, URL_ATTRIBUTE, relativeUrl("file", contentDir));

                        for (Iterator<File> itr = sources.iterator(); itr.hasNext();) {
                            File source = itr.next();

                            if (inside(contentDir, source)) {
                                if (includeEmptyDirs || source.exists()) {
                                    addSourceDir(element, source);
                                    itr.remove();
                                }
                            }
                        }
                    }

                    for (File source : sources) {
                        if (source.exists() || includeEmptyDirs) {
                            Element content =
                                findContent(component, URL_ATTRIBUTE, relativeUrl("file", source));
                            addSourceDir(content, source);
                        }
                    }

                    rewriteDependencies(component);

                    writeDocument(env, ideaFile, document);
                }
            }
        }

        private void addSourceDir(Element contentRoot, File source)
        {
            removeOldElements(contentRoot, SOURCE_FOLDER);
            removeOldElements(contentRoot, EXCLUDE_FOLDER);
            addExcludeFolders(contentRoot);
            addSourceFolder(contentRoot, source);
        }

        /**
         * Add exclude folders.
         * For the time being it only excludes the directory with the project files
         * from the ProjectDefinitions Module
         */
        private void addExcludeFolders(Element content)
        {
            if (!excludes.isEmpty()) {
                Element excludeFolder = createElement(content, EXCLUDE_FOLDER);

                for (String exclude : excludes) {
                    excludeFolder.setAttribute(URL_ATTRIBUTE, exclude);
                }
            }
        }

        private Element findContent(Element component, String attribute, String value)
        {
            String  name = "content";
            Element element = getElementByNameAndAttribute(component, name, attribute, value);

            if (element == null) {
                element = createElement(component, name);
                element.setAttribute(attribute, value);
            }

            return element;
        }

        private Document readIdeaFile(@NotNull File ideaFile, final String defaultTemplate)
        {
            Document result = null;

            try {
                DocumentBuilder reader = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                InputStream     is;

                if (!env.forceBuild() && ideaFile.exists()) {
                    is = new FileInputStream(ideaFile);
                }
                else if (template != null && template.exists()) {
                    is = new FileInputStream(template);
                }
                else {
                    is = getClass().getResourceAsStream(defaultTemplate);
                }

                result = reader.parse(is);
            }
            catch (ParserConfigurationException e) {
                env.handle(e);
            }
            catch (IOException e) {
                env.handle(e);
            }
            catch (SAXException e) {
                env.handle(e);
            }

            return result;
        }

        private File ideaFile(@NotNull final String name, @NotNull String ext)
        {
            return new File(modulesHome, name + ext);
        }

        private void addPackageSpecificInfo(@NotNull Element element)
        {
            switch (packageType) {
            case WAR:
                addWebModule(element);
                break;
            case EAR:
                addEarModule(element);
                break;
            }
        }

        private void assignOutputFolder(@NotNull Element component)
        {
            File o = output;

            if (o != null) {
                findElement(component, OUTPUT_FOLDER).setAttribute(URL_ATTRIBUTE, relativeUrl("file", o));
                findElement(component, EXCLUDE_OUTPUT);
            }
        }

        private void addSourceFolder(Element content, File directory)
        {
            Element sourceFolder = createElement(content, SOURCE_FOLDER);

            sourceFolder.setAttribute(URL_ATTRIBUTE, relativeUrl("file", directory));
            sourceFolder.setAttribute("isTestSource", String.valueOf(testModule));
        }

        private String relativeUrl(@NotNull final String type, @NotNull File file)
        {
            return type + "://$MODULE_DIR$/" + makeRelative(modulesHome, file).getPath();
        }

        private Element getElementByNameAndAttribute(Element element, String name, String attribute,
                                                     String value)
        {
            Element  result = null;
            NodeList children = element.getElementsByTagName(name);

            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(name)) {
                    Node node1 = node.getAttributes().getNamedItem(attribute);

                    if (node1 != null && value.equals(node1.getTextContent())) {
                        result = (Element) node;
                        break;
                    }
                }
            }

            return result;
        }

        private void addEarModule(@NotNull Element element)
        {
            final File o = output;

            if (o != null) {
                element.setAttribute(TYPE_ATTRIBUTE, "J2EE_APPLICATION_MODULE");
                Element component = findComponent(element, "ApplicationModuleProperties");
                addDeploymentDescriptor(component, "application.xml", "1.3", new File(o, "application.xml"));
            }
        }

        private Element addDeploymentDescriptor(Element component, String name, String version, File file)
        {
            Element deploymentDescriptor = findElement(component, "deploymentDescriptor");

            if (deploymentDescriptor.getAttribute("version") == null) {
                deploymentDescriptor.setAttribute("version", version);
            }

            if (deploymentDescriptor.getAttribute("name") == null) {
                deploymentDescriptor.setAttribute("name", name);
            }

            deploymentDescriptor.setAttribute("optional", "false");

            //        if (deploymentDescriptorFile == null) {
            //            deploymentDescriptorFile = file;
            //        }

            deploymentDescriptor.setAttribute("url", relativeUrl("file", file));

            return deploymentDescriptor;
        }

        /**
         * Adds the Web module to the (.iml) project file.
         *
         * @param element Dom element
         */
        @SuppressWarnings({ "UnusedDeclaration" })
        private void addWebModule(Element element)
        {
            //        String warWebapp = helper.getOutput() + "/" + project.getArtifactId();
            //        String warSrc = getPluginSetting("maven-war-plugin", "warSourceDirectory", "src/main/webapp");
            //        String webXml = warSrc + "/WEB-INF/web.xml";
            //
            //        module.setAttribute("type", "J2EE_WEB_MODULE");
            //
            //        Element component = findComponent(module, "WebModuleBuildComponent");
            //        Element setting = findSetting(component, "EXPLODED_URL");
            //        setting.setAttribute("value", getModuleFileUrl(warWebapp));
            //
            //        component = findComponent(module, "WebModuleProperties");
            //
            //        removeOldElements(component, "containerElement");
            //
            //        addDeploymentDescriptor(component, "web.xml", "2.3", webXml);
            //
            //        Element element = findElement(component, "webroots");
            //        removeOldElements(element, "root");
            //
            //        element = createElement(element, "root");
            //        element.setAttribute("relative", "/");
            //        element.setAttribute("url", getModuleFileUrl(warSrc));
        }

        private void rewriteDependencies(Element component)
        {
            Map<String, Element> modulesByName = new HashMap<String, Element>();
            Set<Element>         unusedModules = new HashSet<Element>();

            Map<String, Element> modulesByUrl = new HashMap<String, Element>();
            NodeList             entries = component.getElementsByTagName(MODULE_ENTRY);

            for (int i = 0; i < entries.getLength(); i++) {
                Element orderEntry = (Element) entries.item(i);

                String type = orderEntry.getAttribute(TYPE_ATTRIBUTE);

                if (MODULE_TYPE.equals(type)) {
                    modulesByName.put(orderEntry.getAttribute(MODULE_NAME_ATTRIBUTE), orderEntry);
                }
                else if (MODULE_LIBRARY_TYPE.equals(type)) {
                    // keep track for later so we know what is left
                    unusedModules.add(orderEntry);

                    Element lib = getElementByName(orderEntry, "library");
                    String  name = lib.getAttribute(NAME_ATTRIBUTE);

                    if (name != null) {
                        modulesByName.put(name, orderEntry);
                    }
                    else {
                        Element classesChild = getElementByName(lib, LIBRARY_CLASSES_TAG);

                        if (classesChild != null) {
                            Element rootChild = getElementByName(classesChild, "root");

                            if (rootChild != null) {
                                String url = rootChild.getAttribute(URL_ATTRIBUTE);
                                modulesByUrl.put(url, orderEntry);
                            }
                        }
                    }
                }
            }

            // First check and add dependencies from other modules

            for (String mod : moduleDependencies) {
                Element d = modulesByName.get(mod);

                if (d == null) {
                    d = createElement(component, MODULE_ENTRY);
                    d.setAttribute(TYPE_ATTRIBUTE, MODULE_TYPE);
                    d.setAttribute(MODULE_NAME_ATTRIBUTE, mod);
                }
            }

            // Now libraries

            for (Library l : libraries) {
                addLibrary(l, component, modulesByUrl, unusedModules);
            }

            // Now remove unused modules
            for (Element e : unusedModules) {
                component.removeChild(e);
            }
        }

        private void addLibrary(Library library, Element component, Map<String, Element> modulesByUrl,
                                Set<Element> unusedModules)
        {
            File libraryFile = library.getArtifact(env, PackageType.JAR);

            if (libraryFile != null) {
                final String url = relativeUrl("jar", libraryFile) + "!/";

                Element dep = modulesByUrl.get(url);

                if (dep != null) {
                    unusedModules.remove(dep);
                }
                else {
                    dep = createElement(component, MODULE_ENTRY);
                }

                dep.setAttribute(TYPE_ATTRIBUTE, MODULE_LIBRARY_TYPE);

                Element lib = getElementByName(dep, "library");

                if (lib == null) {
                    lib = createElement(dep, "library");
                }

                // replace classes
                removeOldElements(lib, LIBRARY_CLASSES_TAG);
                Element classes = createElement(lib, LIBRARY_CLASSES_TAG);

                createElement(classes, "root").setAttribute(URL_ATTRIBUTE, url);

                addLibEntry(library, lib, PackageType.SRC, LIBRARY_SOURCES_TAG);
                addLibEntry(library, lib, PackageType.DOC, LIBRARY_JAVADOC_TAG);
            }
        }

        private void addLibEntry(Library library, Element lib, PackageType type, String tag)
        {
            final File url = library.getArtifact(env, type);

            if (url != null) {
                final String protocol;
                final String endMarker;

                if (url.isDirectory()) {
                    protocol = "file";
                    endMarker = "";
                }
                else {
                    protocol = "jar";
                    endMarker = "!/" + library.getSubPath(type);
                }

                removeOldElements(lib, tag);
                Element el = createElement(createElement(lib, tag), "root");

                el.setAttribute(URL_ATTRIBUTE, relativeUrl(protocol, url) + endMarker);
            }
        }
    }
}
