

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


package apb.metadata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//
// User: emilio
// Date: Nov 14, 2008
// Time: 3:07:02 PM

//
public enum TestType
{
    JUNIT("apb.testrunner.JunitTestSetCreator"),
    JUNIT4("apb.testrunner.Junit4TestSetCreator"),
    TESTNG("apb.testrunner.TestNGTestSetCreator"),
    CUSTOM(null);

    TestType(@Nullable String creatorClassName)
    {
        this.creatorClassName = creatorClassName;
    }

    @Nullable private final String creatorClassName;

    @NotNull public String creatorClass(@Nullable String customCreatorClassName)
    {
        final String result = creatorClassName != null ? creatorClassName : customCreatorClassName;

        if (result == null) {
            throw new IllegalArgumentException("For custom creator you need to specify a creator class.");
        }

        return result;
    }
}
