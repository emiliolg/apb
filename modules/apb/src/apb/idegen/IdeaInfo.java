

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


package apb.idegen;

import apb.metadata.BuildProperty;
//
// User: emilio
// Date: Sep 10, 2009
// Time: 3:58:01 PM

//
public class IdeaInfo
{
    //~ Instance fields ......................................................................................

    /**
     * Wheter to include empty source directories (like generated sources)
     * in the idea definitions
     */
    @BuildProperty public boolean includeEmptyDirs = false;

    /**
     * The directory where the idea '.iml' & 'ipr' files are going to be placed
     */
    @BuildProperty public String dir = "$basedir/IDEA";

    /**
     * The name of the jdk to use
     */
    @BuildProperty public final String jdkName = "$java.specification.version";

    /**
     * The name of the template module file
     * If empty it will take the one in apb.jar
     */
    @BuildProperty public final String moduleTemplate = "";

    /**
     * The name of the template project file
     * If empty it will take the one in apb.jar
     */
    @BuildProperty public final String projectTemplate = "";
}
