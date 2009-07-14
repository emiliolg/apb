
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import apb.Environment;

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;

import org.xml.sax.SAXException;

public class XsltTask
    extends Task
            //implements ErrorListener

{
    //~ Instance fields ......................................................................................

    @NotNull private final TransformerFactory factory;

    private File                               inputFile;
    private File                               outputFile;
    @NotNull private final Map<String, String> outputProperties;
    @NotNull private final Map<String, String> params;
    private InputStream                        style;
    private String                             styleName;

    //~ Constructors .........................................................................................

    public XsltTask(@NotNull Environment env)
    {
        super(env);
        factory = TransformerFactory.newInstance();
        outputProperties = new HashMap<String, String>();
        params = new HashMap<String, String>();
    }

    //~ Methods ..............................................................................................

    /**
     * Process the given input file using the specified styleFileName
     * @param inputFileName The name of the input file to process
     * given either relative to the source directory of the project or as an absolute path
     * @param styleFileName The name of the stylesheet to use
     * given either relative to the project's source dir or as an absolute path.
     * @param outputFileName The name of the output file to produce
     * given either relative to the generate source directory of the project or as an absolute path
     */

    public void process(@NotNull String inputFileName, @NotNull String styleFileName,
                        @NotNull String outputFileName)
    {
        setStyleFileName(styleFileName);
        setInputFile(inputFileName);
        setOutputFile(outputFileName);
        execute();
    }

    public void process(@NotNull String inputFileName, @NotNull InputStream stream,
                        @NotNull String outputFileName)
    {
        setStyle(stream);
        setInputFile(inputFileName);
        setOutputFile(outputFileName);
        execute();
    }

    /**
     * Execute the task
     */
    public void execute()
    {
        if (inputFile == null || outputFile == null || style == null) {
            env.handle("Must set input, output & style files");
        }

        final long inputMod = inputFile.lastModified();

        if (inputMod == 0) {
            env.handle("input file " + inputFile.getPath() + " does not exist!");
        }

        if (env.forceBuild() || inputMod > outputFile.lastModified()) {
            final File outdir = outputFile.getParentFile();

            if (!outdir.exists() && !outdir.mkdirs()) {
                env.handle("Cannot create output directory: " + outdir);
            }

            try {
                transform(inputFile, outputFile);
            }
            catch (Exception e) {
                env.handle(e);
            }
        }
    }

    /**
     * Parameters are used to pass parameters to the XSL stylesheet.
     * @param name Name of the XSL parameter
     * @param value Text value to be placed into the param.
     */
    public void addParam(@NotNull String name, @NotNull String value)
    {
        params.put(name, value);
    }

    /**
     * sed to specify how you wish the result tree to be output as specified in the
     * <a href="http://www.w3.org/TR/xslt#output">XSLT specifications</a>.
     * @param name The name of the property
     * @param value The value of the property
     */
    public void addOutputProperty(@NotNull String name, @NotNull String value)
    {
        outputProperties.put(name, value);
    }

    /**
    * The name of the input file to process
    * Given either relative to the source directory of the project or as an absolute path
    * @param inputFileName
    */
    public void setInputFile(@NotNull String inputFileName)
    {
        inputFile = env.fileFromSource(inputFileName);
    }

    /**
    * The name of the output file to produce
    * Given either relative to the generate source directory of the project or as an absolute path
    * @param outputFileName
    */
    public void setOutputFile(@NotNull String outputFileName)
    {
        outputFile = env.fileFromGeneratedSource(outputFileName);
    }

    /**
     * The name of the stylesheet to use.
     * Given either relative to the project's source dir or as an absolute path.
     * @param styleFileName The name of the stylesheet to use.
     */
    public void setStyleFileName(@NotNull String styleFileName)
    {
        styleName = styleFileName;

        try {
            style = new FileInputStream(env.fileFromSource(styleFileName));
        }
        catch (FileNotFoundException e) {
            env.handle(e);
        }
    }

    /**
     * The InputStream of the stylesheet to use.
     * @param style The InputStream of the stylesheet to use.
     */
    public void setStyle(@NotNull InputStream style)
    {
        this.style = style;
    }

    private void transform(File infile, File outfile)
        throws Exception
    {
        final Transformer transformer = createTransformer();

        InputStream  fis = null;
        OutputStream fos = null;

        try {
            fis = new BufferedInputStream(new FileInputStream(infile));
            fos = new BufferedOutputStream(new FileOutputStream(outfile));
            StreamResult res = new StreamResult(fos);
            Source       src = new StreamSource(fis);

            setTransformationParameters(transformer);

            if (env.isVerbose()) {
                env.logVerbose("Processing :'%s'\n", infile);
                env.logVerbose("        to :'%s'\n", outfile);

                if (styleName != null) {
                    env.logVerbose("using xslt :'%s'\n", styleName);
                }
            }
            else {
                env.logInfo("Processing 1 file.\n");
            }

            transformer.transform(src, res);
        }
        finally {
            FileUtils.close(fis);
            FileUtils.close(fos);
        }
    }

    private Transformer createTransformer()
        throws TransformerConfigurationException, IOException, SAXException, ParserConfigurationException
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
        throws IOException, TransformerConfigurationException, ParserConfigurationException, SAXException
    {
        InputStream xslStream = null;

        try {
            xslStream = new BufferedInputStream(style);
            Source src = new StreamSource(xslStream);
            return factory.newTemplates(src);
        }
        finally {
            FileUtils.close(xslStream);
        }
    }

    //~ Inner Classes ........................................................................................

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
