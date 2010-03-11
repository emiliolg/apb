

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


package apb;

import org.jetbrains.annotations.NonNls;

//
// User: emilio
// Date: Sep 3, 2008
// Time: 4:41:31 PM

//
/**
 * I18n class
 * @exclude
 */
public class Messages
{
    //~ Methods ..............................................................................................

    public static String INVOPT(String option)
    {
        return "Invalid option: " + option + " (use -h or --help to see a list of available options.)";
    }

    public static String NONBOOLNEG(String letter, String name)
    {
        return "Can't negate option -" + letter + "(--" + name +
               " ), negation only applies to boolean options.";
    }

    public static String EXPECTEDARG(String letter, String name)
    {
        return "Option -" + letter + " (--" + name + ") expects an argument.";
    }

    public static String INVARG(String value, String name)
    {
        return "Invalid " + name + " value: " + value;
    }

    public static String INV_PROJECT_DIR(String dir)
    {
        return "Project path directory: '" + dir + "' does not exist.\n";
    }

    public static String BUILD_COMPLETED(String ts)
    {
        return "\nBUILD COMPLETED in " + ts + " milliseconds.\n";
    }

    public static String MANIFEST_OVERRIDE(final String file)
    {
        return "Module's manifest is overridden by " + file + "\n";
    }

    //~ Static fields/initializers ...........................................................................

    public static final String COMMANDS =
        "command : help and others. (Execute the help command over a module to get the actual list).";

    public static final String BUILD_FAILED = "\nBUILD FAILED !!\n";

    @NonNls public static final String SHOW_STACK_TRACE = "Show stack trace for build exception.";

    @NonNls public static final String QUIET_OUTPUT = "Quiet output - only show errors.";
    @NonNls public static final String VERBOSE = "Be extra verbose.";
    @NonNls public static final String CONTINUE_AFTER_ERROR = "Continue after error.";
    @NonNls public static final String FORCE_BUILD = "Force build (Do not check timestamps).";
    @NonNls public static final String NON_RECURSIVE = "Do not recurse over module dependencies.";
    @NonNls public static final String DEFINE_PROPERTY = "Define a property.";
    @NonNls public static final String TRACK_EXECUTION = "Track execution statistics.";
    @NonNls public static final String DEBUG = "What to show when doing verbose output.";
    @NonNls public static final String COLON_SEPARATED_PATTERNS = "<pattern:pattern..>";
    @NonNls public static final String SET_TO_INCLUDE = "Set of test files to include.";
    @NonNls public static final String SET_TO_EXCLUDE = "Set of test files to exclude.";
    @NonNls public static final String TEST_GROUPS = "Set of test groups to execute.";
    @NonNls public static final String REPORT_SPEC_FILE =
        "Specify a serialized object with the report specification";
    @NonNls public static final String OUTPUT_FOR_REPORTS =
        "Output directory for reports (Default to tests-dir)";

    @NonNls public static final String MODULE_OR_PROJECT =
        "Mod     : module or project specification defined as 'Mod.java' in the project path.";

    public static final String OPT_IN_BRACKETS = "[options] ";
    public static final String WHERE = "Where:";
    public static final String OPTIONS = "Options: ";
    public static final String HELP = "Display command line options help. ";
    public static final String VERSION = "Print version information and exit.";
}
