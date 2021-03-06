

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

import java.io.File;
import java.util.Collection;

// User: emilio
// Date: Aug 22, 2009
// Time: 11:48:41 AM

/**
 * This interface allows the application to interface with
 * the environment in which the application is running.
 * There is a top level <code>Environment</code> for APB itself and one per Module
 * being built.
 * The current <code>Environment</code> can be obtained from the <code>Apb.getEnv()</code> method.
 *
 */
public interface Environment
{
    //~ Methods ..............................................................................................

    /**
     * Get the environment id
     */
    String getId();

    /**
     * Log items with INFO Level using the specified format string and
     * arguments.
     *
     * @param  msg
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     */
    void logInfo(String msg, Object... args);

    /**
     * Log items with WARNING Level using the specified format string and
     * arguments.
     *
     * @param  msg
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     */
    void logWarning(String msg, Object... args);

    /**
     * Log items with SEVERE Level using the specified format string and
     * arguments.
     *
     * @param  msg
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     */
    void logSevere(String msg, Object... args);

    /**
     * Log items with VERBOSE Level using the specified format string and
     * arguments.
     *
     * @param  msg
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     */
    void logVerbose(String msg, Object... args);

    /**
     * Handle an Error. It creates a build Exception with the specified msg.
     * And delegates the handling to {@link #handle(Throwable t)}
     * @param msg The message used to create the build exception
     */
    void handle(String msg);

    /**
     * Handle an Error.
     * Either raise the exception or log it depending on the value of the failOnError flag
     * @param e The Exception causing the failure
     */
    void handle(Throwable e);

    /**
     * Returns true if we want the build to proceed unconditionally without checking file timestamps
     * @return true if we want the build to proceed unconditionally without checking file timestamps
     */
    boolean forceBuild();

    /**
     * Returns true if log level is verbose
     * @return true if log level is verbose
     */
    boolean isVerbose();

    /**
     * Returns true if must show the following option
     */
    //boolean mustShow(DebugOption option);

    /**
     * Returns true if log level is quiet
     * @return true if log level is quiet
     */
    boolean isQuiet();

    /**
     * Returns true if the build must NOT proceed recursive to the module dependecies
     */
    boolean isNonRecursive();

    /**
     * Get the base directory of the current Module
     * @return the base directory of the current Module
     * @throws IllegalStateException If there is no current module
     */
    File getBaseDir();

    /**
     * Get current module output directory
     * @return current module output directory
     * @throws IllegalStateException If there is no current module
     */
    File getOutputDir();

    /**
     * Return the value of the specified property
     * @param id The property to search
     * @return The value of the property or the empty String if the Property is not found and failOnError is not set
     */
    String getProperty(String id);

    /**
     * Return the value of the specified property
     * @param id The property to search
     * @param defaultValue The default value to return in case the property is not set
     * @return The value of the property
     */
    String getProperty(String id, String defaultValue);

    /**
     * Returns true if the specified property has an associated value in the current environment,
     * false otherwise
     *
     * @param id The id of the property to search
     */
    boolean hasProperty(String id);

    /**
     * Return the value of the specified boolean property
     * @param id The property to search
     * @param defaultValue The default value
     * @return The value of the property or false if the property is not set
     */
    boolean getBooleanProperty(String id, boolean defaultValue);

    /**
     * Process the string expanding property values.
     * The `$' character introduces property expansion.
     * The property  name  or  symbol  to  be expanded  may be enclosed in braces,
     * which are optional but serve to protect the variable to be expanded from characters
     * immediately following it which could be interpreted as part of the name.
     * When braces are used, the matching ending brace is the first `}' not escaped by a backslash
     *
     * @param string The string to be expanded.
     * @return An String with properties expanded.
     */
    String expand(String string);

    /**
     * Returns a File object whose path is relative to the basedir
     * @param name The (Usually relative to the basedir) file name.
     * @return A file whose path is relative to the basedir.
     */
    File fileFromBase(String name);

    /**
     * Returns a File object whose path is relative to the source directory of the current module
     * @param name The (Usually relative to the source directory of the module) file name.
     * @return A file whose path is relative to the source directory of the current module.
     */
    File fileFromSource(String name);

    /**
     * Returns a File object whose path is relative to the generated source directory of the current module
     * @param name The (Usually relative to the generated source directory of the module) file name.
     * @return A file whose path is relative to the generated source directory of the current module.
     */
    File fileFromGeneratedSource(String name);

    /**
     * Get the Extension Jars to be searched when we compile definitions
     * @return the extension Jars to be searched when we compiled definitions
     */
    Collection<File> getExtClassPath();

    /**
     * Returns true if Apb has to abort execution when an error was detected or not.
     */
    boolean isFailOnError();

    /**
     * Abort the build, displaying a message.
     * @param msg Message to be displayed when aborting.
     */
    void abort(String msg);

    /**
     * Get an optional property. It can return null
     * @param id The property to get
     * @return The property if it has value, null otherwise.
     */
    String getOptionalProperty(String id);

    /**
     * Assign a new value for the specified property in the current environment
     * @param name  The property id
     * @param value The new value to assign to the specified property
     */
    void putProperty(String name, String value);

    /**
     * Returns the Logger associated to the current Environment
     */
    Logger getLogger();

    /**
     * Control wheter to execute commands for the current target or for all dependencies of the current target
     */
    void setNonRecursive(boolean b);
}
