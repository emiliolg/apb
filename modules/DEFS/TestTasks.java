

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



import apb.coverage.CoverageReport;

import apb.idegen.IdeaInfo;

import apb.metadata.BuildProperty;
import apb.metadata.TestModule;

public class TestTasks
    extends TestModule
{
    //~ Instance fields ......................................................................................

    @BuildProperty public final IdeaInfo idegen = new IdeaInfo();

    //~ Instance initializers ................................................................................

    {
        coverage.enable = true;

        coverage.ensure = 78;

        coverage.reports(CoverageReport.HTML);

        setenv("APB_PROJECT_PATH", "");

        setProperty("datadir", "$basedir/$dir/data");
        setProperty("apb-jar", "$basedir/../lib/apb.jar");
        setProperty("module-src", "$basedir/$source");

        idegen.contentDirs("$dir/data");
    }
}
