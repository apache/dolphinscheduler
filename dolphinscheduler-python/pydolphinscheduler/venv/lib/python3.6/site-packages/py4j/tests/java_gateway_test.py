# -*- coding: UTF-8 -*-
"""
Created on Dec 10, 2009

@author: barthelemy
"""
from __future__ import unicode_literals, absolute_import

from collections import deque
from contextlib import contextmanager
from decimal import Decimal
import gc
import math
from multiprocessing import Process
import os
import sys
from socket import AF_INET, SOCK_STREAM, socket
import subprocess
import tempfile
from threading import Thread
import time
from traceback import print_exc
import unittest

from py4j.compat import (
    range, isbytearray, ispython3bytestr, bytearray2, long,
    Queue)
from py4j.finalizer import ThreadSafeFinalizer
from py4j.java_gateway import (
    JavaGateway, JavaMember, get_field, get_method,
    GatewayClient, set_field, java_import, JavaObject, is_instance_of,
    GatewayParameters, CallbackServerParameters, quiet_close, DEFAULT_PORT,
    set_default_callback_accept_timeout, GatewayConnectionGuard,
    get_java_class)
from py4j.protocol import (
    Py4JError, Py4JJavaError, Py4JNetworkError, decode_bytearray,
    encode_bytearray, escape_new_line, unescape_new_line, smart_decode)


SERVER_PORT = 25333
TEST_PORT = 25332
PY4J_PREFIX_PATH = os.path.dirname(os.path.realpath(__file__))
PY4J_JAVA_PATHS = [
    os.path.join(PY4J_PREFIX_PATH,
                 "../../../../py4j-java/build/classes/main"),  # gradle
    os.path.join(PY4J_PREFIX_PATH,
                 "../../../../py4j-java/build/classes/test"),  # gradle
    os.path.join(PY4J_PREFIX_PATH,
                 "../../../../py4j-java/build/classes/java/main"),  # gradle 4
    os.path.join(PY4J_PREFIX_PATH,
                 "../../../../py4j-java/build/classes/java/test"),  # gradle 4
    os.path.join(PY4J_PREFIX_PATH,
                 "../../../../py4j-java/build/resources/main"),  # gradle
    os.path.join(PY4J_PREFIX_PATH,
                 "../../../../py4j-java/build/resources/test"),  # gradle
    os.path.join(PY4J_PREFIX_PATH,
                 "../../../../py4j-java/target/classes/"),  # maven
    os.path.join(PY4J_PREFIX_PATH,
                 "../../../../py4j-java/target/test-classes/"),  # maven
    os.path.join(PY4J_PREFIX_PATH,
                 "../../../../py4j-java/bin"),  # ant
]
PY4J_JAVA_PATH = os.pathsep.join(PY4J_JAVA_PATHS)


set_default_callback_accept_timeout(0.125)


def stderr_is_polluted(line):
    """May occur depending on the environment in which py4j is executed.

    The stderr ccanot be relied on when it occurs.
    """
    return "Picked up _JAVA_OPTIONS" in line


def sleep(sleep_time=0.250):
    """Default sleep time to enable the OS to reuse address and port.
    """
    time.sleep(sleep_time)


def start_echo_server():
    subprocess.call(["java", "-cp", PY4J_JAVA_PATH, "py4j.EchoServer"])


def start_echo_server_process():
    # XXX DO NOT FORGET TO KILL THE PROCESS IF THE TEST DOES NOT SUCCEED
    sleep()
    p = Process(target=start_echo_server)
    p.start()
    sleep(1.5)
    return p


def start_example_server():
    subprocess.call([
        "java", "-Xmx512m", "-cp", PY4J_JAVA_PATH,
        "py4j.examples.ExampleApplication"])


def start_short_timeout_example_server():
    subprocess.call([
        "java", "-Xmx512m", "-cp", PY4J_JAVA_PATH,
        "py4j.examples.ExampleApplication$ExampleShortTimeoutApplication"])


def start_ipv6_example_server():
    subprocess.call([
        "java", "-Xmx512m", "-cp", PY4J_JAVA_PATH,
        "py4j.examples.ExampleApplication$ExampleIPv6Application"])


def start_example_app_process():
    # XXX DO NOT FORGET TO KILL THE PROCESS IF THE TEST DOES NOT SUCCEED
    p = Process(target=start_example_server)
    p.start()
    sleep()
    check_connection()
    return p


def start_short_timeout_app_process():
    # XXX DO NOT FORGET TO KILL THE PROCESS IF THE TEST DOES NOT SUCCEED
    p = Process(target=start_short_timeout_example_server)
    p.start()
    sleep()
    check_connection()
    return p


def start_ipv6_app_process():
    # XXX DO NOT FORGET TO KILL THE PROCESS IF THE TEST DOES NOT SUCCEED
    p = Process(target=start_ipv6_example_server)
    p.start()
    # Sleep twice because we do not check connections.
    sleep()
    sleep()
    return p


def check_connection(gateway_parameters=None):
    test_gateway = JavaGateway(gateway_parameters=gateway_parameters)
    try:
        # Call a dummy method just to make sure we can connect to the JVM
        test_gateway.jvm.System.currentTimeMillis()
    except Py4JNetworkError:
        # We could not connect. Let"s wait a long time.
        # If it fails after that, there is a bug with our code!
        sleep(2)
    finally:
        test_gateway.close()


def get_socket():
    testSocket = socket(AF_INET, SOCK_STREAM)
    testSocket.connect(("127.0.0.1", TEST_PORT))
    return testSocket


