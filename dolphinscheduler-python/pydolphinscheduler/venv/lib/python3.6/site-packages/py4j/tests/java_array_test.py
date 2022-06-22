"""
Created on Mar 24, 2010

@author: Barthelemy Dagenais
"""
from __future__ import unicode_literals, absolute_import

import time
import unittest

from py4j.java_gateway import JavaGateway
from py4j.protocol import Py4JError
from py4j.tests.java_gateway_test import (
    start_example_app_process)


class ArrayTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()
        time.sleep(0.5)
        self.gateway = JavaGateway()

    def tearDown(self):
        self.p.terminate()
        self.gateway.shutdown()
        time.sleep(0.5)

    def testArray(self):
        example = self.gateway.entry_point.getNewExample()
        array1 = example.getStringArray()
        array2 = example.getIntArray()
        self.assertEqual(3, len(array1))
        self.assertEqual(4, len(array2))

        self.assertEqual("333", array1[2])
        self.assertEqual(5, array2[1])

        array1[2] = "aaa"
        array2[1] = 6
        self.assertEqual("aaa", array1[2])
        self.assertEqual(6, array2[1])

        new_array = array2[1:3]
        self.assertEqual(2, len(new_array))
        self.assertEqual(1, new_array[1])

    def testCreateArray(self):
        int_class = self.gateway.jvm.int
        string_class = self.gateway.jvm.java.lang.String
        int_array = self.gateway.new_array(int_class, 2)
        string_array = self.gateway.new_array(string_class, 3, 5)
        self.assertEqual(2, len(int_array))
        self.assertEqual(3, len(string_array))
        self.assertEqual(5, len(string_array[0]))

    def testDoubleArray(self):
        double_class = self.gateway.jvm.double
        double_array = self.gateway.new_array(double_class, 2)
        double_array[0] = 2.2
        self.assertAlmostEqual(double_array[0], 2.2)

    def testFloatArray(self):
        float_class = self.gateway.jvm.float
        float_array = self.gateway.new_array(float_class, 2)
        float_array[0] = 2.2
        self.assertAlmostEqual(float_array[0], 2.2)

    def testCharArray(self):
        char_class = self.gateway.jvm.char
        char_array = self.gateway.new_array(char_class, 2)
        char_array[0] = "a"
        self.assertEqual(char_array[0], "a")

    def testSetNoneArray(self):
        string_class = self.gateway.jvm.java.lang.String
        string_array = self.gateway.new_array(string_class, 2)
        string_array[0] = "Hello World"
        string_array[1] = None
        self.assertEqual(string_array[0], "Hello World")
        self.assertIsNone(string_array[1])

    def testSetNonePrimitiveArray(self):
        int_class = self.gateway.jvm.int
        int_array = self.gateway.new_array(int_class, 2)
        with self.assertRaises(Py4JError):
            int_array[0] = None


if __name__ == "__main__":
    unittest.main()
