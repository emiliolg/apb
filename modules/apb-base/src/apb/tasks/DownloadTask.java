

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import apb.Apb;
import apb.BuildException;
import apb.Environment;

import apb.metadata.UpdatePolicy;

import apb.utils.ClassUtils;
import apb.utils.FileUtils;
import apb.utils.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

import static apb.utils.StringUtils.nChars;

public class DownloadTask
    extends Task
{
    //~ Instance fields ......................................................................................

    @NotNull private File   dest;
    @NotNull private String password;
    @NotNull private String user;

    @NotNull private UpdatePolicy updatePolicy;
    @NotNull private URL          source;

    @Nullable private URLConnection connection;

    //~ Constructors .........................................................................................

    /**
     * Construct a DownloadTask to download from a specified URL (from) to a specified local file (to)
     * @param from A URL to download from
     * @param to The destination file to place the downloaded file
     */
    private DownloadTask(@NotNull URL from, @NotNull File to)
    {
        updatePolicy = UpdatePolicy.DAILY;
        source = from;
        dest = to;
        password = "";
        user = "";
    }

    //~ Methods ..............................................................................................

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
    public DownloadTask withUpdatePolicy(@NotNull UpdatePolicy policy)
    {
        updatePolicy = policy;
        return this;
    }

    /**
     * Set the user when using http basic authentication
     * @param u The username
     */
    public DownloadTask withUser(@NotNull String u)
    {
        user = u;
        return this;
    }

    /**
     * Set the password when using http basic authentication
     * @param p The password in cleartext.
     */
    public DownloadTask withPassword(@NotNull String p)
    {
        password = p;
        return this;
    }

    /**
     * Execute the download task
     */
    public void execute()
    {
        try {
            if (!uptodate() && createTargetDir()) {
                env.logInfo("Downloading: %s\n", source);
                env.logInfo("         to: %s\n", FileUtils.normalizePath(dest));
                download();
            }
        }
        catch (UnknownHostException e) {
            env.handle("Unknown Host: " + e.getMessage());
        }
        catch (IOException e) {
            env.logSevere("Error downloading '%s' to '%s'\n", source, dest);
            env.handle(e);
        }
    }

    private long getMDTM(@NotNull File file)
        throws IOException
    {
        URLConnection c = getConnection();

        if ("FtpURLConnection".equals(c.getClass().getSimpleName())) {
            c.connect();

            try {
                Object ftpClient = ClassUtils.fieldValue(c, "ftp");
                ClassUtils.invokeNonPublic(ftpClient, "issueCommandCheck", "MDTM " + file.getName());
                String     result = (String) ClassUtils.invoke(ftpClient, "getResponseString");
                DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                return df.parse(result.substring(4, result.length() - 1)).getTime();
            }
            catch (Exception e) {
                // Fallthrough and return 0
            }
        }

        return 0;
    }

    private void download()
        throws IOException
    {
        URLConnection c = getConnection();
        c.connect();

        //next test for a 304 result (HTTP only)
        // See if it is an ftp connection
        if (c instanceof HttpURLConnection) {
            // test for 401 result (HTTP only)
            if (((HttpURLConnection) c).getResponseCode() == HTTP_UNAUTHORIZED) {
                throw new BuildException("HTTP Authorization failure");
            }
        }

        InputStream is = openInputStream(c);

        if (is == null) {
            env.handle("Can not download " + source + " to " + dest);
        }
        else {
            download(is, c.getContentLength());
        }
    }

    /** Ensure dest target directory exists
     * @return true if it exists or it has been successfully created
     */
    private boolean createTargetDir()
    {
        File          dir = dest.getParentFile();
        final boolean result = dir.exists() || dir.mkdirs();

        if (!result) {
            env.handle("Can not create: " + dir);
        }

        return result;
    }

    private boolean uptodate()
        throws IOException
    {
        if (!dest.exists() || updatePolicy.equals(UpdatePolicy.FORCE)) {
            return false;
        }

        if (updatePolicy.equals(UpdatePolicy.NEVER)) {
            return true;
        }

        final long destTime = dest.lastModified();
        logVerbose("Local  file timestamp: %tc\n", destTime);

        final long now = System.currentTimeMillis();
        final long baseTime = now - updatePolicy.getInterval();

        if (destTime >= baseTime) {
            logVerbose("Update policy time %tc not reached\n", destTime + updatePolicy.getInterval());
            return true;
        }

        final boolean uptodate = destTime >= getSourceTime();

        if (uptodate) {
            dest.setLastModified(now);
        }

        return uptodate;
    }

    private long getSourceTime()
        throws IOException
    {
        // Special hack for ftp
        final long result =
            "ftp".equalsIgnoreCase(source.getProtocol()) ? getMDTM(new File(source.getFile()))
                                                         : getConnection().getLastModified();

        logVerbose("Remote file timestamp: %tc\n", result);

        return result;
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

    private InputStream openInputStream(URLConnection c)
    {
        for (int i = 0; i < NUMBER_RETRIES; i++) {
            try {
                return c.getInputStream();
            }
            catch (IOException ex) {
                env.logWarning("Error opening connection: " + ex);
            }
        }

        return null;
    }

    @NotNull private URLConnection getConnection()
        throws IOException
    {
        URLConnection c = connection;

        if (c == null) {
            //set up the URL connection
            c = source.openConnection();

            // prepare Java 1.1 style credentials
            if (!user.isEmpty() || !password.isEmpty()) {
                String encoding = StringUtils.encodeBase64((user + ":" + password).getBytes());
                c.setRequestProperty("Authorization", "Basic " + encoding);
            }

            connection = c;
        }

        return c;
    }

    //~ Static fields/initializers ...........................................................................

    private static final long DOTS_PER_LINE = 50;
    private static final int  NUMBER_RETRIES = 3;
    private static final int  BUFFER_SIZE = 100 * 1024;

    //~ Inner Classes ........................................................................................

    public static class Builder
    {
        @NotNull private URL url;

        Builder(@NotNull String from)
        {
            try {
                url = new URL(from);
            }
            catch (MalformedURLException e) {
                throw new BuildException(e);
            }
        }

        Builder(@NotNull URL from)
        {
            url = from;
        }

        /**
        * Specify the target file or directory
        * @param to The File or directory to copy to
        */
        @NotNull public DownloadTask to(@NotNull String to)
        {
            return to(Apb.getEnv().fileFromBase(to));
        }

        /**
        * Specify the target file or directory
        * @param to The File or directory to copy to
        */
        @NotNull public DownloadTask to(@NotNull File to)
        {
            if (to.isDirectory()) {
                throw new BuildException("The specified destination is a directory");
            }

            if (!to.canWrite() && to.exists()) {
                throw new BuildException("Can't write to " + to.getAbsolutePath());
            }

            return new DownloadTask(url, to);
        }
    }

    private static class DownloadProgress
    {
        private final Environment env;
        private int               currentSize;
        private int               dots;
        private int               totalSize;

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
    //    public static void main(String[] args)
    //    {
    //        Environment env = BaseEnvironment.create();
    //        env.setVerbose();
    //
    //        //        DownloadTask t = new DownloadTask(env, args[0], args[1]);
    //        DownloadTask        t = new DownloadTask(env, "http://www.ibiblio.org/maven/ant/jars/ant-1.6.5.jar", "ant.jar");
    //        t.execute();
    //    }
}