def safe_shutdown(instance):
    if hasattr(instance, 'gateway'):
        try:
            instance.gateway.shutdown()
        except Exception:
            print_exc()


@contextmanager
def gateway(*args, **kwargs):
    g = JavaGateway(
        gateway_parameters=GatewayParameters(
            *args, auto_convert=True, **kwargs))
    time = g.jvm.System.currentTimeMillis()
    try:
        yield g
        # Call a dummy method to make sure we haven't corrupted the streams
        assert time <= g.jvm.System.currentTimeMillis()
    finally:
        g.shutdown()


@contextmanager
def example_app_process():
    p = start_example_app_process()
    try:
        yield p
    finally:
        p.join()


class TestConnection(object):
    """Connection that does nothing. Useful for testing."""

    counter = -1

    def __init__(self, return_message="yro"):
        self.address = "127.0.0.1"
        self.port = 1234
        self.return_message = return_message
        self.is_connected = True

    def start(self):
        pass

    def stop(self):
        pass

    def send_command(self, command):
        TestConnection.counter += 1
        if not command.startswith("m\nd\n"):
            self.last_message = command
        return self.return_message + str(TestConnection.counter)


class ProtocolTest(unittest.TestCase):
    def tearDown(self):
        # Safety check in case there was an exception...
        safe_shutdown(self)

    def testEscape(self):
        self.assertEqual("Hello\t\rWorld\n\\", unescape_new_line(
            escape_new_line("Hello\t\rWorld\n\\")))
        self.assertEqual("Hello\t\rWorld\n\\", unescape_new_line(
            escape_new_line("Hello\t\rWorld\n\\")))

    def testProtocolSend(self):
        testConnection = TestConnection()
        self.gateway = JavaGateway()

        # Replace gateway client by test connection
        self.gateway.set_gateway_client(testConnection)

        e = self.gateway.getExample()
        self.assertEqual("c\nt\ngetExample\ne\n", testConnection.last_message)
        e.method1(1, True, "Hello\nWorld", e, None, 1.5)
        self.assertEqual(
            "c\no0\nmethod1\ni1\nbTrue\nsHello\\nWorld\nro0\nn\nd1.5\ne\n",
            testConnection.last_message)
        del(e)

    def testProtocolReceive(self):
        p = start_echo_server_process()
        try:
            testSocket = get_socket()
            testSocket.sendall("!yo\n".encode("utf-8"))
            testSocket.sendall("!yro0\n".encode("utf-8"))
            testSocket.sendall("!yo\n".encode("utf-8"))
            testSocket.sendall("!ysHello World\n".encode("utf-8"))
            # No extra echange (method3) because it is already cached.
            testSocket.sendall("!yi123\n".encode("utf-8"))
            testSocket.sendall("!yd1.25\n".encode("utf-8"))
            testSocket.sendall("!yo\n".encode("utf-8"))
            testSocket.sendall("!yn\n".encode("utf-8"))
            testSocket.sendall("!yo\n".encode("utf-8"))
            testSocket.sendall("!ybTrue\n".encode("utf-8"))
            testSocket.sendall("!yo\n".encode("utf-8"))
            testSocket.sendall("!yL123\n".encode("utf-8"))
            testSocket.sendall("!ydinf\n".encode("utf-8"))
            testSocket.close()
            sleep()

            self.gateway = JavaGateway(
                gateway_parameters=GatewayParameters(auto_field=True))
            ex = self.gateway.getNewExample()
            self.assertEqual("Hello World", ex.method3(1, True))
            self.assertEqual(123, ex.method3())
            self.assertAlmostEqual(1.25, ex.method3())
            self.assertTrue(ex.method2() is None)
            self.assertTrue(ex.method4())
            self.assertEqual(long(123), ex.method8())
            self.assertEqual(float("inf"), ex.method8())
            self.gateway.shutdown()

        except Exception:
            print_exc()
            self.fail("Problem occurred")
        p.join()


class IntegrationTest(unittest.TestCase):
    def setUp(self):
        self.p = start_echo_server_process()
        # This is to ensure that the server is started before connecting to it!

    def tearDown(self):
        # Safety check in case there was an exception...
        safe_shutdown(self)
        self.p.join()

    def testIntegration(self):
        try:
            testSocket = get_socket()
            testSocket.sendall("!yo\n".encode("utf-8"))
            testSocket.sendall("!yro0\n".encode("utf-8"))
            testSocket.sendall("!yo\n".encode("utf-8"))
            testSocket.sendall("!ysHello World\n".encode("utf-8"))
            testSocket.sendall("!yro1\n".encode("utf-8"))
            testSocket.sendall("!yo\n".encode("utf-8"))
            testSocket.sendall("!ysHello World2\n".encode("utf-8"))
            testSocket.close()
            sleep()

            self.gateway = JavaGateway(
                gateway_parameters=GatewayParameters(auto_field=True))
            ex = self.gateway.getNewExample()
            response = ex.method3(1, True)
            self.assertEqual("Hello World", response)
            ex2 = self.gateway.entry_point.getNewExample()
            response = ex2.method3(1, True)
            self.assertEqual("Hello World2", response)
            self.gateway.shutdown()
        except Exception:
            self.fail("Problem occurred")

    def testException(self):
        try:
            testSocket = get_socket()
            testSocket.sendall("!yo\n".encode("utf-8"))
            testSocket.sendall("!yro0\n".encode("utf-8"))
            testSocket.sendall("!yo\n".encode("utf-8"))
            testSocket.sendall(b"!x\n")
            testSocket.close()
            sleep()

            self.gateway = JavaGateway(
                gateway_parameters=GatewayParameters(auto_field=True))
            ex = self.gateway.getNewExample()

            self.assertRaises(Py4JError, lambda: ex.method3(1, True))
            self.gateway.shutdown()
        except Exception:
            self.fail("Problem occurred")


