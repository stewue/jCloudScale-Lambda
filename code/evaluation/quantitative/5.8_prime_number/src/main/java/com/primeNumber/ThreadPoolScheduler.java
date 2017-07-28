package com.primeNumber;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadPoolScheduler {
    int threadCount;
    int from;
    int to;

    private AtomicLong result = new AtomicLong(0);
    private CountDownLatch countDown;

    public ThreadPoolScheduler ( int threadCount, int from, int to ){
        this.threadCount = threadCount;
        this.from = from;
        this.to = to;
    }

    public long search(){
        ExecutorService executor = Executors.newCachedThreadPool();
        LinearSplitter splitter = new LinearSplitter();

        // init
        result.set(0);
        countDown = new CountDownLatch(threadCount);

        for( Range range : splitter.split(new Range(from, to), threadCount) ){
            executor.execute( new SearchContainer(range) );
        }

        //waiting for result
        try
        {
            countDown.await();
        }
        catch (InterruptedException e){

        }

        executor.shutdown();

        return result.get();
    }

    private void reportResult(long count)
    {
        result.addAndGet(count);
        countDown.countDown();
    }

    private class SearchContainer implements Runnable
    {
        Range range;

        public SearchContainer(Range range)
        {
            this.range = range;
        }

        @Override
        public void run()
        {
            Searcher searcher = new Searcher( range );

            long result = searcher.run();
            reportResult( result );
        }
    }
}
