"""
The protocol module defines the primitives and the escaping used by
Py4J protocol.

This is a text-based protocol that is efficient for general-purpose
method calling, but very inefficient with large numbers (because
they are text-based).

Binary protocol (e.g., protobuf) was considered in the past, but
internal benchmarking showed that it was less efficient in
terms of size and time. This is due to the fact that a lot
of small strings are exchanged (method name, class name, variable
names, etc.).

Created on Oct 14, 2010

:author: Barthelemy Dagenais
"""
from __future__ import unicode_literals, absolute_import

from base64 import standard_b64encode, standard_b64decode

from decimal import Decimal

from py4j.compat import (
    long, basestring, unicode, bytearray2,
    bytestr, isbytestr, isbytearray, ispython3bytestr,
    bytetoint, bytetostr, strtobyte)


JAVA_MAX_INT = 2147483647
JAVA_MIN_INT = -2147483648

JAVA_INFINITY = "Infinity"
JAVA_NEGATIVE_INFINITY = "-Infinity"
JAVA_NAN = "NaN"


ESCAPE_CHAR = "\\"

# Entry point
ENTRY_POINT_OBJECT_ID = "t"
CONNECTION_PROPERTY_OBJECT_ID = "c"
GATEWAY_SERVER_OBJECT_ID = "GATEWAY_SERVER"
STATIC_PREFIX = "z:"

# JVM
DEFAULT_JVM_ID = "rj"
DEFAULT_JVM_NAME = "default"

# Types
BYTES_TYPE = "j"
INTEGER_TYPE = "i"
LONG_TYPE = "L"
BOOLEAN_TYPE = "b"
DOUBLE_TYPE = "d"
DECIMAL_TYPE = "D"
STRING_TYPE = "s"
REFERENCE_TYPE = "r"
ARRAY_TYPE = "t"
SET_TYPE = "h"
LIST_TYPE = "l"
MAP_TYPE = "a"
NULL_TYPE = "n"
PACKAGE_TYPE = "p"
CLASS_TYPE = "c"
METHOD_TYPE = "m"
NO_MEMBER = "o"
VOID_TYPE = "v"
ITERATOR_TYPE = "g"
PYTHON_PROXY_TYPE = "f"

# Protocol
END = "e"
ERROR = "x"
FATAL_ERROR = "z"
SUCCESS = "y"
RETURN_MESSAGE = "!"


# Shortcuts
SUCCESS_PACKAGE = SUCCESS + PACKAGE_TYPE
SUCCESS_CLASS = SUCCESS + CLASS_TYPE
CLASS_FQN_START = 2
END_COMMAND_PART = END + "\n"
NO_MEMBER_COMMAND = SUCCESS + NO_MEMBER

# Commands
CALL_COMMAND_NAME = "c\n"
FIELD_COMMAND_NAME = "f\n"
CONSTRUCTOR_COMMAND_NAME = "i\n"
SHUTDOWN_GATEWAY_COMMAND_NAME = "s\n"
LIST_COMMAND_NAME = "l\n"
REFLECTION_COMMAND_NAME = "r\n"
MEMORY_COMMAND_NAME = "m\n"
HELP_COMMAND_NAME = "h\n"
ARRAY_COMMAND_NAME = "a\n"
JVMVIEW_COMMAND_NAME = "j\n"
EXCEPTION_COMMAND_NAME = "p\n"
DIR_COMMAND_NAME = "d\n"
STREAM_COMMAND_NAME = "S\n"

# Array subcommands
ARRAY_GET_SUB_COMMAND_NAME = "g\n"
ARRAY_SET_SUB_COMMAND_NAME = "s\n"
ARRAY_SLICE_SUB_COMMAND_NAME = "l\n"
ARRAY_LEN_SUB_COMMAND_NAME = "e\n"
ARRAY_CREATE_SUB_COMMAND_NAME = "c\n"

# Reflection subcommands
REFL_GET_UNKNOWN_SUB_COMMAND_NAME = "u\n"
REFL_GET_MEMBER_SUB_COMMAND_NAME = "m\n"
REFL_GET_JAVA_LANG_CLASS_SUB_COMMAND_NAME = "c\n"


