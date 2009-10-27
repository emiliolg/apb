

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



import java.util.ArrayList;
import java.util.List;

import apb.metadata.BuildProperty;
import apb.metadata.BuildTarget;
import apb.metadata.Module;

import static java.util.Arrays.asList;

import static apb.tasks.CoreTasks.printf;

public final class Properties
    extends Module
{
    //~ Instance fields ......................................................................................

    @BuildProperty public boolean bool = true;

    @BuildProperty public Boolean   o_bool = true;
    @BuildProperty public byte      b = 1;
    @BuildProperty public Byte      o_b = 1;
    @BuildProperty public char      chr = 'a';
    @BuildProperty public Character o_chr = 'a';
    @BuildProperty public double    d = 1;
    @BuildProperty public Double    o_d = 1.0;
    @BuildProperty public float     f = 1;
    @BuildProperty public Float     o_f = 1.0f;
    @BuildProperty public int       i = 1;
    @BuildProperty public Integer   o_i = 1;

    @BuildProperty(elementType = Byte.class)
    public List<Byte> l_b = new ArrayList<Byte>(asList((byte) 10, (byte) 20));

    @BuildProperty(elementType = Boolean.class)
    public List<Boolean> l_bool = new ArrayList<Boolean>(asList(true, false));

    @BuildProperty(elementType = Character.class)
    public List<Character> l_chr = new ArrayList<Character>(asList('a', 'b'));

    @BuildProperty(elementType = Double.class)
    public List<Double> l_d = new ArrayList<Double>(asList(10.0, 20.0));

    @BuildProperty(elementType = MyEnum.class)
    public List<MyEnum> l_e = new ArrayList<MyEnum>(asList(MyEnum.BETA, MyEnum.GAMMA));

    @BuildProperty(elementType = String.class)
    public List<String> l_estr = new ArrayList<String>(asList("str1$id", "str2${id}value"));

    @BuildProperty(elementType = Float.class)
    public List<Float> l_f = new ArrayList<Float>(asList(10.0f, 20.0f));

    @BuildProperty(elementType = Integer.class)
    public List<Integer> l_i = new ArrayList<Integer>(asList(10, 20));

    @BuildProperty(elementType = Long.class)
    public List<Long> l_l = new ArrayList<Long>(asList(10L, 20L));

    @BuildProperty(elementType = Short.class)
    public List<Short> l_s = new ArrayList<Short>(asList((short) 10, (short) 20));

    @BuildProperty(elementType = String.class)
    public List<String>          l_str = new ArrayList<String>(asList("str1", "str2"));
    @BuildProperty public long   l = 1;
    @BuildProperty public Long   o_l = 1L;
    @BuildProperty public MyEnum e = MyEnum.ALFA;
    @BuildProperty public Short  o_s = 1;
    @BuildProperty public short  s = 1;
    @BuildProperty public String estr = "str$id ${id}value";

    @BuildProperty public String str = "str_value";

    @BuildProperty Inner inner = new Inner();

    //~ Methods ..............................................................................................

    @BuildTarget public void info()
    {
        print("str");
        print("estr");
        print("bool");
        print("chr");
        print("b");
        print("s");
        print("i");
        print("l");
        print("f");
        print("d");
        print("e");

        print("o-bool");
        print("o-chr");
        print("o-b");
        print("o-s");
        print("o-i");
        print("o-l");
        print("o-f");
        print("o-d");

        print("l-str");
        print("l-estr");
        print("l-bool");
        print("l-chr");
        print("l-b");
        print("l-s");
        print("l-i");
        print("l-l");
        print("l-f");
        print("l-d");
        print("l-e");

        print("inner.str");
        print("inner.bool");
        print("inner.chr");
        print("inner.b");
        print("inner.s");
        print("inner.i");
        print("inner.l");
        print("inner.f");
        print("inner.d");
        print("inner.e");
    }

    @BuildTarget public void field()
    {
        print("str", str);
        print("estr", estr);
        print("bool", bool);
        print("chr", chr);
        print("b", b);
        print("s", s);
        print("i", i);
        print("l", l);
        print("f", f);
        print("d", d);
        print("e", e);

        print("o-bool", o_bool);
        print("o-chr", o_chr);
        print("o-b", o_b);
        print("o-s", o_s);
        print("o-i", o_i);
        print("o-l", o_l);
        print("o-f", o_f);
        print("o-d", o_d);

        print("l-str", l_str);
        print("l-estr", l_estr);
        print("l-bool", l_bool);
        print("l-chr", l_chr);
        print("l-b", l_b);
        print("l-s", l_s);
        print("l-i", l_i);
        print("l-l", l_l);
        print("l-f", l_f);
        print("l-d", l_d);
        print("l-e", l_e);

        print("inner.str", inner.str);
        print("inner.bool", inner.bool);
        print("inner.chr", inner.chr);
        print("inner.b", inner.b);
        print("inner.s", inner.s);
        print("inner.i", inner.i);
        print("inner.l", inner.l);
        print("inner.f", inner.f);
        print("inner.d", inner.d);
        print("inner.e", inner.e);
    }

    void print(String propName)
    {
        printf("%s=%s\n", propName, getHelper().getProperty(propName));
    }

    void print(String propName, Object value)
    {
        printf("%s=%s\n", propName, String.valueOf(value));
    }

    //~ Enums ................................................................................................

    static enum MyEnum
    {
        ALFA,
        BETA,
        GAMMA
    }

    //~ Inner Classes ........................................................................................

    static class Inner
    {
        @BuildProperty public boolean bool = false;
        @BuildProperty public byte    b = 2;
        @BuildProperty public char    chr = 'b';
        @BuildProperty public double  d = 2;
        @BuildProperty public float   f = 2;
        @BuildProperty public int     i = 2;
        @BuildProperty public long    l = 2;
        @BuildProperty public MyEnum  e = MyEnum.BETA;
        @BuildProperty public short   s = 2;
        @BuildProperty public String  str = "str_inner";
    }
}
