

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


package apb.tests.testutils;

import java.util.List;

import junit.framework.Assert;
import junit.framework.ComparisonCompactor;
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

// User: emilio
// Date: Sep 26, 2009
// Time: 1:09:45 PM

public class CheckOutput
{
    //~ Methods ..............................................................................................

    public static void checkOutput(List<String> output, String... expectedLines)
    {
        StringBuilder msg = new StringBuilder();
        boolean       many = false;

        for (int i = 0, j = 0; i < expectedLines.length || j < output.size(); i++, j++) {
            String current = output(output, j);
            String expected = i < expectedLines.length ? expectedLines[i] : "";

            //Check if partial match
            if (expected.endsWith("\\+")) {
                expected = expected.substring(0, expected.length() - 2);

                // Check if many
                if (expected.endsWith("\\+")) {
                    expected = expected.substring(0, expected.length() - 2);
                    many = true;
                }

                current = apb.utils.StringUtils.truncateTo(current, expected);
            }

            if (!expected.equals(current)) {
                msg.append(new ComparisonCompactor(20, expected, current).compact("Line (" + i + "):"));
                msg.append("\n");
            }

            if (many) {
                while (expected.equals(apb.utils.StringUtils.truncateTo(output(output, ++j), expected))) {
                    ;
                }

                --j;
                many = false;
            }
        }

        if (msg.length() > 0) {
            Assert.fail(msg.toString());
        }
    }

    public static String output(List<String> output, int i)
    {
        String current = i < output.size() ? output.get(i) : "\n";
        return current.substring(0, current.length() - 1);
    }
}