# List subcommands
LIST_SORT_SUBCOMMAND_NAME = "s\n"
LIST_REVERSE_SUBCOMMAND_NAME = "r\n"
LIST_SLICE_SUBCOMMAND_NAME = "l\n"
LIST_CONCAT_SUBCOMMAND_NAME = "a\n"
LIST_MULT_SUBCOMMAND_NAME = "m\n"
LIST_IMULT_SUBCOMMAND_NAME = "i\n"
LIST_COUNT_SUBCOMMAND_NAME = "f\n"

# Field subcommands
FIELD_GET_SUBCOMMAND_NAME = "g\n"
FIELD_SET_SUBCOMMAND_NAME = "s\n"

# Memory subcommands
MEMORY_DEL_SUBCOMMAND_NAME = "d\n"
MEMORY_ATTACH_SUBCOMMAND_NAME = "a\n"

# Help subcommands
HELP_OBJECT_SUBCOMMAND_NAME = "o\n"
HELP_CLASS_SUBCOMMAND_NAME = "c\n"

# JVM subcommands
JVM_CREATE_VIEW_SUB_COMMAND_NAME = "c\n"
JVM_IMPORT_SUB_COMMAND_NAME = "i\n"
JVM_SEARCH_SUB_COMMAND_NAME = "s\n"
REMOVE_IMPORT_SUB_COMMAND_NAME = "r\n"

# Callback specific
PYTHON_PROXY_PREFIX = "p"
ERROR_RETURN_MESSAGE = RETURN_MESSAGE + ERROR + NULL_TYPE + "\n"
SUCCESS_RETURN_MESSAGE = RETURN_MESSAGE + SUCCESS + "\n"
OUTPUT_VOID_COMMAND = RETURN_MESSAGE + SUCCESS + VOID_TYPE + "\n"

AUTH_COMMAND_NAME = "A"
CALL_PROXY_COMMAND_NAME = "c"
GARBAGE_COLLECT_PROXY_COMMAND_NAME = "g"

# Dir subcommands
DIR_FIELDS_SUBCOMMAND_NAME = "f\n"
DIR_METHODS_SUBCOMMAND_NAME = "m\n"
DIR_STATIC_SUBCOMMAND_NAME = "s\n"
DIR_JVMVIEW_SUBCOMMAND_NAME = "v\n"

OUTPUT_CONVERTER = {
    NULL_TYPE: (lambda x, y: None),
    BOOLEAN_TYPE: (lambda value, y: value.lower() == "true"),
    LONG_TYPE: (lambda value, y: long(value)),
    DECIMAL_TYPE: (lambda value, y: Decimal(value)),
    INTEGER_TYPE: (lambda value, y: int(value)),
    BYTES_TYPE: (lambda value, y: decode_bytearray(value)),
    DOUBLE_TYPE: (lambda value, y: float(value)),
    STRING_TYPE: (lambda value, y: unescape_new_line(value)),
}

INPUT_CONVERTER = []

# ERRORS
ERROR_ON_SEND = "on_send"
ERROR_ON_RECEIVE = "on_receive"


def escape_new_line(original):
    """Replaces new line characters by a backslash followed by a n.

    Backslashes are also escaped by another backslash.

    :param original: the string to escape

    :rtype: an escaped string
    """
    if original:
        return smart_decode(original).replace("\\", "\\\\").\
            replace("\r", "\\r").replace("\n", "\\n")
    else:
        return original


def unescape_new_line(escaped):
    """Replaces escaped characters by unescaped characters.

    For example, double backslashes are replaced by a single backslash.

    The behavior for improperly formatted strings is undefined and can change.

    :param escaped: the escaped string

    :rtype: the original string
    """
    if escaped:
        return ESCAPE_CHAR.join(
            "\n".join(
                ("\r".join(p.split(ESCAPE_CHAR + "r")))
                .split(ESCAPE_CHAR + "n"))
            for p in escaped.split(ESCAPE_CHAR + ESCAPE_CHAR))
    else:
        return escaped


def smart_decode(s):
    if isinstance(s, unicode):
        return s
    elif isinstance(s, bytestr):
        # Should never reach this case in Python 3
        return unicode(s, "utf-8")
    else:
        return unicode(s)


