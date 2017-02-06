package com.github.chanhohang;

import org.openjdk.jmh.annotations.Benchmark;

public class JMH_SubString_Capitalize
{

    @Benchmark
    public String measureCapitalize_1()
    {
        return capitalize( "someInputString" );
    }

    private String capitalize( String original )
    {
        if (null == original)
        {
            return null;
        }

        int length = original.length();

        if (0 == length)
        {
            return original;
        }

        String originalFirst = original.substring( 0, 1 );
        String originalLast = original.substring( 1, length );

        originalFirst = originalFirst.toUpperCase();

        return originalFirst.concat( originalLast );
    }

}
