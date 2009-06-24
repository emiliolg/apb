package apb.testrunner.output;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.Document;
import apb.utils.StringUtils;
import apb.utils.XmlUtils;
//
// User: emilio
// Date: Jun 23, 2009
// Time: 7:08:04 PM

//
public class XmlTestReport extends BaseTestReport {
    String ATTR_CLASSNAME = "classname";
    String ATTR_FAILURES = "failures";
    String ATTR_MESSAGE = "message";
    String ATTR_NAME = "name";
    String ATTR_PKG = "package";
    String ATTR_SKIPPED = "skipped";
    String ATTR_TESTS = "tests";
    String ATTR_TIME = "time";
    String ATTR_TYPE = "type";
    String FAILURE = "failure";
    String HOSTNAME = "hostname";
    String PROPERTIES = "properties";
    String TESTCASE = "testcase";
    String TESTSUITE = "testsuite";
    String TIMESTAMP = "timestamp";
    /**
     * The XML document.
     */
    private Document doc;
    /**
     * tests that failed.
     */
    private Set<String> failedTests = new HashSet<String>();
    /**
     * The wrapper for the whole testsuite.
     */
    private Element rootElement;
    /**
     * Element for the current test.
     */
    private Map<CharSequence, Element> testElements = new HashMap<CharSequence, Element>();
    /**
     * Timing helper.
     */
    private Map<String, Long> testStarts = new HashMap<String, Long>();
    private static final String DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    private static final long serialVersionUID = 7656301463663508457L;
    /** constant for unnnamed testsuites/cases */
    private static final String UNKNOWN = "unknown";

    public XmlTestReport(boolean showOutput, @NotNull String fileName) {
        super(showOutput, fileName);
    }

    public void startSuite(@NotNull String suiteName)
    {
        super.startSuite(suiteName);

        doc = getDocumentBuilder().newDocument();

        rootElement = doc.createElement(TESTSUITE);
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
        testStarts.put(testName, System.currentTimeMillis());
    }

    public void endTest()
    {
        super.endTest();
        final String test = getCurrentTest();

        if (!testStarts.containsKey(test)) {
            startTest(test);
        }

        Element currentTest;

        if (!failedTests.contains(test)) {
            currentTest = doc.createElement(TESTCASE);
            currentTest.setAttribute(ATTR_NAME, test == null ? UNKNOWN : test);
            currentTest.setAttribute(ATTR_CLASSNAME, getCurrentSuite());
            rootElement.appendChild(currentTest);
            testElements.put(test, currentTest);
        }
        else {
            currentTest = testElements.get(test);
        }

        long ellapsed = System.currentTimeMillis() - testStarts.get(test);
        currentTest.setAttribute(ATTR_TIME, "" + (ellapsed / ONE_SECOND));
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
        rootElement.appendChild(nested);
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
            failedTests.add(test);
        }

        Element nested = doc.createElement(type);

        Element currentTest = test != null ? testElements.get(test) : rootElement;

        currentTest.appendChild(nested);

        String message = t.getMessage();

        if (message != null && message.length() > 0) {
            nested.setAttribute(ATTR_MESSAGE, t.getMessage());
        }

        nested.setAttribute(ATTR_TYPE, t.getClass().getName());

        String strace = StringUtils.getStackTrace(t);
        Text trace = doc.createTextNode(strace);
        nested.appendChild(trace);
    }

    private void writeDocument(@NotNull Document document)
    {
        final String suite = getCurrentSuite();

        if (suite != null) {
            XmlUtils.writeDocument(document, reportFile(suite, ".xml"));
        }
    }
}
