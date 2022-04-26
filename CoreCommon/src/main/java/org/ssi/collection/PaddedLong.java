package org.ssi.collection;

import org.openjdk.jol.info.ClassLayout;

public class PaddedLong {
    public long value = 0;
    private long p1, p2, p3, p4, p5 =7 ;
    public static void main(String args[]){
        System.out.println(ClassLayout.parseClass(PaddedLong.class).toPrintable());
    }
}
