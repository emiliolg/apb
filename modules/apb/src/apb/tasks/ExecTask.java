
// ...........................................................................................................
//
// Copyright (c) 1993, 2009, Oracle and/or its affiliates. All rights reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Oracle Corp.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// ...........................................................................................................

package apb.tasks;

import java.io.BufferedReader;
import java.io.File;
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

        b.directory(getCurrentDirectory());

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
            env.logInfo("%s\n", line);
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

            env.logVerbose("Current directory: %s\n", getCurrentDirectory());
        }
    }
}
