

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


package apb.tests.build;

import java.util.EnumSet;
import java.util.Map;

import apb.Apb;
import apb.DefinitionException;

import apb.tests.testutils.TestStandaloneLogger;

import apb.utils.ClassUtils;
import apb.utils.DebugOption;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class LoggerTest
    extends ApbTestCase
{
    //~ Methods ..............................................................................................

    public void testWithTrack()
        throws Exception, DefinitionException
    {
        build("Chat", "bye");

        for (int i = 0; i < output.size(); i++) {
            assertMatch(WITH_TRACK[i] + '\n', output.get(i));
        }
    }

    @Override protected void createEnv(Map<String, String> properties)
    {
        try {
            env = Apb.createBaseEnvironment(new TestStandaloneLogger(output), properties);

            ClassUtils.invoke(env, "setDebugOptions", EnumSet.of(DebugOption.TRACK));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void assertMatch(String pattern, String str)
    {
        if (!str.matches(pattern)) {
            failNotEquals("", pattern, str);
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static final String[] WITH_TRACK = {
            "    .Chat.bye.                      About to execute 'Chat.bye'",
            "        .Chat.hello.                    About to execute 'Chat.hello'",
            "        .Chat.hello.                    Hello World !",
            "        .Chat.hello.                    Execution of 'Chat.hello'. Finished in [0-9]+ milliseconds. Memory usage: [0-9]+M of [0-9]+M",
            "        .Chat.chat.                     About to execute 'Chat.chat'",
            "        .Chat.chat.                     How are you doing !",
            "        .Chat.chat.                     Execution of 'Chat.chat'. Finished in [0-9]+ milliseconds. Memory usage: [0-9]+M of [0-9]+M",
            "        .Chat.bye.                      About to execute 'Chat.bye'",
            "        .Chat.bye.                      Good Bye World !",
            "        .Chat.bye.                      Execution of 'Chat.bye'. Finished in [0-9]+ milliseconds. Memory usage: [0-9]+M of [0-9]+M",
            "    .Chat.bye.                      Execution of 'Chat.bye'. Finished in [0-9]+ milliseconds. Memory usage: [0-9]+M of [0-9]+M",
            ""
        };
}
