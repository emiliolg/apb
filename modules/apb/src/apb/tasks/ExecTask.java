
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import apb.Environment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//
// User: emilio
// Date: Oct 1, 2008
// Time: 4:58:01 PM

//
public class ExecTask
    extends CommandTask
{
    //~ Instance fields ......................................................................................

    private int                          exitValue;
    @Nullable private final List<String> output;

    //~ Constructors .........................................................................................

    public ExecTask(@NotNull Environment env, @NotNull List<String> cmd)
    {
        super(env, cmd);
        output = null;
    }

    public ExecTask(@NotNull Environment env, @NotNull String... args)
    {
        this(env, new ArrayList<String>(Arrays.asList(args)));
    }

    public ExecTask(@NotNull Environment env, @NotNull List<String> output, @NotNull List<String> cmd)
    {
        super(env, cmd);
        this.output = output;
    }

    //~ Methods ..............................................................................................

    public static List<String> executeTo(@NotNull Environment env, @NotNull List<String> cmd)
    {
        List<String> output = new ArrayList<String>();
        ExecTask     task = new ExecTask(env, output, cmd);
        task.execute();
        return output;
    }

    public void execute()
    {
        logCommand();

        final Process p;

        final Thread loggerThread;

        try {
            p = createProcess();

            if (output == null) {
                loggerThread = null;
                logStream(p.getInputStream(), false, false);
            }
            else {
                loggerThread = logStream(p.getErrorStream(), true, true);
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

    private Process createProcess()
        throws IOException
    {
        ProcessBuilder      b = new ProcessBuilder(getArguments());
        Map<String, String> e = b.environment();

        for (Map.Entry<String, String> entry : getEnvironment().entrySet()) {
            e.put(entry.getKey(), entry.getValue());
        }

        b.directory(getCurrentDirectory());

        if (output == null) {
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
        assert output != null;
        final BufferedReader out = new BufferedReader(new InputStreamReader(is));

        String line;

        while ((line = out.readLine()) != null) {
            output.add(line);
        }
    }

    private void logCommand()
    {
        if (env.isVerbose()) {
            logVerbose("Executing: \n");

            for (String arg : getArguments()) {
                logVerbose("     %s\n", arg);
            }

            final Map<String, String> e = getEnvironment();

            if (!e.isEmpty()) {
                logVerbose("Environment: \n");

                for (Map.Entry<String, String> entry : e.entrySet()) {
                    logVerbose("            %s='%s'\n", entry.getKey(), entry.getValue());
                }
            }

            logVerbose("Current directory: %s\n", getCurrentDirectory());
        }
    }
}
