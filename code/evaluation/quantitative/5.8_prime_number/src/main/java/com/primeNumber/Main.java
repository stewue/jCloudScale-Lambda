package com.primeNumber;

import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;

public class Main
{
    @StartUp
    public static void main ( String [] args ){
        System.out.println("Starting...");
        long start = System.nanoTime();

        // input parameters.
        final int threadCount = 5;
        final int from = 1;
        final int to = Integer.MAX_VALUE/100;

        System.out.println( String.format("Searching prime numbers between %s and %s in %s threads.", from, to, threadCount) );

        ThreadPoolScheduler scheduler = new ThreadPoolScheduler( threadCount, from, to );
        System.out.println( String.format("Execution finished. %s prime numbers found.", scheduler.search() ) );

        long elapsed = (System.nanoTime() - start)/1000000;

        long min = elapsed /(60*1000);
        long sec = (elapsed % (60*1000))/1000;
        long msec = elapsed % 1000;
        System.out.println( String.format("Elapsed: %02d:%02d.%03d", min, sec, msec) );
    }
}
