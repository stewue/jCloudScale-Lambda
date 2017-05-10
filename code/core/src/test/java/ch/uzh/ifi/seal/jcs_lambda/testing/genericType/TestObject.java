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
    public List<Integer> list = new ArrayList<>();

    public TestObject(){
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
    }

    @CloudMethod
    public int sum(){
        int sum = 0;

        for ( Integer element : list ){
            sum += element;
        }

        return sum;
    }

    @CloudMethod
    public List<Integer> addAndReturn (){
        list.add(5);
        return list;
    }

    public List<Integer> getList (){
        return list;
    }


}
