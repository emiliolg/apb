

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


package apb.metadata;  // Copyright 2008-2009 Emilio Lopez-Gabeiras

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

// User: emilio
// Date: Jun 28, 2009
// Time: 11:11:40 AM

/**
 * This class represents an Update policy, that allow to configure the frecuency for downloading updates
 */
public class UpdatePolicy
{
    //~ Instance fields ......................................................................................

    private long interval;

    //~ Constructors .........................................................................................

    private UpdatePolicy(long interval)
    {
        this.interval = interval;
    }

    //~ Methods ..............................................................................................

    /**
     * Create an Update policy based on a given number of hours
     * @param hours number of hours (it can be fractional)
     * @return The update policy
     */
    public static UpdatePolicy every(double hours)
    {
        return new UpdatePolicy((long) (hours * MS_PER_HOUR));
    }

    public long getInterval()
    {
        return interval;
    }

    public double getHours()
    {
        return (int) (interval / MS_PER_HOUR);
    }

    @Override public String toString()
    {
        return interval < 0 ? "Never" : interval == 0 ? "Always" : "Every " + getHours() + "hours.";
    }

    @Override public boolean equals(Object o)
    {
        return this == o ||  //
               o != null && o instanceof UpdatePolicy &&  //
               interval == ((UpdatePolicy) o).interval;
    }

    @Override public int hashCode()
    {
        return (int) (interval ^ (interval >>> 32));
    }

    //~ Static fields/initializers ...........................................................................

    private static final int MS_PER_HOUR = 60 * 60 * 1000;

    public static UpdatePolicy WEEKLY = every(24 * 7);
    public static UpdatePolicy DAILY = every(24);
    public static UpdatePolicy HOURLY = every(1);
    public static UpdatePolicy ALWAYS = new UpdatePolicy(0);
    public static UpdatePolicy NEVER = new UpdatePolicy(-1);
}
