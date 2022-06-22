# -*- coding: UTF-8 -*-
from contextlib import contextmanager
import gc
from multiprocessing import Process
import subprocess
import unittest

from py4j.java_gateway import (
    JavaGateway, GatewayParameters, CallbackServerParameters,
    DEFAULT_PORT, DEFAULT_PYTHON_PROXY_PORT)
from py4j.clientserver import (
    ClientServer, JavaParameters, PythonParameters)
from py4j.tests.java_gateway_test import (
    PY4J_JAVA_PATH, check_connection, sleep)
from py4j.tests.py4j_callback_recursive_example import HelloState
from py4j.tests.instrumented import (
    InstrJavaGateway, InstrumentedPythonPing, register_creation,
    CREATED, FINALIZED, MEMORY_HOOKS, InstrClientServer)


def start_instrumented_gateway_server():
    subprocess.call([
        "java", "-Xmx512m", "-cp", PY4J_JAVA_PATH,
        "py4j.instrumented.InstrumentedApplication"])


def start_instrumented_clientserver():
    subprocess.call([
        "java", "-Xmx512m", "-cp", PY4J_JAVA_PATH,
        "py4j.instrumented.InstrumentedClientServerApplication"])


def start_gateway_server_example_app_process(start_gateway_server=True):
    # XXX DO NOT FORGET TO KILL THE PROCESS IF THE TEST DOES NOT SUCCEED
    if start_gateway_server:
        p = Process(target=start_instrumented_gateway_server)
    else:
        p = Process(target=start_instrumented_clientserver)
    p.start()
    sleep()
    check_connection()
    return p


@contextmanager
def gateway_server_example_app_process(start_gateway_server=True):
    p = start_gateway_server_example_app_process(start_gateway_server)
    try:
        yield p
    finally:
        p.join()


class HelloState2(HelloState):
    def __init__(self, run_gc=True):
        self.gateway = None
        self.run_gc = run_gc
        super(HelloState2, self).__init__()
        register_creation(self)

    def _play_with_jvm(self):
        al = self.gateway.jvm.java.util.ArrayList()
        al.append("Hello World")
        obj = self.gateway.jvm.py4j.\
            instrumented.InstrumentedObject("test")
        al.append(obj)
        return str(al)

    def sayHello(self, int_value=None, string_value=None):
        self._play_with_jvm()
        if self.run_gc:
            python_gc()
        return super(HelloState2, self).sayHello(
            int_value, string_value)

    class Java:
        implements = ["py4j.examples.IHello"]


def assert_python_memory(test, size):
    test.assertEqual(size, len(CREATED))
    test.assertEqual(size, len(FINALIZED))
    test.assertEqual(set(CREATED), set(FINALIZED))


def python_gc():
    """Runs the gc three times to ensure that all circular reference are
    correctly removed.
    """
    for i in range(3):
        gc.collect()


