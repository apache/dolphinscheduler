from functools import partial
import gc
import time
import sys

from py4j.java_gateway import JavaGateway, CallbackServerParameters


ITERATIONS_FOR_LENGTHY_METHOD = 3


class ComparablePython(object):

    def __init__(self, value):
        self.value = value

    def compareTo(self, obj):
        if obj is None:
            # Hack to return the value of this object.
            return self.value
        value = obj.compareTo(None)
        return self.value - value

    class Java:
        implements = ["java.lang.Comparable"]


def callStaticMethodNoParam(iterations, staticMethod):
    i = 0
    result = 0
    while i < iterations:
        result = staticMethod()
        i += 1
    # Make sure that the last result is returned so Python does not discard the
    # output value.
    return result


def callInstanceMethodWithShortParam(iterations, instanceMethod):
    shortParam = "Super Long Param"
    i = 0
    while i < iterations:
        instanceMethod(shortParam)
        instanceMethod(1)
        i += 1


def callFunc(iterations, func):
    i = 0
    result = None
    while i < iterations:
        result = func()
        i += 1
    return result


def benchmark(name, func):
    start = time.time()
    func()
    stop = time.time()
    print("{0} - {1}".format(stop - start, name))
    gc.collect()


def main(iterations):
    small_iterations = iterations / 10 if iterations > 10 else iterations
    gateway = JavaGateway(
        callback_server_parameters=CallbackServerParameters())
    currentTimeMillis = gateway.jvm.java.lang.System.currentTimeMillis
    sb = gateway.jvm.java.lang.StringBuilder()
    append = sb.append
    sb2 = gateway.jvm.java.lang.StringBuilder()

    def reflection():
        sb2.append(2)
        sb2.append("hello")

    def constructorAndMemoryManagement():
        sb = gateway.jvm.java.lang.StringBuilder("Hello World")
        sb.append("testing")

    def javaCollection():
        al = gateway.jvm.java.util.ArrayList()
        al.append("test")
        al.append(1)
        al.append(True)
        len(al)
        result = []
        for elem in al:
            result.append(elem)
        return result

    def callBack():
        al = gateway.jvm.java.util.ArrayList()
        cp10 = ComparablePython(10)
        cp1 = ComparablePython(1)
        cp5 = ComparablePython(5)
        cp7 = ComparablePython(7)
        al.append(cp10)
        al.append(cp1)
        al.append(cp5)
        al.append(cp7)
        gateway.jvm.java.util.Collections.sort(al)

    def longParamCall():
        longParam = "s" * 1024 * 1024 * 10
        sb = gateway.jvm.java.lang.StringBuilder()
        sb.append(longParam)
        sb.toString()

    benchmark(
        "callStaticMethodNoParam",
        partial(callStaticMethodNoParam, iterations, currentTimeMillis))
    benchmark(
        "callInstanceMethodWithShortParam",
        partial(callInstanceMethodWithShortParam, iterations, append))
    benchmark(
        "callWithReflection",
        partial(callFunc, iterations, reflection))
    benchmark(
        "constructorAndMemoryManagement",
        partial(callFunc, iterations, constructorAndMemoryManagement))
    benchmark(
        "longParamAndMemoryManagement",
        partial(callFunc, ITERATIONS_FOR_LENGTHY_METHOD, longParamCall))
    benchmark(
        "javaCollection",
        partial(callFunc, small_iterations, javaCollection))
    benchmark(
        "callBack",
        partial(callFunc, small_iterations, callBack))
    gateway.shutdown()


if __name__ == "__main__":
    # 1. Run py4j-java, e.g.,
    #    cd py4j-java; ./gradlew testsJar;
    #    java -Xmx4096m -cp build/libs/py4j-tests-0.10.0.jar \
    #    py4j.example.ExampleApplication
    # 2. Run python program:
    #    cd py4j-python; export PYTHONPATH=src
    #    python3 src/py4j/tests/benchmark1.py
    iterations = 100000
    if len(sys.argv) > 1:
        iterations = int(sys.argv[1])
    main(iterations)
