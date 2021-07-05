
# TaskGroupQueue
正常：
初始化：-1；
获取作业组资源：获取到作业组资源后-1->0
执行结束后：0->taskInstance.state

    -1：初始化
    0：获取到了作业组资源
    >0：已经释放作业组资源

容错：
初始化：被容错的master启动的task，这个时候taskInstance里面的值可能不是null，如果taskInstance是容错的那么状态只能是0;1;2;3。
这种情况需要获取到groupQueueID
*****获取TaskGroupQueue,如果状态是-1，返回id，如果是0，证明**已经获取了作业组资源，这个时候需要退回作业组资源，然后让他重新争取******


    之后的状态和正常的一样了
    执行结束后：由于更新为taskGroupQueue终结状态的state只能是在taskInstance执行成功之后更新，所以master在分发task后down机，没有拿到最终状态，另一个master起动执行该task
                ，会重新发一个task给worker，

方案和代码位置：
初始化：保存taskInstance的位置：保持数据一致，提前或延后可能或出现master宕机的情况，这个时候作业组的数据会有问题，要是之前保存，
taskInstance还没有持久化，但是taskGroupQueue已经持久化了，还争取了作业组资源，到时候master down了，没人能把资源还回去。还需要判断状态

    获取作业组资源：
            master在dispatch task的时候。那个时候会有很多状态和type的判断，type判断不动，状态的话：
                1.如果是finish的状态：判断taskGroupQueue的状态是不是0，如果是0就证明获取了资源，但是没有释放，这个时候task已经结束了，所以把资源释放掉。
                                      把taskGroupQueue的状态更新成taskInstance的状态
                2.如果是running状态：证明死掉的master，提交的该taskInstance已经获取到了作业组资源，要不然不可能是running，这个时候关于taskGroupQueue状态不用动，理论上应该是>-1的状态。
                然后啥都不做，原有代码会return；
    退还资源位置：