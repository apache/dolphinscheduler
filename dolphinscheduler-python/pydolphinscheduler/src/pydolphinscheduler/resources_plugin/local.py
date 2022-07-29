import os.path


class Local:

    def __init__(self, prefix: str):
        self._prefix = prefix

    @property
    def prefix(self):
        return self._prefix

    def read_file(self, suf: str):
        try:
            f = open(self._prefix + suf, 'r')
            content = f.read()
            f.close()
        except FileNotFoundError:
            print("{} is not found.".format(self.prefix + suf))
        except PermissionError:
            print("You don't have permission to access {}.".format(self.prefix + suf))
        return content


