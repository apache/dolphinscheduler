"""
Created on Feb 2, 2016

@author: Nick White
"""
from __future__ import unicode_literals, absolute_import

from multiprocessing import Process
import subprocess
import unittest
import ssl
import os

from py4j.java_gateway import (
    JavaGateway, CallbackServerParameters,
    set_default_callback_accept_timeout, GatewayParameters)
from py4j.tests.java_gateway_test import (
    PY4J_JAVA_PATH, safe_shutdown, sleep)

set_default_callback_accept_timeout(0.125)


def start_example_tls_server():
    subprocess.call([
        "java", "-cp", PY4J_JAVA_PATH,
        "py4j.examples.ExampleSSLApplication"])


def start_example_tls_process():
    p = Process(target=start_example_tls_server)
    p.start()
    sleep()
    return p


class Adder(object):
    def doOperation(self, i, j):
        return i + j

    class Java:
        implements = ["py4j.examples.Operator"]


class TestIntegration(unittest.TestCase):
    """Tests cases borrowed from other files, but executed over a
    TLS connection.
    """
    def setUp(self):
        key_file = os.path.join(
            os.path.dirname(os.path.realpath(__file__)),
            "selfsigned.pem")

        client_ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLSv1)
        client_ssl_context.verify_mode = ssl.CERT_REQUIRED
        client_ssl_context.check_hostname = True
        client_ssl_context.load_verify_locations(cafile=key_file)

        server_ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLSv1)
        server_ssl_context.load_cert_chain(key_file, password='password')

        callback_server_parameters = CallbackServerParameters(
            ssl_context=server_ssl_context)
        # address must match cert, because we're checking hostnames
        gateway_parameters = GatewayParameters(
            address='localhost',
            ssl_context=client_ssl_context)

        self.p = start_example_tls_process()
        self.gateway = JavaGateway(
            gateway_parameters=gateway_parameters,
            callback_server_parameters=callback_server_parameters)
        # It seems SecureServerSocket may need a little more time to
        # initialize on some platforms/slow machines.
        sleep(0.500)

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()
        sleep()

    def testUnicode(self):
        sleep()
        sb = self.gateway.jvm.java.lang.StringBuffer()
        sb.append("\r\n\tHello\r\n\t")
        self.assertEqual("\r\n\tHello\r\n\t", sb.toString())

    def testMethodConstructor(self):
        sleep()
        adder = Adder()
        oe1 = self.gateway.jvm.py4j.examples.OperatorExample()
        # Test method
        oe1.randomBinaryOperator(adder)
        # Test constructor
        oe2 = self.gateway.jvm.py4j.examples.OperatorExample(adder)
        self.assertIsNotNone(oe2)


if __name__ == "__main__":
    unittest.main()
