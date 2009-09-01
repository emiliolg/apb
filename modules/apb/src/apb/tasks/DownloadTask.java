

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
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import apb.BuildException;
import apb.Environment;
import apb.metadata.UpdatePolicy;
import apb.utils.ClassUtils;
import apb.utils.FileUtils;
import apb.utils.StringUtils;
import static apb.utils.StringUtils.nChars;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DownloadTask
    extends Task
{
    //~ Instance fields ......................................................................................

    @Nullable private URLConnection connection;

    @NotNull private File    dest;
    @Nullable private String password;
    @NotNull private URL     source;

    @NotNull private UpdatePolicy updatePolicy;
    @Nullable private String      user;

    //~ Constructors .........................................................................................

    /**
     * Construct a DownloadTask to download from a specified URL (from) to a specified local file (to)
     * @param env The Environment
     * @param from A URL to download from
     * @param to The destination file to place the downloaded file
     */
    public DownloadTask(@NotNull Environment env, @NotNull String from, @NotNull String to)
    {
        super(env);
        updatePolicy = UpdatePolicy.DAILY;
        setSource(from);
        setDest(to);
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
     * @param updatePolicy The update policy to be used.
     */
    public void setUpdatePolicy(@NotNull UpdatePolicy updatePolicy)
    {
        this.updatePolicy = updatePolicy;
    }

    /**
     * Set the source URL for the download
     * @param from The URL to download the file form
     */
    public void setSource(@NotNull String from)
    {
        try {
            source = new URL(from);
        }
        catch (MalformedURLException e) {
            throw new BuildException(e);
        }
    }

    /**
     * Set the user when using http basic authentication
     * @param user The username
     */
    public void setUser(@NotNull String user)
    {
        this.user = user;
    }

    /**
     * Set the password when using http basic authentication
     * @param password The password in cleartext.
     */
    public void setPassword(@NotNull String password)
    {
        this.password = password;
    }

    /**
     * Set the destination file
     * @param to The path of the destination file
     */
    public void setDest(@NotNull String to)
    {
        dest = new File(to);

        if (dest.isDirectory()) {
            throw new BuildException("The specified destination is a directory");
        }

        if (!dest.canWrite() && dest.exists()) {
            throw new BuildException("Can't write to " + dest.getAbsolutePath());
        }
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
            if (user != null || password != null) {
                String encoding = StringUtils.encodeBase64(user + ":" + password);
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

    private static class DownloadProgress
    {
        private int         currentSize;
        private int         dots;
        private final Environment env;
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
    //    public static void main(String[] args)
    //    {
    //        Environment env = new StandaloneEnv();
    //        env.setVerbose();
    //
    //        //        DownloadTask t = new DownloadTask(env, args[0], args[1]);
    //        DownloadTask        t = new DownloadTask(env, "http://www.ibiblio.org/maven/ant/jars/ant-1.6.5.jar", "ant.jar");
    //        t.execute();
    //    }
}
