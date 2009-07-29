package tests;

import apb.metadata.TestModule;
import apb.coverage.CoverageReport;
import static apb.coverage.CoverageReport.HTML;
import static apb.coverage.CoverageReport.Column.CLASS;
import apb.testrunner.output.TestReport;
//
// User: emilio
// Date: Mar 4, 2009
// Time: 5:03:52 PM

//
public class Math extends TestModule {
    {
        dependencies(localLibrary("../lib/junit-3.8.2.jar"));
        useProperties("module");

        //enableDebugger = true;
        //coverage.enable = true;
        //coverage.ensure = 90;
        //coverage.reports(HTML.orderBy(CLASS));

    }
}
