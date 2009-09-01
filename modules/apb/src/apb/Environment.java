

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
import java.util.EnumSet;

import apb.utils.DebugOption;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;  // Copyright 2008-2009 Emilio Lopez-Gabeiras

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

// User: emilio
// Date: Aug 22, 2009
// Time: 11:48:41 AM

public interface Environment
{
    //~ Methods ..............................................................................................

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
    void handle(@NotNull String msg);

    /**
     * Handle an Error.
     * Either raise the exception or log it depending on the value of the failOnError flag
     * @param e The Exception causing the failure
     */
    void handle(@NotNull Throwable e);

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
    boolean mustShow(DebugOption option);

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
    @NotNull File getBaseDir();

    /**
     * Get current module output directory
     * @return current module output directory
     * @throws IllegalStateException If there is no current module
     */
    @NotNull File getOutputDir();

    /**
     * Returns a representation of the current Operating System
     */
    @NotNull Os getOs();

    /**
     * Return the value of the specified property
     * @param id The property to search
     * @return The value of the property or the empty String if the Property is not found and failOnError is not set
     */
    @NotNull String getProperty(@NotNull String id);

    /**
     * Return the value of the specified property
     * @param id The property to search
     * @param defaultValue The default value to return in case the property is not set
     * @return The value of the property
     */
    @NotNull String getProperty(@NotNull String id, @NotNull String defaultValue);

    boolean hasProperty(@NotNull String id);

    /**
     * Return the value of the specified boolean property
     * @param id The property to search
     * @param defaultValue The default value
     * @return The value of the property or false if the property is not set
     */
    boolean getBooleanProperty(@NotNull String id, boolean defaultValue);

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
    @NotNull String expand(@Nullable String string);

    /**
     * Returns a File object whose path is relative to the basedir
     * @param name The (Usually relative to the basedir) file name.
     * @return A file whose path is relative to the basedir.
     */
    @NotNull File fileFromBase(@NotNull String name);

    /**
     * Returns a File object whose path is relative to the source directory of the current module
     * @param name The (Usually relative to the source directory of the module) file name.
     * @return A file whose path is relative to the source directory of the current module.
     */
    @NotNull File fileFromSource(@NotNull String name);

    /**
     * Returns a File object whose path is relative to the generated source directory of the current module
     * @param name The (Usually relative to the generated source directory of the module) file name.
     * @return A file whose path is relative to the generated source directory of the current module.
     */
    @NotNull File fileFromGeneratedSource(@NotNull String name);

    /**
     * Get the Extension Jars to be searched when we compile definitions
     * @return the extension Jars to be searched when we compiled definitions
     */
    Collection<File> getExtClassPath();

    boolean isFailOnError();

    /**
     * Abort the build, displaying a message.
     * @param msg Message to be displayed when aborting.
     */
    void abort(String msg);

    /**
     * Log with the lowest level
     */
<<<<<<< HEAD:modules/apb/src/apb/Environment.java
    void setQuiet();
=======
    @NotNull public ProjectHelper getProjectHelper()
    {
        if (currentElement == null || !(currentElement instanceof ProjectHelper)) {
            throw new IllegalStateException("Not current Project");
        }

        return (ProjectHelper) currentElement;
    }

    public ProjectElementHelper getCurrent()
    {
        return currentElement;
    }

    public void forward(@NotNull String command, Iterable<? extends Module> modules)
    {
        for (Module module : modules) {
            final ProjectBuilder pb = ProjectBuilder.getInstance();
            pb.build(pb.getHelper(module), command);
        }
    }

    public void putProperty(String name, String value)
    {
        if (mustShow(DebugOption.PROPERTIES)) {
            logVerbose("property %s=%s\n", name, value);
        }

        projectProperties.put(name, value);
    }

    public void setProperties(Map<String, String> values)
    {
        System.out.println("values = " + values);
    }

    public String getBaseProperty(String propertyName)
    {
        return baseProperties.get(propertyName);
    }

    public void setDebugOptions(@NotNull EnumSet<DebugOption> options)
    {
        debugOptions.addAll(options);

        if (!options.isEmpty()) {
            setVerbose();
        }
    }

    protected void setVerbose() {}
    /**
     * Get an optional property. It can return null
     * @param id The property to get
     * @return The property if it has value, null otherwise.
     */
    @Nullable String getOptionalProperty(String id);

    void putProperty(@NotNull String name, @NotNull String value);
}
