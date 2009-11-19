

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import apb.Apb;
import apb.BuildException;

import apb.utils.FileUtils;
import apb.utils.StreamUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Collections.singletonList;

import static apb.tasks.FileSet.fromDir;
import static apb.tasks.FileSet.fromFile;

import static apb.utils.FileUtils.createOutputStream;

public class XsltTask
    extends CopyTask
{
    //~ Instance fields ......................................................................................

    @NotNull private final File styleFile;

    @NotNull private final Map<String, String> outputProperties;
    @NotNull private final Map<String, String> params;
    @NotNull private String                    useExtension;

    @NotNull private final TransformerFactory factory;

    //~ Constructors .........................................................................................

    private XsltTask(@NotNull List<FileSet> fileSets, @NotNull File to, @NotNull File style)
    {
        super(fileSets, to);
        factory = TransformerFactory.newInstance();
        outputProperties = new HashMap<String, String>();
        params = new HashMap<String, String>();
        styleFile = style;
        useExtension = "";
    }

    //~ Methods ..............................................................................................

    /**
     * Specify the desired file extension to be used for the targets.
     * If not specified the extension from the source will be used when the destination is a directory
     * @param ext The desired extension to be used
     */
    public XsltTask usingExtension(@NotNull String ext)
    {
        useExtension = ext;
        return this;
    }

    /**
     * Parameters are used to pass parameters to the XSL stylesheet.
     * @param name Name of the XSL parameter
     * @param value Text value to be placed into the param.
     */
    public XsltTask withParameter(@NotNull String name, @NotNull String value)
    {
        params.put(name, value);
        return this;
    }

    /**
     * sed to specify how you wish the result tree to be output as specified in the
     * <a href="http://www.w3.org/TR/xslt#output">XSLT specifications</a>.
     * @param name The name of the property
     * @param value The value of the property
     */
    public XsltTask withOutputProperty(@NotNull String name, @NotNull String value)
    {
        outputProperties.put(name, value);
        return this;
    }

    @Override protected void doCopyFile(File source, File dest)
        throws IOException
    {
        if (!useExtension.isEmpty()) {
            dest = FileUtils.changeExtension(dest, useExtension);
        }

        transform(source, dest);
    }

    private void transform(File infile, File outfile)
        throws IOException
    {
        InputStream  fis = null;
        OutputStream fos = null;

        try {
            final Transformer transformer = createTransformer();
            fis = new BufferedInputStream(new FileInputStream(infile));
            fos = new BufferedOutputStream(createOutputStream(outfile));
            StreamResult res = new StreamResult(fos);
            Source       src = new StreamSource(fis);

            setTransformationParameters(transformer);

            if (env.isVerbose()) {
                logVerbose("Processing :'%s'\n", infile);
                logVerbose("        to :'%s'\n", outfile);
                logVerbose("using xslt :'%s'\n", styleFile);
            }
            else {
                env.logInfo("Processing 1 file.\n");
            }

            transformer.transform(src, res);

            fis.close();
            fos.flush();
            fos.close();
        }
        catch (TransformerException e) {
            throw new BuildException(e);
        }
        finally {
            StreamUtils.close(fis);
            StreamUtils.close(fos);
        }
    }

    private Transformer createTransformer()
        throws TransformerConfigurationException
    {
        Templates templates = readTemplates();

        Transformer transformer = templates.newTransformer();

        for (String name : outputProperties.keySet()) {
            transformer.setOutputProperty(name, outputProperties.get(name));
        }

        // configure the transformer...
        transformer.setErrorListener(new XsltErrorListener());
        return transformer;
    }

    /**
    * Sets the paramters for the transformer.
    */
    private void setTransformationParameters(Transformer transformer)
    {
        for (String name : params.keySet()) {
            final String value = params.get(name);
            transformer.setParameter(name, value);
        }
    }

    private Templates readTemplates()
        throws TransformerConfigurationException
    {
        InputStream xslStream = null;

        try {
            xslStream = new BufferedInputStream(new FileInputStream(styleFile));
            Source src = new StreamSource(xslStream);
            return factory.newTemplates(src);
        }
        catch (FileNotFoundException e) {
            throw new BuildException(e);
        }
        finally {
            StreamUtils.close(xslStream);
        }
    }

    //~ Inner Classes ........................................................................................

    public static class Builder
    {
        @Nullable private File               to;
        @NotNull private final List<FileSet> from;

        Builder(@NotNull FileSet... from)
        {
            this.from = Arrays.asList(from);
        }

        /**
         * Private constructor called from factory methods
         * @param from The source to transform. It can be a file or a directory
         */

        Builder(@NotNull File from)
        {
            this.from = singletonList(from.isFile() ? fromFile(from) : fromDir(from));
        }

        /**
        * Specify the target file or directory
        * @param fileName The File or directory to transform to
        * @throws IllegalArgumentException if trying to copy a directoy to a single file.
        */
        @NotNull public Builder to(@NotNull String fileName)
        {
            return to(Apb.getEnv().fileFromBase(fileName));
        }

        /**
         * Specify the target file or directory
         * @param file The File or directory to transform to
         * @throws IllegalArgumentException if trying to copy a directoy to a signle file.
         */
        @NotNull public Builder to(@NotNull File file)
        {
            if (file.isFile() && (from.size() != 1 || !from.get(0).isFile())) {
                throw new IllegalArgumentException("Trying to transform multiple files to a single file '" +
                                                   file.getPath() + "'.");
            }

            to = file;
            return this;
        }

        /**
         * Specify the name of the stylesheet to use
         * @param fileName the name of the stylesheet to use
         * @throws IllegalStateException if {@link #to} was not invoked before
         */
        @NotNull public XsltTask usingStyle(@NotNull String fileName)
        {
            return usingStyle(Apb.getEnv().fileFromBase(fileName));
        }

        /**
         * Specify a file to be used as a stylesheet
         * @param file a file to be used as a stylesheet
         * @throws IllegalStateException if {@link #to} was not invoked before
         */
        @NotNull public XsltTask usingStyle(@NotNull File file)
        {
            final File target = to;

            if (target == null) {
                throw new IllegalStateException("Must invoked 'to' method to specify the output file or directory");
            }

            if (!file.exists()) {
                throw new BuildException(new FileNotFoundException(file.getPath()));
            }

            return new XsltTask(from, target, file);
        }
    }

    private class XsltErrorListener
        implements ErrorListener
    {
        //ErrorListener methods

        public void warning(TransformerException exception)
            throws TransformerException
        {
            env.logWarning(exception.getMessage());
        }

        public void error(TransformerException exception)
            throws TransformerException
        {
            env.handle(exception);
        }

        public void fatalError(TransformerException exception)
            throws TransformerException
        {
            env.handle(exception);
        }
    }
}
