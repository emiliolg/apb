package math.test;

import junit.framework.Assert;
import junit.framework.TestCase;

import math.Fraction;

//
public class FractionTest
    extends TestCase
{
    //~ Methods ..............................................................................................

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

    @Override protected void setUp()
        throws Exception
    {
        super.setUp();
        String s = System.getProperty("module");
        System.out.println("module = " + s);
    }

    //~ Static fields/initializers ...........................................................................

    //    public void testDivide()
    //    {
    //        assertEquals(ONE, ONE_QUARTER.divide(ONE_QUARTER));
    //    }

    private static final Fraction ONE = new Fraction(1, 1);
    private static final Fraction ONE_HALF = new Fraction(1, 2);
    private static final Fraction ONE_QUARTER = new Fraction(1, 4);
}
