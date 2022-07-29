
class Github:
    def __init__(self, path: str):
        self.path = path

    def __call__(self, func):
        def wrapper():
            print("github_resource __call__")
            r = func()
        return wrapper

def func():
    printf("hello")


if __name__ == "__main__":
    func()

