
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

package apb.utils;

import apb.Apb;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static apb.utils.FileUtils.makeRelative;
import static apb.utils.StreamUtils.buffered;
import static apb.utils.StreamUtils.close;

public class SchemaUtils
{
    //~ Constructors .........................................................................................

    private SchemaUtils() {}

    //~ Methods ..............................................................................................

    public static void copySchema(final File[] schemas, final File[] bindings, final File targetDir)
        throws IOException
    {
        final Set<File> fileSet = collectReferencedFiles(schemas, bindings);

        final File baseDir = findLongestCommonPrefix(fileSet);

        for (final File file : fileSet) {
            FileUtils.copyFile(file, targetName(baseDir, targetDir, file), false);
        }

        for (int i = 0, length = schemas.length; i < length; i++) {
            schemas[i] = targetName(baseDir, targetDir, schemas[i]);
        }

        for (int i = 0, length = bindings.length; i < length; i++) {
            bindings[i] = targetName(baseDir, targetDir, bindings[i]);
        }
    }

    private static File targetName(File baseDir, File targetDir, File file)
    {
        return new File(targetDir, makeRelative(baseDir, file).getPath());
    }

    private static File findLongestCommonPrefix(final Set<File> fileSet)
    {
        File candidate = null;

        for (final File file : fileSet) {
            final File parentDir = file.getParentFile();
            candidate = longestCommonPrefix(candidate, parentDir);
        }

        return candidate;
    }

    private static File longestCommonPrefix(File a, File b)
    {
        //Fast path
        if (a == null) {
            return b;
        }

        if (b == null) {
            return a;
        }

        while (!a.equals(b)) {
            if (a.getPath().length() > b.getPath().length()) {
                a = a.getParentFile();
            }
            else {
                b = b.getParentFile();
            }
        }

        return a;
    }

    private static Set<File> collectReferencedFiles(File[] schemas, File[] bindings)
    {
        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        final Set<File> fileSet = new HashSet<File>();

        for (File schema : schemas) {
            collectReferencedFiles(schema, fileSet, inputFactory);
        }

        for (File binding : bindings) {
            collectReferencedFiles(binding, fileSet, inputFactory);
        }

        return Collections.unmodifiableSet(fileSet);
    }

    private static void collectReferencedFiles(final File schema, final Set<File> fileSet,
                                               final XMLInputFactory inputFactory)
    {
        final Queue<File> pending = new ArrayDeque<File>();

        pending.add(FileUtils.normalizeFile(schema));

        File next;

        while ((next = pending.poll()) != null) {
            if (fileSet.add(next)) {
                collectFiles(next, inputFactory, pending);
            }
        }
    }

    private static void collectFiles(File file, XMLInputFactory inputFactory, Queue<File> pending)
    {
        try {
            final URL         doc = file.toURI().toURL();
            final InputStream is = buffered(doc.openStream());

            try {
                final XMLStreamReader sr = inputFactory.createXMLStreamReader(is);

                collectFiles(sr, doc, pending);

                sr.close();
            }
            finally {
                close(is);
            }
        }
        catch (IOException e) {
            logException(e);
        }
        catch (XMLStreamException e) {
            logException(e);
        }
    }

    private static void collectFiles(XMLStreamReader sr, URL doc, Queue<File> pending)
        throws XMLStreamException
    {
        while (sr.hasNext()) {
            final int eventCode = sr.next();

            if (eventCode == XMLStreamConstants.START_ELEMENT) {
                final QName srName = sr.getName();

                if (IMPORT_TAG.equals(srName) || INCLUDE_TAG.equals(srName) || JAXB_TAG.equals(srName)) {
                   final String location = findAttribute(sr, "schemaLocation");

                    if (location != null) {
                        processLocation(doc, pending, location);
                    }
                }
            }
        }
    }

    private static String findAttribute(XMLStreamReader sr, String attributeName) {
        for (int i = 0; i < sr.getAttributeCount(); i++){
            final String localName = sr.getAttributeLocalName(i);
            if(localName.equals(attributeName)){
                return sr.getAttributeValue(i);
            }

        }
        return null;
    }

    private static void processLocation(URL doc, Queue<File> pending, String location)
    {
        try {
            URI locUri = new URI(location);

            if (locUri.isAbsolute()) {
                return;
            }

            locUri = doc.toURI().resolve(locUri);

            pending.add(new File(locUri));
        }
        catch (URISyntaxException e) {
            logException(e);
        }
    }

    private static void logException(Exception e)
    {
        Apb.getEnv().logVerbose(e.toString());
    }

    //~ Static fields/initializers ...........................................................................

    private static final QName IMPORT_TAG = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "import");
    private static final QName INCLUDE_TAG = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "include");
    private static final QName JAXB_TAG = new QName("http://java.sun.com/xml/ns/jaxb", "bindings");
}
