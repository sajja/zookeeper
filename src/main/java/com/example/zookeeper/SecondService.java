package com.example.zookeeper;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.RetryNTimes;
import com.netflix.curator.x.discovery.ServiceDiscoveryBuilder;
import com.netflix.curator.x.discovery.ServiceInstance;
import com.netflix.curator.x.discovery.UriSpec;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class SecondService {
    public static void httpServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/test", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) throws IOException {
                String response = "Welcome Real's HowTo test page";
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        });
        init();
        server.start();
    }

    public static void init() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("localhost:2181", new RetryNTimes(5, 1000));
        curatorFramework.start();
        ServiceInstance<SecondService> serviceInstance = ServiceInstance.<SecondService>builder()
                .uriSpec(new UriSpec("{scheme}://{address}:{port}/test"))
                .address("localhost")
                .port(8080)
                .name("worker")
                .build();

        ServiceDiscoveryBuilder.<SecondService>builder(SecondService.class)
                .basePath("load-balancing-example")
                .client(curatorFramework)
                .thisInstance(serviceInstance)
                .build()
                .start();
    }

    public static void main(String[] args) throws Exception {
        httpServer();
    }
}

