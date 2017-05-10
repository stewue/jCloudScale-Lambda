package ch.uzh.ifi.seal.jcs_lambda.testing.asynchronous;

import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference.JcsMessageQueue;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestAsynchronous {

    @Test
    @StartUp
    public void test() throws Exception {
        int a = 2;
        int b = 5;

        JcsMessageQueue queue = JcsMessageQueue.getInstance();

        ExecutorService es = Executors.newFixedThreadPool(3);
        final Future<Integer> future = es.submit(new Callable() {
            public Object call() throws Exception {
                TestObject testObject = new TestObject();
                return testObject.sum( a, b );
            }
        });

        assert( future.isDone() == false );

        int result = future.get();

        assert( queue.getPendingRequests() == 0 );
        assert( future.isDone() );
        assert( result == a + b );
    }
}