class CloseTest(unittest.TestCase):
    def testNoCallbackServer(self):
        # Test that the program can continue to move on and that no close
        # is required.
        JavaGateway()
        self.assertTrue(True)

    def testCallbackServer(self):
        # A close is required to stop the thread.
        gateway = JavaGateway(
            callback_server_parameters=CallbackServerParameters())
        gateway.close()
        self.assertTrue(True)
        sleep(2)


class MethodTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()
        # This is to ensure that the server is started before connecting to it!
        self.gateway = JavaGateway()

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()

    def testNoneArg(self):
        ex = self.gateway.getNewExample()
        try:
            ex.method2(None)
            ex2 = ex.method4(None)
            self.assertEquals(ex2.getField1(), 3)
            self.assertEquals(2, ex.method7(None))
        except Exception:
            print_exc()
            self.fail()

    def testMagicMethods(self):
        ex = self.gateway.getNewExample()
        self.assertRaises(AttributeError, lambda: ex.__add__("asd"))

    def testUnicode(self):
        sb = self.gateway.jvm.java.lang.StringBuffer()
        sb.append("\r\n\tHello\r\n\t")
        self.assertEqual("\r\n\tHello\r\n\t", sb.toString())

    def testEscape(self):
        sb = self.gateway.jvm.java.lang.StringBuffer()
        sb.append("\r\n\tHello\r\n\t")
        self.assertEqual("\r\n\tHello\r\n\t", sb.toString())


class FieldTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()

    def testAutoField(self):
        self.gateway = JavaGateway(
            gateway_parameters=GatewayParameters(auto_field=True))
        ex = self.gateway.getNewExample()
        self.assertEqual(ex.field10, 10)
        self.assertEqual(ex.field11, long(11))
        sb = ex.field20
        sb.append("Hello")
        self.assertEqual("Hello", sb.toString())
        self.assertTrue(ex.field21 is None)

    def testAutoFieldDeprecated(self):
        self.gateway = JavaGateway(auto_field=True)
        ex = self.gateway.getNewExample()
        self.assertEqual(ex.field10, 10)

    def testNoField(self):
        self.gateway = JavaGateway(
            gateway_parameters=GatewayParameters(auto_field=True))
        ex = self.gateway.getNewExample()
        member = ex.field50
        self.assertTrue(isinstance(member, JavaMember))

    def testNoAutoField(self):
        self.gateway = JavaGateway(
            gateway_parameters=GatewayParameters(auto_field=False))
        ex = self.gateway.getNewExample()
        self.assertTrue(isinstance(ex.field10, JavaMember))
        self.assertTrue(isinstance(ex.field50, JavaMember))
        self.assertEqual(10, get_field(ex, "field10"))

        # This field does not exist
        self.assertRaises(Exception, get_field, ex, "field50")

        # With auto field = True
        ex._auto_field = True
        sb = ex.field20
        sb.append("Hello")
        self.assertEqual("Hello", sb.toString())

    def testSetField(self):
        self.gateway = JavaGateway(
            gateway_parameters=GatewayParameters(auto_field=False))
        ex = self.gateway.getNewExample()

        set_field(ex, "field10", 2334)
        self.assertEquals(get_field(ex, "field10"), 2334)

        sb = self.gateway.jvm.java.lang.StringBuffer("Hello World!")
        set_field(ex, "field21", sb)
        self.assertEquals(get_field(ex, "field21").toString(), "Hello World!")

        self.assertRaises(Exception, set_field, ex, "field1", 123)

    def testGetMethod(self):
        # This is necessary if a field hides a method...
        self.gateway = JavaGateway()
        ex = self.gateway.getNewExample()
        self.assertEqual(1, get_method(ex, "method1")())


class DeprecatedTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()

    def test_gateway_client(self):
        gateway_client = GatewayClient(port=DEFAULT_PORT)
        self.gateway = JavaGateway(gateway_client=gateway_client)

        i = self.gateway.jvm.System.currentTimeMillis()
        self.assertGreater(i, 0)

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()


class UtilityTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()
        self.gateway = JavaGateway()

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()

    def testGetJavaClass(self):
        ArrayList = self.gateway.jvm.java.util.ArrayList
        clazz1 = ArrayList._java_lang_class
        clazz2 = get_java_class(ArrayList)

        self.assertEqual("java.util.ArrayList", clazz1.getName())
        self.assertEqual("java.util.ArrayList", clazz2.getName())
        self.assertEqual("java.lang.Class", clazz1.getClass().getName())
        self.assertEqual("java.lang.Class", clazz2.getClass().getName())

    def testIsInstance(self):
        a_list = self.gateway.jvm.java.util.ArrayList()
        a_map = self.gateway.jvm.java.util.HashMap()

        # FQN
        self.assertTrue(is_instance_of(self.gateway, a_list, "java.util.List"))
        self.assertFalse(
            is_instance_of(
                self.gateway, a_list, "java.lang.String"))

        # JavaClass
        self.assertTrue(
            is_instance_of(
                self.gateway, a_list, self.gateway.jvm.java.util.List))
        self.assertFalse(
            is_instance_of(
                self.gateway, a_list, self.gateway.jvm.java.lang.String))

        # JavaObject
        self.assertTrue(is_instance_of(self.gateway, a_list, a_list))
        self.assertFalse(is_instance_of(self.gateway, a_list, a_map))


