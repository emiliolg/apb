
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static apb.utils.FileUtils.makeRelative;
import static apb.utils.StreamUtils.buffered;
import static apb.utils.StreamUtils.close;

public class SchemaUtils
{
    //~ Constructors .........................................................................................

    private SchemaUtils() {}

    //~ Methods ..............................................................................................

    public static File[] copySchema(final File[] schemas, final File targetDir)
        throws XMLStreamException, IOException
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
        String candidate = null;

        for (final File file : fileSet) {
            final String basePath =
                file.isDirectory() ? file.getAbsolutePath() : file.getParentFile().getAbsolutePath();
            candidate = longestCommonPrefix(candidate, basePath);
        }

        return new File(candidate);
    }

    private static String longestCommonPrefix(String a, String b)
    {
        //Fast path
        if (a == null) {
            return b;
        }

        if (b == null) {
            return a;
        }

        int i = 0;

        for (int limit = Math.min(a.length(), b.length()); i < limit; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                break;
            }
        }

        return a.substring(0, i);
    }

    private static Set<File> collectReferencedFiles(File[] schemas)
        throws XMLStreamException, FileNotFoundException
    {
        final Set<File> fileSet = new HashSet<File>();

        for (File schema : schemas) {
            collectReferencedFiles(schema, fileSet);
        }

        return Collections.unmodifiableSet(fileSet);
    }

    private static void collectReferencedFiles(final File schema, final Set<File> fileSet)
        throws XMLStreamException, FileNotFoundException
    {
        if (!fileSet.contains(schema)) {
            fileSet.add(schema);
            final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            InputStream           is = null;

            try {
                is = buffered(new FileInputStream(schema));
                final XMLStreamReader sr = inputFactory.createXMLStreamReader(is);

                for (int eventCode = sr.next(); sr.hasNext(); eventCode = sr.next()) {
                    if (eventCode == XMLStreamReader.START_ELEMENT &&
                            (IMPORT_TAG.equals(sr.getName()) || INCLUDE_TAG.equals(sr.getName()))) {
                        final String location = getSchemaLocation(sr);

                        if (location != null) {
                            final File inclusion = new File(schema.getParentFile(), location);

                            if (inclusion.exists()) {
                                collectReferencedFiles(inclusion.getAbsoluteFile(), fileSet);
                            }
                        }
                    }
                }
            }
            finally {
                close(is);
            }
        }
    }

    private static String getSchemaLocation(final XMLStreamReader sr)
    {
        for (int i = 0; i < sr.getAttributeCount(); i++) {
            if (sr.getAttributeLocalName(i).equals("schemaLocation")) {
                return sr.getAttributeValue(i);
            }
        }

        return null;
    }

    //~ Static fields/initializers ...........................................................................

    private static final QName IMPORT_TAG = new QName("http://www.w3.org/2001/XMLSchema", "import");
    private static final QName INCLUDE_TAG = new QName("http://www.w3.org/2001/XMLSchema", "include");
}
