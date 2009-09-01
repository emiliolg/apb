package apb.testrunner;

import junit.framework.Test;
import junit.framework.TestResult;

public class SkippedTest implements Test  {
    private final Test test;

    public SkippedTest(Test test) {
        this.test = test;
    }


    public int countTestCases() {
        return test.countTestCases();
    }

    public void run(TestResult testResult) {
        if(testResult instanceof TestResultWrapper){
            ((TestResultWrapper)testResult).addSkipped();
        }
    }
}
