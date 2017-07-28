package com.primeNumber;


import java.util.ArrayList;
import java.util.List;

public class LinearSplitter {

    public Range[] split( Range range, int count )
    {
        if(range.getTo() - range.getFrom() + 1 < count){
            throw new RuntimeException("Not enough elements for split.");
        }

        List<Range> ranges = new ArrayList<>();

        double step = Math.max(0, (double)(range.getTo() - range.getFrom() + 1)/count - 1);

        double start = range.getFrom();
        double end = start;

        for(int i=0; i <count; ++i)
        {
            end = start + step;
            end = Math.min(range.getTo(), end);

            ranges.add(new Range((int)Math.round(start), (int)Math.round(end)));

            start = end+1;
        }

        return ranges.toArray(new Range[ranges.size()]);
    }
}

