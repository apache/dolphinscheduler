from pathlib import Path
from unittest.mock import patch, PropertyMock

import pytest

from pydolphinscheduler.constants import ResourcePluginType
from pydolphinscheduler.core import Task
from pydolphinscheduler.exceptions import PyResPluginException
from pydolphinscheduler.resources_plugin import ResourcePlugin
from pydolphinscheduler.resources_plugin.local import Local
from pydolphinscheduler.utils import file
from tests.testing.file import delete_file

file_name = "local_res.sh"
file_content = "echo Test local res plugin"
res_plugin_prefix = Path(__file__).parent
file_path = res_plugin_prefix.joinpath(file_name)


@pytest.fixture()
def setup_crt_first():
    """Set up and teardown about create file first and then delete it."""
    file.write(content=file_content, to_path=file_path)
    yield
    delete_file(file_path)


@pytest.mark.parametrize(
    "val, expected",
    [
        (file_name, file_content),
    ],
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
@patch(
    "pydolphinscheduler.core.task.Task.ext",
    new_callable=PropertyMock,
    return_value={".sh", },
)
@patch(
    "pydolphinscheduler.core.task.Task.ext_attr",
    new_callable=PropertyMock,
    return_value="_raw_script",
)
@patch(
    "pydolphinscheduler.core.task.Task._raw_script",
    create=True,
    new_callable=PropertyMock,
)
def test_task_obtain_res_plugin(m_raw_script, m_ext_attr, m_ext, m_code_version, val, expected, setup_crt_first):
    """Test task obtaining resource plug-in."""
    m_raw_script.return_value = val
    task = Task(
        name="test_task_ext_attr",
        task_type=ResourcePluginType.LOCAL,
        resource_plugin=ResourcePlugin(
            type=ResourcePluginType.LOCAL,
            prefix=str(res_plugin_prefix),
        )
    )
    assert expected == getattr(task, "raw_script")


@pytest.mark.parametrize(
    "attr, expected",
    [
        (
            {
                "prefix": res_plugin_prefix,
                "file_name": file_name
            },
            file_content
        )
    ],
)
def test_local_res_read_file(attr, expected, setup_crt_first):
    """Test the read_file function of the local resource plug-in"""
    local = Local(str(attr.get("prefix")))
    local.read_file(attr.get("file_name"))
    assert expected == local.read_file(file_name)


@pytest.mark.parametrize(
    "attr",
    [
        {
            "prefix": res_plugin_prefix,
            "file_name": file_name
        },
    ],
)
def test_local_res_file_not_found(attr):
    """test local resource plugin file does not exist"""
    with pytest.raises(
        PyResPluginException,
        match="{} is not found".format(Path(attr.get("prefix")).joinpath(attr.get("file_name")))
    ):
        local = Local(str(attr.get("prefix")))
        local.read_file(attr.get("file_name"))
