package com.example.zookeeper;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.ExponentialBackoffRetry;
import com.netflix.curator.x.discovery.ServiceDiscovery;
import com.netflix.curator.x.discovery.ServiceDiscoveryBuilder;
import com.netflix.curator.x.discovery.ServiceInstance;

import java.util.Collection;

public class SecondClient {
    public static CuratorFramework curator() {
        return CuratorFrameworkFactory.newClient("localhost", new ExponentialBackoffRetry(1000, 3));
    }

    public static ServiceDiscovery<SecondService> discovery() {
        CuratorFramework curator = curator();
        curator.start();

        return ServiceDiscoveryBuilder.builder(SecondService.class)
                .client(curator)
                .basePath("load-balancing-example")
                .build();
    }

    public static void main(String[] args) throws Exception {
        ServiceDiscovery<SecondService> discovery = discovery();
        discovery.start();

        Collection<ServiceInstance<SecondService>> services = discovery.queryForInstances("worker");
        for (ServiceInstance<SecondService> service : services) {
            System.out.println(service.buildUriSpec());
        }
    }
}
