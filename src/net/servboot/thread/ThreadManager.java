package net.servboot.thread;

public class ThreadManager {
    private static long count = 0;

    public static long getNext() {
        return ++count;
    }
}
