# 로드밸런싱

로드 밸런싱은 라우팅 알고리즘(일반적으로 클러스터 환경)을 통해 서버 압력을 합리적으로 분산하여 서버 성능을 최대한 최적화합니다.

## DolphinScheduler-Worker 로드 밸런싱 알고리즘

DolphinScheduler-Master는 작업자에게 작업을 분산하기 위한 4가지 로드 밸런싱 알고리즘을 제공합니다.

- **랜덤** (랜덤)
- **라운드 로빈**(ROUND_ROBIN)
- **부드러운 라운드 로빈**(FIXED_WEIGHTED_ROUND_ROBIN)
- **동적 부드러운 라운드 로빈**(DYNAMIC_WEIGHTED_ROUND_ROBIN) - 기본 알고리즘

## 로드 밸런싱 구성

구성 파일에서 부하 분산 알고리즘을 구성합니다.

위치: `master-server/conf/application.yaml````yaml
worker-load-balancer-configuration-properties:
  # types: RANDOM, ROUND_ROBIN, FIXED_WEIGHTED_ROUND_ROBIN, DYNAMIC_WEIGHTED_ROUND_ROBIN
  type: DYNAMIC_WEIGHTED_ROUND_ROBIN
````

## 작업자 체중 구성

### 부드러운 라운드 로빈 구성(FIXED_WEIGHTED_ROUND_ROBIN)

'FIXED_WEIGHTED_ROUND_ROBIN' 알고리즘의 경우 각 작업자의 구성 파일에서 고정 가중치를 수정할 수 있습니다.

위치: `worker-server/conf/application.yaml````yaml
worker:
  host-weight: 100 #default value is 100
````

### 동적 부드러운 라운드 로빈 구성(DYNAMIC_WEIGHTED_ROUND_ROBIN)

`DYNAMIC_WEIGHTED_ROUND_ROBIN` 알고리즘을 사용하면 다양한 측정항목에 대한 가중치를 구성할 수 있습니다.```yaml
master:
  worker-load-balancer-configuration-properties:
    type: DYNAMIC_WEIGHTED_ROUND_ROBIN
    # Dynamic weight configuration, only used for DYNAMIC_WEIGHTED_ROUND_ROBIN algorithm
    # The sum of memory-usage, cpu-usage, task-thread-pool-usage weights must be 100
    dynamic-weight-config-properties:
      memory-usage-weight: 30    # Memory usage weight
      cpu-usage-weight: 30       # CPU usage weight  
      task-thread-pool-usage-weight: 40  # Task thread pool usage weight
````

## 로드 밸런싱 알고리즘 세부정보

### 무작위(RANDOM)

작업을 실행하기 위해 사용 가능한 작업자 노드 하나를 무작위로 선택합니다.

### 라운드 로빈(ROUND_ROBIN)

각 작업자가 작업을 균등하게 받을 수 있도록 고정된 순서로 작업자 노드를 선택합니다.

### 부드러운 라운드 로빈(FIXED_WEIGHTED_ROUND_ROBIN)

각 워커에는 가중치(준비 후 일정하게 유지됨)와 현재 가중치(동적으로 변경됨)라는 두 가지 가중치가 있습니다.각 라우팅 중에 모든 작업자가 순회되고 해당 current_weight가 가중치만큼 증가합니다.모든 작업자의 총 가중치는 total_weight로 누적됩니다.current_weight가 가장 높은 작업자가 선택되어 작업을 실행하고 해당 작업자의 current_weight가 total_weight만큼 감소됩니다.

- 예: 예를 들어 3명의 작업자(A, B, C)가 각각 1, 2, 3의 가중치를 갖고 있다고 가정합니다.
- 작업자 선택 순서는 다음과 같습니다: C B C A B C C B C A B C C B C A B C C B C A B C C B C A B C ... (이 30라운드 스케줄링 예에서 각 작업자에게 할당된 작업 수는 C:15, B:10, A:5이며 정확히 가중치 비율과 일치합니다.)

### 동적 부드러운 라운드 로빈(DYNAMIC_WEIGHTED_ROUND_ROBIN) - 기본 알고리즘

이 알고리즘은 정기적으로 레지스트리에 자체 로드 정보를 보고합니다.우리는 주로 다음과 같은 특정 가중치 구성을 사용하여 CPU 사용량, 메모리 사용량 및 작업자 스레드 풀 사용량을 기준으로 평가합니다.
- **메모리 사용량** (기본 가중치: 30%)
- **CPU 사용량** (기본 가중치: 30%)
- **작업 스레드 풀 사용량** (기본 가중치: 40%)

**무게 계산 원리:**
각 작업자의 동적 가중치는 다음 공식을 사용하여 계산됩니다.```
Weight = 100 - (CPU Weight × CPU Usage + Memory Weight × Memory Usage + Thread Pool Weight × Thread Pool Usage) ÷ 3
````

따라서 워커의 부하가 낮을수록 가중치는 높아지며, 시스템은 부하가 낮은 워커 노드를 우선적으로 선택하여 작업을 실행하게 됩니다.

최종 작업자 노드 선택 프로세스에서 워크플로는 원활한 라운드 로빈과 일치하지만 이 알고리즘에서는 작업자 가중치가 동적으로 변경된다는 점만 다릅니다.
이 동적 원활한 라운드 로빈 알고리즘을 통해 DolphinScheduler는 부하가 가장 낮은 작업자에게 작업을 지능적으로 배포하여 진정한 동적 부하 분산을 달성할 수 있습니다.