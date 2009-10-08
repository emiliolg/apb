package apb.testrunner;

import junit.framework.TestResult;
import junit.framework.Test;
import junit.framework.TestListener;

import java.util.List;
import java.util.ArrayList;

public class TestResultWrapper extends TestResult {
    private final List<JUnitTestSet.TestListenerAdaptor> listeners = new ArrayList<JUnitTestSet.TestListenerAdaptor>();

    @Override
    public void addListener(TestListener testListener) {
        super.addListener(testListener);
        if(testListener instanceof JUnitTestSet.TestListenerAdaptor){
            listeners.add((JUnitTestSet.TestListenerAdaptor)testListener);
        }
    }

    public void addSkipped() {

        for (JUnitTestSet.TestListenerAdaptor listener : listeners) {
            listener.addSkipped();
        }
    }

}
