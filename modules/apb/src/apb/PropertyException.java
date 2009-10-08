
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

package apb;

//
// User: emilio
// Date: Sep 8, 2008
// Time: 4:30:40 PM

//
public class PropertyException
    extends RuntimeException
{
    //~ Instance fields ......................................................................................

    private String additionalInfo;
    private final String id;
    private String source;

    //~ Constructors .........................................................................................

    public PropertyException(String propertyId)
    {
        id = propertyId;
        additionalInfo = "";
        source = "";
    }

    public PropertyException(String id, String msg)
    {
        this(id);
        additionalInfo = msg;
    }

    //~ Methods ..............................................................................................

    public String getMessage()
    {
        String result = "Undefined Property: " + id;

        if (!source.isEmpty()) {
            result += "\n" + source;
        }

        if (additionalInfo.isEmpty()) {
            result += "\n" + additionalInfo;
        }

        return result;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public void setAdditionalInfo(String info)
    {
        additionalInfo = info;
    }

    //~ Static fields/initializers ...........................................................................

    private static final long serialVersionUID = -3287928686099873402L;
}
