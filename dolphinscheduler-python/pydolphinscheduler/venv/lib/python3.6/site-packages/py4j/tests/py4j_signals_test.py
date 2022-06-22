# -*- coding: UTF-8 -*-
from __future__ import unicode_literals, absolute_import

from collections import defaultdict
import unittest

from py4j.java_gateway import (
    server_connection_started, server_connection_stopped,
    server_started, server_stopped, pre_server_shutdown, post_server_shutdown,
    JavaGateway, GatewayParameters, CallbackServerParameters)
from py4j.clientserver import (
    ClientServer, JavaParameters, PythonParameters)
from py4j.tests.client_server_test import (
    clientserver_example_app_process)
from py4j.tests.java_callback_test import (
    IHelloImpl, gateway_example_app_process)
from py4j.tests.py4j_callback_recursive_example import (
    HelloState)


class MockListener(object):

    def __init__(self, test_case):
        self.test_case = test_case
        self.received = defaultdict(int)

    def started(self, sender, **kwargs):
        self.test_case.assertTrue(kwargs["server"] is not None)
        self.received["started"] += 1

    def connection_started(self, sender, **kwargs):
        self.test_case.assertTrue(kwargs["connection"] is not None)
        self.received["connection_started"] += 1

    def connection_stopped(self, sender, **kwargs):
        self.test_case.assertTrue(kwargs["connection"] is not None)
        self.received["connection_stopped"] += 1

    def stopped(self, sender, **kwargs):
        self.test_case.assertTrue(kwargs["server"] is not None)
        self.received["stopped"] += 1

    def pre_shutdown(self, sender, **kwargs):
        self.test_case.assertTrue(kwargs["server"] is not None)
        self.received["pre_shutdown"] += 1

    def post_shutdown(self, sender, **kwargs):
        self.test_case.assertTrue(kwargs["server"] is not None)
        self.received["post_shutdown"] += 1


class JavaGatewayTest(unittest.TestCase):

    def test_all_regular_signals_auto_start(self):
        listener = MockListener(self)
        with gateway_example_app_process(None):
            server_started.connect(listener.started)
            gateway = JavaGateway(
                gateway_parameters=GatewayParameters(),
                callback_server_parameters=CallbackServerParameters())
            server_stopped.connect(
                listener.stopped, sender=gateway.get_callback_server())
            server_connection_started.connect(
                listener.connection_started,
                sender=gateway.get_callback_server())
            server_connection_stopped.connect(
                listener.connection_stopped,
                sender=gateway.get_callback_server())
            pre_server_shutdown.connect(
                listener.pre_shutdown, sender=gateway.get_callback_server())
            post_server_shutdown.connect(
                listener.post_shutdown, sender=gateway.get_callback_server())
            example = gateway.entry_point.getNewExample()
            impl = IHelloImpl()
            self.assertEqual("This is Hello!", example.callHello(impl))
            gateway.shutdown()
        self.assertEqual(1, listener.received["started"])
        self.assertEqual(1, listener.received["stopped"])
        self.assertEqual(1, listener.received["pre_shutdown"])
        self.assertEqual(1, listener.received["post_shutdown"])
        self.assertEqual(1, listener.received["connection_started"])
        self.assertEqual(1, listener.received["connection_stopped"])


class ClientServerTest(unittest.TestCase):

    def test_all_regular_signals(self):
        listener = MockListener(self)

        server_started.connect(listener.started)

        hello_state = HelloState()
        client_server = ClientServer(
            JavaParameters(), PythonParameters(), hello_state)
        server_stopped.connect(
            listener.stopped, sender=client_server.get_callback_server())
        server_connection_started.connect(
            listener.connection_started,
            sender=client_server.get_callback_server())
        server_connection_stopped.connect(
            listener.connection_stopped,
            sender=client_server.get_callback_server())
        pre_server_shutdown.connect(
            listener.pre_shutdown, sender=client_server.get_callback_server())
        post_server_shutdown.connect(
            listener.post_shutdown, sender=client_server.get_callback_server())
        with clientserver_example_app_process(True):
            client_server.shutdown()

        self.assertEqual(1, listener.received["started"])
        self.assertEqual(1, listener.received["stopped"])
        self.assertEqual(1, listener.received["pre_shutdown"])
        self.assertEqual(1, listener.received["post_shutdown"])
        self.assertEqual(1, listener.received["connection_started"])
        self.assertEqual(1, listener.received["connection_stopped"])

    def test_signals_started_from_python(self):
        listener = MockListener(self)

        with clientserver_example_app_process():
            server_started.connect(listener.started)

            client_server = ClientServer(
                JavaParameters(), PythonParameters())
            example = client_server.entry_point.getNewExample()
            impl = IHelloImpl()
            self.assertEqual("This is Hello!", example.callHello(impl))

            server_stopped.connect(
                listener.stopped, sender=client_server.get_callback_server())
            server_connection_started.connect(
                listener.connection_started,
                sender=client_server.get_callback_server())
            server_connection_stopped.connect(
                listener.connection_stopped,
                sender=client_server.get_callback_server())
            pre_server_shutdown.connect(
                listener.pre_shutdown,
                sender=client_server.get_callback_server())
            post_server_shutdown.connect(
                listener.post_shutdown,
                sender=client_server.get_callback_server())
            client_server.shutdown()

        self.assertEqual(1, listener.received["started"])
        self.assertEqual(1, listener.received["stopped"])
        self.assertEqual(1, listener.received["pre_shutdown"])
        self.assertEqual(1, listener.received["post_shutdown"])
        # Connection initiated from JavaClient, so no signal sent.
        self.assertEqual(0, listener.received["connection_started"])
        self.assertEqual(0, listener.received["connection_stopped"])
