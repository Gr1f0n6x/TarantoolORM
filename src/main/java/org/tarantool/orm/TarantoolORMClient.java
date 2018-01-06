package org.tarantool.orm;

import org.tarantool.SocketChannelProvider;
import org.tarantool.TarantoolClient;
import org.tarantool.TarantoolClientConfig;
import org.tarantool.TarantoolClientImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * Created by GrIfOn on 06.01.2018.
 */
public class TarantoolORMClient {
    public static TarantoolClient build(String host, int port) {
        TarantoolClientConfig config = new TarantoolClientConfig();
        config.username = "guest";

        return new TarantoolClientImpl(getSocketChannelProvider(host, port), config);
    }

    public static TarantoolClient build(String username, String password, String host, int port) {
        TarantoolClientConfig config = new TarantoolClientConfig();
        config.username = username;
        config.password = password;

        return new TarantoolClientImpl(getSocketChannelProvider(host, port), config);
    }

    public static TarantoolClient build(SocketChannelProvider socketProvider, TarantoolClientConfig config) {
        return new TarantoolClientImpl(socketProvider, config);
    }

    private static SocketChannelProvider getSocketChannelProvider(String host, int port) {
        return (retryNumber, lastError) -> {
            if (lastError != null) {
                lastError.printStackTrace(System.out);
            }
            try {
                return SocketChannel.open(new InetSocketAddress(host, port));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        };
    }
}
