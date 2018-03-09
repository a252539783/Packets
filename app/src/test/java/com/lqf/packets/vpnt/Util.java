package com.lqf.packets.vpnt;

/**
 * Created by Administrator on 2017/12/7.
 */

public class Util {

    public static void pln(int[] array) {
        for (int i=0;i<array.length;i++)
        {
            p(String.format("%-10d",array[i]));
        }
        pln("");
    }

    public static void p(Object o) {
        System.out.print(o);
    }

    public static void pln(Object o)
    {
        System.out.println(o);
    }
}
