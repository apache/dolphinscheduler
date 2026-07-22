# Load Balancing

Load balancing distributes server pressure reasonably through routing algorithms (typically in cluster environments) to optimize server performance to the maximum extent.

## DolphinScheduler-Worker Load Balancing Algorithms

DolphinScheduler-Master provides four load balancing algorithms for distributing tasks to workers:

- **Random** (RANDOM)
- **Round Robin** (ROUND_ROBIN)
- **Smooth Round Robin** (FIXED_WEIGHTED_ROUND_ROBIN)
- **Dynamic Smooth Round Robin** (DYNAMIC_WEIGHTED_ROUND_ROBIN) - Default algorithm

## Load Balancing Configuration

Configure the load balancing algorithm in the configuration file:

Location: `master-server/conf/application.yaml`

```yaml
worker-load-balancer-configuration-properties:
  # types: RANDOM, ROUND_ROBIN, FIXED_WEIGHTED_ROUND_ROBIN, DYNAMIC_WEIGHTED_ROUND_ROBIN
  type: DYNAMIC_WEIGHTED_ROUND_ROBIN
```

## Worker Weight Configuration

### Smooth Round Robin Configuration (FIXED_WEIGHTED_ROUND_ROBIN)

For the `FIXED_WEIGHTED_ROUND_ROBIN` algorithm, you can modify the fixed weight in each worker's configuration file:

Location: `worker-server/conf/application.yaml`

```yaml
worker:
  host-weight: 100 #default value is 100
```

### Dynamic Smooth Round Robin Configuration (DYNAMIC_WEIGHTED_ROUND_ROBIN)

When using the `DYNAMIC_WEIGHTED_ROUND_ROBIN` algorithm, you can configure the weights for various metrics:

```yaml
master:
  worker-load-balancer-configuration-properties:
    type: DYNAMIC_WEIGHTED_ROUND_ROBIN
    # Dynamic weight configuration, only used for DYNAMIC_WEIGHTED_ROUND_ROBIN algorithm
    # The sum of memory-usage, cpu-usage, task-thread-pool-usage weights must be 100
    dynamic-weight-config-properties:
      memory-usage-weight: 30    # Memory usage weight
      cpu-usage-weight: 30       # CPU usage weight  
      task-thread-pool-usage-weight: 40  # Task thread pool usage weight
```

## Load Balancing Algorithm Details

### Random (RANDOM)

Randomly selects one available worker node to execute tasks.

### Round Robin (ROUND_ROBIN)

Selects worker nodes in a fixed order to ensure each worker receives tasks evenly.

### Smooth Round Robin (FIXED_WEIGHTED_ROUND_ROBIN)

Each worker has two weights: weight (remains constant after warm-up) and current_weight (dynamically changes). During each routing, all workers are traversed, and their current_weight is increased by their weight. The total weight of all workers is accumulated as total_weight. The worker with the highest current_weight is selected to execute the task, and then that worker's current_weight is decreased by total_weight.

- Example: For instance, with 3 workers (A, B, C) having weights of 1, 2, and 3 respectively
- Worker selection order will be: C B C A B C C B C A B C C B C A B C C B C A B C C B C A B C ... (In this 30-round scheduling example, the number of tasks allocated to each worker is: C:15, B:10, A:5, exactly matching the weight ratio)

### Dynamic Smooth Round Robin (DYNAMIC_WEIGHTED_ROUND_ROBIN) - Default Algorithm

This algorithm reports its own load information to the registry at regular intervals. We primarily evaluate based on CPU usage, memory usage, and worker thread pool usage, with specific weight configurations as follows:
- **Memory Usage** (Default weight: 30%)
- **CPU Usage** (Default weight: 30%)
- **Task Thread Pool Usage** (Default weight: 40%)

**Weight Calculation Principle:**
The dynamic weight of each worker is calculated using the following formula:

```
Weight = 100 - (CPU Weight × CPU Usage + Memory Weight × Memory Usage + Thread Pool Weight × Thread Pool Usage) ÷ 3
```

Therefore, when a worker's load is lower, its weight will be higher, and the system will prioritize selecting worker nodes with lower loads to execute tasks.

In the final worker node selection process, the workflow is consistent with smooth round robin, with the only difference being that in this algorithm, worker weights change dynamically.
Through this dynamic smooth round robin algorithm, DolphinScheduler can intelligently distribute tasks to workers with the lowest loads, achieving true dynamic load balancing.
