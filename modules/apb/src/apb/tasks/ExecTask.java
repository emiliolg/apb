

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
import java.util.HashMap;
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
    extends Task
{
    //~ Instance fields ......................................................................................

    @NotNull private final List<String>        cmd;
    @NotNull private final Map<String, String> environment;
    private int                                exitValue;
    @Nullable private final List<String>       output;

    //~ Constructors .........................................................................................

    public ExecTask(@NotNull Environment env, @NotNull List<String> cmd)
    {
        super(env);
        this.cmd = cmd;
        output = null;
        environment = new HashMap<String, String>();
    }

    public ExecTask(@NotNull Environment env, @NotNull String... args)
    {
        this(env, new ArrayList<String>(Arrays.asList(args)));
    }

    public ExecTask(@NotNull Environment env, @NotNull List<String> output, @NotNull List<String> cmd)
    {
        super(env);
        this.cmd = cmd;
        this.output = output;
        environment = new HashMap<String, String>();
    }

    //~ Methods ..............................................................................................

    public static List<String> executeTo(@NotNull Environment env, @NotNull List<String> cmd)
    {
        List<String> output = new ArrayList<String>();
        ExecTask     task = new ExecTask(env, output, cmd);
        task.execute();
        return output;
    }

    /**
     * Add one or more arguments to the command line to be executed
     * @param arguments The arguments to be added
     */
    public void addArguments(@NotNull String... arguments)
    {
        for (String arg : arguments) {
            if (arg != null)
                cmd.add(arg);
        }
    }

    public void execute()
    {
        try {
            logCommand();
            Process p = createProcess();

            if (output == null) {
                logStream(p.getInputStream());
            }
            else {
                logStream(p.getErrorStream());
                loadStream(p.getInputStream());
            }

            try {
                p.waitFor();
            }
            catch (InterruptedException e) {
                env.handle(e);
            }

            exitValue = p.exitValue();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void putEnv(String key, String value)
    {
        environment.put(key, value);
    }

    public void putAll(@Nullable Map<String, String> environmentVariables)
    {
        if (environmentVariables != null) {
            environment.putAll(environmentVariables);
        }
    }

    public int getExitValue()
    {
        return exitValue;
    }

    private Process createProcess()
        throws IOException
    {
        ProcessBuilder      b = new ProcessBuilder(cmd);
        Map<String, String> e = b.environment();

        for (Map.Entry<String, String> entry : environment.entrySet()) {
            e.put(entry.getKey(), entry.getValue());
        }

        b.directory(currentDirectory);

        if (output == null) {
            b.redirectErrorStream(true);
        }

        return b.start();
    }

    private void logStream(InputStream in)
        throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line;

        while ((line = reader.readLine()) != null) {
            env.logInfo(line);
        }
    }

    private void loadStream(InputStream is)
        throws IOException
    {
        if (output != null) {
            BufferedReader out = new BufferedReader(new InputStreamReader(is));
            String         line;

            while ((line = out.readLine()) != null) {
                output.add(line);
            }
        }
    }

    private void logCommand()
    {
        if (env.isVerbose()) {
            env.logVerbose("Executing: \n");

            for (String arg : cmd) {
                env.logVerbose("     %s\n", arg);
            }

            if (!environment.isEmpty()) {
                env.logVerbose("Environment: \n");

                for (Map.Entry<String, String> entry : environment.entrySet()) {
                    env.logVerbose("            %s='%s'\n", entry.getKey(), entry.getValue());
                }
            }

            env.logVerbose("Current directory: %s\n", currentDirectory);
        }
    }
}
