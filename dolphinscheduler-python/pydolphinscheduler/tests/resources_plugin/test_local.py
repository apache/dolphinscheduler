import os

import pytest

from pydolphinscheduler.constants import ResourcePluginType
from pydolphinscheduler.resources_plugin import ResourcePlugin
from unittest.mock import patch
from pydolphinscheduler.tasks.shell import Shell
from pydolphinscheduler.utils import file
from tests.testing.file import delete_file

file_path = 'local_res.sh'
file_content = "echo \"test res_local\""
# res_plugin_prefix = Path(__file__).absolute().parent
pwd = os.path.abspath(__file__)
res_plugin_prefix = os.path.abspath(os.path.dirname(pwd) + os.path.sep + ".") + "/"

@pytest.fixture
def setup_crt_first():
    """Set up and teardown about create file first and then delete it."""
    file.write(content=file_content, to_path=file_path)
    yield
    delete_file(file_path)

@pytest.mark.parametrize(
    "attr, expect",
    [
        (
            {
                "name": "test-local-res-command-content",
                "command": file_path,
                "resource_plugin": ResourcePlugin(
                    type=ResourcePluginType.LOCAL,
                    prefix=res_plugin_prefix,
                )
            },
            file_content
        )
    ],
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
def test_resources_local_shell_command_content(mock_code_version, attr, expect, setup_crt_first):
    """Test task shell task command content."""
    task = Shell(**attr)
    assert expect == getattr(task, "raw_script")







