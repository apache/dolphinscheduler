"""
Created on Apr 27, 2010

@author: barthelemy
"""
from py4j.java_gateway import JavaGateway, CallbackServerParameters


class Addition(object):
    def doOperation(self, i, j, k=None):
        if k is None:
            return i + j
        else:
            return 3722507311

    class Java:
        implements = ["py4j.examples.Operator"]


if __name__ == "__main__":
    gateway = JavaGateway(
        callback_server_parameters=CallbackServerParameters())
    operator = Addition()
    numbers = gateway.entry_point.randomBinaryOperator(operator)
    print(numbers)
    numbers = gateway.entry_point.randomTernaryOperator(operator)
    print(numbers)
    gateway.shutdown()
