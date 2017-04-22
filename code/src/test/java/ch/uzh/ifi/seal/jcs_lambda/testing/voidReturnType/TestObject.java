package ch.uzh.ifi.seal.jcs_lambda.testing.voidReturnType;

import ch.uzh.ifi.seal.jcs_lambda.annotations.ByReference;
import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;

import java.util.UUID;

public class TestObject {
    public String _uuid_ = UUID.randomUUID().toString();

    @ByReference
    private int a;

    public void setA ( int a ){
        this.a = a;
    }

    public int getA (){
        return a;
    }

    @CloudMethod( timeout = 15, memory = 512 )
    public void doSomething(){
        System.out.println("Cloud only");
        a = 1234;
        try {
            Thread.sleep(2000);
        }
        catch ( Exception e ){}
    }

    public String toString (){
        return "a: " + a;
    }
}
