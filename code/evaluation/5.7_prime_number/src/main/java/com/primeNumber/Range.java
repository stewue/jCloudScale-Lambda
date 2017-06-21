package com.primeNumber;

public class Range
{
    private long from;
    private long to;

    public Range(long from, long to)
    {
        if(from > to)
            throw new RuntimeException("Range is incorrect! From ("+from+") > To ("+to+")");

        this.from = from;
        this.to = to;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    @Override
    public String toString()
    {
        return "(" + from + ", " + to + ")";
    }
}
