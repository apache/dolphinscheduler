from py4j.java_gateway import JavaGateway, CallbackServerParameters


class PythonListener(object):

    def __init__(self, gateway):
        self.gateway = gateway

    def notify(self, obj):
        print("Notified by Java")
        print(obj)
        gateway.jvm.System.out.println("Hello from python!")

        return "A Return Value"

    class Java:
        implements = ["py4j.examples.ExampleListener"]


if __name__ == "__main__":
    gateway = JavaGateway(
        callback_server_parameters=CallbackServerParameters())
    listener = PythonListener(gateway)
    gateway.entry_point.registerListener(listener)
    gateway.entry_point.notifyAllListeners()
    gateway.shutdown()
