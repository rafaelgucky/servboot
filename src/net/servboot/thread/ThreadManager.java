package net.servboot.thread;

import net.servboot.client.ClientRequestTask;

public class ThreadManager {
    private static long count = 0;

    public static long getNext() {
        return ++count;
    }

    public static ClientRequestTask getCurrentThread() {
        return (ClientRequestTask) Thread.currentThread();
    }
}
