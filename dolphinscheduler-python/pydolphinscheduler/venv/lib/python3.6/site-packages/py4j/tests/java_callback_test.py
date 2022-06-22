"""
Created on Apr 5, 2010

@author: Barthelemy Dagenais
"""
from __future__ import unicode_literals, absolute_import

from contextlib import contextmanager
from multiprocessing import Process
import subprocess
from threading import Thread
from traceback import print_exc
import unittest

from py4j.compat import range
from py4j.java_gateway import (
    JavaGateway, PythonProxyPool, CallbackServerParameters,
    set_default_callback_accept_timeout, is_instance_of)
from py4j.protocol import Py4JJavaError
from py4j.tests.java_gateway_test import (
    PY4J_JAVA_PATH, safe_shutdown, sleep, check_connection)


set_default_callback_accept_timeout(0.125)


def start_example_server():
    subprocess.call([
        "java", "-cp", PY4J_JAVA_PATH,
        "py4j.examples.ExampleApplication"])


def start_no_mem_example_server():
    subprocess.call([
        "java", "-cp", PY4J_JAVA_PATH,
        "py4j.examples.ExampleApplication$ExampleNoMemManagementApplication"])


def start_python_entry_point_server(*args):
    java_args = [
        "java", "-cp", PY4J_JAVA_PATH,
        "py4j.examples.ExampleApplication$ExamplePythonEntryPointApplication"]
    subprocess.call(java_args + list(args))


def start_example_server2():
    subprocess.call([
        "java", "-cp", PY4J_JAVA_PATH,
        "py4j.examples.OperatorExampleTest"])


def start_example_server3():
    subprocess.call([
        "java", "-Xmx512m", "-cp", PY4J_JAVA_PATH,
        "py4j.examples.InterfaceExample"])


def start_example_app_process(app=None, args=()):
    # XXX DO NOT FORGET TO KILL THE PROCESS IF THE TEST DOES NOT SUCCEED
    if not app:
        target = start_example_server
    elif app == "nomem":
        target = start_no_mem_example_server
    elif app == "pythonentrypoint":
        target = start_python_entry_point_server
    p = Process(target=target, args=args)
    p.start()
    sleep()
    check_connection()
    return p


@contextmanager
def gateway_example_app_process(app=None, args=()):
    p = start_example_app_process(app, args)
    try:
        yield p
    finally:
        p.join()


def start_example_app_process2():
    # XXX DO NOT FORGET TO KILL THE PROCESS IF THE TEST DOES NOT SUCCEED
    p = Process(target=start_example_server2)
    p.start()
    sleep()
    check_connection()
    return p


def start_example_app_process3():
    # XXX DO NOT FORGET TO KILL THE PROCESS IF THE TEST DOES NOT SUCCEED
    p = Process(target=start_example_server3)
    p.start()
    sleep()
    check_connection()
    return p


class Returner(object):

    def __init__(self, bad_type=False):
        self.bad_type = bad_type

    def getChar(self):
        return "a"

    def getFloat(self):
        if self.bad_type:
            return "abc"
        else:
            return 1.25

    def getInt(self):
        if self.bad_type:
            return 1111111111111111111111111111
        else:
            return 25

    def doNothing(self):
        print("Doing nothing")

    def getNull(self):
        return None

    class Java:
        implements = ["py4j.examples.IReturnConverter"]


class FalseAddition(object):
    def doOperation(self, i, j, k=None):
        if k is None:
            # Integer overflow!
            return 3722507311
        else:
            return 3722507311

    class Java:
        implements = ["py4j.examples.Operator"]


class GoodAddition(object):
    def doOperation(self, i, j):
        return i + j

    class Java:
        implements = ["py4j.examples.Operator"]


class CustomBytesOperator(object):
    def returnBytes(self, byte_array):
        try:
            b = bytearray()
            for abyte in byte_array:
                b.append(abyte + 1)
            return b
        except Exception:
            print_exc()
            return None

    class Java:
        implements = ["py4j.examples.BytesOperator"]


class Runner(Thread):
    def __init__(self, runner_range, pool):
        Thread.__init__(self)
        self.range = runner_range
        self.pool = pool
        self.ok = True

    def run(self):
        for i in self.range:
            try:
                id = self.pool.put(i)
                self.ok = id in self.pool and self.pool[id] == i
                if not self.ok:
                    break
            except Exception as e:
                print(e)
                self.ok = False
                break


class TestPool(unittest.TestCase):

    def testPool(self):
        pool = PythonProxyPool()
        runners = [Runner(range(0, 10000), pool) for _ in range(0, 3)]
        for runner in runners:
            runner.start()

        for runner in runners:
            runner.join()

        for runner in runners:
            self.assertTrue(runner.ok)


class SimpleProxy(object):
    def hello(self, i, j):
        return "Hello\nWorld" + str(i) + str(j)


class IHelloImpl(object):
    def sayHello(self, i=None, s=None):
        if i is None:
            return "This is Hello!"
        else:
            return "This is Hello;\n{0}{1}".format(i, s)

    class Java:
        implements = ["py4j.examples.IHello"]


