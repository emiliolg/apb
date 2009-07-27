

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.jetbrains.annotations.NotNull;

import static apb.utils.Constants.*;
//
// User: emilio
// Date: Nov 14, 2008
// Time: 5:08:03 PM

//
/**
 * An XML Report that generates an HTML report 'ala' JUnit
 */
public class JUnitTestReport
    extends XmlTestReport
{
    //~ Constructors .........................................................................................

    public JUnitTestReport(boolean showOutput, @NotNull String prefix)
    {
        super(showOutput, prefix);
    }

    //~ Methods ..............................................................................................

    public void stopRun()
    {
        super.stopRun();
        generateHtml();
    }

    @NotNull public JUnitTestReport init(@NotNull File dir)
    {
        JUnitTestReport result = new JUnitTestReport(showOutput, fileName);
        result.reportsDir = dir;
        return result;
    }

    private static void copyFile(File file, PrintStream out)
        throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF8));

        String line;

        while ((line = reader.readLine()) != null) {
            if (!line.startsWith(XML_HEADER_START)) {
                out.println("    " + line);
            }
        }

        reader.close();
    }

    private void generateHtml()
    {
        try {
            File reportFile = aggregate();

            final InputStream stylesheet = getClass().getResourceAsStream(STYLE_SHEET);
            Xslt.transform(stylesheet, reportFile, new File(reportsDir, HTML_REPORT));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private File aggregate()
        throws IOException
    {
        final File reportFile = new File(reportsDir, AGGREGATED_FILE);
        //reportFile.deleteOnExit();

        PrintStream fos = new PrintStream(reportFile, UTF8);

        fos.println(XML_HEADER);
        fos.println("<testsuites>");

        for (File file : listReportFiles(reportFile)) {
            copyFile(file, fos);
        }

        fos.println("</testsuites>");

        fos.close();

        return reportFile;
    }

    private File[] listReportFiles(final File targetFile)
    {
        return reportsDir.listFiles(new FileFilter() {
                public boolean accept(File file)
                {
                    return !file.equals(targetFile) && file.getName().endsWith(".xml");
                }
            });
    }

    //~ Static fields/initializers ...........................................................................

    private static final String HTML_REPORT = "report.html";
    private static final String STYLE_SHEET = "/resources/junit-noframes.xsl";

    private static final long serialVersionUID = 2784000716925297814L;

    private static final String AGGREGATED_FILE = "Test-Suites.xml";
}
