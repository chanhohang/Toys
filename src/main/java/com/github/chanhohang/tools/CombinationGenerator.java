package com.github.chanhohang.tools;

import java.util.Arrays;

public class CombinationGenerator {

  public static void main(String[] args) {
    CombinationGenerator instance = new CombinationGenerator();
    instance.generateCompliance1_8_01a();
  }

  public void generateCompliance1_8_01a() {
    String[] parameters = new String[] { "a", "b", "c", "d", "e", "f",
      "g" };

    String formatStr = "insert into X values %s %s %s %s %s %s %s";
    for (int i = 0; i < Math.pow(2, parameters.length); i++) {
      String mask = String.format("%7s", Integer.toBinaryString(i)).replace(' ', '0');
      Object[] value = Arrays.copyOf(parameters, parameters.length);
      for (int y = 0; y < parameters.length - 1; y++) {
        if (mask.charAt(y) == '1')
          value[y] = "-";
      }

      System.out.println(String.format(formatStr, value));
    }
  }

}
