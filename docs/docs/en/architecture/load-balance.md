# Load Balance

Load balancing refers to the reasonable allocation of server pressure through routing algorithms (usually in cluster environments) to achieve the maximum optimization of server performance.

## DolphinScheduler-Worker Load Balancing Algorithms

DolphinScheduler-Master allocates tasks to workers, and by default provides three algorithms:

- Weighted random (random)

- Smoothing polling (round-robin)

- Linear load (lower weight)

The default configuration is the linear load.

As the routing sets on the client side, the master service, you can change master.host.selector in master.properties to configure the algorithm.

e.g. master.host.selector=random (case-insensitive)

## Worker Load Balancing Configuration

The configuration file is worker.properties

### Weight

All the load algorithms above are weighted based on weights, which affect the routing outcome. You can set different weights for different machines by modifying the `worker.weight` value.

### Preheating

Consider JIT optimization, worker runs at low power for a period of time after startup, so that it can gradually reach its optimal state, a process we call preheating. If you are interested, you can read some articles about JIT.

So the worker gradually reaches its maximum weight with time after starts up ( by default ten minutes, there is no configuration about the pre-heating duration, it's recommend to submit a PR if have needs to change the duration).

## Load Balancing Algorithm in Details

### Random (Weighted)

This algorithm is relatively simple, select a worker by random (the weight affects its weighting).

### Smoothed Polling (Weighted)

An obvious drawback of the weighted polling algorithm, which is under special weights circumstance, weighted polling scheduling generates an imbalanced sequence of instances, and this unsmooth load may cause some instances to experience transient high loads, leading to a risk of system crash. To address this scheduling flaw, we provide a smooth weighted polling algorithm.

Each worker has two weights parameters, weight (which remains constant after warm-up is complete) and current_weight (which changes dynamically). For every route, calculate the current_weight + weight and is iterated over all the workers, the weight of all the workers sum up and count as total_weight, then the worker with the largest current_weight is selected as the worker for this task. By meantime, set worker's current_weight-total_weight.

### Linear Weighting (Default Algorithm)

This algorithm reports its own load information to the registry at regular intervals. Make decision on two main pieces of information:

- load average (default is the number of CPU cores * 2)
- available physical memory (default is 0.3, in G)

If either of these is lower than the configured item, then this worker will not participate in the load. (no traffic will be allocated)

You can customise the configuration by changing the following properties in worker.properties

- worker.max.cpuload.avg=-1 (worker max cpuload avg, only higher than the system cpu load average, worker server can be dispatched tasks. default value -1: the number of cpu cores * 2)
- worker.reserved.memory=0.3 (worker reserved memory, only lower than system available memory, worker server can be dispatched tasks. default value 0.3, the unit is G)

