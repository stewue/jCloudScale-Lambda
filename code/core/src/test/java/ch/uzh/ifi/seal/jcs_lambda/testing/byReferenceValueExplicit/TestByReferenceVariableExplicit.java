package ch.uzh.ifi.seal.jcs_lambda.testing.byReferenceValueExplicit;

import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestByReferenceVariableExplicit {
    @Test
    @StartUp
    public void test(){
        int a = 7;
        Complex complex = new Complex();
        complex.y = "Hallo";

        Map<String, Integer> map = new HashMap<>();
        map.put( "a", 2);
        map.put( "b", 7);
        complex.z = map;

        TestObject testObject = new TestObject();
        testObject.initialize( a, complex );
        int result = testObject.doSomething();

        Assert.assertEquals( testObject.getB().y, "TEST STRING" );
    }
}
