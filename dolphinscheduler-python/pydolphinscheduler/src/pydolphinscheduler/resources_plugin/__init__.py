import importlib
import os
import importlib.util


class ResourcePlugin:

    def __init__(self, type: str, prefix: str):
        self._type = type
        self._prefix = prefix

    def get_modules(self, package="."):
        """Get the names of all modules under a package"""
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
        spec.loader.exec_module(module)
        plugin = getattr(module, self._type.capitalize())
        return plugin(self._prefix)

    @property
    def resource(self):
        pwd = os.path.abspath(__file__)
        parent_path = os.path.abspath(os.path.dirname(pwd) + os.path.sep + ".")
        modules = self.get_modules(parent_path)
        if self._type not in [module for module in modules]:
            raise ValueError('{} type is not supported'.format(self._type))

        script_name = self._type + '.py'
        script_path = parent_path + '/' + script_name

        res = self.import_module(script_name, script_path)
        return res
