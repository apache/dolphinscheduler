"""
Created on Mar 26, 2010

@author: Barthelemy Dagenais
"""
from __future__ import unicode_literals, absolute_import

import unittest

from py4j.java_gateway import JavaGateway, GatewayParameters
from py4j.tests.java_gateway_test import (
    start_example_app_process, safe_shutdown, sleep)


class AutoConvertTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()
        self.gateway = JavaGateway(
            gateway_parameters=GatewayParameters(auto_convert=True))

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()
        sleep()

    def testAutoConvert(self):
        sj = self.gateway.jvm.java.util.HashSet()
        sj.add("b")
        sj.add(1)
        sp = {1, "b"}
        self.assertTrue(sj.equals(sp))


class SetTest(unittest.TestCase):
    def setUp(self):
        self.p = start_example_app_process()
        self.gateway = JavaGateway()

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()
        sleep()

    def testTreeSet(self):
        set1 = set()
        set2 = self.gateway.jvm.java.util.TreeSet()
        set1.add("a")
        set2.add("a")
        self.assertEqual(len(set1), len(set2))
        self.assertEqual("a" in set1, "a" in set2)
        self.assertEqual(repr(set1), repr(set2))

        set1.add("b")
        set2.add("b")
        self.assertEqual(len(set1), len(set2))
        self.assertEqual("a" in set1, "a" in set2)
        self.assertEqual("b" in set1, "b" in set2)
        # not a good assumption with Python 3.3. Oh dear.
        # self.assertEqual(repr(set1), repr(set2))

        set1.remove("a")
        set2.remove("a")
        self.assertEqual(len(set1), len(set2))
        self.assertEqual("a" in set1, "a" in set2)
        self.assertEqual("b" in set1, "b" in set2)
        # self.assertEqual(repr(set1), repr(set2))

        set1.clear()
        set2.clear()
        self.assertEqual(len(set1), len(set2))
        self.assertEqual("a" in set1, "a" in set2)
        self.assertEqual("b" in set1, "b" in set2)
        # self.assertEqual(repr(set1), repr(set2))

    def testHashSet(self):
        set1 = set()
        set2 = self.gateway.jvm.java.util.HashSet()
        set1.add("a")
        set2.add("a")
        set1.add(1)
        set2.add(1)
        set1.add("b")
        set2.add("b")
        self.assertEqual(len(set1), len(set2))
        self.assertEqual("a" in set1, "a" in set2)
        self.assertEqual("b" in set1, "b" in set2)
        self.assertEqual(1 in set1, 1 in set2)

        set1.remove(1)
        set2.remove(1)
        self.assertEqual(len(set1), len(set2))
        self.assertEqual("a" in set1, "a" in set2)
        self.assertEqual("b" in set1, "b" in set2)
        self.assertEqual(1 in set1, 1 in set2)

        set1.clear()
        set2.clear()
        self.assertEqual(len(set1), len(set2))
        self.assertEqual("a" in set1, "a" in set2)
        self.assertEqual("b" in set1, "b" in set2)
        self.assertEqual(1 in set1, 1 in set2)


if __name__ == "__main__":
    unittest.main()
