
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

package apb.tasks;

import apb.Environment;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.SAXException;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Map;
import java.util.HashMap;


public class XSLTTask
    extends Task implements ErrorListener {
    //~ Instance fields ......................................................................................



    private Map<String, String> params = new HashMap<String,String>();
    @NotNull private final File   inputFile;
    @NotNull private final File   outputFile;
    private Transformer transformer;
    @NotNull private File styleFile;
    private TransformerFactory factory;
    private Templates templates;
    private Map<String,String>  outputProperties = new HashMap<String,String>();

    //~ Constructors .........................................................................................

    public XSLTTask(@NotNull Environment env, @NotNull File style,  @NotNull File inputFile, @NotNull File outputFile)
    {
        super(env);
        this.styleFile = style;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    //~ Methods ..............................................................................................



    public void execute()
    {
        if(!inputFile.exists()){
            env.handle("input file "+inputFile.getPath()+" does not exist!");

        }

        outputFile.getParentFile().mkdirs();
        try {
            transform(inputFile, outputFile);
        } catch (Exception e) {
            e.printStackTrace();
            env.handle(e);
        }
    }

    public void addParam(String name, String expresssion){
                params.put(name, expresssion);
    }

    public void addOutputPropertyParam(String name, String value){
                outputProperties.put(name, value);
    }


    private void transform(File infile, File outfile) throws Exception {
        if (transformer == null) {
            createTransformer();
        }

        InputStream fis = null;
        OutputStream fos = null;
        try {
            fis = new BufferedInputStream(new FileInputStream(infile));
            fos = new BufferedOutputStream(new FileOutputStream(outfile));
            StreamResult res = new StreamResult(fos);
            Source src = new StreamSource(fis);

            setTransformationParameters();
            transformer.transform(src, res);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ignored) {
                // ignore
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ignored) {
                // ignore
            }
        }
    }

    private void createTransformer() throws TransformerConfigurationException, IOException, SAXException, ParserConfigurationException {
           if (templates == null) {
            readTemplates();
        }

        transformer = templates.newTransformer();

        for (String name : outputProperties.keySet()) {
                transformer.setOutputProperty(name, outputProperties.get(name));
        }

        // configure the transformer...
        transformer.setErrorListener(this);
    }

     /**
     * Sets the paramters for the transformer.
     */
    private void setTransformationParameters() {
         for (String name : params.keySet()) {
              final String value = params.get(name);
            transformer.setParameter(name, value);
         }
    }

       private void readTemplates()
           throws IOException, TransformerConfigurationException,
            ParserConfigurationException, SAXException {

           InputStream xslStream = null;
           try {
               xslStream
                   = new BufferedInputStream(new FileInputStream(styleFile));
               Source src = new StreamSource(xslStream);
               templates = getFactory().newTemplates(src);
           } finally {
               if (xslStream != null) {
                   xslStream.close();
               }
           }
       }

    private TransformerFactory getFactory() {
        if (factory == null){
            factory = TransformerFactory.newInstance();
        }

        return factory;
    }


    //ErrorListener methods

    public void warning(TransformerException exception) throws TransformerException {
        env.logWarning(exception.getMessage());
    }

    public void error(TransformerException exception) throws TransformerException {
        env.handle(exception);
    }

    public void fatalError(TransformerException exception) throws TransformerException {
        env.handle(exception);
    }
}