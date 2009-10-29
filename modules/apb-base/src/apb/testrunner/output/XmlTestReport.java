
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

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import apb.Environment;
import apb.utils.StringUtils;
import apb.utils.XmlUtils;

public class XmlTestReport
    extends BaseTestReport
{
    //~ Instance fields ......................................................................................

    final String ATTR_CLASSNAME = "classname";
    final String ATTR_FAILURES = "failures";
    final String ATTR_MESSAGE = "message";
    final String ATTR_NAME = "name";
    final String ATTR_PKG = "package";
    final String ATTR_SKIPPED = "skipped";
    final String ATTR_TESTS = "tests";
    final String ATTR_TIME = "time";
    final String ATTR_TYPE = "type";
    final String FAILURE = "failure";
    final String HOSTNAME = "hostname";
    final String PROPERTIES = "properties";
    final String TESTCASE = "testcase";
    final String TESTSUITE = "testsuite";
    final String TIMESTAMP = "timestamp";

    /**
     * The XML document.
     */
    private Document doc;

    /**
     * tests that failed.
     */
    private final Set<String> failedTests = new HashSet<String>();

    /**
     * Element for the current test.
     */
    private final Map<CharSequence, Element> testElements = new HashMap<CharSequence, Element>();

    /**
     * Timing helper.
     */
    private final Map<String, Long> testStarts = new HashMap<String, Long>();

    //~ Constructors .........................................................................................

    public XmlTestReport(boolean showOutput, @NotNull String fileName)
    {
        super(showOutput, fileName);
    }

    //~ Methods ..............................................................................................

    public void startSuite(@NotNull String suiteName)
    {
        super.startSuite(suiteName);

        doc = getDocumentBuilder().newDocument();

        final Element rootElement = doc.createElement(TESTSUITE);
        doc.appendChild(rootElement);

        rootElement.setAttribute(ATTR_NAME, suiteName);
        rootElement.setAttribute(ATTR_PKG, suiteName.substring(0, suiteName.lastIndexOf(".")));

        //add the timestamp
        final String timestamp = timestamp();
        rootElement.setAttribute(TIMESTAMP, timestamp);

        //and the hostname.
        rootElement.setAttribute(HOSTNAME, getHostname());

        // Output properties
        Element propsElement = doc.createElement(PROPERTIES);
        rootElement.appendChild(propsElement);
    }

    public void endSuite()
    {
        if (suiteOpen) {
            super.endSuite();
            final Element rootElement = doc.getDocumentElement();
            rootElement.setAttribute(ATTR_TESTS, "" + getSuiteTestsRun());
            rootElement.setAttribute(ATTR_FAILURES, "" + getSuiteTestFailures());
            rootElement.setAttribute(ATTR_SKIPPED, "" + getSuiteTestSkipped());
            rootElement.setAttribute(ATTR_TIME, "" + (getSuiteTimeEllapsed() / ONE_SECOND));

            if (getSuiteTestsRun() > 0) {
                writeDocument(doc);
            }
        }
    }

    public void startTest(@NotNull String testName)
    {
        super.startTest(testName);
        testStarts.put(qualify(testName), System.currentTimeMillis());
    }

    public void endTest()
    {
        super.endTest();
        final String test = getCurrentTest();

        final String fullTestName = qualify(test);

        if (!testStarts.containsKey(fullTestName)) {
            startTest(test);
        }

        Element currentTest;

        if (!failedTests.contains(fullTestName)) {
            currentTest = doc.createElement(TESTCASE);
            currentTest.setAttribute(ATTR_NAME, test == null ? UNKNOWN : test);
            currentTest.setAttribute(ATTR_CLASSNAME, getCurrentSuite());
            doc.getDocumentElement().appendChild(currentTest);
            testElements.put(fullTestName, currentTest);
        }
        else {
            currentTest = testElements.get(fullTestName);
        }

        long elapsed = System.currentTimeMillis() - testStarts.get(fullTestName);
        currentTest.setAttribute(ATTR_TIME, "" + (elapsed / ONE_SECOND));
    }

    public void failure(@NotNull Throwable t)
    {
        super.failure(t);
        formatError(FAILURE, getCurrentTest(), t);
    }

    @NotNull public XmlTestReport init(@NotNull File dir)
    {
        XmlTestReport result = new XmlTestReport(showOutput, fileName);
        result.reportsDir = dir;
        return result;
    }

    protected void printOutput(String title, String content)
    {
        Element nested = doc.createElement(title);
        doc.getDocumentElement().appendChild(nested);
        nested.appendChild(doc.createCDATASection(content));
    }

    private static String timestamp()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        dateFormat.setLenient(true);
        return dateFormat.format(new Date());
    }

    private static DocumentBuilder getDocumentBuilder()
    {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        catch (Exception exc) {
            throw new ExceptionInInitializerError(exc);
        }
    }

    private String qualify(String test)
    {
        return getCurrentSuite() + '-' + test;
    }

    private String getHostname()
    {
        try {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            return "localhost";
        }
    }

    private void formatError(String type, String test, Throwable t)
    {
        if (test != null) {
            endTest();
            failedTests.add(qualify(test));
        }

        Element nested = doc.createElement(type);

        Element currentTest = test != null ? testElements.get(qualify(test)) : doc.getDocumentElement();

        currentTest.appendChild(nested);

        String message = t.getMessage();

        if (message != null && message.length() > 0) {
            nested.setAttribute(ATTR_MESSAGE, t.getMessage());
        }

        nested.setAttribute(ATTR_TYPE, t.getClass().getName());

        String strace = StringUtils.getStackTrace(t);
        Text   trace = doc.createTextNode(strace);
        nested.appendChild(trace);
    }

    private void writeDocument(@NotNull Document document)
    {
        final String suite = getCurrentSuite();

        if (suite != null) {
            XmlUtils.writeDocument(document, reportFile(suite, ".xml"));
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static final String DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    private static final long   serialVersionUID = 7656301463663508457L;

    /** constant for unnnamed testsuites/cases */
    private static final String UNKNOWN = "unknown";

    //~ Inner Classes ........................................................................................

    public static class Builder
        implements TestReport.Builder
    {
        @NotNull private String   output = "test-output";
        @Nullable private Boolean showOutput;

        public Builder showOutput(boolean b)
        {
            showOutput = b;
            return this;
        }

        @NotNull public TestReport build(@NotNull Environment env)
        {
            boolean show =
                showOutput == null ? env.getBooleanProperty(SHOW_OUTPUT_PROPERTY, false) : showOutput;
            return new XmlTestReport(show, output);
        }

        public TestReport.Builder to(@NotNull String outputFileName)
        {
            output = outputFileName;
            return this;
        }
    }
}
