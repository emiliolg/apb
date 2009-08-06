

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


package apb.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @BuildProperty public boolean debug = true;

    /**
     * Use of the default error formatting
     * Warning ! This options is incompatible with warnExcludes and failOnWarnings
     *
     */
    @BuildProperty public boolean defaultErrorFormatter = false;

    /**
     * Output source location where deprecated APIs are used
     */
    @BuildProperty public boolean deprecated = false;

    /**
     * Fail on warnings
     */
    @BuildProperty public boolean failOnWarning = false;

    /**
     * Whether to enable recommended warnings
     */
    @BuildProperty public boolean lint = false;

    /**
     * Enable specific warnings (Comma separated list) :
     * {all,cast,deprecation,divzero,empty,unchecked,fallthrough,path,serial,finally,overrides,
     * -cast,-deprecation,-divzero,-empty,-unchecked,-fallthrough,-path,-serial,-finally,-overrides,none}
     */
    @BuildProperty public String lintOptions = "";

    /**-     * Provide source compatibility with specified release
     */
    @BuildProperty public String source = "";

    /**
     * Generate class files for specific VM version
     */
    @BuildProperty public String target = "";

    /**
     * Wheter to validate that all dependencies are being used
     * If it's true and there are some unused dependencies it will fail with the list of unused ones
     */
    @BuildProperty public boolean validateDependencies = false;

    /**
     * Generate warnings
     */
    @BuildProperty public boolean warn = true;

    /**
     * Options to pass to the annotation processor
     */
    private final Map<String, String> annotationOptions = new HashMap<String, String>();

    /**
     * The list of files to exclude from compilation.
     */
    private final List<String> excludes = new ArrayList<String>(asList("**/package-info.java"));

    /**
     * Extra classpath jars
     * This is usually used for annotation processing, etc
     */
    private final List<Library> extraLibraries = new ArrayList<Library>();

    /**
     * The list of files to compile.
     *
     */
    private final List<String> includes = new ArrayList<String>(asList("**/*.java"));

    /**
     * Do not generate warnings for the following list of files
     */
    private final List<String> warnExcludes = new ArrayList<String>();

    /**
     * Controls whether annotation processing and/or compilation is done.
     */
    private ProcessingOption processingOption = ProcessingOption.DEFAULT;

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

    public List<Library> extraLibraries()
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
    public void extraLibraries(Library... libraries)
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

    public ProcessingOption getProcessingOption() {
        return processingOption;
    }

    public void setProcessingOption(@NotNull ProcessingOption processingOption) {
        this.processingOption = processingOption;
    }

    public enum ProcessingOption {
        /**
         * Both compilation and annotation processing are performed.
         */
        DEFAULT {
            public String paramValue() {
                return null;
            }
        },

        /**
         * No annotation processing takes place.
         */
        NONE {
            public String paramValue() {
                return "none";
            }
        },

        /**
         * Only annotation processing is done.
         */
        ONLY {
            public String paramValue() {
                return "only";
            }
        };
        
        public abstract String paramValue();
    }
}
