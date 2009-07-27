

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


package apb.sunapi;

import java.io.IOException;
import java.io.Writer;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import org.w3c.dom.Document;
//
// User: emilio
// Date: Jul 27, 2009
// Time: 2:16:16 PM

//
public class XmlSerializer
{
    //~ Methods ..............................................................................................

    public static void serialize(Document document, Writer output)
        throws IOException
    {
        final OutputFormat  format = new OutputFormat(document, apb.utils.Constants.UTF8, true);
        final XMLSerializer serializer = new XMLSerializer(output, format);
        serializer.serialize(document);
    }
}