class GatewayServerTest(unittest.TestCase):

    def tearDown(self):
        MEMORY_HOOKS.clear()
        CREATED.clear()
        FINALIZED.clear()

    def testPythonToJava(self):
        def work_with_object(gateway):
            obj = gateway.jvm.py4j.\
                instrumented.InstrumentedObject("test")
            return str(obj)

        def internal_work():
            gateway2 = InstrJavaGateway(gateway_parameters=GatewayParameters(
                port=DEFAULT_PORT+5))
            sleep()
            work_with_object(gateway2)
            python_gc()
            sleep()
            gateway2.shutdown()

        with gateway_server_example_app_process():
            gateway = JavaGateway()
            gateway.entry_point.startServer2()
            internal_work()
            python_gc()
            gateway.jvm.py4j.instrumented.MetricRegistry.forceFinalization()
            sleep()
            createdSet = gateway.jvm.py4j.instrumented.MetricRegistry.\
                getCreatedObjectsKeySet()
            finalizedSet = gateway.jvm.py4j.instrumented.MetricRegistry.\
                getFinalizedObjectsKeySet()

            # 4 objects: GatewayServer, GatewayConnection, CallbackClient,
            # InstrumentedObject
            self.assertEqual(4, len(createdSet))
            self.assertEqual(4, len(finalizedSet))
            self.assertEqual(createdSet, finalizedSet)
            gateway.shutdown()

            # 4 objects: JavaGateway, GatewayClient, GatewayProperty,
            # GatewayConnection
            assert_python_memory(self, 4)

    def testPythonToJavaToPython(self):
        def play_with_ping(gateway):
            ping = InstrumentedPythonPing()
            pingpong = gateway.jvm.py4j.examples.PingPong()
            total = pingpong.start(ping)
            return total

        def internal_work():
            gateway2 = InstrJavaGateway(
                gateway_parameters=GatewayParameters(
                    port=DEFAULT_PORT+5),
                callback_server_parameters=CallbackServerParameters(
                    port=DEFAULT_PYTHON_PROXY_PORT+5))
            sleep()
            play_with_ping(gateway2)
            python_gc()
            sleep()
            gateway2.shutdown()

        with gateway_server_example_app_process():
            gateway = JavaGateway()
            gateway.entry_point.startServer2()
            internal_work()
            python_gc()
            gateway.jvm.py4j.instrumented.MetricRegistry.forceFinalization()
            sleep()
            createdSet = gateway.jvm.py4j.instrumented.MetricRegistry.\
                getCreatedObjectsKeySet()
            finalizedSet = gateway.jvm.py4j.instrumented.MetricRegistry.\
                getFinalizedObjectsKeySet()

            # 9 objects: GatewayServer, 4 GatewayConnection, CallbackClient,
            # 3 CallbackConnection
            self.assertEqual(9, len(createdSet))
            self.assertEqual(9, len(finalizedSet))
            self.assertEqual(createdSet, finalizedSet)
            gateway.shutdown()

            # 11 objects: JavaGateway, CallbackSerer, GatewayClient,
            # GatewayProperty, PythonPing, 4 GatewayConnection,
            # 3 CallbackConnection. Notice the symmetry
            assert_python_memory(self, 12)

    def testPythonToJavaToPythonClose(self):
        def play_with_ping(gateway):
            ping = InstrumentedPythonPing()
            pingpong = gateway.jvm.py4j.examples.PingPong()
            total = pingpong.start(ping)
            return total

        def internal_work(assert_memory):
            gateway2 = InstrJavaGateway(
                gateway_parameters=GatewayParameters(
                    port=DEFAULT_PORT+5),
                callback_server_parameters=CallbackServerParameters(
                    port=DEFAULT_PYTHON_PROXY_PORT+5))
            sleep()
            play_with_ping(gateway2)
            python_gc()
            sleep()
            gateway2.close(close_callback_server_connections=True,
                           keep_callback_server=True)
            sleep()
            assert_memory()
            gateway2.shutdown()
            sleep()

        with gateway_server_example_app_process():
            gateway = JavaGateway()
            gateway.entry_point.startServer2()

            def perform_memory_tests():
                python_gc()
                gateway.jvm.py4j.instrumented.MetricRegistry.\
                    forceFinalization()
                sleep()
                createdSet = gateway.jvm.py4j.instrumented.MetricRegistry.\
                    getCreatedObjectsKeySet()
                finalizedSet = gateway.jvm.py4j.instrumented.MetricRegistry.\
                    getFinalizedObjectsKeySet()

                # 10 objects: GatewayServer, 4 GatewayConnection,
                # CallbackClient, 4 CallbackConnection
                self.assertEqual(10, len(createdSet))
                # 13 objects: JavaGateway, CallbackSerer, GatewayClient,
                # GatewayProperty, PythonPing, 4 GatewayConnection,
                # 4 CallbackConnection. Notice the symmetry between callback
                # and gateway connections.
                self.assertEqual(13, len(CREATED))
                # 4 gateway connections, 3 callback connections.
                # There is still one callback connection staying around
                # following Java finalization that called back Python.
                self.assertEqual(7, len(finalizedSet))
                # Same amount of connections for the Python side
                self.assertEqual(7, len(FINALIZED))

            internal_work(perform_memory_tests)
            python_gc()
            gateway.jvm.py4j.instrumented.MetricRegistry.forceFinalization()
            sleep()
            gateway.shutdown()
            # 14 objects: JavaGateway, CallbackSerer, GatewayClient,
            # GatewayProperty, PythonPing, 5 GatewayConnection,
            # 4 CallbackConnection. Notice the symmetry
            # One more gateway connection created because we called shutdown
            # after close (which requires a connection to send a shutdown
            # command).
            assert_python_memory(self, 14)

    def testJavaToPythonToJavaCleanGC(self):
        def internal_work(gateway):
            hello_state = HelloState2()
            gateway2 = InstrJavaGateway(
                gateway_parameters=GatewayParameters(
                    port=DEFAULT_PORT+5),
                callback_server_parameters=CallbackServerParameters(
                    port=DEFAULT_PYTHON_PROXY_PORT+5),
                python_server_entry_point=hello_state)
            hello_state.gateway = gateway2
            sleep()

            gateway.entry_point.startServerWithPythonEntry(True)
            sleep()
            gateway2.shutdown()

            # Check that Java correctly called Python
            self.assertEqual(2, len(hello_state.calls))
            self.assertEqual((None, None), hello_state.calls[0])
            self.assertEqual((2, "Hello World"), hello_state.calls[1])

        with gateway_server_example_app_process():
            gateway = JavaGateway()
            internal_work(gateway)
            python_gc()
            gateway.jvm.py4j.instrumented.MetricRegistry.forceFinalization()
            sleep()
            createdSet = gateway.jvm.py4j.instrumented.MetricRegistry.\
                getCreatedObjectsKeySet()
            finalizedSet = gateway.jvm.py4j.instrumented.MetricRegistry.\
                getFinalizedObjectsKeySet()
            # 6 objects: 2 InstrumentedObject (sayHello called twice), 1
            # InstrGatewayServer, 1 CallbackClient, 1 CallbackConnection, 1
            # GatewayConnection
            self.assertEqual(6, len(createdSet))
            self.assertEqual(6, len(finalizedSet))
            self.assertEqual(createdSet, finalizedSet)
            gateway.shutdown()

            # 7 objects: JavaGateway, GatewayClient, CallbackServer,
            # GatewayProperty, HelloState, GatewayConnection,
            # CallbackConnection
            assert_python_memory(self, 7)

    def testJavaToPythonToJavaNoGC(self):
        def internal_work(gateway):
            hello_state = HelloState2(run_gc=False)
            gateway2 = InstrJavaGateway(
                gateway_parameters=GatewayParameters(
                    port=DEFAULT_PORT+5),
                callback_server_parameters=CallbackServerParameters(
                    port=DEFAULT_PYTHON_PROXY_PORT+5),
                python_server_entry_point=hello_state)
            hello_state.gateway = gateway2
            sleep()

            gateway.entry_point.startServerWithPythonEntry(True)
            sleep()
            gateway2.shutdown()

            # Check that Java correctly called Python
            self.assertEqual(2, len(hello_state.calls))
            self.assertEqual((None, None), hello_state.calls[0])
            self.assertEqual((2, "Hello World"), hello_state.calls[1])

        with gateway_server_example_app_process():
            gateway = JavaGateway()
            # We disable gc to test whether a shut down on one side will
            # garbage collect everything.
            gc.disable()
            internal_work(gateway)
            gc.enable()
            python_gc()
            gateway.jvm.py4j.instrumented.MetricRegistry.forceFinalization()
            sleep()
            createdSet = gateway.jvm.py4j.instrumented.MetricRegistry.\
                getCreatedObjectsKeySet()
            finalizedSet = gateway.jvm.py4j.instrumented.MetricRegistry.\
                getFinalizedObjectsKeySet()
            # 6 objects: 2 InstrumentedObject (sayHello called twice), 1
            # InstrGatewayServer, 1 CallbackClient, 1 CallbackConnection, 1
            # GatewayConnection
            self.assertEqual(6, len(createdSet))
            self.assertEqual(6, len(finalizedSet))
            self.assertEqual(createdSet, finalizedSet)
            gateway.shutdown()

            # 7 objects: JavaGateway, GatewayClient, CallbackServer,
            # GatewayProperty, HelloState, GatewayConnection,
            # CallbackConnection
            assert_python_memory(self, 7)

    def testJavaToPythonToJavaCleanGCNoShutdown(self):
        def internal_work(gateway):
            hello_state = HelloState2()
            gateway2 = InstrJavaGateway(
                gateway_parameters=GatewayParameters(
                    port=DEFAULT_PORT+5),
                callback_server_parameters=CallbackServerParameters(
                    port=DEFAULT_PYTHON_PROXY_PORT+5),
                python_server_entry_point=hello_state)
            hello_state.gateway = gateway2
            sleep()

            gateway.entry_point.startServerWithPythonEntry(False)
            sleep()
            gateway2.shutdown()

            # Check that Java correctly called Python
            self.assertEqual(2, len(hello_state.calls))
            self.assertEqual((None, None), hello_state.calls[0])
            self.assertEqual((2, "Hello World"), hello_state.calls[1])

        with gateway_server_example_app_process():
            gateway = JavaGateway()
            internal_work(gateway)
            python_gc()
            gateway.jvm.py4j.instrumented.MetricRegistry.forceFinalization()
            sleep()
            createdSet = gateway.jvm.py4j.instrumented.MetricRegistry.\
                getCreatedObjectsKeySet()
            finalizedSet = gateway.jvm.py4j.instrumented.MetricRegistry.\
                getFinalizedObjectsKeySet()
            # 6 objects: 2 InstrumentedObject (sayHello called twice), 1
            # InstrGatewayServer, 1 CallbackClient, 1 CallbackConnection, 1
            # GatewayConnection
            self.assertEqual(6, len(createdSet))
            self.assertEqual(6, len(finalizedSet))
            self.assertEqual(createdSet, finalizedSet)
            gateway.shutdown()

            # 7 objects: JavaGateway, GatewayClient, CallbackServer,
            # GatewayProperty, HelloState, GatewayConnection,
            # CallbackConnection
            assert_python_memory(self, 7)

    def testJavaToPythonToJavaNoGCNoShutdown(self):
        def internal_work(gateway):
            hello_state = HelloState2(run_gc=False)
            gateway2 = InstrJavaGateway(
                gateway_parameters=GatewayParameters(
                    port=DEFAULT_PORT+5),
                callback_server_parameters=CallbackServerParameters(
                    port=DEFAULT_PYTHON_PROXY_PORT+5),
                python_server_entry_point=hello_state)
            hello_state.gateway = gateway2
            sleep()

            gateway.entry_point.startServerWithPythonEntry(False)
            sleep()
            gateway2.shutdown()

            # Check that Java correctly called Python
            self.assertEqual(2, len(hello_state.calls))
            self.assertEqual((None, None), hello_state.calls[0])
            self.assertEqual((2, "Hello World"), hello_state.calls[1])

        with gateway_server_example_app_process():
            gateway = JavaGateway()
            # We disable gc to test whether a shut down on one side will
            # garbage collect everything.
            gc.disable()
            internal_work(gateway)
            gc.enable()
            python_gc()
            gateway.jvm.py4j.instrumented.MetricRegistry.forceFinalization()
            sleep()
            createdSet = gateway.jvm.py4j.instrumented.MetricRegistry.\
                getCreatedObjectsKeySet()
            finalizedSet = gateway.jvm.py4j.instrumented.MetricRegistry.\
                getFinalizedObjectsKeySet()
            # 6 objects: 2 InstrumentedObject (sayHello called twice), 1
            # InstrGatewayServer, 1 CallbackClient, 1 CallbackConnection, 1
            # GatewayConnection
            self.assertEqual(6, len(createdSet))
            self.assertEqual(6, len(finalizedSet))
            self.assertEqual(createdSet, finalizedSet)
            gateway.shutdown()

            # 7 objects: JavaGateway, GatewayClient, CallbackServer,
            # GatewayProperty, HelloState, GatewayConnection,
            # CallbackConnection
            assert_python_memory(self, 7)


