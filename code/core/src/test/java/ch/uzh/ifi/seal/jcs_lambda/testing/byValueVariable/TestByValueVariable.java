package ch.uzh.ifi.seal.jcs_lambda.testing.byValueVariable;

import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;
import org.junit.Assert;
import org.junit.Test;

public class TestByValueVariable {

    @Test
    @StartUp
    public void test(){
        int aOrigin = TestObject.a;
        int bOrigin = TestObject.b;

        int a = 0;
        int c = 8;
        int d = 3;

        TestObject testObject = new TestObject();
        testObject.initialize( a, c );

        int result = testObject.sum( d );

        Assert.assertEquals( result, aOrigin + bOrigin + c + d );
    }
}
