
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jetbrains.annotations.Nullable;

import apb.Apb;

import static apb.utils.FileUtils.makeRelative;
import static apb.utils.StreamUtils.buffered;
import static apb.utils.StreamUtils.close;

public class SchemaUtils
{
    //~ Constructors .........................................................................................

    private SchemaUtils() {}

    //~ Methods ..............................................................................................

    public static File[] copySchema(final File[] schemas, final File targetDir)
        throws IOException
    {
        final Set<File> fileSet = collectReferencedFiles(schemas);

        final File baseDir = findLongestCommonPrefix(fileSet);

        for (final File file : fileSet) {
            final File targetFile = new File(targetDir, makeRelative(baseDir, file).getPath());
            FileUtils.copyFile(file, targetFile, false);
        }

        for (int i = 0, length = schemas.length; i < length; i++) {
            schemas[i] = new File(targetDir, makeRelative(baseDir, schemas[i]).getPath());
        }

        return schemas;
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

    private static Set<File> collectReferencedFiles(File[] schemas)
    {
        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        final Set<File> fileSet = new HashSet<File>();

        for (File schema : schemas) {
            collectReferencedFiles(schema, fileSet, inputFactory);
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
            int eventCode = sr.next();

            if (eventCode == XMLStreamConstants.START_ELEMENT &&
                    (IMPORT_TAG.equals(sr.getName()) || INCLUDE_TAG.equals(sr.getName()))) {
                processLocation(doc, pending, getSchemaLocation(sr));
            }
        }
    }

    private static void processLocation(URL doc, Queue<File> pending, String location)
    {
        if (location == null) {
            return;
        }

        try {
            URI locUri = new URI(location);

            if (locUri.isAbsolute()) {
                return;
            }

            locUri = locUri.resolve(doc.toURI());

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

    @Nullable private static String getSchemaLocation(final XMLStreamReader sr)
    {
        for (int i = 0; i < sr.getAttributeCount(); i++) {
            if ("schemaLocation".equals(sr.getAttributeLocalName(i))) {
                return sr.getAttributeValue(i);
            }
        }

        return null;
    }

    //~ Static fields/initializers ...........................................................................

    private static final QName IMPORT_TAG = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "import");
    private static final QName INCLUDE_TAG = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "include");
}
