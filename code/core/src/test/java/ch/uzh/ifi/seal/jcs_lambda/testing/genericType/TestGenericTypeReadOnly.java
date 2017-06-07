package ch.uzh.ifi.seal.jcs_lambda.testing.genericType;

import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestGenericTypeReadOnly {

    @Test
    @StartUp
    public void test(){
        TestObject testObject = new TestObject();

        int result = testObject.sum();

        List<Complex> list = testObject.getList();
        int checksum = 0;

        for ( Complex element : list ){
            checksum += element.a + element.b;
        }

        Assert.assertEquals( result, checksum );
    }

    @Test
    public void test2(){
        TestObject testObject = new TestObject();
        int size = testObject.getList().size();

        List<Complex> list = testObject.addAndReturn();

        Assert.assertEquals( list.size(), size + 1 );
    }
}