class MemoryManagementTest(unittest.TestCase):
    def setUp(self):
        ThreadSafeFinalizer.clear_finalizers(True)
        self.p = start_example_app_process()

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()
        gc.collect()

    def testNoAttach(self):
        self.gateway = JavaGateway()
        gateway2 = JavaGateway()
        sb = self.gateway.jvm.java.lang.StringBuffer()
        sb.append("Hello World")
        self.gateway.shutdown()

        self.assertRaises(Exception, lambda: sb.append("Python"))

        self.assertRaises(
            Exception, lambda: gateway2.jvm.java.lang.StringBuffer())

    def testDetach(self):
        self.gateway = JavaGateway()
        gc.collect()
        finalizers_size_start = len(ThreadSafeFinalizer.finalizers)

        sb = self.gateway.jvm.java.lang.StringBuffer()
        sb.append("Hello World")
        self.gateway.detach(sb)
        sb2 = self.gateway.jvm.java.lang.StringBuffer()
        sb2.append("Hello World")
        sb2._detach()
        gc.collect()

        self.assertEqual(
            len(ThreadSafeFinalizer.finalizers) - finalizers_size_start, 0)
        self.gateway.shutdown()

    def testGCCollect(self):
        self.gateway = JavaGateway()
        gc.collect()
        finalizers_size_start = len(ThreadSafeFinalizer.finalizers)

        def internal():
            sb = self.gateway.jvm.java.lang.StringBuffer()
            sb.append("Hello World")
            sb2 = self.gateway.jvm.java.lang.StringBuffer()
            sb2.append("Hello World")
            finalizers_size_middle = len(ThreadSafeFinalizer.finalizers)
            return finalizers_size_middle
        finalizers_size_middle = internal()
        gc.collect()

        # Before collection: two objects created + two returned objects (append
        # returns a stringbuffer reference for easy chaining).
        self.assertEqual(finalizers_size_middle, 4)

        # Assert after collection
        self.assertEqual(
            len(ThreadSafeFinalizer.finalizers) - finalizers_size_start, 0)

        self.gateway.shutdown()

    def testGCCollectNoMemoryManagement(self):
        self.gateway = JavaGateway(
            gateway_parameters=GatewayParameters(
                enable_memory_management=False))
        gc.collect()
        # Should have nothing in the finalizers
        self.assertEqual(len(ThreadSafeFinalizer.finalizers), 0)

        def internal():
            sb = self.gateway.jvm.java.lang.StringBuffer()
            sb.append("Hello World")
            sb2 = self.gateway.jvm.java.lang.StringBuffer()
            sb2.append("Hello World")
            finalizers_size_middle = len(ThreadSafeFinalizer.finalizers)
            return finalizers_size_middle
        finalizers_size_middle = internal()
        gc.collect()

        # Before collection: two objects created + two returned objects (append
        # returns a stringbuffer reference for easy chaining).
        self.assertEqual(finalizers_size_middle, 0)

        # Assert after collection
        self.assertEqual(len(ThreadSafeFinalizer.finalizers), 0)

        self.gateway.shutdown()


class TypeConversionTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()
        self.gateway = JavaGateway()

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()

    def testLongInt(self):
        ex = self.gateway.getNewExample()
        self.assertEqual(1, ex.method7(1234))
        self.assertEqual(4, ex.method7(2147483648))
        self.assertEqual(4, ex.method7(-2147483649))
        self.assertEqual(4, ex.method7(long(2147483648)))
        self.assertEqual(long(4), ex.method8(3))
        self.assertEqual(4, ex.method8(3))
        self.assertEqual(long(4), ex.method8(long(3)))
        self.assertEqual(long(4), ex.method9(long(3)))
        try:
            ex.method8(3000000000000000000000000000000000000)
            self.fail("Should not be able to convert overflowing long")
        except Py4JError:
            self.assertTrue(True)
        # Check that the connection is not broken (refs #265)
        self.assertEqual(4, ex.method8(3))

    def testBigDecimal(self):
        ex = self.gateway.getNewExample()
        self.assertEqual(Decimal("2147483.647"), ex.method10(2147483647, 3))
        self.assertEqual(Decimal("-13.456"), ex.method10(Decimal("-14.456")))

    def testFloatConversion(self):
        java_inf = self.gateway.jvm.java.lang.Double.parseDouble("Infinity")
        self.assertEqual(float("inf"), java_inf)
        java_inf = self.gateway.jvm.java.lang.Double.parseDouble("+Infinity")
        self.assertEqual(float("inf"), java_inf)
        java_neg_inf = self.gateway.jvm.java.lang.Double.parseDouble(
            "-Infinity")
        self.assertEqual(float("-inf"), java_neg_inf)
        java_nan = self.gateway.jvm.java.lang.Double.parseDouble("NaN")
        self.assertTrue(math.isnan(java_nan))

        python_double = 17.133574204226083
        java_float = self.gateway.jvm.java.lang.Double(python_double)
        self.assertAlmostEqual(python_double, java_float, 15)

    def testUnboxingInt(self):
        ex = self.gateway.getNewExample()
        self.assertEqual(4, ex.getInteger(4))


class UnicodeTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()
        self.gateway = JavaGateway()

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()

    # def testUtfMethod(self):
        # ex = self.gateway.jvm.py4j.examples.UTFExample()

        # Only works for Python 3
        # self.assertEqual(2, ex.strangeMéthod())

    def testUnicodeString(self):
        # NOTE: this is unicode because of import future unicode literal...
        ex = self.gateway.jvm.py4j.examples.UTFExample()
        s1 = "allo"
        s2 = "alloé"
        array1 = ex.getUtfValue(s1)
        array2 = ex.getUtfValue(s2)
        self.assertEqual(len(s1), len(array1))
        self.assertEqual(len(s2), len(array2))
        self.assertEqual(ord(s1[0]), array1[0])
        self.assertEqual(ord(s2[4]), array2[4])


class StreamTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()
        self.gateway = JavaGateway()

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()

    def testBinarySuccess(self):
        e = self.gateway.getNewExample()

        # not binary - just get the Java object
        v1 = e.getStream()
        self.assertTrue(
            is_instance_of(
                self.gateway, v1, "java.nio.channels.ReadableByteChannel"))

        # pull it as a binary stream
        with e.getStream.stream() as conn:
            self.assertTrue(isinstance(conn, GatewayConnectionGuard))
            expected =\
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
            self.assertEqual(expected, smart_decode(conn.read(len(expected))))

    def testBinaryFailure(self):
        e = self.gateway.getNewExample()
        self.assertRaises(Py4JJavaError, lambda: e.getBrokenStream())
        self.assertRaises(Py4JJavaError, lambda: e.getBrokenStream.stream())

    def testNotAStream(self):
        e = self.gateway.getNewExample()
        self.assertEqual(1, e.method1())
        self.assertRaises(Py4JError, lambda: e.method1.stream())


class ByteTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()
        self.gateway = JavaGateway()

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()

    def testJavaByteConversion(self):
        ex = self.gateway.jvm.py4j.examples.UTFExample()
        ba = bytearray([0, 1, 127, 128, 255, 216, 1, 220])
        self.assertEqual(0, ex.getPositiveByteValue(ba[0]))
        self.assertEqual(1, ex.getPositiveByteValue(ba[1]))
        self.assertEqual(127, ex.getPositiveByteValue(ba[2]))
        self.assertEqual(128, ex.getPositiveByteValue(ba[3]))
        self.assertEqual(255, ex.getPositiveByteValue(ba[4]))
        self.assertEqual(216, ex.getPositiveByteValue(ba[5]))
        self.assertEqual(0, ex.getJavaByteValue(ba[0]))
        self.assertEqual(1, ex.getJavaByteValue(ba[1]))
        self.assertEqual(127, ex.getJavaByteValue(ba[2]))
        self.assertEqual(-128, ex.getJavaByteValue(ba[3]))
        self.assertEqual(-1, ex.getJavaByteValue(ba[4]))

    def testProtocolConversion(self):
        # b1 = tobytestr("abc\n")
        b2 = bytearray([1, 2, 3, 255, 0, 128, 127])

        # encoded1 = encode_bytearray(b1)
        encoded2 = encode_bytearray(b2)

        # self.assertEqual(b1, decode_bytearray(encoded1))
        self.assertEqual(b2, decode_bytearray(encoded2))

    def testBytesType(self):
        ex = self.gateway.jvm.py4j.examples.UTFExample()
        int_list = [0, 1, 10, 127, 128, 255]
        ba1 = bytearray(int_list)
        # Same for Python2, bytes for Python 3
        ba2 = bytearray2(int_list)
        a1 = ex.getBytesValue(ba1)
        a2 = ex.getBytesValue(ba2)
        for i1, i2 in zip(a1, int_list):
            self.assertEqual(i1, i2)

        for i1, i2 in zip(a2, int_list):
            self.assertEqual(i1, i2)

    def testBytesType2(self):
        ex = self.gateway.jvm.py4j.examples.UTFExample()
        int_list = [0, 1, 10, 127, 255, 128]
        a1 = ex.getBytesValue()
        # Python 2: bytearray (because str is too easy to confuse with normal
        # strings)
        # Python 3: bytes (because bytes is closer to the byte[] representation
        # in Java)
        self.assertTrue(isbytearray(a1) or ispython3bytestr(a1))
        for i1, i2 in zip(a1, int_list):
            self.assertEqual(i1, i2)

    def testLargeByteArray(self):
        # Regression test for #109, an error when passing large byte arrays.
        self.gateway.jvm.java.nio.ByteBuffer.wrap(bytearray(range(255)))


class ExceptionTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()
        self.gateway = JavaGateway()

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()

    def testJavaError(self):
        try:
            self.gateway.jvm.Integer.valueOf("allo")
        except Py4JJavaError as e:
            self.assertEqual(
                "java.lang.NumberFormatException",
                e.java_exception.getClass().getName())
        except Exception:
            self.fail()

    def testJavaConstructorError(self):
        try:
            self.gateway.jvm.Integer("allo")
        except Py4JJavaError as e:
            self.assertEqual(
                "java.lang.NumberFormatException",
                e.java_exception.getClass().getName())
        except Exception:
            self.fail()

    def doError(self):
        id = ""
        try:
            self.gateway.jvm.Integer.valueOf("allo")
        except Py4JJavaError as e:
            id = e.java_exception._target_id
        return id

    def testJavaErrorGC(self):
        id = self.doError()
        java_object = JavaObject(id, self.gateway._gateway_client)
        try:
            # Should fail because it should have been garbage collected...
            java_object.getCause()
            self.fail()
        except Py4JError:
            self.assertTrue(True)

    def testReflectionError(self):
        try:
            self.gateway.jvm.Integer.valueOf2("allo")
        except Py4JJavaError:
            self.fail()
        except Py4JNetworkError:
            self.fail()
        except Py4JError:
            self.assertTrue(True)

    def testStrError(self):
        try:
            self.gateway.jvm.Integer.valueOf("allo")
        except Py4JJavaError as e:
            self.assertTrue(str(e).startswith(
                "An error occurred while calling z:java.lang.Integer.valueOf."
                "\n: java.lang.NumberFormatException:"))
        except Exception:
            self.fail()


class JVMTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()
        self.gateway = JavaGateway()

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()

    def testConstructors(self):
        jvm = self.gateway.jvm
        sb = jvm.java.lang.StringBuffer("hello")
        sb.append("hello world")
        sb.append(1)
        self.assertEqual(sb.toString(), "hellohello world1")

        l1 = jvm.java.util.ArrayList()
        l1.append("hello world")
        l1.append(1)
        self.assertEqual(2, len(l1))
        self.assertEqual("hello world", l1[0])
        l2 = ["hello world", 1]
        self.assertEqual(str(l2), str(l1))

    def testStaticMethods(self):
        System = self.gateway.jvm.java.lang.System
        self.assertGreater(System.currentTimeMillis(), 0)
        self.assertEqual("123", self.gateway.jvm.java.lang.String.valueOf(123))

    def testStaticFields(self):
        Short = self.gateway.jvm.java.lang.Short
        self.assertEqual(-32768, Short.MIN_VALUE)
        System = self.gateway.jvm.java.lang.System
        self.assertFalse(System.out.checkError())

    def testDefaultImports(self):
        self.assertGreater(self.gateway.jvm.System.currentTimeMillis(), 0)
        self.assertEqual("123", self.gateway.jvm.String.valueOf(123))

    def testNone(self):
        ex = self.gateway.entry_point.getNewExample()
        ex.method4(None)

    def testJavaGatewayServer(self):
        server = self.gateway.java_gateway_server
        self.assertEqual(
            server.getListeningPort(), DEFAULT_PORT)

    def testJVMView(self):
        newView = self.gateway.new_jvm_view("myjvm")
        time = newView.System.currentTimeMillis()
        self.assertGreater(time, 0)
        time = newView.java.lang.System.currentTimeMillis()
        self.assertGreater(time, 0)

    def testImport(self):
        newView = self.gateway.new_jvm_view("myjvm")
        java_import(self.gateway.jvm, "java.util.*")
        java_import(self.gateway.jvm, "java.io.File")
        self.assertIsNotNone(self.gateway.jvm.ArrayList())
        self.assertIsNotNone(self.gateway.jvm.File("hello.txt"))
        self.assertRaises(Exception, lambda: newView.File("test.txt"))

        java_import(newView, "java.util.HashSet")
        self.assertIsNotNone(newView.HashSet())

    def testEnum(self):
        self.assertEqual("FOO", str(self.gateway.jvm.py4j.examples.Enum2.FOO))

    def testInnerClass(self):
        self.assertEqual(
            "FOO",
            str(self.gateway.jvm.py4j.examples.EnumExample.MyEnum.FOO))
        self.assertEqual(
            "HELLO2",
            self.gateway.jvm.py4j.examples.EnumExample.InnerClass.MY_CONSTANT2)


class HelpTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()
        self.gateway = JavaGateway()

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()

    def testHelpObject(self):
        ex = self.gateway.getNewExample()
        help_page = self.gateway.help(ex, short_name=True, display=False)
        self.assertGreater(len(help_page), 1)

    def testHelpObjectWithPattern(self):
        ex = self.gateway.getNewExample()
        help_page = self.gateway.help(
            ex, pattern="m*", short_name=True, display=False)
        self.assertGreater(len(help_page), 1)

    def testHelpClass(self):
        String = self.gateway.jvm.java.lang.String
        help_page = self.gateway.help(String, short_name=False, display=False)
        self.assertGreater(len(help_page), 1)
        self.assertIn("String", help_page)


class Runner(Thread):
    def __init__(self, runner_range, gateway):
        Thread.__init__(self)
        self.range = runner_range
        self.gateway = gateway
        self.ok = True

    def run(self):
        ex = self.gateway.getNewExample()
        for i in self.range:
            try:
                a_list = ex.getList(i)
                if len(a_list) != i:
                    self.ok = False
                    break
                self.gateway.detach(a_list)
                # gc.collect()
            except Exception:
                self.ok = False
                break


class ThreadTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()
        gateway_client = GatewayClient()
        self.gateway = JavaGateway()
        self.gateway.set_gateway_client(gateway_client)

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()

    def testStress(self):
        # Real stress test!
        # runner1 = Runner(xrange(1,10000,2),self.gateway)
        # runner2 = Runner(xrange(1000,1000000,10000), self.gateway)
        # runner3 = Runner(xrange(1000,1000000,10000), self.gateway)
        # Small stress test
        runner1 = Runner(range(1, 10000, 1000), self.gateway)
        runner2 = Runner(range(1000, 1000000, 100000), self.gateway)
        runner3 = Runner(range(1000, 1000000, 100000), self.gateway)
        runner1.start()
        runner2.start()
        runner3.start()
        runner1.join()
        runner2.join()
        runner3.join()
        self.assertTrue(runner1.ok)
        self.assertTrue(runner2.ok)
        self.assertTrue(runner3.ok)


