package math;

public class Fraction
{
    //~ Instance fields ......................................................................................

    private int denominator;
    private int numerator;

    //~ Constructors .........................................................................................

    public Fraction(int numerator, int denominator)
    {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    //~ Methods ..............................................................................................

    @Override public boolean equals(Object obj)
    {
        return this == obj ||
               obj instanceof Fraction &&
               numerator * ((Fraction) obj).denominator == denominator * ((Fraction) obj).numerator;
    }

    public Fraction multiply(Fraction that)
    {
        return new Fraction(numerator * that.numerator, denominator * that.denominator);
    }

    public Fraction divide(Fraction that)
    {
        return new Fraction(numerator * that.denominator, denominator * that.denominator);
    }

    @Override public String toString()
    {
        return numerator + "/" + denominator;
    }
}
