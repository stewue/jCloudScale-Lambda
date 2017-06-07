package ch.uzh.ifi.seal.jcs_lambda.testing.genericType;

import ch.uzh.ifi.seal.jcs_lambda.annotations.ByReference;
import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.Local;
import ch.uzh.ifi.seal.jcs_lambda.annotations.ReadOnly;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public class TestObject {

    @ReadOnly
    public List<Complex> list = new ArrayList<>();

    public TestObject(){
        list.add( new Complex(1,2) );
        list.add( new Complex(3,4) );
        list.add( new Complex(5,6) );
        list.add( new Complex(7,8) );
    }

    @CloudMethod
    public int sum(){
        int sum = 0;

        for ( Complex element : list ){
            sum += element.a + element.b;
        }

        return sum;
    }

    @CloudMethod
    public List<Complex> addAndReturn (){
        list.add( new Complex(9,10 ) );
        return list;
    }

    public List<Complex> getList (){
        return list;
    }


}