class GatewayLauncherTest(unittest.TestCase):
    def tearDown(self):
        safe_shutdown(self)

    def testDefaults(self):
        self.gateway = JavaGateway.launch_gateway()
        self.assertTrue(self.gateway.jvm)

    def testJavaPath(self):
        self.gateway = JavaGateway.launch_gateway(java_path=None)
        self.assertTrue(self.gateway.jvm)

    def testCreateNewProcessGroup(self):
        self.gateway = JavaGateway.launch_gateway(
            create_new_process_group=True)
        self.assertTrue(self.gateway.jvm)

    def testAccessSubprocess(self):
        self.gateway = JavaGateway.launch_gateway()
        self.assertTrue(self.gateway.java_process)

    def testShutdownSubprocess(self):
        self.gateway = JavaGateway.launch_gateway()
        self.assertTrue(self.gateway.java_process)
        # Popen.poll() returns None iff the subprocess has not terminated.
        self.assertTrue(self.gateway.java_process.poll() is None)
        self.gateway.shutdown()
        # Unfortunately the Java process has not terminated quite yet.
        # If we check that poll() is not None, we will often find that poll()
        # still is None.
        # One thing that definitely works is to wait one second and assert
        # the Java process has terminated *then*.
        # This is not ideal, since it introduces a bit of an extra delay in
        # what would otherwise be a millisecond test.
        # Waiting only a fraction of a second (2**-5) seems to be enough.
        if sys.version_info < (3,):
            sleep()
            self.assertFalse(self.gateway.java_process.poll() is None)
        else:
            self.gateway.java_process.wait(2**-5)
        # Popen.wait() will raise a TimeoutExpired exception if the subprocess
        # has not yet terminated.

    def testShutdownSubprocessThatDiesOnExit(self):
        self.gateway = JavaGateway.launch_gateway(die_on_exit=True)
        self.assertTrue(self.gateway.java_process)
        # Popen.poll() returns None iff the subprocess has not terminated.
        self.assertTrue(self.gateway.java_process.poll() is None)
        self.gateway.shutdown()
        # If we change shutdown() to automatically do the following, then we
        # should remove the following from this test.
        self.assertTrue(self.gateway.java_process.poll() is None)
        self.gateway.java_process.stdin.write("\n".encode("utf-8"))
        self.gateway.java_process.stdin.flush()
        if sys.version_info < (3,):
            sleep()
            self.assertFalse(self.gateway.java_process.poll() is None)
        else:
            self.gateway.java_process.wait(1)

    def testJavaopts(self):
        self.gateway = JavaGateway.launch_gateway(javaopts=["-Xmx64m"])
        self.assertTrue(self.gateway.jvm)

    def testCwd(self):
        parent_directory = os.path.dirname(os.getcwd())
        self.gateway = JavaGateway.launch_gateway(cwd=parent_directory)
        java_cwd = self.gateway.jvm.System.getProperty("user.dir")
        self.assertEqual(parent_directory, java_cwd)

    def testRedirectToNull(self):
        self.gateway = JavaGateway.launch_gateway()
        for i in range(4097):  # Hangs if not properly redirected
            self.gateway.jvm.System.out.println("Test")

    def testRedirectToNullOtherProcessGroup(self):
        self.gateway = JavaGateway.launch_gateway(
            create_new_process_group=True)
        for i in range(4097):  # Hangs if not properly redirected
            self.gateway.jvm.System.out.println("Test")

    def testRedirectToQueue(self):
        end = os.linesep
        qout = Queue()
        qerr = Queue()
        self.gateway = JavaGateway.launch_gateway(
            redirect_stdout=qout, redirect_stderr=qerr)
        for i in range(10):
            self.gateway.jvm.System.out.println("Test")
            self.gateway.jvm.System.err.println("Test2")
        sleep()
        for i in range(10):
            self.assertEqual("Test{0}".format(end), qout.get())
            # Assert IN because some Java/OS outputs some garbage on stderr.
            line = qerr.get()
            if stderr_is_polluted(line):
                line = qerr.get()
            self.assertIn("Test2{0}".format(end), line)
        self.assertTrue(qout.empty)
        self.assertTrue(qerr.empty)

    def testRedirectToDeque(self):
        end = os.linesep
        qout = deque()
        qerr = deque()
        self.gateway = JavaGateway.launch_gateway(
            redirect_stdout=qout, redirect_stderr=qerr)
        for i in range(10):
            self.gateway.jvm.System.out.println("Test")
            self.gateway.jvm.System.err.println("Test2")
        sleep()
        for i in range(10):
            self.assertEqual("Test{0}".format(end), qout.pop())
            # Assert IN because some Java/OS outputs some garbage on stderr.
            line = qerr.pop()
            if stderr_is_polluted(line):
                line = qerr.pop()
            self.assertEqual("Test2{0}".format(end), line)
        self.assertEqual(0, len(qout))
        self.assertEqual(0, len(qerr))

    def testRedirectToFile(self):
        end = os.linesep
        (out_handle, outpath) = tempfile.mkstemp(text=True)
        (err_handle, errpath) = tempfile.mkstemp(text=True)

        stdout = open(outpath, "w")
        stderr = open(errpath, "w")

        try:
            self.gateway = JavaGateway.launch_gateway(
                redirect_stdout=stdout, redirect_stderr=stderr)
            for i in range(10):
                self.gateway.jvm.System.out.println("Test")
                self.gateway.jvm.System.err.println("Test2")
            self.gateway.shutdown()
            sleep()
            # Should not be necessary
            quiet_close(stdout)
            quiet_close(stderr)

            # Test that the redirect files were written to correctly
            with open(outpath, "r") as stdout:
                lines = stdout.readlines()
                self.assertEqual(10, len(lines))
                self.assertEqual("Test{0}".format(end), lines[0])

            with open(errpath, "r") as stderr:
                lines = stderr.readlines()
                if not stderr_is_polluted(lines[0]):
                    self.assertEqual(10, len(lines))
                    # XXX Apparently, it's \n by default even on windows...
                    # Go figure
                    self.assertEqual("Test2\n", lines[0])
        finally:
            os.close(out_handle)
            os.close(err_handle)
            os.unlink(outpath)
            os.unlink(errpath)

    def testGatewayAuth(self):
        self.gateway = JavaGateway.launch_gateway(enable_auth=True)

        # Make sure the default client can connect to the server.
        klass = self.gateway.jvm.java.lang.String
        help_page = self.gateway.help(klass, short_name=True, display=False)
        self.assertGreater(len(help_page), 1)

        # Replace the client with one that does not authenticate.
        # Make sure it fails.
        bad_client = GatewayClient(gateway_parameters=GatewayParameters(
            address=self.gateway.gateway_parameters.address,
            port=self.gateway.gateway_parameters.port))
        self.gateway.set_gateway_client(bad_client)
        try:
            self.gateway.help(klass, short_name=True, display=False)
            self.fail("Expected failure to communicate with gateway server.")
        except Exception:
            # Expected
            pass
        finally:
            # Restore a good client. This allows the gateway to be shut down.
            good_client = GatewayClient(
                gateway_parameters=self.gateway.gateway_parameters)
            self.gateway.set_gateway_client(good_client)


