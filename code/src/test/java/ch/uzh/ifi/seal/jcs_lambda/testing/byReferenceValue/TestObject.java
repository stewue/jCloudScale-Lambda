package ch.uzh.ifi.seal.jcs_lambda.testing.byReferenceValue;

import ch.uzh.ifi.seal.jcs_lambda.annotations.ByReference;
import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.ReadOnly;

import java.util.UUID;

public class TestObject {
    public String _uuid_ = UUID.randomUUID().toString();

    @ReadOnly
    private int a;

    @ByReference
    private Complex b;

    public void initialize( int a, Complex b){
        this.a = a;
        this.b = b;
    }

    @CloudMethod( timeout = 15, memory = 512 )
    public int doSomething(){
        System.out.println("Cloud only");
        System.out.println( "IS NULL: " + b.y );

        b.y = "Modified";
        b = new Complex();

        return a * 2;
    }
}
