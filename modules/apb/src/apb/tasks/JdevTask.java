
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import apb.Environment;
import apb.ModuleHelper;

import apb.metadata.LocalLibrary;

import apb.utils.XmlUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
//
// User: emilio
// Date: Dec 19, 2008
// Time: 11:48:34 AM

//
public class JdevTask
    extends Task
{
    //~ Instance fields ......................................................................................

    @NotNull private ModuleHelper module;
    @Nullable private File        source;

    //~ Constructors .........................................................................................

    public JdevTask(@NotNull Environment env, @Nullable File sourceExtensionFile)
    {
        super(env);
        module = env.getModuleHelper();
        source = sourceExtensionFile;
    }

    //~ Methods ..............................................................................................

    public static void generateExtension(@NotNull Environment env, @NotNull String sourceExtensionFile)
    {
        JdevTask task = new JdevTask(env, env.fileFromBase(sourceExtensionFile));
        task.execute();
    }

    public void execute()
    {
        build();
    }

    public void build()
    {
        File outputFile = new File(module.getOutput(), "META-INF/extension.xml");

        if (mustBuild(outputFile)) {
            Document doc = open();

            if (doc != null) {
                Element root = generateBasicStructure(doc);

                generateDependencies(doc, root);
                generateClasspath(doc, root);
                generateHooks(doc, root);

                env.logInfo("Writing: %s\n", outputFile);
                XmlUtils.writeDocument(doc, outputFile);
            }
        }
    }

    private boolean mustBuild(File outputFile)
    {
        boolean result = env.forceBuild() || !outputFile.exists();

        if (!result) {
            final long outTime = outputFile.lastModified();
            long       sourceTime = module.lastModified();

            if (source != null) {
                sourceTime = Math.max(source.lastModified(), sourceTime);
            }

            result = outTime < sourceTime;
        }

        return result;
    }

    private Element generateBasicStructure(Document doc)
    {
        NodeList list = doc.getElementsByTagName("extension");
        Element  extension = (Element) (list.getLength() > 0 ? list.item(0) : null);

        if (extension == null) {
            extension = doc.createElement("extension");
            doc.appendChild(extension);

            XmlUtils.addValuedElement(doc, extension, "name", module.getId());
            XmlUtils.addValuedElement(doc, extension, "owner", "Oracle");
        }

        extension.setAttribute("id", module.getPackageName());
        extension.setAttribute("xmlns", "http://jcp.org/jsr/198/extension-manifest");
        extension.setAttribute("version", "0");
        extension.setAttribute("esdk-version", "1.0");

        return extension;
    }

    private void generateDependencies(Document doc, Element root)
    {
        Element deps = XmlUtils.findChildElement(root, "dependencies");

        if (deps == null) {
            deps = doc.createElement("dependencies");
            root.appendChild(deps);
        }

        XmlUtils.removeAllChildren(deps);

        // todo What to do with Oracle extensions...
        // This cannot be tied to a prefix or something like that...
        for (ModuleHelper m : module.getDirectDependencies()) {
            XmlUtils.addValuedElement(doc, deps, "import", m.getPackageName());
        }
    }

    private void generateClasspath(Document doc, Element root)
    {
        Element deps = XmlUtils.findChildElement(root, "classpaths");

        if (deps == null) {
            deps = doc.createElement("classpaths");
            root.appendChild(deps);
        }

        XmlUtils.removeAllChildren(deps);

        for (LocalLibrary lib : module.getLocalLibraries()) {
            XmlUtils.addValuedElement(doc, deps, "classpath", lib.path);
        }
    }

    private void generateHooks(final Document doc, final Element root)
    {
        Element deps = XmlUtils.findChildElement(root, "hooks");

        if (deps == null) {
            deps = doc.createElement("hooks");
            root.appendChild(deps);
        }
    }

    @Nullable private Document open()
    {
        Document document = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder        builder = factory.newDocumentBuilder();

            if (source != null && source.exists()) {
                document = builder.parse(source);
            }
            else {
                document = builder.newDocument();
            }
        }
        catch (ParserConfigurationException e) {
            env.handle(e);
        }
        catch (SAXException e) {
            env.handle(e);
        }
        catch (IOException e) {
            env.handle(e);
        }

        return document;
    }
}
