

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


package math.test;

import java.util.Properties;

import junit.framework.TestCase;
import junit.framework.Assert;
import math.Fraction;
//
// User: emilio
// Date: Mar 4, 2009
// Time: 5:07:21 PM

//
public class FractionTest
    extends TestCase
{
    //~ Methods ..............................................................................................

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String s = System.getProperty("module");
        System.out.println("module = " + s);
    }

    public void testEquals()
    {
        Assert.assertEquals(ONE_HALF, new Fraction(2, 4));
        Assert.assertEquals(ONE_HALF, ONE_HALF);
        Assert.assertFalse(ONE_HALF.equals(ONE_QUARTER));
    }

    public void testMultiply()
    {
        assertEquals(ONE_QUARTER, ONE_HALF.multiply(ONE_HALF));
    }

    public void testToString()
    {
        assertEquals(ONE_QUARTER.toString(), "1/4");
    }

//    public void testDivide()
//    {
//        assertEquals(ONE, ONE_QUARTER.divide(ONE_QUARTER));
//    }

    //~ Static fields/initializers ...........................................................................

    private static final Fraction ONE = new Fraction(1, 1);
    private static final Fraction ONE_HALF = new Fraction(1, 2);
    private static final Fraction ONE_QUARTER = new Fraction(1, 4);
}
