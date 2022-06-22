# -*- coding: UTF-8 -*-
"""Module that provides a simple signals library.

The signals pattern is very similar to the listener/observer pattern.

"""
from inspect import ismethod
from threading import Lock

from py4j.compat import range


def make_id(func):
    if ismethod(func):
        return (id(func.__self__), id(func.__func__))
    return id(func)


NONE_ID = make_id(None)


class Signal(object):
    """Basic signal class that can register receivers (listeners) and dispatch
    events to these receivers.

    As opposed to many signals libraries, receivers are not stored as weak
    references, so it is us to the client application to unregister them.

    Greatly inspired from Django Signals:
    https://github.com/django/django/blob/master/django/dispatch/dispatcher.py
    """

    def __init__(self):
        self.lock = Lock()
        # Someday, we may implement caching, but in practice, we expect the
        # number of receivers to be very small.
        self.receivers = []

    def connect(self, receiver, sender=None, unique_id=None):
        """Registers a receiver for this signal.

        The receiver must be a callable (e.g., function or instance method)
        that accepts named arguments (i.e., ``**kwargs``).

        In case that the connect method might be called multiple time, it is
        best to provide the receiver with a unique id to make sure that the
        receiver is not registered more than once.

        :param receiver: The callable that will receive the signal.
        :param sender: The sender to which the receiver will respond to. If
            None, signals from any sender are sent to this receiver
        :param unique_id: The unique id of the callable to make sure it is not
            registered more than once. Optional.
        """
        full_id = self._get_id(receiver, unique_id, sender)

        with self.lock:
            for receiver_id, _ in self.receivers:
                if receiver_id == full_id:
                    break
            else:
                self.receivers.append((full_id, receiver))

    def disconnect(self, receiver, sender=None, unique_id=None):
        """Unregisters a receiver for this signal.

        :param receiver: The callable that was registered to receive the
            signal.
        :param unique_id: The unique id of the callable if it was provided.
            Optional.
        :return: True if the receiver was found and disconnected. False
            otherwise.
        :rtype: bool
        """
        full_id = self._get_id(receiver, unique_id, sender)
        disconnected = False

        with self.lock:
            for index in range(len(self.receivers)):
                temp_id = self.receivers[index][0]
                if temp_id == full_id:
                    del self.receivers[index]
                    disconnected = True
                    break

        return disconnected

    def send(self, sender, **params):
        """Sends the signal to all connected receivers.

        If a receiver raises an error, the error is propagated back and
        interrupts the sending processing. It is thus possible that not all
        receivers will receive the signal.

        :param: named parameters to send to the receivers.
        :param: the sender of the signal. Optional.
        :return: List of (receiver, response) from receivers.
        :rtype: list
        """
        responses = []
        for receiver in self._get_receivers(sender):
            response = receiver(signal=self, sender=sender, **params)
            responses.append((receiver, response))
        return responses

    def _get_receivers(self, sender):
        """Internal method that may in the future resolve weak references or
        perform other work such as identifying dead receivers.
        """
        sender_id = make_id(sender)
        receivers = []
        with self.lock:
            for ((_, rsender_id), receiver) in self.receivers:
                if rsender_id == NONE_ID or rsender_id == sender_id:
                    receivers.append(receiver)
        return receivers

    def _get_id(self, receiver, unique_id, sender):
        sender_id = make_id(sender)
        if unique_id:
            full_id = (unique_id, sender_id)
        else:
            full_id = (make_id(receiver), sender_id)
        return full_id
