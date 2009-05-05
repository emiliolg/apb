package apb.testrunner;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import java.util.Enumeration;

public class SkippedTest implements Test  {
    private Test test;

    public SkippedTest(Test test) {
        this.test = test;
    }




    private void addSkipped(Test testToSkip, TestResultWrapper testResult) {
        if(testToSkip instanceof TestSuite){
            for (Enumeration e= ((TestSuite)testToSkip).tests(); e.hasMoreElements(); ) {
                Test test= (Test)e.nextElement();
                addSkipped(test, testResult);
            }
        }
        else{
            testResult.addSkipped();
        }
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
