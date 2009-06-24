

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


package apb.utils;
//
// User: emilio
// Date: Jun 23, 2009
// Time: 5:08:24 PM

//
public class Constants
{
    //~ Static fields/initializers ...........................................................................

    public static final String UTF8 = "UTF-8";
    public static final String XML_HEADER_START = "<?xml";
    public static final String XML_HEADER_END = "?>";
    public static final String XML_HEADER =
        XML_HEADER_START + " version=\"1.0\" encoding=\"" + UTF8 + "\" " + XML_HEADER_END;
}
