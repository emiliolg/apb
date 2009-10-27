

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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks a method as a BuildTarget for apb
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BuildTarget
{
    /**
     * Specify the list of BuildTargets this method depends on
     */
    String[] depends() default {};

    /**
     * Specify that this BuildTarget must be invoked <b>before</b> a given Target
     */
    String before() default "";

    /**
     * Specify the name of the Target.
     * If not name is specified it will be inferred from the method name
     */
    String name() default "";

    /**
     * Specify a description from the BuildTarget, for help and documentation purpouses.
     */
    String description() default "";

    /**
     * Specify that the command must be applied recursively to the dependencies
     */
    boolean recursive() default true;
}
