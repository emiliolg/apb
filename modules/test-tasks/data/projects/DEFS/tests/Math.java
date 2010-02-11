

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


package tests;

import apb.testrunner.output.TestReport;

import static apb.coverage.CoverageReport.*;
import static apb.coverage.CoverageReport.Column.*;
import static apb.coverage.CoverageReport.Depth.ALL;

//
public class Math
    extends base.BaseTestModule
{
    //~ Instance initializers ................................................................................

    {
        dependencies(localLibrary("../lib/junit-4.7.jar"));
        useProperties("name");

        coverage.enable = true;
        coverage.ensure = 5;
        coverage.reports(HTML.orderBy(CLASS).descending().columns(CLASS, METHOD, LINE).threshold(40).depth(ALL));

        reports(TestReport.JUNIT, TestReport.SIMPLE.showOutput(true));
    }
}
