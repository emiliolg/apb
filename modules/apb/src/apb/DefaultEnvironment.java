

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
import java.util.EnumSet;
import java.util.Map;
import java.util.TreeMap;

import apb.utils.DebugOption;
import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// User: emilio
// Date: Aug 24, 2009
// Time: 7:34:40 PM

public abstract class DefaultEnvironment
    implements Environment
{
    //~ Instance fields ......................................................................................

    /**
     * Control what to show when logging
     */

    @NotNull protected final EnumSet<DebugOption> debugOptions;

    @NotNull protected final Map<String, String> properties;
    private boolean                              failOnError = true;
    private boolean                              forceBuild;

    private boolean nonRecursive;

    /**
     * Processing and messaging options
     */
    private boolean quiet;

    //~ Constructors .........................................................................................

    protected DefaultEnvironment()
    {
        debugOptions = EnumSet.noneOf(DebugOption.class);
        properties = new TreeMap<String, String>();
    }

    //~ Methods ..............................................................................................

    @Nullable public final String getOptionalProperty(@NotNull String id)
    {
        String result = overrideProperty(id);
        return result != null ? result : properties.get(id);
    }

    public void putProperty(@NotNull String name, @NotNull String value)
    {
        if (mustShow(DebugOption.PROPERTIES)) {
            logVerbose("property %s=%s\n", name, value);
        }

        properties.put(name, value);
    }

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
    @NotNull public String expand(@Nullable String string)
    {
        if (apb.utils.StringUtils.isEmpty(string)) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        StringBuilder id = new StringBuilder();

        boolean insideId = false;
        boolean closeWithBrace = false;

        for (int i = 0; i < string.length(); i++) {
            char chr = string.charAt(i);

            if (insideId) {
                insideId =
                    closeWithBrace
                    ? chr != '}' : java.lang.Character.isJavaIdentifierPart(chr) || chr == '.' || chr == '-';

                if (insideId) {
                    id.append(chr);
                }
                else {
                    result.append(getProperty(id.toString()));
                    id.setLength(0);

                    if (!closeWithBrace) {
                        result.append(chr);
                    }
                }
            }
            else if (chr == '$') {
                insideId = true;

                if (i + 1 < string.length() && string.charAt(i + 1) == '{') {
                    i++;
                    closeWithBrace = true;
                }
            }
            else if (chr == '\\' && i + 1 < string.length() && string.charAt(i + 1) == '$') {
                result.append('$');
                i++;
            }
            else {
                result.append(chr);
            }
        }

        if (insideId) {
            result.append(getProperty(id.toString()));
        }

        return result.toString();
    }

    /**
     * Handle an Error. It creates a build Exception with the specified msg.
     * And delegates the handling to {@link #handle(Throwable t)}
     * @param msg The message used to create the build exception
     */
    public void handle(@NotNull String msg)
    {
        handle(new BuildException(msg));
    }

    /**
     * Handle an Error.
     * Either raise the exception or log it depending on the value of the failOnError flag
     * @param e The Exception causing the failure
     */
    public void handle(@NotNull Throwable e)
    {
        if (isFailOnError()) {
            throw (e instanceof BuildException) ? (BuildException) e : new BuildException(e);
        }

        logSevere(e.getMessage());
    }

    @NotNull public Os getOs()
    {
        return Os.getInstance();
    }

    public void abort(String msg)
    {
        logInfo(msg);
        System.exit(1);
    }

    /**
     * Return the value of the specified boolean property
     * @param id The property to search
     * @param defaultValue The default value
     * @return The value of the property or false if the property is not set
     */
    public final boolean getBooleanProperty(@NotNull String id, boolean defaultValue)
    {
        return Boolean.parseBoolean(getProperty(id, Boolean.toString(defaultValue)));
    }

    /**
     * Returns true if log level is quiet
     * @return true if log level is quiet
     */
    public boolean isQuiet()
    {
        return quiet;
    }

    public void setQuiet()
    {
        quiet = true;
    }

    /**
     * Returns true if the build must NOT proceed recursive to the module dependecies
     */
    public boolean isNonRecursive()
    {
        return nonRecursive;
    }

    public void setNonRecursive()
    {
        nonRecursive = true;
    }

    public void setFailOnError(boolean b)
    {
        failOnError = b;
    }

    /**
     * Returns true if we want the build to proceed unconditionally without checking file timestamps
     * @return true if we want the build to proceed unconditionally without checking file timestamps
     */
    public boolean forceBuild()
    {
        return forceBuild;
    }

    public void setForceBuild(boolean b)
    {
        forceBuild = b;
    }

    /**
     * Returns true if log level is verbose
     * @return true if log level is verbose
     */
    public boolean isVerbose()
    {
        return !debugOptions.isEmpty();
    }

    /**
     * Returns true if must show the following option
     */
    public boolean mustShow(DebugOption option)
    {
        return debugOptions.contains(option);
    }

    public void setDebugOptions(@NotNull EnumSet<DebugOption> options)
    {
        debugOptions.addAll(options);

        if (!options.isEmpty()) {
            setVerbose();
        }
    }

    /**
     * Return the value of the specified property
     * @param id The property to search
     * @param defaultValue The default value to return in case the property is not set
     * @return The value of the property
     */
    @NotNull public final String getProperty(@NotNull String id, @NotNull String defaultValue)
    {
        String result = getOptionalProperty(id);
        return result == null ? defaultValue : result;
    }

    /**
     * Return the value of the specified property
     * @param id The property to search
     * @return The value of the property or the empty String if the Property is not found and failOnError is not set
     */
    @NotNull public final String getProperty(@NotNull String id)
    {
        String result = getOptionalProperty(id);

        if (result == null) {
            handle(new PropertyException(id));
            result = "";
        }

        return result;
    }

    public boolean hasProperty(@NotNull String id)
    {
        return getOptionalProperty(id) != null;
    }

    public boolean isFailOnError()
    {
        return failOnError;
    }

    /**
     * Returns a File object whose path is relative to the basedir
     * @param name The (Usually relative to the basedir) file name.
     * @return A file whose path is relative to the basedir.
     */
    @NotNull public final File fileFromBase(@NotNull String name)
    {
        final File child = new File(expand(name));
        return FileUtils.normalizeFile(child.isAbsolute()
                                       ? child : new File(getModuleHelper().getBaseDir(), child.getPath()));
    }

    /**
     * Returns a File object whose path is relative to the source directory of the current module
     * @param name The (Usually relative to the source directory of the module) file name.
     * @return A file whose path is relative to the source directory of the current module.
     */
    @NotNull public final File fileFromSource(@NotNull String name)
    {
        final File child = new File(expand(name));
        return child.isAbsolute() ? child : new File(getModuleHelper().getSource(), child.getPath());
    }

    /**
     * Returns a File object whose path is relative to the generated source directory of the current module
     * @param name The (Usually relative to the generated source directory of the module) file name.
     * @return A file whose path is relative to the generated source directory of the current module.
     */
    @NotNull public final File fileFromGeneratedSource(@NotNull String name)
    {
        final File child = new File(expand(name));
        return child.isAbsolute() ? child : new File(getModuleHelper().getGeneratedSource(), child.getPath());
    }

    /**
     * Return current ModuleHelper
     * @return current Module Helper
     */
    @NotNull public ModuleHelper getModuleHelper()
    {
        throw new IllegalStateException("Not current Module");
    }

    /**
     * Get the base directory of the current Module
     * @return the base directory of the current Module
     * @throws IllegalStateException If there is no current module
     */
    @NotNull public File getBaseDir()
    {
        return getModuleHelper().getBaseDir();
    }

    /**
     * Get current module output directory
     * @return current module output directory
     * @throws IllegalStateException If there is no current module
     */
    @NotNull public File getOutputDir()
    {
        return getModuleHelper().getOutput();
    }

    @Nullable protected String overrideProperty(@NotNull String id)
    {
        return null;
    }

    @Nullable protected String retrieveProperty(@NotNull String id)
    {
        return properties.get(id);
    }

    protected void setVerbose() {}
}