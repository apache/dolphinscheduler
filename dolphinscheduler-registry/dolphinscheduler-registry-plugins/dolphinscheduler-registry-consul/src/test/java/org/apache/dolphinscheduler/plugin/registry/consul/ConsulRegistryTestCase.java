package org.apache.dolphinscheduler.plugin.registry.consul;

public class ConsulRegistryTestCase {

    public static void main(String[] args) throws InterruptedException {
        ConsulRegistryProperties prop = new ConsulRegistryProperties();
        prop.setUrl("http://127.0.0.1:8500");
        prop.setNamespace("test");
        ConsulRegistry consulRegistry = new ConsulRegistry(prop);
        consulRegistry.start();
        consulRegistry.put("x/1", "mytest", true);
        Thread.sleep(3000);
        System.out.println("before destorySession-" + consulRegistry.get("x/1"));
        consulRegistry.destorySession();
        System.out.println("after destorySession-" + consulRegistry.get("x/1"));

    }
}
