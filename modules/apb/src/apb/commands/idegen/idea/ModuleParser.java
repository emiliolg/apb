

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


package apb.commands.idegen.idea;
//
// User: emilio
// Date: Mar 30, 2009
// Time: 3:33:13 PM

//
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import apb.utils.FileUtils;
import apb.utils.NameUtils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ModuleParser
    extends Parser
{
    //~ Instance fields ......................................................................................

    public final List<Library> libraries;
    public final Set<File>     outputs;
    public final Set<File>     testOutputs;
    private boolean            inClass;
    private Library            library;

    //~ Constructors .........................................................................................

    protected ModuleParser(final File moduleFile, Map<String, ModuleParser> allModules, Parser parent)
        throws IOException
    {
        super(moduleFile, allModules, parent);
        libraries = new ArrayList<Library>();
        outputs = new HashSet<File>();
        testOutputs = new HashSet<File>();
    }

    //~ Methods ..............................................................................................

    public static String modulesDir(File sourceDir, String sourceModule)
    {
        if (sourceModule.endsWith(MODULE_EXT)) {
            sourceModule = sourceModule.substring(0, sourceModule.length() - MODULE_EXT.length());
        }

        String moduleDir = sourceDir.getPath();

        if (endsWith(moduleDir, sourceModule)) {
            moduleDir = moduleDir.substring(0, moduleDir.length() - sourceModule.length() - 1);
        }

        return moduleDir;
    }

    /**
     * This method is a temporary fix. As there is no global definition for modules (which should be read from the project), this method tries to guess the module location.
     * Useful when a module is not located in the default base dir.
     */
    public static List<String> modulesDirCandidates(File sourceDir, String sourceModule)
    {
        List<String> candidates = new ArrayList<String>(2);
        String       firstCandidate = modulesDir(sourceDir, sourceModule) + File.separator;

        candidates.add(firstCandidate);

        File mDir = new File(firstCandidate);

        if (mDir.isDirectory()) {
            File c2 = new File(mDir.getParent(), ALTERNATIVE_MODULES_DIR);
            candidates.add(c2.getPath() + File.separator);
        }

        return candidates;
    }

    public static ModuleParser create(final File moduleFile, Map<String, ModuleParser> allModules,
                                      Parser parent)
        throws IOException, ParserConfigurationException, SAXException
    {
        final String path = FileUtils.normalizePath(moduleFile);
        ModuleParser result = allModules.get(path);

        if (result == null) {
            result = new ModuleParser(moduleFile, allModules, parent);
            result.parse();
            allModules.put(path, result);
        }

        return result;
    }

    public static void main(String[] args)
        throws IOException, SAXException, ParserConfigurationException
    {
        if (args.length < 1) {
            System.err.println("Must specify an \".iml\" file");
            System.exit(1);
        }

        File moduleFile = new File(args[0]);

        if (!moduleFile.exists()) {
            System.err.println("Invalid module file: " + moduleFile);
            System.exit(1);
        }

        final ModuleParser parser = create(moduleFile, new HashMap<String, ModuleParser>(), null);

        for (String mod : parser.getModuleNames()) {
            System.out.println("new " + NameUtils.javaIdFromId(mod) + "(),");
        }

        for (Library library : parser.getLibraries()) {
            for (File file : library.getPaths()) {
                System.out.println(NameUtils.javaIdFromId(FileUtils.removeExtension(file)) + ".LIB,");
            }
        }
    }

    public void endElement(String uri, String localName, String qName)
        throws SAXException
    {
        checkEndLibraryTag(localName);
        super.endElement(uri, localName, qName);
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
    {
        try {
            checkModule(localName, attributes);
        }
        catch (IOException e) {
            throw new SAXException(e);
        }
        catch (ParserConfigurationException e) {
            throw new SAXException(e);
        }

        checkLibraryTag(localName, attributes);
        checkOutputTags(localName, attributes);
        checkLibrary(localName, attributes);
    }

    public String toString()
    {
        return sourceFile.toString();
    }

    public List<ModuleParser> getModules()
    {
        return modules;
    }

    public Set<File> getOutputs()
    {
        return outputs;
    }

    public List<Library> getLibraries()
    {
        return libraries;
    }

    public Set<File> getTestOutputs()
    {
        return testOutputs;
    }

    /**
     * Parses a string that possibly starts from $PROJECT_DIR$/$MODULE_DIR$ into a File object.
     */
    protected File parseFile(String filepath)
    {
        if (filepath.startsWith(MODULE_DIR)) {
            return new File(sourceDir, filepath.substring(MODULE_DIR.length()));
        }

        return new File(filepath);
    }

    private static boolean endsWith(String moduleDir, String sourceModule)
    {
        int mdl = moduleDir.length();
        int sml = sourceModule.length();
        int j = mdl - sml;

        if (j < 0) {
            return false;
        }

        for (int i = 0; i < sml && j < mdl; i++, j++) {
            final char c = sourceModule.charAt(i);

            if (c != '.' && c != moduleDir.charAt(j)) {
                return false;
            }
        }

        return true;
    }

    private static File buildModuleFile(String moduleName, String moduleDir)
    {
        final String iml = moduleName + MODULE_EXT;

        for (;;) {
            File result = new File(moduleDir + moduleName, iml);
            int  dot = moduleName.indexOf('.');

            if (dot == -1 || result.exists()) {
                return result;
            }

            moduleDir += moduleName.substring(0, dot) + File.separator;
            moduleName = moduleName.substring(dot + 1);
        }
    }

    private void addOutput(File path)
    {
        if (path != null) {
            outputs.add(path);
        }
    }

    private void addTestOutput(File path)
    {
        if (path != null && path.exists()) {
            testOutputs.add(FileUtils.normalizeFile(path));
        }
    }

    private File buildModuleFile(String moduleName)
    {
        File         result = null;
        List<String> moduleDirCandidates = modulesDirCandidates(sourceDir, sourceFile.getName());

        for (String candidateDir : moduleDirCandidates) {
            result = buildModuleFile(moduleName, candidateDir);

            if (result.exists()) {
                break;
            }
        }

        return result;
    }

    private void checkEndLibraryTag(String localName)
    {
        if (library != null) {
            if (localName.equals(CLASSES_TAG)) {
                inClass = false;
            }
            else if (localName.equals(LIBRARY_TAG) && !excludeJars) {
                libraries.add(library);
                library = null;
            }
        }
    }

    private void checkLibrary(String localName, Attributes attributes)
    {
        if (library != null) {
            if (localName.equals(CLASSES_TAG)) {
                inClass = true;
            }
            else if (inClass && localName.equals(ROOT_TAG)) {
                library.add(parseUrl(attributes.getValue(URL_ATTR)));
            }
        }
    }

    private void checkLibraryTag(String localName, Attributes attributes)
    {
        if (localName.equals(LIBRARY_TAG)) {
            library = new Library(attributes.getValue(NAME_ATTR));
            inClass = false;
        }
    }

    private void checkModule(String localName, Attributes attributes)
        throws IOException, ParserConfigurationException, SAXException
    {
        final String value = attributes.getValue(MODULE_NAME_ATTR);

        if (localName.equals(ORDER_ENTRY_TAG) && attributes.getValue(TYPE_ATTR).equals(MODULE_TAG)) {
            addModule(buildModuleFile(value));
        }
    }

    private void checkOutputTags(String localName, Attributes attributes)
    {
        if (isSourceTag(localName)) {
            System.out.println("ModuleParser.checkOutputTags!!!");
        }
        else if (isOutputTag(localName)) {
            File path = parseUrl(attributes.getValue(URL_ATTR));

            //Hack for fuegoblocks
            if (path != null && "java".equals(path.getName())) {
                File webPath = new File(path.getParent(), "web");
                addOutput(webPath);
            }

            addOutput(path);
        }
        else if (isTestOutputTag(localName)) {
            addTestOutput(parseUrl(attributes.getValue(URL_ATTR)));
        }
    }

    private File parseUrl(String url)
    {
        if (url == null) {
            throw new IllegalStateException("@url missing in <output>");
        }

        String filepath;

        if (url.startsWith(FILE_URL)) {
            filepath = url.substring(FILE_URL.length());
        }
        else if (url.startsWith(JAR_URL)) {
            filepath = url.substring(JAR_URL.length(), url.length() - 2);
        }
        else {
            throw new IllegalStateException("Unknown classpath element: " + url);
        }

        File result = null;

        if (filepath != null) {
            result = parseFile(filepath);
        }

        return result;
    }

    //~ Static fields/initializers ...........................................................................

    private static final String ALTERNATIVE_MODULES_DIR = "external";

    private static final String FILE_URL = "file://";
    private static final String JAR_URL = "jar://";
    private static final String CLASSES_TAG = "CLASSES";
    private static final String LIBRARY_TAG = "library";
    private static final String URL_ATTR = "url";
    private static final String NAME_ATTR = "name";
    private static final String ROOT_TAG = "root";
    private static final String MODULE_TAG = "module";
    private static final String ORDER_ENTRY_TAG = "orderEntry";
    private static final String TYPE_ATTR = "type";
    private static final String MODULE_NAME_ATTR = "module-name";
    private static final String MODULE_EXT = ".iml";
}
