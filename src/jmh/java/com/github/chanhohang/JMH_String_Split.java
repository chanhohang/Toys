package com.github.chanhohang;

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;

public class JMH_String_Split
{

    private static String testString = "a,bc,def,ghi,jk,lmo,pq,rstuv,wxy,z";

    private static String separator = ",";

    @Benchmark
    public String[] measureSplitIntoWords_split()
    {
        return splitIntoWords_split( testString, separator );
    }

    @Benchmark
    public String[] measureSplitIntoWords_tokenizer()
    {
        return splitIntoWords_tokenizer( testString, separator );
    }

    @Benchmark
    public String[] measureSplitIntoWords_indexOf()
    {
        return splitIntoWords_IndexOf( testString, separator );
    }

    @Benchmark
    public String[] measureSplitIntoTokens_split_true()
    {
        return splitIntoTokens_split( testString, separator, true );
    }

    @Benchmark
    public String[] measureSplitIntoTokens_split_false()
    {
        return splitIntoTokens_split( testString, separator, false );
    }

    @Benchmark
    public String[] measureSplitIntoTokens_indexOf_true()
    {
        return splitIntoTokens_indexOf( testString, separator, true );
    }

    @Benchmark
    public String[] measureSplitIntoTokens_indexOf_false()
    {
        return splitIntoTokens_indexOf( testString, separator, false );
    }

    private String[] splitIntoWords_split( String textToParse, String separator )
    {
        return textToParse.split( separator );
    }

    private String[] splitIntoWords_IndexOf( String textToParse, String separator )
    {
        List<String> result = new ArrayList<>();
        int endPosition = -1;
        int startPosition = 0;
        int separatorLength = separator.length();
        endPosition = textToParse.indexOf( separator, startPosition );
        while ( endPosition != -1 )
        {
            result.add( textToParse.substring( startPosition, endPosition ) );
            startPosition = endPosition + separatorLength;
            endPosition = textToParse.indexOf( separator, startPosition );
        }

        result.add( textToParse.substring( startPosition ) );

        return result.toArray( new String[] {} );
    }

    private String[] splitIntoWords_tokenizer( String textToParse, String separator )
    {
        StringTokenizer st = new StringTokenizer( textToParse, separator );
        String[] strings = new String[st.countTokens()];
        int i = 0;
        while ( st.hasMoreTokens() )
        {
            strings[i++] = st.nextToken();
        }

        return strings;
    }

    private static String[] EMPTY_STRING = new String[0];

    private String[] splitIntoTokens_split( String textToParse, String separator, boolean includeSeparatorInTokens )
    {
        if (textToParse.length() == 0)
        {
            return EMPTY_STRING;
        }

        String[] result = textToParse.split( separator );

        if (includeSeparatorInTokens)
        {
            for ( int i = 0; i < result.length - 1; i++ )
            {
                result[i] = result[i] + separator;
            }
        }
        return result;
    }

    private String[] splitIntoTokens_indexOf( String textToParse, String separator, boolean includeSeparatorInTokens )
    {
        if (textToParse.length() == 0)
        {
            return new String[0];
        }

        List<String> tokens = new ArrayList<String>();
        int lastIndex = 0;
        for ( int index = textToParse.indexOf( separator ); index != -1; index = textToParse.indexOf( separator,
                lastIndex ) )
        {
            String token = textToParse.substring( lastIndex, index );
            if (includeSeparatorInTokens)
            {
                tokens.add( token + separator );
            }
            else
            {
                tokens.add( token );
            }
            lastIndex = index + separator.length();
        }
        // add on everything since last match
        tokens.add( textToParse.substring( lastIndex ) );
        return (String[]) tokens.toArray( new String[0] );
    }

    @Test
    public void testEquals()
    {
        assertArrayEquals( measureSplitIntoWords_indexOf(), measureSplitIntoWords_split() );
        assertArrayEquals( measureSplitIntoWords_indexOf(), measureSplitIntoWords_tokenizer() );
        assertArrayEquals( measureSplitIntoTokens_indexOf_false(), measureSplitIntoTokens_split_false() );
        assertArrayEquals( measureSplitIntoTokens_indexOf_true(), measureSplitIntoTokens_split_true() );
    }
}
