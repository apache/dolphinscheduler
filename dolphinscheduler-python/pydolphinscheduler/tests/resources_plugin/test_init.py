import os
import shutil

import pytest
from pydolphinscheduler.constants import ResourcePluginType
from pydolphinscheduler.resources_plugin import ResourcePlugin
from pydolphinscheduler.exceptions import PyResPluginException

modules_dir = 'modules'
modules_names = ["a", "b", "c"]
# res_plugin_prefix = Path(__file__).absolute().parent
pwd = os.path.abspath(__file__)
cur_path = os.path.abspath(os.path.dirname(pwd) + os.path.sep + ".") + "/"
modules_path = cur_path + modules_dir


@pytest.fixture
def setup_crt_first():
    """Set up and teardown about create folder first and then delete it."""
    os.mkdir(modules_dir)
    yield
    shutil.rmtree(modules_dir)


@pytest.fixture
def create_modules(setup_crt_first):
    """Temporarily create an empty module based on module_names."""
    for modules_name in modules_names:
        open(modules_path + "/" + "%s.py" % modules_name, "w")
    yield


@pytest.mark.parametrize(
    "attr, expect",
    [
        (
            {
                "type": ResourcePluginType.LOCAL,
                "prefix": "/tmp/",
            },
            modules_names
        )
    ],
)
def test_resources_get_modules_name(attr, expect, create_modules):
    """Test resource plugin to get all model names under a package"""
    res = ResourcePlugin(**attr)
    assert expect == res.get_modules(modules_path)


@pytest.mark.parametrize(
    "attr, expect",
    [
        (
            {
                "type": ResourcePluginType.LOCAL,
                "prefix": "/tmp/",
            },
            "Local",
        )
    ],
)
def test_resources_import_modules(attr, expect):
    """Test resource plug-in to import model"""
    res_plugin = ResourcePlugin(**attr)
    script_name = "local.py"
    script_path = "/home/chenrj/gitrep/dolphinscheduler/dolphinscheduler-python/pydolphinscheduler/src/pydolphinscheduler/resources_plugin/local.py"
    res = res_plugin.import_module(script_name, script_path)
    assert expect == res.__class__.__name__


@pytest.mark.parametrize(
    "attr, expect",
    [
        (
            {
                "type": ResourcePluginType.LOCAL,
                "prefix": "/tmp/",
            },
            "Local",
        )
    ],
)
def test_resources_resources(attr, expect):
    """Test the factory mode of the resource plugin, and return the corresponding plugin according to the plugin type"""
    res_plugin = ResourcePlugin(**attr)
    res = res_plugin.resource
    assert expect == res.__class__.__name__


@pytest.mark.parametrize(
    "attr",
    [
        {
            "type": "a",
            "prefix": "/tmp/",
        }
    ],
)
def test_resources_unsupported_res(attr):
    """Test unsupported plug-ins"""
    with pytest.raises(
        ValueError, match="{} type is not supported".format(attr.get("type"))
    ):
        res_plugin = ResourcePlugin(**attr)
        res_plugin.resource






