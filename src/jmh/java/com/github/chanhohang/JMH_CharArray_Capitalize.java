package com.github.chanhohang;

import org.apache.commons.lang3.RandomStringUtils;
import org.openjdk.jmh.annotations.Benchmark;

public class JMH_CharArray_Capitalize
{

    @Benchmark
    public String measureCapitalize_1()
    {
        return capitalize( RandomStringUtils.random( 255 ) );
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

        char[] value = original.toCharArray();
        value[0] = Character.toUpperCase( value[0] );

        return String.valueOf( value );
    }

}
