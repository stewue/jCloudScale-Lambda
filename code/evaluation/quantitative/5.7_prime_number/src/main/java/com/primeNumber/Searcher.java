package com.primeNumber;

import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.ReadOnly;

public class Searcher {
    @ReadOnly
    private Range range;
    private long result;

    public Searcher (){

    }

    public  Searcher ( Range range ){
        if(range.getFrom() <= 0 || range.getTo() <= 0)
            throw new RuntimeException("Range contains negative or zero parameters.");

        this.range = range;
        this.result = 0;
    }

    private boolean isPrime(long number) {
        // even numbers
        if(number % 2 == 0){
            return false;
        }

        long max = (long) Math.floor( Math.sqrt(number) );

        for(long i=3; i <= max; i+=2){
            if(number % i == 0){
                return false;
            }
        }

        return true;
    }

    @CloudMethod( memory = 1536, timeout = 60 )
    public long run()
    {
        long start = Math.max( 1, range.getFrom() );
        long finish = Math.max( start, range.getTo() );

        if ( start > 2 )
        {
            if( start%2 == 0 ){
                start = start+1;
            }
        }
        else
        {
            if(start == 1){
                result = finish >= 2 ? 2 : 1;
            }
            else{
                if(start == 2){
                    result = 1;
                }
            }

            start = 3;
        }

        for(long i = start; i <= finish; i += 2){
            if(isPrime(i)){
                result++;
            }
        }

        System.out.println("#### In "+range+" found "+ result +" prime numbers.####");

        return result;
    }
}
