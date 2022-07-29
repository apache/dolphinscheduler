import importlib
import os

from pydolphinscheduler.resources_plugin.local import Local


class ResourcePlugin:

    def __init__(self, type: str, prefix: str):
        self._type = type
        self._prefix = prefix

    def get_modules(self, package="."):
        modules = []
        files = os.listdir(package)
        for file in files:
            if not file.startswith("__"):
                name, ext = os.path.splitext(file)
                modules.append(name)
        return modules

    def import_module(self, script_name, script_path):
        """Import module"""
        spec = importlib.util.spec_from_file_location(script_name, script_path)
        module = importlib.util.module_from_spec(spec)
        setattr(module, self._type, self._type)
        setattr(module, self._prefix, self._prefix)
        spec.loader.exec_module(module)
        return module

    @property
    def resource(self):
        #         all_resource = importlib.import_module("")
        #         if self.type not in [str(i)  for i in all_resource]:
        #             raise ValueError()
        pwd = os.path.abspath(__file__)
        parent_path = os.path.abspath(os.path.dirname(pwd) + os.path.sep + ".")
        modules = self.get_modules(parent_path)
        print(modules)
        if self._type not in [module for module in modules]:
            raise ValueError('{} type is not supported'.format(self._type))

        script_name = self._type + '.py'
        script_path = parent_path + '/' + script_name

        res = self.import_module(script_name, script_path)
        return res
