"""
Created on Sep 17, 2010

@author: barthelemy
"""
from __future__ import unicode_literals, absolute_import

from threading import Thread
import unittest

from py4j.compat import range
from py4j.java_gateway import JavaGateway
from py4j.tests.java_gateway_test import (
    start_example_app_process, sleep)


class TestJVM1(Thread):
    def __init__(self, gateway):
        Thread.__init__(self)
        self.gateway = gateway

    def run(self):
        for i in range(3):
            print(self.gateway.jvm.java.lang.System.currentTimeMillis())
            sleep()


class TestJVM2(Thread):
    def __init__(self, System):
        Thread.__init__(self)
        self.System = System

    def run(self):
        for i in range(3):
            print(self.System.currentTimeMillis())
            sleep()


class TestJVM3(Thread):
    def __init__(self, jvm):
        Thread.__init__(self)
        self.jvm = jvm

    def run(self):
        for i in range(3):
            print(self.jvm.java.lang.System.currentTimeMillis())
            sleep()


class TestJVM4(Thread):
    def __init__(self, System):
        Thread.__init__(self)
        self.System = System

    def run(self):
        print(self.System.currentTimeMillis())
        try:
            self.System.loadLibrary("toto")
        except Exception:
            print("Good!")
        print(self.System.currentTimeMillis())


class JVMMultiProcessTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()
        self.gateway = JavaGateway()

    def tearDown(self):
        self.p.terminate()
        self.gateway.shutdown()
        sleep()

    def testMultiProcessJVMAccess(self):
        workers = [TestJVM1(self.gateway) for _ in range(8)]

        for worker in workers:
            worker.start()

        for worker in workers:
            worker.join()

    def testMultiProcessSystemReference(self):
        System = self.gateway.jvm.java.lang.System
        workers = [TestJVM2(System) for _ in range(8)]

        for worker in workers:
            worker.start()

        for worker in workers:
            worker.join()

    def testMultiProcessJVMReference(self):
        jvm = self.gateway.jvm
        workers = [TestJVM3(jvm) for _ in range(8)]

        for worker in workers:
            worker.start()

        for worker in workers:
            worker.join()

    def testMultiProcessJVMError(self):
        System = self.gateway.jvm.java.lang.System
        workers = [TestJVM4(System) for _ in range(8)]

        for worker in workers:
            worker.start()

        for worker in workers:
            worker.join()


if __name__ == "__main__":
    unittest.main()
