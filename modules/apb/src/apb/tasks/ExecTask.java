
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//
// User: emilio
// Date: Oct 1, 2008
// Time: 4:58:01 PM

//
public class ExecTask
    extends ConditionalTask
{
    //~ Instance fields ......................................................................................

    private boolean redirectErrorStream;

    @NotNull private File currentDirectory;

    private int                                exitValue;
    @NotNull private final List<String>        args;
    @Nullable private List<String>             output;
    @NotNull private final Map<String, String> environment;

    /**
     * The command to be executed
     */
    @NotNull private final String cmd;

    //~ Constructors .........................................................................................

    ExecTask(@NotNull String cmd, @NotNull List<String> args)
    {
        this.cmd = cmd;
        this.args = args;
        environment = new HashMap<String, String>();
        currentDirectory = env.getBaseDir();
    }

    //~ Methods ..............................................................................................

    public void execute()
    {
        logCommand();

        final Process p;

        final Thread loggerThread;

        try {
            p = createProcess();

            loggerThread = redirectErrorStream ? null : logStream(p.getErrorStream(), true, true);

            if (output == null) {
                logStream(p.getInputStream(), false, false);
            }
            else {
                loadStream(p.getInputStream());
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            try {
                p.waitFor();
            }
            finally {
                if (loggerThread != null) {
                    loggerThread.join();
                }
            }
        }
        catch (InterruptedException e) {
            env.handle(e);
        }

        exitValue = p.exitValue();
    }

    public int getExitValue()
    {
        return exitValue;
    }

    private static Thread startJob(Runnable target)
    {
        final Thread thread = new Thread(target);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    public ExecTask outputTo(@NotNull List<String> o)
    {
        output = o;
        return this;
    }

    /**
     * Define current working directory for the command to be executed
     * @param directory The directory to change to
     */
    public ExecTask onDir(@NotNull String directory)
    {
        currentDirectory = env.fileFromBase(directory);
        return this;
    }

    /**
     * Set the wnvironment to the one specified
     * @param e The environment to be set
     */
    public ExecTask withEnvironment(@NotNull Map<String, String> e)
    {
        environment.clear();
        environment.putAll(e);
        return this;
    }

    /**
     * Wheter to redirect the standard error of the command to the same output of the standard output or not
     */
    public ExecTask redirectErrorStream(boolean b)
    {
        redirectErrorStream = b;
        return this;
    }

    protected void insertArguments(List<String> argList)
    {
        args.addAll(0, argList);
    }

    private Process createProcess()
        throws IOException
    {
        List<String> allArgs = new ArrayList<String>();
        allArgs.add(cmd);
        allArgs.addAll(args);
        ProcessBuilder      b = new ProcessBuilder(allArgs);
        Map<String, String> e = b.environment();

        for (Map.Entry<String, String> entry : environment.entrySet()) {
            e.put(entry.getKey(), entry.getValue());
        }

        b.directory(currentDirectory);

        if (redirectErrorStream) {
            b.redirectErrorStream(true);
        }

        return b.start();
    }

    @Nullable private Thread logStream(InputStream in, final boolean severe, final boolean background)
        throws IOException
    {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        if (background) {
            final Runnable target =
                new Runnable() {
                    public void run()
                    {
                        try {
                            loggerLoop(reader, severe);
                        }
                        catch (IOException e) {
                            env.logSevere(e.toString());
                        }
                    }
                };

            return startJob(target);
        }

        loggerLoop(reader, severe);
        return null;
    }

    private void loggerLoop(BufferedReader reader, boolean severe)
        throws IOException
    {
        String line;

        while ((line = reader.readLine()) != null) {
            if (severe) {
                env.logSevere("%s\n", line);
            }
            else {
                env.logInfo("%s\n", line);
            }
        }
    }

    private void loadStream(InputStream is)
        throws IOException
    {
        List<String> o = output;
        assert o != null;
        final BufferedReader out = new BufferedReader(new InputStreamReader(is));

        String line;

        while ((line = out.readLine()) != null) {
            o.add(line);
        }
    }

    private void logCommand()
    {
        if (env.isVerbose()) {
            logVerbose("Executing: \n");

            for (String arg : args) {
                logVerbose("     %s\n", arg);
            }

            final Map<String, String> e = environment;

            if (!e.isEmpty()) {
                logVerbose("Environment: \n");

                for (Map.Entry<String, String> entry : e.entrySet()) {
                    logVerbose("            %s='%s'\n", entry.getKey(), entry.getValue());
                }
            }

            logVerbose("Current directory: %s\n", currentDirectory);
        }
    }
}
