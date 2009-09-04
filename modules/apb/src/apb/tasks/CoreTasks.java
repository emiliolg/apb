

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


package apb.tasks;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import apb.Apb;
import apb.Environment;

import apb.metadata.UpdatePolicy;

import org.jetbrains.annotations.NotNull;

public class CoreTasks
{
    //~ Methods ..............................................................................................

    /**
     * A convenience method to write a formatted string to the apb output as a log with INFO level.
     *
     * @param  format
     *         A format string as described in {@link java.util.Formatter}.
     *         Properties in the string will be expanded.
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.
     */
    public static void printf(String format, Object... args)
    {
        final Environment env = Apb.getEnv();
        env.logInfo(env.expand(format), args);
    }

    /**
     * Deletes a file or a directory
     * @param name The File or Directory to copy from
     *             Properties in the name will be expanded.
     */
    @NotNull public static DeleteTask delete(@NotNull String name)
    {
        final Environment env = Apb.getEnv();
        return new DeleteTask(new File(env.expand(name)));
    }

    /**
     * Deletes a file or a directory
     * @param file The File or Directory to copy from
     */
    @NotNull public static DeleteTask delete(@NotNull File file)
    {
        return new DeleteTask(file);
    }

    /**
     * Downloads a remote file
     * @param url The File or Directory to copy from
     */
    @NotNull public static Download download(@NotNull String url)
    {
        return new Download(url);
    }

    /**
     * Copy a given file
     * @param from The File or Directory to copy from
     */
    @NotNull public static Copy copy(@NotNull String from)
    {
        return copy(new File(from));
    }

    /**
     * Copy a given file
     * @param from The File or Directory to copy from
     */
    @NotNull public static Copy copy(@NotNull File from)
    {
        return new Copy(Apb.getEnv(), from, false);
    }

    /**
     * Copy a given file doing filtering (keyword expansion)
     * @param from The File or Directory to copy from
     */
    @NotNull public static Copy copyFiltering(@NotNull String from)
    {
        return copyFiltering(new File(from));
    }

    /**
     * Copy a given file doing filtering (keyword expansion)
     * @param from The File or Directory to copy from
     */
    @NotNull public static Copy copyFiltering(@NotNull File from)
    {
        return new Copy(Apb.getEnv(), from, true);
    }

    /**
     * Executes a system command.
     * @param args The command to execute
     */
    @NotNull public static Exec exec(@NotNull String... args)
    {
        return exec(Arrays.asList(args));
    }

    /**
     * Executes a system command.
     * @param args The command to execute
     */
    @NotNull public static Exec exec(@NotNull List<String> args)
    {
        return new Exec(args);
    }

    public static void main(String[] args)
    {
        copy("/x/dir").to("/y/dir").excluding("*.class").execute();
    }

    //~ Inner Classes ........................................................................................

    //

    static class Download
        extends Task
    {
        @NotNull private String target;
        @NotNull private String url;
        private UpdatePolicy    updatePolicy;

        Download(@NotNull String url)
        {
            this.url = url;
            updatePolicy = UpdatePolicy.DAILY;
        }

        @Override public void execute()
        {
            final DownloadTask task = new DownloadTask(env, url, target);
            task.setUpdatePolicy(updatePolicy);
            task.execute();
        }

        /**
         * Specify the target file or directory
         * If not specified, then the file/s will be copied to the current module output
         * @param to The File or directory to copy from
         * @throws IllegalArgumentException if trying to copy a directoy to a single file.
         */
        @NotNull public Download to(@NotNull String to)
        {
            target = to;
            return this;
        }

        /**
         * Define the update policy that specified the frecuency used to check if the source has been updated
         * <p>
         * Examples:
         * <table>
         * <tr>
         *      <td><code>UpdatePolicy.ALWAYS</code>
         *      <td> Check every time the task is executed
         * <tr>
         *      <td><code>UpdatePolicy.NEVER</code>
         *      <td> Only downloads the file if it does not exist
         * <tr>
         *      <td><code>UpdatePolicy.DAILY</code>
         *      <td> Check the source if the local file is older than a day.
         * <tr>
         *      <td><code>UpdatePolicy.every(6)</code>
         *      <td> Check the source every 6 hours
         * <tr>
         *      <td><code>UpdatePolicy.every(0.5)</code>
         *      <td> Check the source every 30 minutes
         * </table>
         * </p>
         * @param policy The update policy to be used.
         */
        public void withUpdatePolicy(@NotNull UpdatePolicy policy)
        {
            updatePolicy = policy;
        }
    }

    static class Exec
        extends Task
    {
        private List<String> args;

        Exec(@NotNull List<String> args)
        {
            this.args = args;
        }

        @Override public void execute()
        {
            new ExecTask(env, args).execute();
        }
    }
}
