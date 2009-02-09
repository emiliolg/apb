
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

package apb;
//
// User: emilio
// Date: Oct 27, 2008
// Time: 2:26:47 PM

public class Proxy
{
    //~ Instance fields ......................................................................................

    private String host;
    private String nonProxyHosts;
    private String password;
    private int    port;
    private String protocol;
    private String username;

    //~ Constructors .........................................................................................

    private Proxy(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    //~ Methods ..............................................................................................

    public static Proxy getDefaultProxy(Environment env)
    {
        String host = env.getProperty("proxy.host");
        int    port = -1;

        try {
            String p = env.getProperty("proxy.port");

            if (p != null) {
                port = Integer.parseInt(p);
            }
        }
        catch (NumberFormatException e) {}

        Proxy proxy = null;

        if (host != null && port != -1) {
            proxy = new Proxy(host, port);
            proxy.nonProxyHosts = env.getProperty("proxy.nonProxyHosts");
            proxy.username = env.getProperty("proxy.user");
            proxy.password = env.getProperty("proxy.password");
            proxy.protocol = env.getProperty("proxy.protocol");
        }

        return proxy;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    public String getNonProxyHosts()
    {
        return nonProxyHosts;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }
}
