
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import apb.utils.FileUtils;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
//
// User: emilio
// Date: Mar 30, 2009
// Time: 3:33:59 PM

//
public abstract class Parser
    extends DefaultHandler
{
    //~ Instance fields ......................................................................................

    protected final Map<String, ModuleParser> allModules;
    protected File[]                          classPath;
    protected final List<ModuleParser>        modules;

    protected final File sourceDir;

    protected final File sourceFile;
    boolean              excludeJars;
    private boolean      alreadyParsed;
    private boolean      includeSource;
    private String       name;
    private final Parser parent;

    //~ Constructors .........................................................................................

    protected Parser(final File file, final Map<String, ModuleParser> allModules, Parser parent)
        throws IOException
    {
        this.parent = parent;

        if (!file.exists()) {
            throw new IOException("File not found: " + file);
        }

        sourceFile = file;
        sourceDir = file.getParentFile();
        modules = new ArrayList<ModuleParser>();
        this.allModules = allModules;

        String fileName = file.getName();
        name = fileName.substring(0, fileName.lastIndexOf('.'));
    }

    //~ Methods ..............................................................................................

    public File getSourceFile()
    {
        return sourceFile;
    }

    public void setIncludeSource(boolean includeSource)
    {
        this.includeSource = includeSource;
    }

    public List<String> getModuleNames()
    {
        List<String> names = new ArrayList<String>();

        for (ModuleParser moduleParser : modules) {
            names.add(moduleParser.getName());
        }

        return names;
    }

    public List<File> getModuleFiles()
    {
        List<File> names = new ArrayList<File>();

        for (ModuleParser moduleParser : modules) {
            names.add(moduleParser.getSourceFile());
        }

        return names;
    }

    public String getName()
    {
        return name;
    }

    public Parser getParent()
    {
        return parent;
    }

    public List<String> getRecursiveModuleNames()
    {
        //REFACTORME: use an OrderedSet for removing duplicates instead of a list.
        List<String> names = new ArrayList<String>();

        for (ModuleParser module : modules) {
            //don't add duplicates
            if (!names.contains(module.getName())) {
                names.add(module.getName());

                //addAll (don't add duplicates)
                for (String moduleName : module.getModuleNames()) {
                    if (!names.contains(moduleName)) {
                        names.add(moduleName);
                    }
                }
            }
        }

        return names;
    }

    public void parse()
        throws ParserConfigurationException, SAXException, IOException
    {
        if (alreadyParsed) {}
        else {
            try {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setNamespaceAware(true);
                spf.newSAXParser().parse(sourceFile, this);
            }
            catch (SAXParseException e) {
                throw new SAXException("Could not parse file: " + sourceFile + ":" + e.getLineNumber() +
                                       ": " + e.getMessage(),
                                       e.getException() == null ? e : e.getException());
            }

            for (ModuleParser moduleParser : modules) {
                moduleParser.parse();
            }

            alreadyParsed = true;
        }
    }

    protected abstract File parseFile(String filepath);

    protected boolean isOutputTag(String localName)
    {
        return localName.equals(OUTPUT_TAG);
    }

    protected boolean isSourceTag(String localName)
    {
        return includeSource && localName.equals(SOURCE_FOLDER_TAG);
    }

    protected boolean isTestOutputTag(String localName)
    {
        return localName.equals(TEST_OUTPUT_TAG);
    }

    protected void addModule(File moduleFile)
        throws ParserConfigurationException, SAXException, IOException
    {
        if (moduleFile != null) {
            final String path = FileUtils.normalizePath(moduleFile);
            ModuleParser moduleParser = allModules.get(path);

            if (moduleParser == null) {
                moduleParser = ModuleParser.create(moduleFile, allModules, this);
                allModules.put(path, moduleParser);
            }

            if (!modules.contains(moduleParser)) {
                modules.add(moduleParser);
            }
        }
    }

    /**
     * Parses a string that possibly starts from $PROJECT_DIR$
     */
    protected File checkFile(String filepath)
    {
        File result = null;

        if (filepath != null) {
            File f = parseFile(filepath);

            if (f.exists()) {
                result = f;
            }
            else {
                System.err.println("Warning: non-existent file " + f);
            }
        }

        return result;
    }

    protected boolean isParentTopLevel()
    {
        return getParent() != null && getParent().isTopLevel();
    }

    protected boolean isTopLevel()
    {
        return getParent() == null;
    }

    //~ Static fields/initializers ...........................................................................

    protected static final String MODULE_DIR = "$MODULE_DIR$/";
    private static final String   TEST_OUTPUT_TAG = "output-test";
    private static final String   OUTPUT_TAG = "output";
    private static final String   SOURCE_FOLDER_TAG = "sourceFolder";
}
