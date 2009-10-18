

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


package apb;
//
// User: emilio
// Date: Mar 4, 2009
// Time: 11:34:45 AM

/**
 * Signals that a Module or Project has a problem in its definition.
 * Usually some syntax error that doesn't allow the Java source to be compiled.
 *
 */
public class DefinitionException
    extends Throwable
{
    //~ Constructors .........................................................................................

    /**
     * Constructs an {@code DefinitionException} fot the specified element and with the specified cause.
     *
     * @param elementName
     *        The element (Module or Project) that has the definition problem
     *
     * @param cause
     *        The cause (which is saved for later retrieval by the
     *        {@link #getCause()} method).  (A null value is permitted,
     *        and indicates that the cause is nonexistent or unknown.)
     *
     */
    public DefinitionException(String elementName, Throwable cause)
    {
        super("Cannot load definition for: " + elementName, cause);
    }

    //~ Static fields/initializers ...........................................................................

    private static final long serialVersionUID = 727227781131642345L;
}
