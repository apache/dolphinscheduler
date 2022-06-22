# -*- coding: UTF-8 -*-
from contextlib import contextmanager
import gc
from multiprocessing import Process
import subprocess
import threading
import unittest

from py4j.clientserver import (
    ClientServer, JavaParameters, PythonParameters)
from py4j.java_gateway import GatewayConnectionGuard, is_instance_of, \
    GatewayParameters, DEFAULT_PORT, DEFAULT_PYTHON_PROXY_PORT
from py4j.protocol import Py4JError, Py4JJavaError, smart_decode
from py4j.tests.java_callback_test import IHelloImpl, IHelloFailingImpl
from py4j.tests.java_gateway_test import (
    PY4J_JAVA_PATH, check_connection, sleep, WaitOperator)
from py4j.tests.memory_leak_test import python_gc
from py4j.tests.py4j_callback_recursive_example import (
    PythonPing, HelloState)


def start_clientserver_example_server(*args):
    subprocess.call([
        "java", "-Xmx512m", "-cp", PY4J_JAVA_PATH,
        "py4j.examples.SingleThreadApplication"] + list(args))


def start_short_timeout_clientserver_example_server(*args):
    subprocess.call([
        "java", "-Xmx512m", "-cp", PY4J_JAVA_PATH,
        "py4j.examples.SingleThreadApplication$"
        "SingleThreadShortTimeoutApplication"] + list(args))


def start_java_clientserver_example_server(*args):
    subprocess.call([
        "java", "-Xmx512m", "-cp", PY4J_JAVA_PATH,
        "py4j.examples.SingleThreadClientApplication"] + list(args))


def start_java_clientserver_gc_example_server(*args):
    subprocess.call([
        "java", "-Xmx512m", "-cp", PY4J_JAVA_PATH,
        "py4j.examples.SingleThreadClientGCApplication"] + list(args))


def start_clientserver_example_app_process(
        start_java_client=False, start_short_timeout=False,
        start_gc_test=False, auth_token=None):
    args = ()
    gw_params = GatewayParameters()
    if auth_token:
        args = ("--auth-token", auth_token)
        gw_params = GatewayParameters(auth_token=auth_token)

    # XXX DO NOT FORGET TO KILL THE PROCESS IF THE TEST DOES NOT SUCCEED
    if start_short_timeout:
        p = Process(target=start_short_timeout_clientserver_example_server,
                    args=args)
    elif start_java_client:
        p = Process(target=start_java_clientserver_example_server, args=args)
    elif start_gc_test:
        p = Process(target=start_java_clientserver_gc_example_server,
                    args=args)
    else:
        p = Process(target=start_clientserver_example_server, args=args)
    p.start()
    sleep()

    check_connection(gateway_parameters=gw_params)
    return p


@contextmanager
def clientserver_example_app_process(
        start_java_client=False, start_short_timeout=False,
        start_gc_test=False, join=True, auth_token=None):
    p = start_clientserver_example_app_process(
        start_java_client, start_short_timeout, start_gc_test, auth_token)
    try:
        yield p
    finally:
        if join:
            p.join()


def start_java_multi_client_server_app():
    subprocess.call([
        "java", "-Xmx512m", "-cp", PY4J_JAVA_PATH,
        "py4j.examples.MultiClientServer"])


def start_java_multi_client_server_app_process():
    # XXX DO NOT FORGET TO KILL THE PROCESS IF THE TEST DOES NOT SUCCEED
    p = Process(target=start_java_multi_client_server_app)
    p.start()
    sleep()
    # test both gateways...
    check_connection(
        gateway_parameters=GatewayParameters(port=DEFAULT_PORT))
    check_connection(
        gateway_parameters=GatewayParameters(port=DEFAULT_PORT + 2))
    return p


@contextmanager
def java_multi_client_server_app_process():
    p = start_java_multi_client_server_app_process()
    try:
        yield p
    finally:
        p.join()


