

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


package apb.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Collections;

import apb.BaseEnvironment;
//
// User: emilio
// Date: Dec 10, 2008
// Time: 5:51:45 PM

//
public class StandaloneEnv
    extends BaseEnvironment
{
    //~ Instance fields ......................................................................................

    private final Logger logger;

    //~ Constructors .........................................................................................

    public StandaloneEnv()
    {
        this(Collections.<String, String>emptyMap());
    }

    public StandaloneEnv(Map<String, String> properties)
    {
        super(properties);
        logger = createLogger("apb");
        postInit();
    }

    //~ Methods ..............................................................................................

    public void logInfo(String msg, Object... args)
    {
        logger.log(Level.INFO, msg, args);
    }

    public void logWarning(String msg, Object... args)
    {
        logger.log(Level.WARNING, msg, args);
    }

    public void logSevere(String msg, Object... args)
    {
        logger.log(Level.SEVERE, msg, args);
    }

    public void logVerbose(String msg, Object... args)
    {
        logger.log(Level.FINE, msg, args);
    }

    public void setVerbose()
    {
        super.setVerbose();
        setLogLevel(Level.FINE);
    }

    public void setQuiet()
    {
        super.setQuiet();
        setLogLevel(Level.WARNING);
    }

    void setLogLevel(Level level)
    {
        logger.setLevel(level);

        for (Handler handler : logger.getHandlers()) {
            handler.setLevel(level);
        }
    }

    Logger createLogger(final String name)
    {
        Logger lg = Logger.getLogger(name);
        lg.setUseParentHandlers(false);
        Handler h = new ConsoleHandler();
        h.setFormatter(createFormatter());
        lg.addHandler(h);
        lg.setLevel(Level.INFO);
        return lg;
    }

    private SimpleFormatter createFormatter()
    {
        boolean useColor = getBooleanProperty("color", true) && System.console() != null;
        return useColor ? new ColorFormatter(this) : new SimpleFormatter(this);
    }
}
