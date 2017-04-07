package ch.uzh.ifi.seal.jcs_lambda.testing.complex;

import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestComplexTypes {

    @Test
    @StartUp
    public void test(){
        int a = 7;
        int b = 3;
        String c = "Hello world";

        int x = 10;
        String y = "Second chance";
        Map<String, Integer> z = new HashMap<>();
        z.put( "KEY", 0 );
        int z_size = z.size();

        Complex d  = new Complex();
        d.x = x;
        d.y = y;
        d.z = z;

        InObject inObject = new InObject();
        inObject.setA( a );
        inObject.setB( b );
        inObject.setC( c );
        inObject.setD( d );

        OutObject result = modify( inObject );

        Assert.assertEquals( result.getE(), a + b );
        Assert.assertEquals( result.getF().length(), 1 );
        Assert.assertEquals( result.getG().x, 2 * x );
        Assert.assertEquals( result.getG().y.length(), 2 * y.length() + 1 );
        Assert.assertEquals( result.getG().z.size(), z_size + 1 );
    }

    @CloudMethod
    private OutObject modify( InObject inObject ){
        System.out.println("Cloud only");

        OutObject outObject = new OutObject();
        outObject.setE( inObject.getA() + inObject.getB() );
        outObject.setF( inObject.getC().substring(0, 1) );

        Complex complex = inObject.getD();
        complex.x *= 2;
        complex.y = complex.y + "#" + complex.y;
        complex.z.put( "ADD", 1 );

        outObject.setG( complex );

        return outObject;
    }
}
