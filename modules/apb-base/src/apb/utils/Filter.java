

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


package apb.utils;

import java.util.regex.Pattern;

import apb.Environment;
//
// User: emilio
// Date: Sep 29, 2009
// Time: 11:14:48 AM

public interface Filter
{
    //~ Methods ..............................................................................................

    String filter(String str);

    //~ Inner Classes ........................................................................................

    public static class Factory
    {
        /**
        * This filter expands ocurrences of $var or ${var} by the value of the property
        * in the specified Environment
        *
        * @param   env
        *          the envorinment used to expand the properties
        *
        * @see apb.Environment#expand(String)
        */
        public static Filter expandProperties(final Environment env)
        {
            return new Filter() {
                public String filter(String str)
                {
                    return env.expand(str);
                }

                public String toString()
                {
                    return "Expanding Properties";
                }
            };
        }

        /**
         * This filter replaces each substring of the input that matches the given <a
         * href="../util/regex/Pattern.html#sum">regular expression</a> with the
         * given replacement.
         *
         * @param   regex
         *          the regular expression to which this string is to be matched
         *
         * @throws java.util.regex.PatternSyntaxException
         *          if the regular expression's syntax is invalid
         *
         * @see java.util.regex.Pattern
         */
        public static Filter replaceAll(String regex, final String replacement)
        {
            final Pattern pattern = Pattern.compile(regex);
            return new Filter() {
                public String filter(String str)
                {
                    return pattern.matcher(str).replaceAll(replacement);
                }

                public String toString()
                {
                    return "Replacing " + pattern + " by " + replacement;
                }
            };
        }
    }
}
