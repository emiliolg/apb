// ...........................................................................................................
// (C) Copyright  1996/2007 Fuego Inc.  All Rights Reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Fuego Inc.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// $Revision: $
// ...........................................................................................................
package apb.utils;

import static apb.utils.StreamUtils.buffered;
import static apb.utils.StreamUtils.close;
import static apb.utils.FileUtils.makeRelative;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

public class SchemaUtils {

    public static File copySchema(final File schema, final File targetDir)
            throws XMLStreamException, IOException
    {
        final Set<File> fileSet = collectReferencedFiles(schema);
        final File baseDir = findLongestCommonPrefix(fileSet);
        for (final File file : fileSet) {
            final File targetFile = new File(targetDir, makeRelative(baseDir, file).getPath());
            FileUtils.copyFile(file, targetFile, false);
        }
        return new File(targetDir, makeRelative(baseDir, schema).getPath());
    }

    private static File findLongestCommonPrefix(final Set<File> fileSet) {
        String candidate = null;
        for (final File file : fileSet) {
            final String basePath = file.isDirectory()?file.getAbsolutePath():file.getParentFile().getAbsolutePath();
            candidate = longestCommonPrefix(candidate, basePath);
        }
        return new File(candidate);
    }

    private static String longestCommonPrefix(String a, String b) {
        //Fast path
        if(a == null) return b;
        if(b == null) return a;

        int i = 0;
        for(int limit = Math.min(a.length(), b.length()); i < limit; i++) {
            if(a.charAt(i) != b.charAt(i)) {
                break;
            }
        }

        return a.substring(0, i);
    }

    private static Set<File> collectReferencedFiles(File schema) throws XMLStreamException, FileNotFoundException {
        final Set<File> fileSet = new HashSet<File>();
        collectReferencedFiles(schema, fileSet);
        return Collections.unmodifiableSet(fileSet);
    }

    private static void collectReferencedFiles(final File schema, final Set<File> fileSet)
            throws XMLStreamException, FileNotFoundException {
        if(!fileSet.contains(schema)) {
            fileSet.add(schema);
            final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            InputStream is = null;
            try {
                is = buffered(new FileInputStream(schema));
                final XMLStreamReader sr = inputFactory.createXMLStreamReader(is);
                for(int eventCode = sr.next(); sr.hasNext(); eventCode = sr.next()) {
                    if(eventCode == XMLStreamReader.START_ELEMENT
                            && (IMPORT_TAG.equals(sr.getName()) || INCLUDE_TAG.equals(sr.getName()))) {
                        final String location = getSchemaLocation(sr);
                        if(location != null) {
                            final File inclusion = new File(schema.getParentFile(), location);
                            if(inclusion.exists()) {
                                collectReferencedFiles(inclusion.getAbsoluteFile(), fileSet);
                            }
                        }
                    }
                }

            } finally {
                close(is);
            }
        }
    }

    private static String getSchemaLocation(final XMLStreamReader sr) {
        for(int i = 0; i < sr.getAttributeCount(); i++) {
            if(sr.getAttributeLocalName(i).equals("schemaLocation")) {
                return sr.getAttributeValue(i);
            }
        }
        return null;
    }

    private static final QName IMPORT_TAG= new QName("http://www.w3.org/2001/XMLSchema", "import");
    private static final QName INCLUDE_TAG= new QName("http://www.w3.org/2001/XMLSchema", "include");
}
