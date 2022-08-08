import importlib
import importlib.util

from pathlib import Path
from typing import Any, Generator

from pydolphinscheduler.exceptions import PyDSConfException

path_resources_plugin = Path(__file__).parent


# [start resource_plugin_definition]
class ResourcePlugin:
    def __init__(self, type: str, prefix: str):
        self.type = type
        self.prefix = prefix

    def get_all_modules(self) -> Generator[Path, Any, None]:
        """Get all res files path in resources_plugin directory."""
        return (ex for ex in path_resources_plugin.iterdir() if ex.is_file() and not ex.name.startswith("__"))

    def import_module(self, script_name, script_path):
        """Import module"""
        spec = importlib.util.spec_from_file_location(script_name, script_path)
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)
        plugin = getattr(module, self.type.capitalize())
        return plugin(self.prefix)

    @property
    def resource(self):
        """Dynamically return resource plugin"""
        for ex in self.get_all_modules():
            if ex.stem == self.type:
                return self.import_module(ex.name, str(ex))
        raise PyDSConfException('{} type is not supported'.format(self.type))
# [end resource_plugin_definition]