class HelloObjects(object):
    def __init__(self):
        self.calls = 0

    def sendObject(self, o1, o2):
        # make a cycle between the objects,
        # this ensure the objects are not collected as
        # soon as the functions return and require
        # a full gc to be cleared
        o1.cycle = o2
        o2.cycle = o1
        self.calls += 1
        return ""

    class Java:
        implements = ["py4j.examples.IHelloObject"]


class GarbageCollectionTest(unittest.TestCase):

    def testSendObjects(self):
        """This test receives 1000 calls creating object cycles.
        Typically, the garbage collector will starts in the middle of
        Py4J connection code, which will create an error if the garbage
        collection is done on the same thread.
        """
        hello = HelloObjects()
        client_server = ClientServer(
            JavaParameters(), PythonParameters(), hello)

        with clientserver_example_app_process(
                start_gc_test=True, join=False) as p:
            p.join()
            client_server.shutdown()
            self.assertEquals(1000, hello.calls)

    def testCleanConnections(self):
        """This test intentionally create multiple connections in multiple
        threads so each connection is in a thread local of each thread.
        After that, it verifies that if the connection is cleaned and closed
        properly without a resource leak.
        """
        with clientserver_example_app_process():
            client_server = ClientServer(
                JavaParameters(), PythonParameters())
            connections = client_server._gateway_client.deque
            conditions = []

            def assert_connection():
                # Creates a connection.
                client_server.jvm.System.currentTimeMillis()
                # Should at least create one connection.
                conditions.append(0 < len(connections))

            threads = [
                threading.Thread(target=assert_connection),
                threading.Thread(target=assert_connection),
                threading.Thread(target=assert_connection),
            ]
            for t in threads:
                t.start()
            for t in threads:
                t.join()

            # Here we explicitly call garbage collection to clean
            # `ClientServerConnection`s by
            # `JavaClient.ThreadLocalConnectionCleaner`
            python_gc()

            # Should have zero connections left.
            self.assertEqual(len(client_server._gateway_client.deque), 0)
            self.assertTrue(all(connections))
            client_server.shutdown()


