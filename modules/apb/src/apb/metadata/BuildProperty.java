
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

package apb.metadata;

import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks a Field as a property.<br>
 * This has essentially 2 efects:
 * <ul>
 * <li>If the field is a String the inital value is expanded using {@link apb.Environment#expand(String)}
 * <li> The value of the field is stored as a property in the environment
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(FIELD)
public @interface BuildProperty
{
    /**
     * The order attribute is used to force a property to be processes before.<br>
     * So if you want to ensure that a given Property is processed before others, give the property a low
     * order value.
     */
    int order() default 10000;
}
