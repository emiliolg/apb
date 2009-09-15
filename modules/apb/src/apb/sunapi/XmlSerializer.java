
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

import java.io.Writer;

import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
//
// User: emilio
// Date: Jul 27, 2009
// Time: 2:16:16 PM

//
public class XmlSerializer
{
    //~ Constructors .........................................................................................

    private XmlSerializer() {}

    //~ Methods ..............................................................................................

    public static void serialize(Document document, Writer output)
        throws LSException
    {
        final DOMImplementationLS ls =
            (DOMImplementationLS) document.getImplementation().getFeature("LS", "3.0");

        final LSOutput out = ls.createLSOutput();
        out.setCharacterStream(output);

        final LSSerializer serializer = ls.createLSSerializer();

        serializer.getDomConfig().setParameter("format-pretty-print", true);
        serializer.write(document, out);
    }
}
