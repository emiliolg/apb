

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
//
// User: emilio
// Date: Sep 3, 2008
// Time: 12:29:57 PM

//
public interface Dependency
    extends Named,
            Dependencies
{
    //~ Methods ..............................................................................................

    @NotNull Module asModule();

    @NotNull Library asLibrary();

    boolean isModule();

    boolean isLibrary();

    /**
     * Wheter a dependency must be included when compiling or in runtime
     * Pure dependencies will be included always
     * Compile-Only dependencies will be included only if the parameter is true
     * Runtime-Only dependencies will be included only if the parameter is false
     * @param compile true for compilation, false for runtime
     * @return if this dependency must be included or not
     */
    boolean mustInclude(boolean compile);
}