def encode_float(float_value):
    float_str = smart_decode(repr(float_value))
    if float_str == "-inf":
        float_str = JAVA_NEGATIVE_INFINITY
    elif float_str == "inf":
        float_str = JAVA_INFINITY
    elif float_str == "nan":
        float_str = JAVA_NAN
    return float_str


def encode_bytearray(barray):
    if isbytestr(barray):
        return bytetostr(standard_b64encode(barray))
    else:
        newbytestr = bytestr(barray)
        return bytetostr(standard_b64encode(newbytestr))


def decode_bytearray(encoded):
    new_bytes = strtobyte(encoded)
    return bytearray2([bytetoint(b) for b in standard_b64decode(new_bytes)])


def is_python_proxy(parameter):
    """Determines whether parameter is a Python Proxy, i.e., it has a Java
    internal class with an `implements` member.

    :param parameter: the object to check.
    :rtype: True if the parameter is a Python Proxy
    """
    try:
        is_proxy = len(parameter.Java.implements) > 0
    except Exception:
        is_proxy = False

    return is_proxy


def get_command_part(parameter, python_proxy_pool=None):
    """Converts a Python object into a string representation respecting the
    Py4J protocol.

    For example, the integer `1` is converted to `u"i1"`

    :param parameter: the object to convert
    :rtype: the string representing the command part
    """
    command_part = ""

    if parameter is None:
        command_part = NULL_TYPE
    elif isinstance(parameter, bool):
        command_part = BOOLEAN_TYPE + smart_decode(parameter)
    elif isinstance(parameter, Decimal):
        command_part = DECIMAL_TYPE + smart_decode(parameter)
    elif isinstance(parameter, int) and parameter <= JAVA_MAX_INT\
            and parameter >= JAVA_MIN_INT:
        command_part = INTEGER_TYPE + smart_decode(parameter)
    elif isinstance(parameter, long) or isinstance(parameter, int):
        command_part = LONG_TYPE + smart_decode(parameter)
    elif isinstance(parameter, float):
        command_part = DOUBLE_TYPE + encode_float(parameter)
    elif isbytearray(parameter):
        command_part = BYTES_TYPE + encode_bytearray(parameter)
    elif ispython3bytestr(parameter):
        command_part = BYTES_TYPE + encode_bytearray(parameter)
    elif isinstance(parameter, basestring):
        command_part = STRING_TYPE + escape_new_line(parameter)
    elif is_python_proxy(parameter):
        command_part = PYTHON_PROXY_TYPE + python_proxy_pool.put(parameter)
        for interface in parameter.Java.implements:
            command_part += ";" + interface
    else:
        command_part = REFERENCE_TYPE + parameter._get_object_id()

    command_part += "\n"

    return command_part


def get_return_value(answer, gateway_client, target_id=None, name=None):
    """Converts an answer received from the Java gateway into a Python object.

    For example, string representation of integers are converted to Python
    integer, string representation of objects are converted to JavaObject
    instances, etc.

    :param answer: the string returned by the Java gateway
    :param gateway_client: the gateway client used to communicate with the Java
        Gateway. Only necessary if the answer is a reference (e.g., object,
        list, map)
    :param target_id: the name of the object from which the answer comes from
        (e.g., *object1* in `object1.hello()`). Optional.
    :param name: the name of the member from which the answer comes from
        (e.g., *hello* in `object1.hello()`). Optional.
    """
    if is_error(answer)[0]:
        if len(answer) > 1:
            type = answer[1]
            value = OUTPUT_CONVERTER[type](answer[2:], gateway_client)
            if answer[1] == REFERENCE_TYPE:
                raise Py4JJavaError(
                    "An error occurred while calling {0}{1}{2}.\n".
                    format(target_id, ".", name), value)
            else:
                raise Py4JError(
                    "An error occurred while calling {0}{1}{2}. Trace:\n{3}\n".
                    format(target_id, ".", name, value))
        else:
            raise Py4JError(
                "An error occurred while calling {0}{1}{2}".
                format(target_id, ".", name))
    else:
        type = answer[1]
        if type == VOID_TYPE:
            return
        else:
            return OUTPUT_CONVERTER[type](answer[2:], gateway_client)


