

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import apb.BuildException;
import apb.Environment;

import apb.metadata.UpdatePolicy;

import apb.utils.FileUtils;
import apb.utils.StandaloneEnv;
import apb.utils.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

import static apb.utils.StringUtils.nChars;

public class DownloadTask
    extends Task
{
    //~ Instance fields ......................................................................................

    @NotNull private File    dest;
    @Nullable private String password;
    @NotNull private URL     source;

    @NotNull private UpdatePolicy updatePolicy;
    @Nullable private String      user;

    //~ Constructors .........................................................................................

    public DownloadTask(@NotNull Environment env, @NotNull String from, @NotNull String to)
    {
        super(env);
        updatePolicy = UpdatePolicy.ALWAYS;
        setSource(from);
        setDest(to);
    }

    //~ Methods ..............................................................................................

    public static void main(String[] args)
    {
        Environment env = new StandaloneEnv();
        env.setVerbose();
        DownloadTask t = new DownloadTask(env, args[0], args[1]);
        //t.execute();
                //t = new DownloadTask(env, "ftp://mirrors.kernel.org/robots.txt", "robots.txt");
        //t.execute();
        t = new DownloadTask(env, "http://www.ibiblio.org/maven/ant/jars/ant-1.6.5.jar", "ant.jar");
    t.execute();

    }

    /**
     * Define when
     * @param updatePolicy
     */
    public void setUpdatePolicy(@NotNull UpdatePolicy updatePolicy)
    {
        this.updatePolicy = updatePolicy;
    }

    public void setDest(@NotNull String to)
    {
        dest = new File(to);

        if (dest.exists()) {
            if (dest.isDirectory()) {
                throw new BuildException("The specified destination is a directory");
            }

            if (!dest.canWrite()) {
                throw new BuildException("Can't write to " + dest.getAbsolutePath());
            }
        }
    }

    public void setSource(@NotNull String from)
    {
        try {
            source = new URL(from);
        }
        catch (MalformedURLException e) {
            throw new BuildException(e);
        }
    }

    public void setUser(@NotNull String user)
    {
        this.user = user;
    }

    public void setPassword(@NotNull String password)
    {
        this.password = password;
    }

    public void execute()
    {
        try {
            //set up the URL connection
            URLConnection connection = source.openConnection();

            establishAuthentication(connection);

            if (!uptodate(connection)) {
                env.logInfo("Downloading: %s\n", source);
                env.logInfo("         to: %s\n", dest.getAbsolutePath());
                download(connection);
            }
        }
        catch (UnknownHostException e) {
            env.handle("Unknown Host: " + e.getMessage());
        }catch (IOException e) {
            env.logSevere("Error downloading '%s' to '%s'\n", source, dest);
            env.handle(e);
        }
    }

    private void establishAuthentication(URLConnection connection)
    {
        // prepare Java 1.1 style credentials
        if (user != null || password != null) {
            String encoding = StringUtils.encodeBase64(user + ":" + password);
            connection.setRequestProperty("Authorization", "Basic " + encoding);
        }
    }

    private void download(URLConnection connection)
        throws IOException
    {
        //connect to the remote site (may take some time)
        connection.connect();

        //next test for a 304 result (HTTP only)
        if (connection instanceof HttpURLConnection) {
            // test for 401 result (HTTP only)
            if (((HttpURLConnection) connection).getResponseCode() == HTTP_UNAUTHORIZED) {
                throw new BuildException("HTTP Authorization failure");
            }
        }

        InputStream is = openInputStream(connection);

        if (is == null) {
            env.handle("Can not download " + source + " to " + dest);
        }
        else {
            download(is, connection.getContentLength());
        }
    }

    private boolean uptodate(URLConnection connection)
    {
        if (env.forceBuild() || !dest.exists()) {
            return false;
        }

        if (updatePolicy.equals(UpdatePolicy.NEVER)) {
            return true;
        }

        final long destTime = dest.lastModified();
        env.logVerbose("Local  file timestamp: %tc\n", destTime);

        final long baseTime = System.currentTimeMillis() - updatePolicy.getInterval();

        if (destTime >= baseTime) {
            env.logVerbose("Update policy time %tc not reached\n", destTime + updatePolicy.getInterval());
            return true;
        }

        //connection.setIfModifiedSince(destTime);

        final long sourceTime = connection.getLastModified();

        env.logVerbose("Remote file timestamp: %tc\n", sourceTime);
        return destTime >= sourceTime;
    }

    private void download(InputStream is, int size)
        throws IOException
    {
        DownloadProgress progress = new DownloadProgress(env);

        FileOutputStream fos = new FileOutputStream(dest);
        progress.begin(size);
        boolean finished = false;

        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int    length;

            while ((length = is.read(buffer)) >= 0) {
                fos.write(buffer, 0, length);
                progress.advance(length);
            }

            finished = true;
        }
        finally {
            FileUtils.close(fos);
            FileUtils.close(is);

            // we have started to (over)write dest, but failed.
            // Try to delete the garbage we'd otherwise leave
            // behind.
            if (!finished) {
                dest.delete();
            }
        }

        progress.end();
    }

    private InputStream openInputStream(URLConnection connection)
    {
        for (int i = 0; i < NUMBER_RETRIES; i++) {
            try {
                return connection.getInputStream();
            }
            catch (IOException ex) {
                env.logWarning("Error opening connection: " + ex);
            }
        }

        return null;
    }

    //~ Static fields/initializers ...........................................................................

    private static final long DOTS_PER_LINE = 50;
    private static final int  NUMBER_RETRIES = 3;
    private static final int  BUFFER_SIZE = 100 * 1024;

    //~ Inner Classes ........................................................................................

    private static class DownloadProgress
    {
        private int         currentSize;
        private int         dots;
        private Environment env;
        private int         totalSize;

        /**
         * Construct a verbose progress reporter.
         * @param env the Environment
         */
        public DownloadProgress(Environment env)
        {
            this.env = env;
        }

        public void begin(int size)
        {
            totalSize = size;
            currentSize = 0;
            dots = 0;
            env.logInfo("[");
        }

        public void advance(int length)
        {
            currentSize += length;
            int d = (int) (DOTS_PER_LINE * currentSize) / totalSize;
            env.logInfo(nChars(d - dots, '.'));
            dots = d;

            if (dots > DOTS_PER_LINE) {
                env.logInfo("\n ");
                dots = 0;
            }
        }

        public void end()
        {
            env.logInfo("]\n");
        }
    }
}
