class HelloState(object):
    def __init__(self):
        self.calls = []

    def sayHello(self, int_value=None, string_value=None):
        print(int_value, string_value)
        self.calls.append((int_value, string_value))
        return "Said hello to {0}".format(string_value)

    class Java:
        implements = ["py4j.examples.IHello"]


class SimpleHello(object):

    def sayHello(self, int_value=None, string_value=None):
        print(int_value, string_value)
        return "Said hello to {0}".format(string_value)

    class Java:
        implements = ["py4j.examples.IHello"]


class RecursiveHello(object):

    def __init__(self, example_obj):
        self.example_obj = example_obj

    def sayHello(self, int_value=None, string_value=None):
        print(int_value, string_value)
        if int_value is None:
            print("Hello with no param")
            return self.example_obj.callHello2(self)
        else:
            print("Hello with 2 params")
            return "Said hello to {0}".format(string_value)

    class Java:
        implements = ["py4j.examples.IHello"]


class JavaHello(object):

    def __init__(self):
        self.clientserver = None

    def sayHello(self, int_value=None, string_value=None):
        ar = self.clientserver.jvm.java.util.ArrayList()
        ar.append("1")
        print(ar)
        other_value = self.clientserver.jvm.java.lang.\
            System.currentTimeMillis()
        print(int_value, string_value, other_value)
        return "Said hello to {0}".format(string_value)

    class Java:
        implements = ["py4j.examples.IHello"]


class PythonPing(object):

    def __init__(self, fail=False):
        self.fail = fail

    def ping1(self, obj):
        return obj.pong1(self)

    def ping2(self, obj):
        return obj.pong2(self)

    def ping3(self, obj):
        if self.fail:
            raise ValueError("Some Exception")
        else:
            return obj.pong3(self)

    class Java:
        implements = ["py4j.examples.IPing"]