class WaitOperator(object):

    def __init__(self, sleepTime):
        self.sleepTime = sleepTime
        self.callCount = 0

    def doOperation(self, i, j):
        self.callCount += 1
        if self.callCount == 1:
            sleep(self.sleepTime)
        return i + j

    class Java:
        implements = ["py4j.examples.Operator"]


class IPv6Test(unittest.TestCase):

    def testIpV6(self):
        self.p = start_ipv6_app_process()
        gateway = JavaGateway(
            gateway_parameters=GatewayParameters(address="::1"),
            callback_server_parameters=CallbackServerParameters(address="::1"))

        try:
            timeMillis = gateway.jvm.System.currentTimeMillis()
            self.assertGreater(timeMillis, 0)

            operator = WaitOperator(0.1)
            opExample = gateway.jvm.py4j.examples.OperatorExample()
            a_list = opExample.randomBinaryOperator(operator)
            self.assertEqual(a_list[0] + a_list[1], a_list[2])
        finally:
            gateway.shutdown()
            self.p.join()


class RetryTest(unittest.TestCase):

    def testBadRetry(self):
        """Should not retry from Python to Java.
        Python calls a long Java method. The call goes through, but the
        response takes a long time to get back.

        If there is a bug, Python will fail on read and retry (sending the same
        call twice).

        If there is no bug, Python will fail on read and raise an Exception.
        """
        self.p = start_example_app_process()
        gateway = JavaGateway(
            gateway_parameters=GatewayParameters(read_timeout=0.250))
        try:
            value = gateway.entry_point.getNewExample().sleepFirstTimeOnly(500)
            self.fail(
                "Should never retry once the first command went through."
                "number of calls made: {0}".format(value))
        except Py4JError:
            self.assertTrue(True)
        finally:
            gateway.shutdown()
            self.p.join()

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
        self.p = start_short_timeout_app_process()
        gateway = JavaGateway()
        connections = gateway._gateway_client.deque
        try:
            # Call #1
            gateway.jvm.System.currentTimeMillis()
            str_connection = str(connections[0])

            # Call #2 after, should not create new connections if the system is
            # not too slow :-)
            gateway.jvm.System.currentTimeMillis()
            self.assertEqual(1, len(connections))
            str_connection2 = str(connections[0])
            self.assertEqual(str_connection, str_connection2)

            sleep(0.5)
            gateway.jvm.System.currentTimeMillis()
            self.assertEqual(1, len(connections))
            str_connection3 = str(connections[0])
            # A new connection was automatically created.
            self.assertNotEqual(str_connection, str_connection3)
        except Py4JError:
            self.fail("Should retry automatically by default.")
        finally:
            gateway.shutdown()
            self.p.join()

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
        self.p = start_short_timeout_app_process()
        gateway = JavaGateway(
            callback_server_parameters=CallbackServerParameters())
        try:
            operator = WaitOperator(0.5)
            opExample = gateway.jvm.py4j.examples.OperatorExample()

            opExample.randomBinaryOperator(operator)
            self.fail(
                "Should never retry once the first command went through."
                " number of calls made: {0}".format(operator.callCount))
        except Py4JJavaError:
            self.assertTrue(True)
        finally:
            gateway.shutdown()
            self.p.join()

    def testGoodRetryFromJava(self):
        """Should retry from Java to Python.
        Similar use case as testGoodRetry, but from Java: Python calls Java,
        which calls Python back two times in a row. Then python waits for a
        while. Python then calls Java, which calls Python.

        Because Python Callback server has been waiting for too much time, the
        receiving socket has closed so the call from Java to Python will fail
        on send, and Java must retry by creating a new connection
        (CallbackConnection).
        """
        self.p = start_example_app_process()
        gateway = JavaGateway(
            callback_server_parameters=CallbackServerParameters(
                read_timeout=0.250))
        try:
            operator = WaitOperator(0)
            opExample = gateway.jvm.py4j.examples.OperatorExample()
            opExample.randomBinaryOperator(operator)
            str_connection = str(list(gateway._callback_server.connections)[0])

            opExample.randomBinaryOperator(operator)
            str_connection2 = str(
                list(gateway._callback_server.connections)[0])

            sleep(0.5)

            opExample.randomBinaryOperator(operator)
            str_connection3 = str(
                list(gateway._callback_server.connections)[0])

            self.assertEqual(str_connection, str_connection2)
            self.assertNotEqual(str_connection, str_connection3)
        except Py4JJavaError:
            self.fail("Java callbackclient did not retry.")
        finally:
            gateway.shutdown()
            self.p.join()


if __name__ == "__main__":
    unittest.main()
