

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


package math;
//
// User: emilio
// Date: Mar 4, 2009
// Time: 4:57:47 PM

//
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
