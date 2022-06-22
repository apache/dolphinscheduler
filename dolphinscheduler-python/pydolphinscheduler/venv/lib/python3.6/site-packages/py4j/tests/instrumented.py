from collections import deque
import weakref

import py4j.protocol as proto
from py4j.clientserver import (
    ClientServerConnection, ClientServer, JavaClient, PythonServer)
from py4j.java_gateway import (
    CallbackServer, JavaGateway, GatewayClient, GatewayProperty,
    PythonProxyPool, GatewayConnection, CallbackConnection)
from py4j.tests.py4j_callback_recursive_example import PythonPing

# Use deque to be thread-safe
MEMORY_HOOKS = deque()
CREATED = deque()
FINALIZED = deque()


def register_creation(obj):
    obj_str = str(obj)
    CREATED.append(obj_str)
    MEMORY_HOOKS.append(weakref.ref(
        obj,
        lambda wr: FINALIZED.append(obj_str)
    ))


class InstrumentedPythonPing(PythonPing):

    def __init__(self, fail=False):
        super(InstrumentedPythonPing, self).__init__(fail)
        register_creation(self)


class InstrJavaGateway(JavaGateway):
    def __init__(self, *args, **kwargs):
        super(InstrJavaGateway, self). __init__(*args, **kwargs)
        register_creation(self)

    def _create_gateway_client(self):
        gateway_client = InstrGatewayClient(
            gateway_parameters=self.gateway_parameters)
        return gateway_client

    def _create_callback_server(self, callback_server_parameters):
        callback_server = InstrCallbackServer(
            self.gateway_property.pool, self._gateway_client,
            callback_server_parameters=callback_server_parameters)
        return callback_server

    def _create_gateway_property(self):
        gateway_property = InstrGatewayProperty(
            self.gateway_parameters.auto_field, PythonProxyPool(),
            self.gateway_parameters.enable_memory_management)
        if self.python_server_entry_point:
            gateway_property.pool.put(
                self.python_server_entry_point, proto.ENTRY_POINT_OBJECT_ID)
        return gateway_property


class InstrGatewayClient(GatewayClient):

    def __init__(self, *args, **kwargs):
        super(InstrGatewayClient, self).__init__(*args, **kwargs)
        register_creation(self)

    def _create_connection(self):
        connection = InstrGatewayConnection(
            self.gateway_parameters, self.gateway_property)
        connection.start()
        return connection


class InstrGatewayProperty(GatewayProperty):
    """Object shared by callbackserver, gateway, and connections.
    """
    def __init__(self, *args, **kwargs):
        super(InstrGatewayProperty, self).__init__(*args, **kwargs)
        register_creation(self)


class InstrGatewayConnection(GatewayConnection):

    def __init__(self, *args, **kwargs):
        super(InstrGatewayConnection, self).__init__(*args, **kwargs)
        register_creation(self)


class InstrCallbackServer(CallbackServer):
    def __init__(self, *args, **kwargs):
        super(InstrCallbackServer, self).__init__(*args, **kwargs)
        register_creation(self)

    def _create_connection(self, socket_instance, stream):
        connection = InstrCallbackConnection(
            self.pool, stream, socket_instance, self.gateway_client,
            self.callback_server_parameters, self)
        return connection


class InstrCallbackConnection(CallbackConnection):

    def __init__(self, *args, **kwargs):
        super(InstrCallbackConnection, self).__init__(*args, **kwargs)
        register_creation(self)


class InstrClientServerConnection(ClientServerConnection):
    def __init__(self, *args, **kwargs):
        super(InstrClientServerConnection, self).__init__(*args, **kwargs)
        register_creation(self)


class InstrPythonServer(PythonServer):
    def __init__(self, *args, **kwargs):
        super(InstrPythonServer, self).__init__(*args, **kwargs)
        register_creation(self)

    def _create_connection(self, socket, stream):
        connection = InstrClientServerConnection(
            self.java_parameters, self.python_parameters,
            self.gateway_property, self.gateway_client, self)
        connection.init_socket_from_python_server(socket, stream)
        return connection


class InstrJavaClient(JavaClient):

    def __init__(self, *args, **kwargs):
        super(InstrJavaClient, self).__init__(*args, **kwargs)
        register_creation(self)

    def _create_new_connection(self):
        connection = InstrClientServerConnection(
            self.java_parameters, self.python_parameters,
            self.gateway_property, self)
        connection.connect_to_java_server()
        self.set_thread_connection(connection)
        self.deque.append(connection)
        return connection


class InstrClientServer(ClientServer):

    def __init__(self, *args, **kwargs):
        super(InstrClientServer, self).__init__(*args, **kwargs)
        register_creation(self)

    def _create_gateway_client(self):
        java_client = InstrJavaClient(
            self.java_parameters, self.python_parameters)
        return java_client

    def _create_callback_server(self, callback_server_parameters):
        callback_server = InstrPythonServer(
            self._gateway_client, self.java_parameters, self.python_parameters,
            self.gateway_property)
        return callback_server

    def _create_gateway_property(self):
        gateway_property = InstrGatewayProperty(
            self.java_parameters.auto_field, PythonProxyPool(),
            self.java_parameters.enable_memory_management)
        if self.python_server_entry_point:
            gateway_property.pool.put(
                self.python_server_entry_point, proto.ENTRY_POINT_OBJECT_ID)
        return gateway_property
