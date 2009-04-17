

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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import apb.BuildException;
import apb.Environment;
import apb.ModuleHelper;
import apb.ProjectElementHelper;

import apb.metadata.LocalLibrary;
import apb.metadata.Module;
import apb.metadata.PackageType;
import apb.metadata.ProjectElement;
import apb.metadata.TestModule;

import apb.utils.FileUtils;
import apb.utils.XmlUtils;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

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

//
public class IdeaTask
    extends Task
{
    //~ Instance fields ......................................................................................

    private File deploymentDescriptorFile;

    private ProjectElementHelper helper;

    private File    modulesHome;
    private boolean overwrite;
    private File    templateDir;

    //~ Constructors .........................................................................................

    public IdeaTask(@NotNull Environment env)
    {
        super(env);
        helper = env.getCurrent();
        modulesHome =
            IDEA_MODULES_HOME == null ? new File(env.getProjectsHome(), IDEA_DIR)
                                      : env.fileFromBase(IDEA_MODULES_HOME);

        templateDir = new File(modulesHome, "templates");
        overwrite = env.forceBuild();
    }

    //~ Methods ..............................................................................................

    public static void execute(Environment env)
    {
        new IdeaTask(env).execute();
    }

    public void execute()
    {
        if (helper instanceof ModuleHelper) {
            final ModuleHelper mod = (ModuleHelper) helper;
            rewriteModule(mod);

            for (TestModule testModule : mod.getModule().tests()) {
                env.activate(testModule);
                rewriteModule(env.getModuleHelper());
                env.deactivate();
            }

            env.activate(mod.getModule());
        }

        if (helper.isTopLevel()) {
            rewriteProject();
        }
    }

    private void rewriteModule(ModuleHelper module)
    {
        final File ideaFile = ideaFile(module, ".iml");

        if (mustBuild(module, ideaFile)) {
            Document document = readIdeaFile(ideaFile, "module.xml");
            FileUtils.validateDirectory(modulesHome);

            if (document != null) {
                Element docElement = document.getDocumentElement();

                addPackageSpecificInfo(module, docElement);

                Element component = findComponent(docElement, MODULE_ROOT);

                assignOutputFolder(module, component);

                Element content = findElement(component, "content");

                content.setAttribute(URL_ATTRIBUTE, relativeUrl("file", module.getModuledir()));

                removeOldElements(content, SOURCE_FOLDER);
                addSourceFolder(content, module.getSource());

                removeOldElements(content, EXCLUDE_FOLDER);

                rewriteDependencies(module, component);

                writeDocument(ideaFile, document);
            }
        }
    }

    private void writeDocument(File ideaFile, Document document)
    {
        env.logInfo("Writing: %s\n", ideaFile);
        XmlUtils.writeDocument(document, ideaFile);
    }

    private boolean mustBuild(ProjectElementHelper module, File ideaFile)
    {
        return overwrite || !ideaFile.exists() || ideaFile.lastModified() < module.lastModified();
    }

    private void rewriteProject()
    {
        final File ideaFile = ideaFile(helper, ".ipr");

        if (mustBuild(helper, ideaFile)) {
            Document document = readIdeaFile(ideaFile, "project.xml");

            Element docElement = document.getDocumentElement();

            // Set the jdk name if set
            String jdkName = helper.getJdkName();

            if (jdkName == null || jdkName.isEmpty()) {
                jdkName = System.getProperty("java.specification.version");
                env.logInfo("jdkName is not set, using [java version %s] as default.\n", jdkName);
            }

            setJdkName(docElement, jdkName);

            setWildcardResourcePatterns(docElement, "!?*.java");

            Element modulesElement =
                findElement(findComponent(docElement, "ProjectModuleManager"), "modules");

            removeOldElements(modulesElement, "module");

            for (ModuleHelper mod : helper.listAllModules()) {
                addModuleToProject(modulesElement, mod);
            }

            generateProjectDefinitionsModule(modulesElement);
            writeDocument(ideaFile, document);
        }
    }

    private Element findComponent(Element module, String name)
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

    private Element findElement(Element component, String name)
    {
        Element element = getElementByName(component, name);

        if (element == null) {
            element = createElement(component, name);
        }

        return element;
    }

    private Element createElement(Element module, String name)
    {
        final Element result = module.getOwnerDocument().createElement(name);
        module.appendChild(result);
        return result;
    }

    private void removeOldElements(Element element, String name)
    {
        NodeList children = element.getElementsByTagName(name);

        for (int i = 0; i < children.getLength(); i++) {
            element.removeChild(children.item(i));
        }
    }

    private Document readIdeaFile(@NotNull File ideaFile, @NotNull String template)
    {
        Document result = null;

        try {
            DocumentBuilder reader = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream     is;

            if (ideaFile.exists() && !overwrite) {
                is = new FileInputStream(ideaFile);
            }
            else if (templateDir != null && templateDir.exists()) {
                is = new FileInputStream(new File(templateDir, template));
            }
            else {
                is = getClass().getResourceAsStream("/resources/templates/" + template);
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

    private void generateProjectDefinitionsModule(Element modulesElement)
    {
        try {
            final ProjectElement prev = env.getCurrent().getElement();
            final Module         mod = new ProjectDefinitions(helper, env.applicationJarFile());
            env.activate(mod);
            final IdeaTask task = new IdeaTask(env);
            task.overwrite = true;
            task.execute();
            addModuleToProject(modulesElement, env.getModuleHelper());
            env.deactivate();
            env.removeHelper(mod);
            env.activate(prev);
        }
        catch (IOException e) {
            throw new BuildException(e);
        }
    }

    private void addModuleToProject(Element modulesElement, ModuleHelper module)
    {
        final Element moduleElement = createElement(modulesElement, "module");

        String filePath = makeRelative(modulesHome, ideaFile(module, ".iml")).getPath();

        moduleElement.setAttribute("filepath", "$PROJECT_DIR$/" + filePath);
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

    private File ideaFile(@NotNull ProjectElementHelper e, @NotNull String ext)
    {
        return new File(modulesHome, e.getId() + ext);
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

    private void addPackageSpecificInfo(ModuleHelper module, Element element)
    {
        switch (module.getPackageInfo().type) {
        case WAR:
            addWebModule(module, element);
            break;
        case EAR:
            addEarModule(module, element);
            break;
        }
    }

    private void assignOutputFolder(ModuleHelper module, Element component)
    {
        findElement(component, OUTPUT_FOLDER).setAttribute(URL_ATTRIBUTE,
                                                           relativeUrl("file", module.getOutput()));
    }

    private void addSourceFolder(Element content, File directory)
    {
        Element sourceFolder = createElement(content, SOURCE_FOLDER);

        sourceFolder.setAttribute(URL_ATTRIBUTE, relativeUrl("file", directory));
        sourceFolder.setAttribute("isTestSource", "false");
    }

    private String relativeUrl(@NotNull final String type, @NotNull File file)
    {
        return type + "://$MODULE_DIR$/" + makeRelative(modulesHome, file).getPath();
    }

    private Element getElementByName(Element element, String name)
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

    private void addEarModule(ModuleHelper module, Element element)
    {
        element.setAttribute(TYPE_ATTRIBUTE, "J2EE_APPLICATION_MODULE");
        Element component = findComponent(element, "ApplicationModuleProperties");
        addDeploymentDescriptor(component, "application.xml", "1.3",
                                new File(module.getOutput(), "application.xml"));
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

        if (deploymentDescriptorFile == null) {
            deploymentDescriptorFile = file;
        }

        deploymentDescriptor.setAttribute("url", relativeUrl("file", file));

        return deploymentDescriptor;
    }

    /**
     * Adds the Web module to the (.iml) project file.
     *
     * @param module
     * @param element Dom element
     */
    @SuppressWarnings({ "UnusedDeclaration" })
    private void addWebModule(ModuleHelper module, Element element)
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

    private void rewriteDependencies(ModuleHelper module, Element component)
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

        for (ModuleHelper mod : module.getDirectDependencies()) {
            final String moduleName = mod.getId();
            Element      d = modulesByName.get(moduleName);

            if (d == null) {
                d = createElement(component, MODULE_ENTRY);
                d.setAttribute(TYPE_ATTRIBUTE, MODULE_TYPE);
                d.setAttribute(MODULE_NAME_ATTRIBUTE, moduleName);
            }
        }

        // Now libraries

        for (LocalLibrary l : module.getLocalLibraries()) {
            addLibrary(l, component, modulesByUrl, unusedModules);
        }

        // Extra Libraries

        for (LocalLibrary l : module.getCompileInfo().extraLibraries()) {
            addLibrary(l, component, modulesByUrl, unusedModules);
        }

        // Now remove unused modules
        for (Element e : unusedModules) {
            component.removeChild(e);
        }
    }

    private void addLibrary(LocalLibrary library, Element component, Map<String, Element> modulesByUrl,
                            Set<Element> unusedModules)
    {
        for (File libraryFile : library.getFiles(env))  {
            addLibrary(libraryFile, component, unusedModules, modulesByUrl, library.getSourcesFile(env));
        }
    }

    private void addLibrary(File libraryFile, Element component, Set<Element> unusedModules,
                            Map<String, Element> modulesByUrl, final File sourceUrl)
    {
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

        if (sourceUrl != null) {
            removeOldElements(lib, LIBRARY_SOURCES_TAG);
            Element sourcesElement = createElement(lib, LIBRARY_SOURCES_TAG);

            Element sourceEl = createElement(sourcesElement, "root");
            sourceEl.setAttribute(URL_ATTRIBUTE, relativeUrl("jar", sourceUrl) + "!/");
        }
        //
        //            String javaDocUrl = null;
        //
        //            if (javaDocUrl != null) {
        //                removeOldElements(lib, LIBRARY_JAVADOC_TAG);
        //                Element javadocsElement = createElement(lib, LIBRARY_JAVADOC_TAG);
        //                Element sourceEl = createElement(javadocsElement, "root");
        //                sourceEl.setAttribute(URL_ATTRIBUTE, javaDocUrl);
        //            }
    }

    //~ Static fields/initializers ...........................................................................

    @NonNls private static final String WILDCARD_RESOURCE_PATTERNS = "wildcardResourcePatterns";

    @NonNls private static final String IDEA_DIR = "idea";
    private static final String         IDEA_MODULES_HOME = System.getProperty("IDEA_MODULES_HOME");

    @NonNls private static final String MODULE_ROOT = "NewModuleRootManager";

    @NonNls private static final String EXCLUDE_FOLDER = "excludeFolder";

    @NonNls private static final String OUTPUT_FOLDER = "output";

    @NonNls private static final String NAME_ATTRIBUTE = "name";
    @NonNls private static final String MODULE_NAME_ATTRIBUTE = "module-name";
    @NonNls private static final String MODULE_LIBRARY_TYPE = "module-library";
    @NonNls private static final String MODULE_TYPE = "module";

    @NonNls private static final String TYPE_ATTRIBUTE = "type";
    @NonNls private static final String URL_ATTRIBUTE = "url";

    @NonNls private static final String LIBRARY_CLASSES_TAG = "CLASSES";
    @NonNls private static final String LIBRARY_SOURCES_TAG = "SOURCES";

    //    @NonNls private static final String LIBRARY_JAVADOC_TAG = "JAVADOC";
    @NonNls private static final String MODULE_ENTRY = "orderEntry";
    @NonNls private static final String SOURCE_FOLDER = "sourceFolder";

    //~ Inner Classes ........................................................................................

    public static class ProjectDefinitions
        extends Module
    {
        public ProjectDefinitions() {}

        ProjectDefinitions(ProjectElementHelper project, final File jarFile)
            throws IOException
        {
            pkg.type = PackageType.NONE;
            source = "$moduledir";
            final String       path = jarFile.getPath();
            File               srcFile =
                new File(path.substring(0, path.length() - 4) + ModuleHelper.SRC_JAR);
            final LocalLibrary library = localLibrary(makePath(project, jarFile));

            if (srcFile.exists()) {
                library.setSources(makePath(project, srcFile));
            }

            dependencies(library);
        }

        /**
         * Make name absolute
         * @return
         */
        @Override public String getName()
        {
            return getClass().getSimpleName();
        }

        private String makePath(ProjectElementHelper project, File file)
            throws IOException
        {
            return makeRelative(project.getBasedir(), file).getPath();
        }
    }
}
