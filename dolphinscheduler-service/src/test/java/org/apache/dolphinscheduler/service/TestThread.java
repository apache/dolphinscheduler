package org.apache.dolphinscheduler.service;

public class TestThread implements Runnable {

    public void start(){
        Thread thread = new Thread(this);
        thread.start();

    }
    @Override
    public void run() {
        System.out.println("aaaaaa");
    }

    public static void main(String[] args) {
        TestThread testThread = new TestThread();
        testThread.start();
    }
}
