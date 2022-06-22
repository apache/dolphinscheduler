# coding: utf-8
"""
Compatibility functions for unified behavior between Python 2.x and 3.x.

:author: Alex Grönholm
"""
from __future__ import unicode_literals, absolute_import

import inspect
import sys
from threading import Thread

version_info = sys.version_info

if version_info.major < 3:
    def items(d):
        return d.items()

    def iteritems(d):
        return d.iteritems()

    def next(x):
        return x.next()

    range = xrange  # noqa

    long = long  # noqa

    basestring = basestring  # noqa

    unicode = unicode  # noqa

    bytearray2 = bytearray

    unichr = unichr  # noqa

    bytestr = str

    tobytestr = str

    def isbytestr(s):
        return isinstance(s, str)

    def ispython3bytestr(s):
        return False

    def isbytearray(s):
        return isinstance(s, bytearray)

    def bytetoint(b):
        return ord(b)

    def bytetostr(b):
        return b

    def strtobyte(b):
        return b

    import Queue
    Empty = Queue.Empty
    Queue = Queue.Queue

else:
    def items(d):
        return list(d.items())

    def iteritems(d):
        return d.items()

    next = next

    range = range

    long = int

    basestring = str

    unicode = str

    bytearray2 = bytes

    unichr = chr

    bytestr = bytes

    def tobytestr(s):
        return bytes(s, "ascii")

    def isbytestr(s):
        return isinstance(s, bytes)

    def ispython3bytestr(s):
        return isinstance(s, bytes)

    def isbytearray(s):
        return isinstance(s, bytearray)

    def bytetoint(b):
        return b

    def bytetostr(b):
        return str(b, encoding="ascii")

    def strtobyte(s):
        return bytes(s, encoding="ascii")

    import queue
    Queue = queue.Queue
    Empty = queue.Empty


if hasattr(inspect, "getattr_static"):
    def hasattr2(obj, attr):
        return bool(inspect.getattr_static(obj, attr, False))
else:
    hasattr2 = hasattr


class CompatThread(Thread):
    """Compatibility Thread class.

    Allows Python 2 Thread class to accept daemon kwarg in init.
    """

    def __init__(self, *args, **kwargs):
        daemon = None
        try:
            daemon = kwargs.pop("daemon")
        except KeyError:
            pass
        super(CompatThread, self).__init__(*args, **kwargs)

        if daemon:
            self.daemon = daemon
