package Ray.spark.graphX

/**
  * Created by Ray on 2017/5/31.
  * Describe Pregel.
  */
object PregelDescription {

  /**
    * 在迭代计算中,释放内存是必要的,在新图产生后,需要快速将旧图彻底释放掉,否则,十几轮迭代后,会有内存泄漏问题,
    * 很快耗光作业缓存空间.但是直接使用 cache unpersist 和checkpoint 等API,非常需要使用技巧.
    * 所以 Spark 官方文档建议:对于迭代计算,建议使用 Pregel API,它能够正确的释放中间结果,这样就不需要自己费心去操作了.
    *
    * 图是天然的迭代数据结构,顶点的属性值依赖于邻居的属性值,而邻居们的属性值同样也依赖于他们各自邻居属性值(即邻居的邻居),
    * 许多重要的图算法迭代式的重新计算顶点的属性直到达到预设的迭代条件.这些迭代的图算法被抽象成一系列图并行操作.
    *
    * 在计算机科学中,柯里化(Currying)是把接受多个参数的函数变换成接受一个单一参数(最初函数的第一个参数)的函数,
    * 并且返回 接受余下的参数运算且返回结果 的新函数的技术.
    *
    * Scala 里的 Currying化可以把函数从接收多个参数转换成多个参数列表.
    *
    * Pregel有两个参数列表
    * 第一个参数列表:
    *     graph: Graph[VD, ED]		图数据,VD 表示 顶点 数据类型,ED 表示 边 数据类型
    *     initialMsg: A				    初始化消息值,A是数据类型
    *     maxIterations: Int = Int.MaxValue		                    最大迭代计算的次数
    *     activeDirection: EdgeDirection = EdgeDirection.Either		活跃化方向(指出参与迭代运算的主要数据,一种优化策略)
    *
    * 第二个参数列表:
    *     vprog: (VertexId, VD, A) => VD		对接收到消息的图数据(主要指顶点数据)进行处理的函数,返回处理后的顶点数据
    *     sendMsg: EdgeTriplet[VD, ED] => Iterator[(VertexId, A)]		发送消息函数
    *     mergeMsg: (A, A) => A		消息合并函数
    *
    * Pregel 计算过程:
    *    0. 初始化,迭代计前,为每一个图数据(顶点)设置(不是发送)第一个消息 initialMsg,构建迭代使用的原始图数据 g.
    *    1. 对全部的 原始图数据 g 使用 sendMsg 函数后,再对其产生的结果 使用 mergeMsg 函数,
    *       并使用其返回值构建一个新的顶点集 messages.
    *    2. 开始迭代计算,停止条件: 不在有新的消息发送 且 迭代次数小于 最大迭代次数.
    *       0). 使用 messages 对 原始图数据 g 进行更新(joinVertices 操作),更新函数使用 vprog 函数.
    *       1). 对 更新以后的全部原始图数据 g 使用 sendMsg 函后,再对其产生的结果使用 mergeMsg 函数,
    *           并使用其返回值构建一个新的顶点集 messages(更新了上一步产生的 messages).
    *           虽然是全部的原始图数据操作,但实际参与运算的数据只有 messages 在 g 中对应的顶点数据,
    *           并使用 activeDirection 对顶点数据中的属性进行了筛选.
    *       2). 释放旧的 messages 和 g 所占用的内存.
    *    3. 迭代计算完成,返回最新的 g.
    *
    * 总体来看,GraphX 中的 Pregel 操作符是一种应用在拓扑图中的整体的、同步的、并行的消息计算模型.
    * Pregel 操作符是在一系列的超步(super step)里执行的,顶点接收来自上一个超步里的消息汇总,计算出一个新的顶点属性值,
    * 然后发送消息到下一个超步中的邻居顶点.与 Pregel 不同,消息的并行计算已经被封装成了 EdgeTriplet 的一个函数,
    * 计算过程中可以访问源顶点和目标顶点的属性.如果一个顶点在超步里不接收消息,这个顶点将不参与计算.
    * 在所有的消息都处理完成后,Pregel 操作符会结束迭代并返回一个新图.
    *
    * 与标准的 Pregel 实现相比,GraphX 中的顶点只能给邻居顶点发送消息,消息是使用用户定义的消息函数来构建的.
    * 有了这些限制,GraphX 就可以对 Pregel API 做进一步的优化.
    *
    * Pregel 用到的主要优化:
    *   0. Caching for Iterative mrTriplets & Incremental Updates for Iterative mrTriplets:
    *      在很多图分析算法中,不同点的收敛速度变化很大.在迭代后期,只有很少的点会有更新.因此,对于没有更新的点,
    *      下一次 mapReduceTriplets 计算时,EdgeRDD 无需更新相应点值的本地缓存,大幅降低了通信开销.
    *   1. Indexing Active Edges:
    *      没有更新的顶点在下一轮迭代时不需要向邻居重新发送消息.因此,mapReduceTriplets 遍历边时,
    *      如果一条边的邻居点值在上一轮迭代时没有更新,则直接跳过,避免了大量无用的计算和通信.
    *   2. Join Elimination:
    *      Triplet 是由一条边和其两个邻居点组成的三元组,操作 Triplet 的 map 函数常常只需访问其两个邻居点值中的一个.
    *      例如,在 PageRank 计算中,一个点值的更新只与其源顶点的值有关,而与其所指向的目的顶点的值无关,
    *      那么在 mapReduceTriplets 计算中,就不需要目的顶点的数据.
    */

}
