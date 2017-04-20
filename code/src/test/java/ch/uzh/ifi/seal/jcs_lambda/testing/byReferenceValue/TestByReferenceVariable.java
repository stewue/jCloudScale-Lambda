package ch.uzh.ifi.seal.jcs_lambda.testing.byReferenceValue;

import ch.uzh.ifi.seal.jcs_lambda.annotations.ByReference;
import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.crypto.dsig.Reference;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestByReferenceVariable {
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

        Assert.assertEquals( result, 2 * a );
    }
}
