

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


import apb.metadata.*;

import static apb.tasks.CoreTasks.*;

public class Inner
    extends ProjectElement
{
    //~ Instance fields ......................................................................................

    @BuildProperty final Car main = new Car("BMW", 2007);
    @BuildProperty final Car second = new Car("Honda", 2003);

    //~ Methods ..............................................................................................

    @BuildTarget public void cars()
    {
        printf("main:   %s %d\n", main.brand, main.year);
        printf("second: %s %d\n", second.brand, second.year);
    }

    //~ Inner Classes ........................................................................................

    static class Car
    {
        @BuildProperty int    year;
        @BuildProperty String brand;

        Car(String b, int y)
        {
            brand = b;
            year = y;
        }
    }
}
