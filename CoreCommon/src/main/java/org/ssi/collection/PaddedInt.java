package org.ssi.collection;

import org.openjdk.jol.info.ClassLayout;

public class PaddedInt {
    public int value =0;
    private int p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12 =7;

    public static void main(String args[]){
		System.out.println(ClassLayout.parseClass(PaddedInt.class).toPrintable());
	}
}
