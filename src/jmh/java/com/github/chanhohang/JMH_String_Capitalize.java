package com.github.chanhohang;

import org.openjdk.jmh.annotations.Benchmark;

public class JMH_String_Capitalize
{

    @Benchmark
    public String measureCapitalize_subString()
    {
        return capitalize_subString( "someInputString" );
    }

    @Benchmark
    public String measureCapitalize_charArray()
    {
        return capitalize_charArray( "someInputString" );
    }

    private String capitalize_charArray( String original )
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

    private String capitalize_subString( String original )
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
