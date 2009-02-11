
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import static java.util.Arrays.asList;

/**
 * Define information for the compile task
 */
public class CompileInfo
{
    //~ Instance fields ......................................................................................

    /**
     * Generate Debug information.
     */
    public boolean debug = true;

    /**
     * Output source location where deprecated APIs are used
     */
    public boolean deprecated = false;

    /**
     * Whether to enable recommended warnings
     */
    public boolean lint = false;

    /**
     * Enable specific warnings (Comma separated list) :
     * {all,cast,deprecation,divzero,empty,unchecked,fallthrough,path,serial,finally,overrides,
     * -cast,-deprecation,-divzero,-empty,-unchecked,-fallthrough,-path,-serial,-finally,-overrides,none}
     */
    public String lintOptions = "";

    /**-     * Provide source compatibility with specified release
     */
    public String source = "";

    /**
     * Generate class files for specific VM version
     */
    public String target = "";

    /**
     * Generate warnings
     */
    public boolean warn = true;

    /**
     * Do not generate warnings for the following list of files
     */
    private final List<String> warnExcludes = new ArrayList<String>();

    /**
     * Options to pass to the annotation processor
     */
    private final Map<String, String> annotationOptions = new HashMap<String, String>();

    /**
     * The list of files to exclude from compilation.
     */
    private final List<String> excludes = new ArrayList<String>();

    /**
     * Extra classpath jars
     * This is usually used for annotation processing, etc
     */
    private final List<LocalLibrary> extraLibraries = new ArrayList<LocalLibrary>();

    /**
     * The list of files to compile.
     *
     */
    private final List<String> includes = new ArrayList<String>(asList("**/*.java"));

    /**
     * Use of the default error formatting
     * Warning ! This options is incompatible with warnExcludes and failOnWarnings
     *
     */
    public boolean defaultErrorFormatter = false;

    /**
     * Fail on warnings
     */
    public boolean failOnWarning;

    //~ Methods ..............................................................................................

    public List<String> includes()
    {
        return includes;
    }

    public List<String> excludes()
    {
        return excludes;
    }

    public List<String> warnExcludes()
    {
        return warnExcludes;
    }

    public List<LocalLibrary> extraLibraries()
    {
        return extraLibraries;
    }

    /**
     * Method used to define the list of files to be compiled.
     * @param patterns The list of patterns to be included
     */
    public void includes(String... patterns)
    {
        includes.addAll(asList(patterns));
    }

    /**
     * Method used to define the list of files to be excluded from compilation
     * @param patterns The list of patterns to be excluded
     */
    public void excludes(String... patterns)
    {
        excludes.addAll(asList(patterns));
    }
    /**
     * Method used to define the list of files to be excluded from warning generation
     * @param patterns The list of patterns to be excluded
     */
    public void warnExcludes(String... patterns)
    {
        warnExcludes.addAll(asList(patterns));
    }

    /**
     * Method used to define the list of extra libraries to add to the compilation classpath.
     * This is usually used to add annotation libraries
     * @param libraries The list of libraries to add
     */
    public void extraLibraries(LocalLibrary... libraries)
    {
        extraLibraries.addAll(asList(libraries));
    }

    public Map<String, String> annotationOptions()
    {
        return annotationOptions;
    }

    public void setAnnotationOption(@NotNull final String key, @NotNull final String value)
    {
        annotationOptions.put(key, value);
    }
}
