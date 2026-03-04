## DolphinScheduler 작업 SPI 확장

#### 작업 플러그인은 어떻게 개발하나요?

org.apache.dolphinscheduler.spi.task.TaskChannel

플러그인은 위의 인터페이스를 구현할 수 있습니다.주로 작업 생성(작업 초기화, 작업 실행 등) 및 작업 취소가 포함됩니다.Yarn 작업인 경우 org.apache.dolphinscheduler.plugin.task.api.AbstractYarnTask를 구현해야 합니다.

Dolphinscheduler-task-api 모듈에서는 모든 작업에 대한 외부 접근을 위한 API를 제공하고 있으며, Dolphinscheduler-spi 모듈은 알람 모듈, 레지스트리 모듈 등 모든 플러그인 모듈을 정의하는 spi 일반 코드 라이브러리로 자세히 읽고 볼 수 있습니다.

또한 `TaskChannelFactory`는 `PrioritySPI`에서 확장됩니다. 즉, 플러그인 우선순위를 설정할 수 있으며, 이름이 같은 두 플러그인이 있는 경우 `getIdentify` 메서드를 재정의하여 우선순위를 맞춤설정할 수 있습니다.우선순위가 높은 플러그인이 로드되지만 이름과 우선순위가 같은 두 개의 플러그인이 있는 경우 플러그인을 로드할 때 서버에서 'IllegalArgumentException'이 발생합니다.

*공지*

작업 플러그인에는 프런트엔드 페이지가 포함되어 있어 아직 프런트엔드 SPI가 구현되지 않았으므로 해당 플러그인에 해당하는 프런트엔드 페이지를 별도로 구현해야 합니다.

작업 플러그인에 클래스 충돌이 있는 경우 [Shade-Relocating Classes](https://maven.apache.org/plugins/maven-shade-plugin/)를 사용하여 이 문제를 해결할 수 있습니다.