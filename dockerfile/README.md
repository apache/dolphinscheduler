## Build Image
```
  cd ..
  docker build -t dolphinscheduler --build-arg version=1.1.0 --build-arg tar_version=1.1.0-SNAPSHOT -f dockerfile/Dockerfile .
  docker run -p 12345:12345 -p 8888:8888 --rm --name dolphinscheduler -d dolphinscheduler
```
* Visit the url: http://127.0.0.1:8888
* UserName:admin Password:dolphinscheduler123

## Note
* MacOS: The memory of docker needs to be set to 4G, default 2G. Steps: Preferences -> Advanced -> adjust resources -> Apply & Restart
