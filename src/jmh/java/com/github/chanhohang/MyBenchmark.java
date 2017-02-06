/* Copyright Ullink 2017 */
package com.github.chanhohang;

import java.lang.reflect.Field;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class MyBenchmark {

    @Benchmark
    public void test1(Blackhole blackhole) {
        String string = "someInputString";
        string = Character.toUpperCase(string.charAt(0)) + string.substring(1);
        blackhole.consume(string);
    }

    @Benchmark
    public void test2(Blackhole blackhole) {
        String string = "someInputString";
        char c[] = string.toCharArray();
        c[0] = Character.toUpperCase(c[0]);
        string = new String(c);
        blackhole.consume(string);
    }

    @Benchmark
    public void test3(Blackhole blackhole) {
        String string = "someInputString";
        char c[] = string.toCharArray();
        c[0] += 32;
        string = new String(c);
        blackhole.consume(string);
    }

    @Benchmark
    public void test4(Blackhole blackhole) {
        String string = "someInputString";
        StringBuilder sb = new StringBuilder(string);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        string = sb.toString();
        blackhole.consume(string);
    }

    @Benchmark
    public void test5(Blackhole blackhole) {
        String string = "someInputString";
        string = string.substring(0, 1).toUpperCase() + string.substring(1);
        blackhole.consume(string);
    }

    @Benchmark
    public void test6(Blackhole blackhole) {
        String string = "someInputString";
        try {
            Field field = String.class.getDeclaredField("value");
            field.setAccessible(true);
            char[] value = (char[]) field.get(string);
            value[0] = Character.toUpperCase(value[0]);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        blackhole.consume(string);
    }

}
