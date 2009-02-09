
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

package apb.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import apb.coverage.CoverageReport;

/**
 * Information for coverage processeing when running tests
 */
public class CoverageInfo
{
    //~ Instance fields ......................................................................................

    /**
     * If deep is true then not only the module under testing
     * will be consider for coverage analysis but also all modules
     * that this module depends on.
     */
    public boolean deep = false;

    /**
     * Output coverage data for future analysis
     */
    public boolean dumpData = true;

    /**
     * Whether to enable coverage processing
     */
    public boolean enable = false;

    /**
     * Ensure a minimun level of coverage in % (0% = Do not fail)
     */
    public int ensure = 0;

    /**
     * The output directory for reports
     */
    public String output = "$module-dir/output/coverage";

    /**
     * The list of classes to exclude from coverage
     */
    private final List<String> excludes = new ArrayList<String>();

    /**
     * The list of classes to include for coverage.
     * If Empty all classes will be included.
     */
    private final List<String> includes = new ArrayList<String>();

    /**
     * Coverage reports
     */
    private final List<CoverageReport> reports = new ArrayList<CoverageReport>();

    //~ Methods ..............................................................................................

    public List<String> includes()
    {
        return includes;
    }

    public List<String> excludes()
    {
        return excludes;
    }

    public List<CoverageReport> reports()
    {
        return reports;
    }

    /**
     * Method used to define the list of classes to be included for coverage.
     * @param patterns Patterns that define the list of classes to be included (* and ? can be used)
     */
    public void includes(String... patterns)
    {
        includes.addAll(Arrays.asList(patterns));
    }

    /**
     * Method used to set excludes to define the list of classes to exclude from coverage
     * @param patterns Patterns that define the list of classes to be excluded (* and ? can be used)
     */
    public void excludes(String... patterns)
    {
        excludes.addAll(Arrays.asList(patterns));
    }

    public void reports(CoverageReport... rs)
    {
        reports.addAll(Arrays.asList(rs));
    }
}
