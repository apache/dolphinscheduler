from __future__ import unicode_literals, absolute_import


from py4j.tests.java_gateway_test import gateway, example_app_process


def test_help_object():
    with example_app_process():
        with gateway() as g:
            ex = g.getNewExample()
            doc = g.help(ex, display=False)
            assert "Help on class ExampleClass in package py4j.examples" in doc
            assert "method1" in doc
            assert "method2" in doc


def test_doc_object():
    with example_app_process():
        with gateway() as g:
            ex = g.getNewExample()
            doc = ex.__doc__
            assert "Help on class ExampleClass in package py4j.examples" in doc
            assert "method1" in doc
            assert "getField1" in doc


def test_not_callable():
    with example_app_process():
        with gateway() as g:
            ex = g.getNewExample()
            try:
                ex()
                raise AssertionError
            except TypeError as e:
                assert "object is not callable" in str(e)


def test_help_pattern_1():
    with example_app_process():
        with gateway() as g:
            ex = g.getNewExample()
            doc = g.help(ex, display=False, pattern="m*")
            assert "Help on class ExampleClass in package py4j.examples" in doc
            assert "method1" in doc
            assert "getField1" not in doc


def test_help_pattern_2():
    with example_app_process():
        with gateway() as g:
            ex = g.getNewExample()
            doc = g.help(ex, display=False, pattern="getField1(*")
            assert "Help on class ExampleClass in package py4j.examples" in doc
            assert "method1" not in doc
            assert "getField1" in doc


def test_help_method():
    with example_app_process():
        with gateway() as g:
            ex = g.getNewExample()
            doc = g.help(ex.method7, display=False)
            # Make sure multiple method7s appear (overloaded method)
            assert "method7(int)" in doc
            assert "method7(Object)" in doc
            assert "method1" not in doc


def test_doc_method():
    with example_app_process():
        with gateway() as g:
            ex = g.getNewExample()
            doc = ex.method7.__doc__
            # Make sure multiple method7s appear (overloaded method)
            assert "method7(int)" in doc
            assert "method7(Object)" in doc
            assert "method1" not in doc


def test_help_class():
    with example_app_process():
        with gateway() as g:
            clazz = g.jvm.py4j.examples.ExampleClass
            doc = g.help(clazz, display=False)
            assert "Help on class ExampleClass in package py4j.examples" in doc
            assert "method1" in doc
            assert "method2" in doc


def test_doc_class():
    with example_app_process():
        with gateway() as g:
            clazz = g.jvm.py4j.examples.ExampleClass
            doc = clazz.__doc__
            # Make sure multiple method7s appear (overloaded method)
            assert "Help on class ExampleClass in package py4j.examples" in doc
            assert "method1" in doc
            assert "method2" in doc
