

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


package apb.testrunner.output;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import apb.utils.FileUtils;

import org.xml.sax.SAXException;

public class Xslt
{
    //~ Methods ..............................................................................................

    public static void transform(final InputStream style, File infile, File outfile)
        throws TransformerException, IOException, SAXException, ParserConfigurationException
    {
        final Transformer transformer = createTransformer(style);

        InputStream  fis = null;
        OutputStream fos = null;

        try {
            fis = new BufferedInputStream(new FileInputStream(infile));
            fos = new BufferedOutputStream(new FileOutputStream(outfile));
            StreamResult res = new StreamResult(fos);
            Source       src = new StreamSource(fis);

            transformer.transform(src, res);
        }
        finally {
            FileUtils.close(fis);
            FileUtils.close(fos);
        }
    }

    private static Transformer createTransformer(final InputStream style)
        throws TransformerConfigurationException
    {
        InputStream xslStream = null;

        try {
            xslStream = new BufferedInputStream(style);
            Source src = new StreamSource(xslStream);
            return TransformerFactory.newInstance().newTemplates(src).newTransformer();
        }
        finally {
            FileUtils.close(xslStream);
        }
    }
}