class IHelloFailingImpl(object):
    def __init__(self, exception):
        self.exception = exception

    def sayHello(self, i=None, s=None):
        raise self.exception

    class Java:
        implements = ["py4j.examples.IHello"]


class PythonEntryPointTest(unittest.TestCase):

    def test_python_entry_point(self):
        self._run_test()

    def test_python_entry_point_with_auth(self):
        self._run_test("secret-token")

    def _run_test(self, auth_token=None):
        from py4j.tests.py4j_callback_recursive_example import (
            HelloState)
        hello_state = HelloState()
        cb_params = CallbackServerParameters(auth_token=auth_token)
        gateway = JavaGateway(
            callback_server_parameters=cb_params,
            python_server_entry_point=hello_state)

        args = []
        if auth_token:
            args = [auth_token]
        with gateway_example_app_process("pythonentrypoint", args):
            gateway.shutdown()

        # Check that Java correctly called Python
        self.assertEqual(2, len(hello_state.calls))
        self.assertEqual((None, None), hello_state.calls[0])
        self.assertEqual((2, "Hello World"), hello_state.calls[1])


class NoMemManagementTest(unittest.TestCase):
    def testGC(self):
        with gateway_example_app_process("nomem"):
            # This will only work with some JVM.
            gateway = JavaGateway(
                callback_server_parameters=CallbackServerParameters())
            sleep()
            example = gateway.entry_point.getNewExample()
            impl = IHelloImpl()
            self.assertEqual("This is Hello!", example.callHello(impl))
            self.assertEqual(
                "This is Hello;\n10MyMy!\n;",
                example.callHello2(impl))
            self.assertEqual(2, len(gateway.gateway_property.pool))

            # Make sure that finalizers do not block
            impl2 = IHelloImpl()
            self.assertEqual("This is Hello!", example.callHello(impl2))
            self.assertEqual(3, len(gateway.gateway_property.pool))

            gateway.jvm.java.lang.System.gc()

            # Leave time for sotimeout
            sleep(3)
            # Make sure the three objects have not been removed from the pool
            # because the Java side should not send gc request.
            self.assertEqual(len(gateway.gateway_property.pool), 3)
            gateway.shutdown()


class IntegrationTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()
        self.gateway = JavaGateway(
            callback_server_parameters=CallbackServerParameters(
                propagate_java_exceptions=True))
        sleep()

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()
        sleep()

    # Does not work when combined with other tests... because of TCP_WAIT
    def testShutdown(self):
        example = self.gateway.entry_point.getNewExample()
        impl = IHelloImpl()
        self.assertEqual("This is Hello!", example.callHello(impl))
        self.assertEqual(
            "This is Hello;\n10MyMy!\n;",
            example.callHello2(impl))
        self.gateway.shutdown()
        self.assertEqual(0, len(self.gateway.gateway_property.pool))

    def testProxyReturnerFloatErrorTypeConversion(self):
        sleep()
        example = self.gateway.jvm.py4j.examples.ReturnerExample()
        returner = Returner(bad_type=True)
        self.assertRaises(
            Py4JJavaError, example.computeFloat,
            returner)

    def testProxyReturnerIntOverflow(self):
        sleep()
        example = self.gateway.jvm.py4j.examples.ReturnerExample()
        returner = Returner(bad_type=True)
        self.assertRaises(
            Py4JJavaError, example.computeInt,
            returner)

    def testProxyReturnerFloat(self):
        sleep()
        example = self.gateway.jvm.py4j.examples.ReturnerExample()
        returner = Returner()
        output = example.computeFloat(returner)
        self.assertAlmostEqual(output, 1.25)

    def testProxyReturnerChar(self):
        sleep()
        example = self.gateway.jvm.py4j.examples.ReturnerExample()
        returner = Returner()
        output = example.computeChar(returner)
        self.assertEqual(output, "a")

    def testProxyReturnerVoid(self):
        sleep()
        example = self.gateway.jvm.py4j.examples.ReturnerExample()
        returner = Returner()
        output = example.computeNothing(returner)
        self.assertEqual(output, 1)

    def testProxyReturnerNull(self):
        sleep()
        example = self.gateway.jvm.py4j.examples.ReturnerExample()
        returner = Returner()
        output = example.computeNull(returner)
        self.assertIsNone(output)

    def testProxy(self):
        sleep()
        example = self.gateway.entry_point.getNewExample()
        impl = IHelloImpl()
        self.assertEqual("This is Hello!", example.callHello(impl))
        self.assertEqual(
            "This is Hello;\n10MyMy!\n;",
            example.callHello2(impl))

    def testProxyError(self):
        sleep()
        example = self.gateway.entry_point.getNewExample()

        try:
            example.callHello(IHelloFailingImpl(
                ValueError('My interesting Python exception')))
            self.fail()
        except Py4JJavaError as e:
            self.assertIn('interesting Python exception', str(e))

        try:
            example.callHello(IHelloFailingImpl(
                Py4JJavaError(
                    '',
                    self.gateway.jvm.java.lang.IllegalStateException(
                        'My IllegalStateException'))))
            self.fail()
        except Py4JJavaError as e:
            self.assertTrue(is_instance_of(
                self.gateway, e.java_exception,
                'java.lang.IllegalStateException'))

    def testGC(self):
        # This will only work with some JVM.
        sleep()
        example = self.gateway.entry_point.getNewExample()
        impl = IHelloImpl()
        self.assertEqual("This is Hello!", example.callHello(impl))
        self.assertEqual(
            "This is Hello;\n10MyMy!\n;",
            example.callHello2(impl))
        self.assertEqual(2, len(self.gateway.gateway_property.pool))

        # Make sure that finalizers do not block
        impl2 = IHelloImpl()
        self.assertEqual("This is Hello!", example.callHello(impl2))
        self.assertEqual(3, len(self.gateway.gateway_property.pool))

        self.gateway.jvm.java.lang.System.gc()

        # Leave time for sotimeout
        sleep(3)
        self.assertLess(len(self.gateway.gateway_property.pool), 2)

    def testDoubleCallbackServer(self):
        try:
            self.gateway2 = JavaGateway(
                callback_server_parameters=CallbackServerParameters())
            self.fail()
        except Exception:
            self.assertTrue(True)

    def testMethodConstructor(self):
        sleep()
        goodAddition = GoodAddition()
        oe1 = self.gateway.jvm.py4j.examples.OperatorExample()
        # Test method
        oe1.randomBinaryOperator(goodAddition)
        # Test constructor
        oe2 = self.gateway.jvm.py4j.examples.OperatorExample(goodAddition)
        self.assertIsNotNone(oe2)


class NoPropagateTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()
        self.gateway = JavaGateway(
            callback_server_parameters=CallbackServerParameters(
                propagate_java_exceptions=False))
        sleep()

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()
        sleep()

    def testProxyError(self):
        sleep()
        example = self.gateway.entry_point.getNewExample()

        try:
            example.callHello(IHelloFailingImpl(
                Py4JJavaError(
                    '',
                    self.gateway.jvm.java.lang.IllegalStateException(
                        'My IllegalStateException'))))
            self.fail()
        except Py4JJavaError as e:
            self.assertTrue(is_instance_of(
                self.gateway, e.java_exception,
                'py4j.Py4JException'))
            self.assertIn('My IllegalStateException', str(e))


class ResetCallbackClientTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()
        self.gateway = JavaGateway(
            callback_server_parameters=CallbackServerParameters(port=0))
        sleep()

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()
        sleep()

    def testProxy(self):
        sleep()
        pythonAddress = self.gateway.java_gateway_server.getPythonAddress()
        port = self.gateway.get_callback_server().get_listening_port()
        self.gateway.java_gateway_server.resetCallbackClient(
            pythonAddress, port)

        example = self.gateway.entry_point.getNewExample()
        impl = IHelloImpl()
        self.assertEqual("This is Hello!", example.callHello(impl))
        self.assertEqual(
            "This is Hello;\n10MyMy!\n;",
            example.callHello2(impl))


class PeriodicCleanupTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process2()
        self.gateway = JavaGateway(
            callback_server_parameters=CallbackServerParameters())
        sleep()

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()
        sleep()

    def testPeriodicCleanup(self):
        operator = FalseAddition()
        self.assertRaises(
            Exception, self.gateway.entry_point.randomTernaryOperator,
            operator)
        # Time for periodic cleanup
        sleep(2)
        self.assertRaises(
            Exception, self.gateway.entry_point.randomTernaryOperator,
            operator)

    def testBytes(self):
        sleep()
        operator = CustomBytesOperator()
        returnbytes = self.gateway.entry_point.callBytesOperator(operator)
        self.assertEqual(2, returnbytes[0])
        self.assertEqual(6, returnbytes[-1])
        sleep()


class A(object):
    class Java:
        implements = ["py4j.examples.InterfaceA"]


class B(object):
    def getA(self):
        return A()

    class Java:
        implements = ["py4j.examples.InterfaceB"]


class InterfaceTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process3()
        self.gateway = JavaGateway(
            callback_server_parameters=CallbackServerParameters())
        sleep()

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()

    def testByteString(self):
        try:
            self.gateway.entry_point.test(B())
        except Exception:
            print_exc()
            self.fail()


class InterfaceDeprecatedTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process3()
        self.gateway = JavaGateway(start_callback_server=True)

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()

    def testByteString(self):
        try:
            self.gateway.entry_point.test(B())
        except Exception:
            print_exc()
            self.fail()


class LazyStartTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process3()
        self.gateway = JavaGateway(
            callback_server_parameters=CallbackServerParameters(
                eager_load=False))

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()

    def testByteString(self):
        try:
            self.gateway.start_callback_server()
            self.gateway.entry_point.test(B())
            self.gateway.start_callback_server()
            self.gateway.start_callback_server()
            self.gateway.entry_point.test(B())
        except Exception:
            print_exc()
            self.fail()


if __name__ == "__main__":
    unittest.main()