class RetryTest(unittest.TestCase):

    def testBadRetry(self):
        """Should not retry from Python to Java.
        Python calls a long Java method. The call goes through, but the
        response takes a long time to get back.

        If there is a bug, Python will fail on read and retry (sending the same
        call twice).

        If there is no bug, Python will fail on read and raise an Exception.
        """
        client_server = ClientServer(
            JavaParameters(read_timeout=0.250), PythonParameters())
        with clientserver_example_app_process():
            try:
                example = client_server.jvm.py4j.examples.ExampleClass()
                value = example.sleepFirstTimeOnly(500)
                self.fail(
                    "Should never retry once the first command went through."
                    "number of calls made: {0}".format(value))
            except Py4JError:
                self.assertTrue(True)
            finally:
                client_server.shutdown()

    def testGoodRetry(self):
        """Should retry from Python to Java.
        Python calls Java twice in a row, then waits, then calls again.

        Java fails when it does not receive calls quickly.

        If there is a bug, Python will fail on the third call because the Java
        connection was closed and it did not retry.

        If there is a bug, Python might not fail because Java did not close the
        connection on timeout. The connection used to call Java will be the
        same one for all calls (and an assertion will fail).

        If there is no bug, Python will call Java twice with the same
        connection. On the third call, the write will fail, and a new
        connection will be created.
        """
        client_server = ClientServer(
            JavaParameters(), PythonParameters())
        connections = client_server._gateway_client.deque

        with clientserver_example_app_process(False, True):
            try:
                # Call #1
                client_server.jvm.System.currentTimeMillis()
                str_connection = str(connections[0])

                # Call #2
                client_server.jvm.System.currentTimeMillis()
                self.assertEqual(1, len(connections))
                str_connection2 = str(connections[0])
                self.assertEqual(str_connection, str_connection2)

                sleep(0.5)
                client_server.jvm.System.currentTimeMillis()
                self.assertEqual(1, len(connections))
                str_connection3 = str(connections[0])
                # A new connection was automatically created.
                self.assertNotEqual(str_connection, str_connection3)

            except Py4JError:
                self.fail("Should retry automatically by default.")
            finally:
                client_server.shutdown()

    def testBadRetryFromJava(self):
        """Should not retry from Java to Python.
        Similar use case as testBadRetry, but from Java: Java calls a long
        Python operation.

        If there is a bug, Java will call Python, then read will fail, then it
        will call Python again.

        If there is no bug, Java will call Python, read will fail, then Java
        will raise an Exception that will be received as a Py4JError on the
        Python side.
        """

        client_server = ClientServer(
            JavaParameters(), PythonParameters())

        with clientserver_example_app_process(False, True):
            try:
                operator = WaitOperator(0.5)
                opExample = client_server.jvm.py4j.examples.OperatorExample()

                opExample.randomBinaryOperator(operator)
                self.fail(
                    "Should never retry once the first command went through."
                    " number of calls made: {0}".format(operator.callCount))
            except Py4JError:
                # XXX This occurs when WaitOperator tries to send a response to
                # the Java side (this is slightly different then the
                # GatewayServer equivalent where the Py4JError occurs on the
                # clientserver, but the Java side can still send back an
                # exception to the JavaGateway).
                self.assertTrue(True)
            finally:
                client_server.shutdown()

    def testGoodRetryFromJava(self):
        """Should retry from Java to Python.
        Similar use case as testGoodRetry, but from Java: Python calls Java,
        which calls Python back. Then Java waits for a while and calls Python
        again.

        Because Python Server has been waiting for too much time, the
        receiving socket has closed so the call from Java to Python will fail
        on send, and Java must retry by creating a new connection
        (ClientServerConnection).

        Because ClientServer reuses the same connection in each thread, we must
        launch a new thread on the Java side to correctly test the Python
        Server.
        """
        client_server = ClientServer(
            JavaParameters(), PythonParameters(read_timeout=0.250))
        with clientserver_example_app_process():
            try:
                operator = WaitOperator(0)
                opExample = client_server.jvm.py4j.examples.OperatorExample()
                opExample.launchOperator(operator, 500)
                sleep(0.1)
                str_connection = str(
                    list(client_server._callback_server.connections)[0])

                sleep(0.75)
                str_connection2 = str(
                    list(client_server._callback_server.connections)[0])
                self.assertNotEqual(str_connection, str_connection2)
            except Py4JJavaError:
                self.fail("Callbackserver did not retry.")
            finally:
                client_server.shutdown()


