

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


package apb.testrunner.output;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import apb.utils.FileUtils;

/**
 * Manages the Standard error and Standard Output for tests
 */

//
// User: emilio
// Date: Jun 19, 2009
// Time: 11:12:46 AM

class OutputHandler
{
    //~ Instance fields ......................................................................................

    private int count;

    private ByteArrayOutputStream errorByteStream, outputByteStream;
    private boolean               ignoreOutput;
    private PrintStream           prevOut, prevErr;
    private PrintStream out, err;

    //~ Constructors .........................................................................................

    private OutputHandler() {}

    //~ Methods ..............................................................................................

    //    public String consumeOutput()
    //    {
    //
    //        if (printStream != null) {
    //            printStream.close();
    //            printStream = null;
    //        }
    //
    //        String result = byteStream == null ? "" : byteStream.toString();
    //        byteStream = null;
    //        return result;
    //    }

    public static synchronized OutputHandler getInstance()
    {
        if (instance == null) {
            instance = new OutputHandler();
        }

        return instance;
    }

    public void restore()
    {
        if (--count <= 0) {
            count = 0;
            if (out != null) {
                out.close();
                out = null;
            }
            if (err != null) {
                err.close();
                err = null;
            }
            if (prevOut != null) {
                System.setOut(prevOut);
                prevOut = null;
            }

            if (prevErr != null) {
                System.setErr(prevErr);
                prevErr = null;
            }

        }
    }

    public void init(boolean showOutput)
    {
        if (count++ == 0) {
            prevOut = System.out;
            prevErr = System.err;

            if (showOutput) {
                memoryOutput();
            } else {
                nullOutput();
            }
        }
        else if (showOutput && ignoreOutput) {
            memoryOutput();
        }
    }

    private void memoryOutput()
    {
        ignoreOutput = false;
        outputByteStream = new ByteArrayOutputStream();
        errorByteStream = new ByteArrayOutputStream();
        out = new PrintStream(outputByteStream);
        System.setOut(out);
        err = new PrintStream(errorByteStream);
        System.setErr(err);
    }

    private void nullOutput()
    {
        ignoreOutput = true;
        System.setOut(FileUtils.nullOutputStream());
        System.setErr(FileUtils.nullOutputStream());
    }

    //~ Static fields/initializers ...........................................................................

    private static OutputHandler instance;

    public String getOutput() {
        return outputByteStream == null ? "" : outputByteStream.toString();
    }
    public String getError() {
        return outputByteStream == null ? "" : outputByteStream.toString();
    }

    public static void reset() {
        instance = null;
    }
}
