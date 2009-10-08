

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


package apb.tests.utils;

import apb.Messages;

import apb.sunapi.Base64;
import apb.sunapi.XmlSerializer;

import apb.tasks.CoreTasks;

import apb.tests.testutils.FileAssert;

import apb.utils.ClassUtils;
import apb.utils.CollectionUtils;
import apb.utils.ColorUtils;
import apb.utils.Constants;
import apb.utils.FileUtils;
import apb.utils.NameUtils;
import apb.utils.StringUtils;
import apb.utils.XmlUtils;

import junit.framework.TestCase;

//
/**
 * Just to mark coverage of the constructor of Util classes....
 */
public class DummyTest
    extends TestCase
{
    //~ Methods ..............................................................................................

    public void testConstructors()
    {
        new XmlSerializer();
        new Base64();
        new FileAssert();
        new ClassUtils();
        new CollectionUtils();
        new NameUtils();
        new StringUtils();
        new ColorUtils();
        new FileUtils();
        new XmlUtils();
        new CoreTasks();
        new Constants();
        new Messages();
    }
}
