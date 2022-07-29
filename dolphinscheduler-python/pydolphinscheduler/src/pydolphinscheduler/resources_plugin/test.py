import importlib


def import_module(script_name, script_path):
    """Import module"""
    spec = importlib.util.spec_from_file_location(script_name, script_path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


""""Debug only"""
if __name__ == "__main__":
    # res = import_module('shell.py', '/home/chenrj/gitrep/dolphinscheduler/dolphinscheduler-python/pydolphinscheduler/src/pydolphinscheduler/tasks/shell.py')
    # print(res)

    # res = Local("/opt")
    # print(res.prefix)

    res = importlib.import_module("local")
    print(res)