def get_error_message(answer, gateway_client=None):
    """Returns a tuple of:

    1. bool: if the answer is an error
    2. the error message if any (discards null and references)
    """
    is_answer_error = is_error(answer)[0]
    value = None
    if is_answer_error:
        if len(answer) > 1:
            type = answer[1]
            if type == STRING_TYPE:
                value = OUTPUT_CONVERTER[type](answer[2:], gateway_client)
    return (is_answer_error, value)


def compute_exception_message(default_message, extra_message=None):
    """Returns an error message with an extra error message if provided.

    Otherwise returns the default error message.
    """
    message = default_message
    if extra_message:
        message = "{0} -- {1}".format(
            default_message, extra_message)
    return message


def is_error(answer):
    if len(answer) == 0 or answer[0] != SUCCESS:
        return (True, None)
    else:
        return (False, None)


def is_fatal_error(answer):
    return answer and len(answer) > 0 and answer[0] == FATAL_ERROR


def register_output_converter(output_type, converter):
    """Registers an output converter to the list of global output converters.

    An output converter transforms the output of the Java side to an instance
    on the Python side. For example, you could transform a java.util.ArrayList
    to a Python list. See ``py4j.java_collections`` for examples.

    :param output_type: A Py4J type of a return object (e.g., MAP_TYPE,
        BOOLEAN_TYPE).
    :param converter: A function that takes an object_id and a gateway_client
        as parameter and that returns a Python object (like a `bool` or a
        `JavaObject` instance).
    """
    global OUTPUT_CONVERTER
    OUTPUT_CONVERTER[output_type] = converter


def register_input_converter(converter, prepend=False):
    """Registers an input converter to the list of global input converters.

    An input converter transforms the input of the Python side to an instance
    on the Java side. For example, you could transform a Python list into a
    java.util.ArrayList on the Java side. See ``py4j.java_collections`` for
    examples.

    When initialized with `auto_convert=True`, a :class:`JavaGateway
    <py4j.java_gateway.JavaGateway>` will use the input converters on any
    parameter that is not a :class:`JavaObject <py4j.java_gateway.JavaObject>`
    or `basestring` instance.

    :param converter: A converter that declares the methods
        `can_convert(object)` and `convert(object,gateway_client)`.
    :param prepend: Put at the beginning of the input converters list

    """
    global INPUT_CONVERTER
    if prepend:
        INPUT_CONVERTER.insert(0, converter)
    else:
        INPUT_CONVERTER.append(converter)


class Py4JError(Exception):
    """Exception raised when a problem occurs with Py4J."""

    def __init__(self, args=None, cause=None):
        super(Py4JError, self).__init__(args)
        self.cause = cause


class Py4JAuthenticationError(Py4JError):
    """Exception raised when Py4J cannot authenticate a connection."""
    def __init__(self, args=None, cause=None):
        super(Py4JAuthenticationError, self).__init__(args)
        self.cause = cause


class Py4JNetworkError(Py4JError):
    """Exception raised when a network error occurs with Py4J."""
    def __init__(self, args=None, cause=None, when=None):
        super(Py4JNetworkError, self).__init__(args)
        self.cause = cause
        self.when = when


class Py4JJavaError(Py4JError):
    """Exception raised when an exception occurs in the client code.

    The exception instance that was thrown on the Java side can be accessed
    with `Py4JJavaError.java_exception`.

    `str(py4j_java_error)` returns the error message and the stack trace
    available on the Java side (similar to printStackTrace()).

    Note that `str(py4j_java_error)` in Python 2 might not automatically handle
    a non-ascii unicode string but throw an error if the exception contains it.
    """

    def __init__(self, msg, java_exception):
        self.args = (msg, java_exception)
        self.errmsg = msg
        self.java_exception = java_exception
        self.exception_cmd = EXCEPTION_COMMAND_NAME + REFERENCE_TYPE + \
            java_exception._target_id + "\n" + END_COMMAND_PART

    def __str__(self):
        gateway_client = self.java_exception._gateway_client
        answer = gateway_client.send_command(self.exception_cmd)
        return_value = get_return_value(answer, gateway_client, None, None)
        # Note: technically this should return a bytestring 'str' rather than
        # unicodes in Python 2; however, it can return unicodes for now.
        # See https://github.com/bartdag/py4j/issues/306 for more details.
        return "{0}: {1}".format(self.errmsg, return_value)
