

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

import org.jetbrains.annotations.NotNull;

//
// User: emilio
// Date: Sep 1, 2009
// Time: 2:43:34 PM

/**
 * This interace is used by APB to log messages during the build process.
 * Apb will use by default a Logger that prints messages to the standard output.
 */
public interface Logger
{
    //~ Methods ..............................................................................................

    /**
     * Logs a formatted string using the specified
     * format string and arguments.
     * The message must only be logged if the Logger is enabled for the specified level
     *
     * @param level The message log level
     *
     * @param  msg
     *         A format string as described in <a
     *         href="../util/Formatter.html#syntax">Format string syntax</a>
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.
     */
    void log(@NotNull Level level, @NotNull String msg, Object... args);

    /**
     * Enable logging for all mesages of this level or higher ones
     * @param level
     */
    void setLevel(@NotNull Level level);

    //~ Enums ................................................................................................

    /**
     * A enum that defined different Log levels.
     * Log levels are ordered by level granularity.
     * So if <code>Level.INFO</code> is enabled,
     *  messages with <code>INFO, WARNING and SEVERE</code> levels will be output.
     */
    public enum Level
    {
        VERBOSE,
        INFO,
        WARNING,
        SEVERE;
    }
}
