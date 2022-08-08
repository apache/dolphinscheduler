import os
from pathlib import Path
from pydolphinscheduler.exceptions import PyResPluginException


# [start local_res_definition]
class Local:

    def __init__(self, prefix: str):
        self._prefix = prefix

    @property
    def prefix(self):
        """Get the _prefix attribute"""
        return self._prefix

    def read_file(self, suf: str):
        """Get the content of the file, the address of the file is
        the prefix of the resource plugin plus the parameter suf """
        path = Path(self.prefix).joinpath(suf)
        if not path.exists():
            raise PyResPluginException("{} is not found".format(str(path)))
        if not os.access(str(path), os.R_OK):
            raise PyResPluginException("You don't have permission to access {}".format(self.prefix + suf))
        with open(path, 'r') as f:
            content = f.read()
        return content
# [end local_res_definition]