class IntegrationTest(unittest.TestCase):

    def testJavaClientPythonServer(self):
        self._testJavaClientPythonServer()

    def testJavaClientPythonServerWithAuth(self):
        self._testJavaClientPythonServer(auth_token="secret")

    def _testJavaClientPythonServer(self, auth_token=None):
        hello_state = HelloState()
        client_server = ClientServer(
            JavaParameters(auth_token=auth_token),
            PythonParameters(auth_token=auth_token), hello_state)

        with clientserver_example_app_process(True, auth_token=auth_token):
            client_server.shutdown(raise_exception=True)

        # Check that Java correctly called Python
        self.assertEqual(2, len(hello_state.calls))
        self.assertEqual((None, None), hello_state.calls[0])
        self.assertEqual((2, "Hello World"), hello_state.calls[1])

    def testBasicJVM(self):
        with clientserver_example_app_process():
            client_server = ClientServer(
                JavaParameters(), PythonParameters())
            ms = client_server.jvm.System.currentTimeMillis()
            self.assertGreater(ms, 0)
            client_server.shutdown()

    def testErrorInPy4J(self):
        with clientserver_example_app_process():
            client_server = ClientServer(
                JavaParameters(), PythonParameters())
            try:
                client_server.jvm.java.lang.Math.abs(
                    3000000000000000000000000000000000000)
                self.fail("Should not be able to convert overflowing long")
            except Py4JError:
                self.assertTrue(True)
            # Check that the connection is not broken (refs #265)
            val = client_server.jvm.java.lang.Math.abs(-4)
            self.assertEqual(4, val)
            client_server.shutdown()

    def testErrorInPythonCallback(self):
        with clientserver_example_app_process():
            client_server = ClientServer(
                JavaParameters(), PythonParameters(
                    propagate_java_exceptions=True))
            example = client_server.entry_point.getNewExample()

            try:
                example.callHello(IHelloFailingImpl(
                    ValueError('My interesting Python exception')))
                self.fail()
            except Py4JJavaError as e:
                self.assertTrue(is_instance_of(
                    client_server, e.java_exception,
                    'py4j.Py4JException'))
                self.assertIn('interesting Python exception', str(e))

            try:
                example.callHello(IHelloFailingImpl(
                    Py4JJavaError(
                        '',
                        client_server.jvm.java.lang.IllegalStateException(
                            'My IllegalStateException'))))
                self.fail()
            except Py4JJavaError as e:
                self.assertTrue(is_instance_of(
                    client_server, e.java_exception,
                    'java.lang.IllegalStateException'))

            client_server.shutdown()

    def testErrorInPythonCallbackNoPropagate(self):
        with clientserver_example_app_process():
            client_server = ClientServer(
                JavaParameters(), PythonParameters(
                    propagate_java_exceptions=False))
            example = client_server.entry_point.getNewExample()

            try:
                example.callHello(IHelloFailingImpl(
                    Py4JJavaError(
                        '',
                        client_server.jvm.java.lang.IllegalStateException(
                            'My IllegalStateException'))))
                self.fail()
            except Py4JJavaError as e:
                self.assertTrue(is_instance_of(
                    client_server, e.java_exception,
                    'py4j.Py4JException'))
                self.assertIn('My IllegalStateException', str(e))

            client_server.shutdown()

    def testStream(self):
        with clientserver_example_app_process():
            client_server = ClientServer(
                JavaParameters(), PythonParameters())
            e = client_server.entry_point.getNewExample()

            # not binary - just get the Java object
            v1 = e.getStream()
            self.assertTrue(
                is_instance_of(
                    client_server, v1,
                    "java.nio.channels.ReadableByteChannel"))

            # pull it as a binary stream
            with e.getStream.stream() as conn:
                self.assertTrue(isinstance(conn, GatewayConnectionGuard))
                expected =\
                    u"Lorem ipsum dolor sit amet, consectetur adipiscing elit."
                self.assertEqual(
                    expected, smart_decode(conn.read(len(expected))))
            client_server.shutdown()

    def testRecursionWithAutoGC(self):
        with clientserver_example_app_process():
            client_server = ClientServer(
                JavaParameters(auto_gc=True), PythonParameters(auto_gc=True))
            pingpong = client_server.jvm.py4j.examples.PingPong()
            ping = PythonPing()
            self.assertEqual(2, pingpong.start(ping))

            pingpong = client_server.jvm.py4j.examples.PingPong(True)

            try:
                pingpong.start(ping)
                self.fail()
            except Py4JJavaError:
                # TODO Make sure error are recursively propagated
                # Also a problem with old threading model.
                self.assertTrue(True)

            ping = PythonPing(True)
            pingpong = client_server.jvm.py4j.examples.PingPong(False)

            try:
                pingpong.start(ping)
                self.fail()
            except Py4JJavaError:
                # TODO Make sure error are recursively propagated
                # Also a problem with old threading model.
                self.assertTrue(True)

            client_server.shutdown()

    def testJavaGC(self):
        # This will only work with some JVM.
        with clientserver_example_app_process():
            client_server = ClientServer(
                JavaParameters(), PythonParameters())
            example = client_server.entry_point.getNewExample()
            impl = IHelloImpl()
            self.assertEqual("This is Hello!", example.callHello(impl))
            self.assertEqual(
                "This is Hello;\n10MyMy!\n;",
                example.callHello2(impl))
            self.assertEqual(2, len(client_server.gateway_property.pool))

            # Make sure that finalizers do not block by calling the Java
            # finalizer again
            impl2 = IHelloImpl()
            self.assertEqual("This is Hello!", example.callHello(impl2))
            self.assertEqual(3, len(client_server.gateway_property.pool))

            # The two PythonProxies should be evicted on the Java side
            # Java should tell python to release the references.
            client_server.jvm.java.lang.System.gc()

            # Leave time for sotimeout
            sleep(3)
            self.assertLess(len(client_server.gateway_property.pool), 2)
            client_server.shutdown()

    def testPythonGC(self):
        def internal_function(client_server):
            example = client_server.entry_point.getNewExample()
            in_middle = len(
                client_server.java_gateway_server.getGateway().getBindings())
            self.assertEqual(1, example.method1())

            # After this method is executed, Python no longer need
            # a reference to example and it should tell Java to release
            # the reference too
            return in_middle

        with clientserver_example_app_process():
            # This will only work with some JVM.
            client_server = ClientServer(
                JavaParameters(), PythonParameters())
            before = len(
                client_server.java_gateway_server.getGateway().getBindings())
            in_middle = internal_function(client_server)

            # Force Python to run garbage collection in case it did
            # not run yet.
            sleep()
            gc.collect()
            # Long sleep to ensure that the finalizer worker received
            # everything
            sleep(2)

            after = len(
                client_server.java_gateway_server.getGateway().getBindings())

            # Number of references on the Java side should be the same before
            # and after
            self.assertEqual(before, after)

            # Number of references when we created a JavaObject should be
            # higher than at the beginning.
            self.assertGreater(in_middle, before)
            client_server.shutdown()

    def testMultiClientServerWithSharedJavaThread(self):
        with java_multi_client_server_app_process():
            client_server0 = ClientServer(
                JavaParameters(), PythonParameters())
            client_server1 = ClientServer(
                JavaParameters(port=DEFAULT_PORT + 2),
                PythonParameters(port=DEFAULT_PYTHON_PROXY_PORT + 2))

            entry0 = client_server0.entry_point
            entry1 = client_server1.entry_point

            # set up the ability for Java to get the Python thread ids
            threadIdGetter0 = PythonGetThreadId(client_server0)
            threadIdGetter1 = PythonGetThreadId(client_server1)
            entry0.setPythonThreadIdGetter(threadIdGetter0)
            entry1.setPythonThreadIdGetter(threadIdGetter1)
            thisThreadId = threading.current_thread().ident

            # ## Preconditions

            # Make sure we are talking to two different Entry points on
            # Java side
            self.assertEqual(0, entry0.getEntryId())
            self.assertEqual(1, entry1.getEntryId())

            # ## 1 Hop to Shared Java Thread

            # Check that the shared Java thread is the same thread
            # for both ClientServers
            sharedJavaThreadId = entry0.getSharedJavaThreadId()
            self.assertEqual(sharedJavaThreadId,
                             entry1.getSharedJavaThreadId())
            # And that it is distinct from either corresponding to
            # this Python thread
            self.assertNotEqual(sharedJavaThreadId, entry0.getJavaThreadId())
            self.assertNotEqual(sharedJavaThreadId, entry1.getJavaThreadId())

            # ## 2 Hops via Shared Java Thread to Python Thread

            # Check that the shared thread ends up as different threads
            # in the Python side. This part may not be obvious as the
            # top-level idea seems to be for Python thread to be pinned
            # to Java thread. Consider that this case is a simplification
            # of the real case. In the real case there are two
            # ClientServers running in same JVM, but in Python side each
            # ClientServer is in its own process. In that case it makes
            # it obvious that the shared Java thread should indeed
            # end up in different Python threads.
            sharedPythonThreadId0 = entry0.getSharedPythonThreadId()
            sharedPythonThreadId1 = entry1.getSharedPythonThreadId()
            # three way assert to make sure all three python
            # threads are distinct
            self.assertNotEqual(thisThreadId, sharedPythonThreadId0)
            self.assertNotEqual(thisThreadId, sharedPythonThreadId1)
            self.assertNotEqual(sharedPythonThreadId0, sharedPythonThreadId1)
            # Check that the Python thread id does not change between
            # invocations
            self.assertEquals(sharedPythonThreadId0,
                              entry0.getSharedPythonThreadId())
            self.assertEquals(sharedPythonThreadId1,
                              entry1.getSharedPythonThreadId())

            # ## 3 Hops to Shared Java Thread

            # Check that the thread above after 2 hops calls back
            # into the Java shared thread
            self.assertEqual(sharedJavaThreadId,
                             entry0.getSharedViaPythonJavaThreadId())
            self.assertEqual(sharedJavaThreadId,
                             entry1.getSharedViaPythonJavaThreadId())

            client_server0.shutdown()
            client_server1.shutdown()

    def testMultiClientServer(self):
        with java_multi_client_server_app_process():
            client_server0 = ClientServer(
                JavaParameters(), PythonParameters())
            client_server1 = ClientServer(
                JavaParameters(port=DEFAULT_PORT + 2),
                PythonParameters(port=DEFAULT_PYTHON_PROXY_PORT + 2))

            entry0 = client_server0.entry_point
            entry1 = client_server1.entry_point

            # set up the ability for Java to get the Python thread ids
            threadIdGetter0 = PythonGetThreadId(client_server0)
            threadIdGetter1 = PythonGetThreadId(client_server1)
            entry0.setPythonThreadIdGetter(threadIdGetter0)
            entry1.setPythonThreadIdGetter(threadIdGetter1)
            thisThreadId = threading.current_thread().ident

            # Make sure we are talking to two different Entry points on
            # Java side
            self.assertEqual(0, entry0.getEntryId())
            self.assertEqual(1, entry1.getEntryId())

            # ## 0 Hops to Thread ID

            # Check that the two thread getters get the same thread
            self.assertEquals(thisThreadId,
                              int(threadIdGetter0.getThreadId()))
            self.assertEquals(thisThreadId,
                              int(threadIdGetter1.getThreadId()))

            # ## 1 Hop to Thread ID

            # Check that ClientServers on Java side are on different threads
            javaThreadId0 = entry0.getJavaThreadId()
            javaThreadId1 = entry1.getJavaThreadId()
            self.assertNotEqual(javaThreadId0, javaThreadId1)
            # Check that ClientServers on Java side stay on same thread
            # on subsequent calls to them
            self.assertEqual(javaThreadId0, entry0.getJavaThreadId())
            self.assertEqual(javaThreadId1, entry1.getJavaThreadId())
            # Check alternate way of getting thread ids and that they match
            self.assertEqual(javaThreadId0,
                             int(threadIdGetter0.getJavaThreadId()))
            self.assertEqual(javaThreadId1,
                             int(threadIdGetter1.getJavaThreadId()))

            # ## 2 Hops to Thread ID

            # Check that round trips from Python to Java and Python
            # end up back on this thread, regardless of which
            # client server we use for the round trip
            self.assertEqual(thisThreadId,
                             entry0.getPythonThreadId())
            self.assertEqual(thisThreadId,
                             entry1.getPythonThreadId())

            # ## 3 Hops to Thread ID

            # Check that round trips from Python to Java to Python to Java
            # end up on the same thread as 1 hop
            self.assertEqual(javaThreadId0,
                             entry0.getViaPythonJavaThreadId())
            self.assertEqual(javaThreadId1,
                             entry1.getViaPythonJavaThreadId())

            client_server0.shutdown()
            client_server1.shutdown()


class PythonGetThreadId(object):
    def __init__(self, gateway):
        self.gateway = gateway

    def getThreadId(self):
        # Return as a string because python3 doesn't have a long type anymore
        # and as a result when using python3 and the value is 32-bits this
        # arrives at Java as an Integer which fails to be converted to a Long
        return str(threading.current_thread().ident)

    def getJavaThreadId(self):
        # see comment above for why a string.
        return str(self.gateway.jvm.java.lang.Thread.currentThread().getId())

    class Java:
        implements = ["py4j.examples.MultiClientServerGetThreadId"]
