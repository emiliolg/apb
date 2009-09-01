

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

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Oct 27, 2008
// Time: 2:26:47 PM

public class Proxy
{
    //~ Instance fields ......................................................................................

    @NotNull private final String host;
    @NotNull private final String nonProxyHosts;
    @NotNull private final String password;
    private final int                   port;
    @NotNull private final String protocol;
    @NotNull private final String username;

    //~ Constructors .........................................................................................

    private Proxy(@NotNull String host, int port, @NotNull String nonProxyHosts, @NotNull String username,
                  @NotNull String password, @NotNull String protocol)
    {
        this.host = host;
        this.nonProxyHosts = nonProxyHosts;
        this.password = password;
        this.port = port;
        this.protocol = protocol;
        this.username = username;
    }

    //~ Methods ..............................................................................................

    public static Proxy getDefaultProxy(Environment env)
    {
        String host = env.getProperty("proxy.host", "");
        int    port;

        try {
            port = Integer.parseInt(env.getProperty("proxy.port", "-1"));
        }
        catch (NumberFormatException e) {
            port = -1;
        }

        Proxy proxy = null;

        if (!host.isEmpty() && port != -1) {
            proxy =
                new Proxy(host, port, env.getProperty("proxy.nonProxyHosts", ""),
                          env.getProperty("proxy.user", ""), env.getProperty("proxy.password", ""),
                          env.getProperty("proxy.protocol", ""));
        }

        return proxy;
    }

    @NotNull public String getProtocol()
    {
        return protocol;
    }

    @NotNull public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    @NotNull public String getNonProxyHosts()
    {
        return nonProxyHosts;
    }

    @NotNull public String getUsername()
    {
        return username;
    }

    @NotNull public String getPassword()
    {
        return password;
    }
}
