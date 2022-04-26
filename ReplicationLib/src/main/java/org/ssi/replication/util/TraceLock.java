package org.ssi.replication.util;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TraceLock extends ReentrantLock {

    private static final long serialVersionUID = 1997992705529515418L;
    @Nullable
    private volatile Throwable here;

    @NotNull
    public static ReentrantLock create() {
        return Jvm.isDebug() ? new TraceLock() : new ReentrantLock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        super.lockInterruptibly();
        here = new StackTrace();
    }

    @Override
    public void lock() {

        super.lock();
        here = new StackTrace();
    }

    @Override
    public String toString() {

        @Nullable Throwable here = this.here;
        if (here == null)
            return super.toString();

        @NotNull final StringBuilder sb = new StringBuilder(super.toString());

        for (StackTraceElement s : here.getStackTrace()) {
            sb.append("\n\tat ").append(s);
        }

        return sb.toString();

    }

    @Override
    public void unlock() {
        if (getHoldCount() == 1)
            here = null;
        super.unlock();
    }

    @Override
    public boolean tryLock() {
        final boolean b = super.tryLock();
        if (b)
            here = new StackTrace();
        return b;
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {

        final boolean b = super.tryLock(timeout, unit);
        if (b)
            here = new StackTrace();
        return b;
    }
}
