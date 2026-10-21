package net.servboot.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.function.Consumer;

public interface IServerManager {

    /**
     * Sets server port
     * @param port New port
     */
    void setPort(int port);

    /**
     * Returns server port
     * @return server port
     */
    int getPort();

    /**
     * Sets the maximum number of concurrent requests supported
     * @param maxRequests Maximum number of concurrent requests supported
     */
    void setMaxRequests(int maxRequests);

    /**
     * Returns the maximum number of concurrent requests supported
     * @return maximum number of concurrent requests supported
     */
    int getMaxRequests();

    /**
     * Returns the client executor
     * @return client executor
     */
    IClientExecutor getClientExecutor();

    /**
     * Returns the server
     * @return server
     */
    ServerSocket getServer();

    /**
     * Sets the logger. This logger will be called when an exception occurs
     * @param logger The logger
     */
    void setLogger(Consumer<Exception> logger);

    /**
     * Returns the logger
     * @return logger
     */
    Consumer<Exception> getLogger();

    /**
     * Prepare the server
     */
    void initServer() throws IOException;

    /**
     * Starts the server
     */
    void startServer() throws IOException;

    /**
     * Stop the server
     */
    void stopServer();

    /**
     * Returns the amount of free memory in the Java Virtual Machine
     * @return an approximation to the total amount of memory currently available for future allocated objects, measured in bytes.
     */
    long getFreeMemory();

    /**
     * Returns the total amount of memory in the Java virtual machine. The value returned by this method may vary over time, depending on the host environment.
     * @return the total amount of memory currently available for current and future objects, measured in bytes.
     */
    long getTotalMemory();

    /**
     * Returns the maximum amount of memory that the Java virtual machine will attempt to use. If there is no inherent limit then the value Long.MAX_VALUE will be returned
     * @return the maximum amount of memory that the virtual machine will attempt to use, measured in bytes
     */
    long getMaxMemory();

    /**
     * Returns the number of threads running
     * @return clients running
     */
    int getExecutingThreads();
}
