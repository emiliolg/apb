
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

package apb.testrunner.output;

import java.io.File;
import java.io.Serializable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An Interface to implement to generate Test Reports
 * The implemented classes should be serializables.
 */

public interface TestReport
    extends Serializable
{
    //~ Instance fields ......................................................................................

    TestReport SIMPLE = new SimpleReport(false, true);
    TestReport SIMPLE_TO_FILE = new SimpleReport(true, "test-output");

    TestReport SUMMARY = new SimpleReport(false, false);
    TestReport XML_JUNIT = new XmlTestReport(true, "test-");

    //~ Methods ..............................................................................................

    /**
     * Called when a new suite is started
     * @param suiteName The test suite being started
     */
    void startSuite(@NotNull String suiteName);

    /**
     * Called when the current executing suite finished its execution
     */
    void endSuite();

    /**
     * Called when a new test is started
     * @param testName The test being started
     */
    void startTest(@NotNull String testName);

    /**
     * Called when the current executing test finished its execution
     */
    void endTest();

    void failure(@NotNull Throwable t);

    /**
     * Returns the current test under execution or null if not current one
     * @return The current test under execution
     */
    @Nullable String getCurrentTest();

    /**
     * Returns the current test suite under execution or null if not current one
     * @return The current test suite under execution
     */
    @Nullable String getCurrentSuite();

    /**
     * Mark the start of the execution of a batch of suites
     * @param n The numbert of suites to be executed
     */
    void startRun(int n);

    /**
     * Mark the end of the execution of a batch of suites
     */
    void stopRun();

    /**
     * Returns the number of suites run so far in the current batch
     * @return the number of suites run so far in the current batch
     */
    int getSuitesRun();

    /**
     * Returns the number of suites failed.
     * @return the number of suites failed
     */
    int getSuitesFailed();

    /**
     * Returns the total number of suites of the current batch
     * @return the number of suites of the current batch
     */
    int getTotalSuites();

    /**
     * Initialize a new report from a prototype
     * @param reportsDir The directory where the reports are written to
     */
    @NotNull TestReport init(@NotNull File reportsDir);
}