class ClientServerTest(unittest.TestCase):

    def tearDown(self):
        MEMORY_HOOKS.clear()
        CREATED.clear()
        FINALIZED.clear()

    def testPythonToJava(self):
        def work_with_object(clientserver):
            obj = clientserver.jvm.py4j.\
                instrumented.InstrumentedObject("test")
            return str(obj)

        def internal_work():
            clientserver2 = InstrClientServer(
                JavaParameters(port=DEFAULT_PORT+5),
                PythonParameters(port=DEFAULT_PYTHON_PROXY_PORT+5))
            sleep()
            work_with_object(clientserver2)
            python_gc()
            sleep()
            clientserver2.shutdown()

        with gateway_server_example_app_process(False):
            clientserver = ClientServer()
            clientserver.entry_point.startServer2()
            internal_work()
            python_gc()
            clientserver.jvm.py4j.instrumented.MetricRegistry.\
                forceFinalization()
            sleep()
            createdSet = clientserver.jvm.py4j.instrumented.MetricRegistry.\
                getCreatedObjectsKeySet()
            finalizedSet = clientserver.jvm.py4j.instrumented.MetricRegistry.\
                getFinalizedObjectsKeySet()

            # 5 objects: ClientServer, ClientServerConnection, PythonClient,
            # JavaServer, InstrumentedObject
            self.assertEqual(5, len(createdSet))
            self.assertEqual(5, len(finalizedSet))
            self.assertEqual(createdSet, finalizedSet)
            clientserver.shutdown()

            # 5 objects: ClientServer, ClientServerConnection, PythonClient,
            # JavaServer, GatewayProperty
            assert_python_memory(self, 5)

    def testPythonToJavaToPython(self):
        def play_with_ping(clientserver):
            ping = InstrumentedPythonPing()
            pingpong = clientserver.jvm.py4j.examples.PingPong()
            total = pingpong.start(ping)
            return total

        def internal_work():
            clientserver2 = InstrClientServer(
                JavaParameters(port=DEFAULT_PORT+5),
                PythonParameters(port=DEFAULT_PYTHON_PROXY_PORT+5))
            sleep()
            play_with_ping(clientserver2)
            python_gc()
            sleep()
            clientserver2.shutdown()

        with gateway_server_example_app_process(False):
            clientserver = ClientServer()
            clientserver.entry_point.startServer2()
            internal_work()
            python_gc()
            clientserver.jvm.py4j.instrumented.MetricRegistry.\
                forceFinalization()
            sleep()
            createdSet = clientserver.jvm.py4j.instrumented.MetricRegistry.\
                getCreatedObjectsKeySet()
            finalizedSet = clientserver.jvm.py4j.instrumented.MetricRegistry.\
                getFinalizedObjectsKeySet()

            # 4 objects: ClientServer, ClientServerConnection, JavaServer,
            # PythonClient
            self.assertEqual(4, len(createdSet))
            self.assertEqual(4, len(finalizedSet))
            self.assertEqual(createdSet, finalizedSet)
            clientserver.shutdown()

            # 6 objects: ClientServer, PythonServer, JavaClient,
            # GatewayProperty, PythonPing, ClientServerConnection

    def testPythonToJavaToPythonClose(self):
        def play_with_ping(clientserver):
            ping = InstrumentedPythonPing()
            pingpong = clientserver.jvm.py4j.examples.PingPong()
            total = pingpong.start(ping)
            return total

        def internal_work(assert_memory):
            clientserver2 = InstrClientServer(
                JavaParameters(port=DEFAULT_PORT+5),
                PythonParameters(port=DEFAULT_PYTHON_PROXY_PORT+5))
            sleep()
            play_with_ping(clientserver2)
            python_gc()
            sleep()
            clientserver2.close(
                close_callback_server_connections=True,
                keep_callback_server=True)
            sleep()
            assert_memory()
            clientserver2.shutdown()
            sleep()

        with gateway_server_example_app_process(False):
            clientserver = ClientServer()
            clientserver.entry_point.startServer2()

            def perform_memory_tests():
                python_gc()
                clientserver.jvm.py4j.instrumented.MetricRegistry.\
                    forceFinalization()
                sleep()

                createdSet = clientserver.jvm.py4j.instrumented.\
                    MetricRegistry.getCreatedObjectsKeySet()
                finalizedSet = clientserver.jvm.py4j.instrumented.\
                    MetricRegistry.getFinalizedObjectsKeySet()

                # 6 objects: ClientServer, JavaServer,
                # PythonClient, 3 ClientServerConnection.
                self.assertEqual(6, len(createdSet))

                # Should be 2: ClientServer, 1 ClientServerConnection
                # But for some reasons, Java refuses to collect the
                # clientserverconnection even though there are no strong
                # references.
                self.assertEqual(1, len(finalizedSet))

                # 8 objects: ClientServer, PythonServer, JavaClient,
                # GatewayProperty, PythonPing, 3 ClientServerConnection
                self.assertEqual(8, len(CREATED))

                # PythonPing + ClientServerConnection
                self.assertEqual(2, len(FINALIZED))

            internal_work(perform_memory_tests)
            python_gc()
            clientserver.jvm.py4j.instrumented.MetricRegistry.\
                forceFinalization()
            sleep()

            clientserver.shutdown()

            # 9 objects: ClientServer, PythonServer, JavaClient,
            # GatewayProperty, PythonPing, 4 ClientServerConnection
            assert_python_memory(self, 9)

    def testJavaToPythonToJavaCleanGC(self):
        def internal_work(clientserver):
            hello_state = HelloState2()
            clientserver2 = InstrClientServer(
                JavaParameters(port=DEFAULT_PORT+5),
                PythonParameters(port=DEFAULT_PYTHON_PROXY_PORT+5),
                python_server_entry_point=hello_state)
            hello_state.gateway = clientserver2
            sleep()

            clientserver.entry_point.startServerWithPythonEntry(True)
            sleep()
            clientserver2.shutdown()

            # Check that Java correctly called Python
            self.assertEqual(2, len(hello_state.calls))
            self.assertEqual((None, None), hello_state.calls[0])
            self.assertEqual((2, "Hello World"), hello_state.calls[1])

        with gateway_server_example_app_process(False):
            clientserver = ClientServer()
            internal_work(clientserver)
            python_gc()
            clientserver.jvm.py4j.instrumented.MetricRegistry.\
                forceFinalization()
            sleep()
            createdSet = clientserver.jvm.py4j.instrumented.MetricRegistry.\
                getCreatedObjectsKeySet()
            finalizedSet = clientserver.jvm.py4j.instrumented.MetricRegistry.\
                getFinalizedObjectsKeySet()
            # 7 objects: 2 InstrumentedObject (sayHello called twice), 1
            # JavaServer, 1 PythonClient, 1 ClientServer, 2
            # ClientServerConnection (1 to call sayHello)
            self.assertEqual(6, len(createdSet))
            self.assertEqual(6, len(finalizedSet))
            self.assertEqual(createdSet, finalizedSet)
            clientserver.shutdown()

            # 8 objects: ClientServer (ok), PythonServer (ok), JavaClient,
            # GatewayProperty, HelloState (ok), 3 ClientServer Connections (1)
            assert_python_memory(self, 7)

    def testJavaToPythonToJavaNoGC(self):
        def internal_work(clientserver):
            hello_state = HelloState2()
            clientserver2 = InstrClientServer(
                JavaParameters(port=DEFAULT_PORT+5),
                PythonParameters(port=DEFAULT_PYTHON_PROXY_PORT+5),
                python_server_entry_point=hello_state)
            hello_state.gateway = clientserver2
            sleep()

            clientserver.entry_point.startServerWithPythonEntry(True)
            sleep()
            clientserver2.shutdown()

            # Check that Java correctly called Python
            self.assertEqual(2, len(hello_state.calls))
            self.assertEqual((None, None), hello_state.calls[0])
            self.assertEqual((2, "Hello World"), hello_state.calls[1])

        with gateway_server_example_app_process(False):
            clientserver = ClientServer()
            # We disable gc to test whether a shut down on one side will
            # garbage collect everything.
            gc.disable()
            internal_work(clientserver)
            gc.enable()
            python_gc()
            clientserver.jvm.py4j.instrumented.MetricRegistry.\
                forceFinalization()
            sleep()
            createdSet = clientserver.jvm.py4j.instrumented.MetricRegistry.\
                getCreatedObjectsKeySet()
            finalizedSet = clientserver.jvm.py4j.instrumented.MetricRegistry.\
                getFinalizedObjectsKeySet()
            # 7 objects: 2 InstrumentedObject (sayHello called twice), 1
            # JavaServer, 1 PythonClient, 1 ClientServer, 2
            # ClientServerConnection (1 to call sayHello)
            self.assertEqual(6, len(createdSet))
            self.assertEqual(6, len(finalizedSet))
            self.assertEqual(createdSet, finalizedSet)
            clientserver.shutdown()

            # 8 objects: ClientServer (ok), PythonServer (ok), JavaClient,
            # GatewayProperty, HelloState (ok), 3 ClientServer Connections (2)
            assert_python_memory(self, 7)

    def testJavaToPythonToJavaCleanGCNoShutdown(self):
        def internal_work(clientserver):
            hello_state = HelloState2()
            clientserver2 = InstrClientServer(
                JavaParameters(port=DEFAULT_PORT+5),
                PythonParameters(port=DEFAULT_PYTHON_PROXY_PORT+5),
                python_server_entry_point=hello_state)
            hello_state.gateway = clientserver2
            sleep()

            clientserver.entry_point.startServerWithPythonEntry(False)
            sleep()
            clientserver2.shutdown()

            # Check that Java correctly called Python
            self.assertEqual(2, len(hello_state.calls))
            self.assertEqual((None, None), hello_state.calls[0])
            self.assertEqual((2, "Hello World"), hello_state.calls[1])

        with gateway_server_example_app_process(False):
            clientserver = ClientServer()
            # We disable gc to test whether a shut down on one side will
            # garbage collect everything.
            internal_work(clientserver)
            python_gc()
            clientserver.jvm.py4j.instrumented.MetricRegistry.\
                forceFinalization()
            sleep()
            createdSet = clientserver.jvm.py4j.instrumented.MetricRegistry.\
                getCreatedObjectsKeySet()
            finalizedSet = clientserver.jvm.py4j.instrumented.MetricRegistry.\
                getFinalizedObjectsKeySet()
            # 8 objects: 2 InstrumentedObject (sayHello called twice), 1
            # JavaServer, 1 PythonClient, 1 ClientServer, 3
            # ClientServerConnection (1 to call sayHello,
            # 1 that receives shutdown command)
            self.assertEqual(7, len(createdSet))
            self.assertEqual(7, len(finalizedSet))
            self.assertEqual(createdSet, finalizedSet)
            clientserver.shutdown()

            # 8 objects: ClientServer (ok), PythonServer (ok), JavaClient,
            # GatewayProperty, HelloState (ok), 3 ClientServer Connections (2)
            assert_python_memory(self, 7)

    def testJavaToPythonToJavaNoGCNoShutdown(self):
        def internal_work(clientserver):
            hello_state = HelloState2()
            clientserver2 = InstrClientServer(
                JavaParameters(port=DEFAULT_PORT+5),
                PythonParameters(port=DEFAULT_PYTHON_PROXY_PORT+5),
                python_server_entry_point=hello_state)
            hello_state.gateway = clientserver2
            sleep()

            clientserver.entry_point.startServerWithPythonEntry(False)
            sleep()
            clientserver2.shutdown()

            # Check that Java correctly called Python
            self.assertEqual(2, len(hello_state.calls))
            self.assertEqual((None, None), hello_state.calls[0])
            self.assertEqual((2, "Hello World"), hello_state.calls[1])

        with gateway_server_example_app_process(False):
            clientserver = ClientServer()
            # We disable gc to test whether a shut down on one side will
            # garbage collect everything.
            gc.disable()
            internal_work(clientserver)
            gc.enable()
            python_gc()
            clientserver.jvm.py4j.instrumented.MetricRegistry.\
                forceFinalization()
            sleep()
            createdSet = clientserver.jvm.py4j.instrumented.MetricRegistry.\
                getCreatedObjectsKeySet()
            finalizedSet = clientserver.jvm.py4j.instrumented.MetricRegistry.\
                getFinalizedObjectsKeySet()
            # 7 objects: 2 InstrumentedObject (sayHello called twice), 1
            # JavaServer, 1 PythonClient, 1 ClientServer, 3
            # ClientServerConnection (1 to call sayHello,
            # 1 that receives shutdown command)
            self.assertEqual(7, len(createdSet))
            self.assertEqual(7, len(finalizedSet))
            self.assertEqual(createdSet, finalizedSet)
            clientserver.shutdown()

            # 8 objects: ClientServer (ok), PythonServer (ok), JavaClient,
            # GatewayProperty, HelloState (ok), 3 ClientServer Connections (2)
            assert_python_memory(self, 7)
