package ch.uzh.ifi.seal.jcs_lambda.testing.voidReturnType;

import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;
import org.junit.Assert;
import org.junit.Test;

public class TestVoidReturnType {
    @Test
    @StartUp
    public void test(){
        int a = 99;
        TestObject testObject = new TestObject();
        testObject.setA( a );
        testObject.doSomething();
        int result = testObject.getA();

        Assert.assertEquals( result, 1234 );
    }
}
