/*
 * Copyright 2012 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ssi.collection;

import org.openjdk.jol.info.ClassLayout;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PaddedAtomicInt extends AtomicInteger
{
    public PaddedAtomicInt()
    {
    }

    public PaddedAtomicInt(final int initialValue)
    {
        super(initialValue);
    }

    public long sumPaddingToPreventOptimisation()
    {
        return p1 + p2 + p3 + p4 +p5 + p6
                +p7 + p8 + p9 + p10 +p11 + p12;
    }

    private volatile int p1, p2, p3, p4, p5, p6, p7,p8,p9,p10,p11,p12 = 7;

    public static void main(String args[]){
        System.out.println(ClassLayout.parseClass(PaddedLong.class).toPrintable());
    }
}
