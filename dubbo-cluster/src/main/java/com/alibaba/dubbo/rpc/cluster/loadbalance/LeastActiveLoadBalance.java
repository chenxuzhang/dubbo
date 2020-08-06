/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.rpc.cluster.loadbalance;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcStatus;

import java.util.List;
import java.util.Random;

/**
 * LeastActiveLoadBalance
 *
 */
public class LeastActiveLoadBalance extends AbstractLoadBalance {

    public static final String NAME = "leastactive";

    private final Random random = new Random();

    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        int length = invokers.size(); // Number of invokers
        int leastActive = -1; // The least active value of all invokers // 最小活跃数
        int leastCount = 0; // The number of invokers having the same least active value (leastActive) // 相同最小活跃数的Invoker数量
        int[] leastIndexs = new int[length]; // The index of invokers having the same least active value (leastActive) // 记录相同最小活跃数的Invoker下标
        int totalWeight = 0; // The sum of with warmup weights // 总权重
        int firstWeight = 0; // Initial value, used for comparision // 最小活跃数第一个Invoker的权重,用于比对不同的Invoker是否有不同的权重
        boolean sameWeight = true; // Every invoker has the same weight value?
        for (int i = 0; i < length; i++) {
            Invoker<T> invoker = invokers.get(i); // RpcStatus获取某个服务提供者的某个方法的调用的活跃数(正在执行的数量,方法执行前+1,方法执行完毕-1)
            int active = RpcStatus.getStatus(invoker.getUrl(), invocation.getMethodName()).getActive(); // Active number
            int afterWarmup = getWeight(invoker, invocation); // Weight // 权重(含预热情况)
            if (leastActive == -1 || active < leastActive) { // 最小活跃数为-1 或者 比较Invoker的最小活跃数
                leastActive = active; // Record the current least active value // 重新记录最小活跃数
                leastCount = 1; // Reset leastCount, count again based on current leastCount // 重新计量最小活跃数Invoker数量
                leastIndexs[0] = i; // Reset // 保存Invoker下标
                totalWeight = afterWarmup; // Reset
                firstWeight = afterWarmup; // Record the weight the first invoker
                sameWeight = true; // Reset, every invoker has the same weight value?
            } else if (active == leastActive) { // If current invoker's active value equals with leaseActive, then accumulating.
                leastIndexs[leastCount++] = i; // Record index number of this invoker // 更新最小活跃数Invoker数量 和 保存Invoker下标
                totalWeight += afterWarmup; // Add this invoker's weight to totalWeight. // 统计总权重
                // If every invoker has the same weight?
                if (sameWeight && i > 0 // 是否有不同权重的Invoker
                        && afterWarmup != firstWeight) {
                    sameWeight = false;
                }
            }
        }
        // assert(leastCount > 0)
        if (leastCount == 1) { // 最小活跃数Invoker只有一个
            // If we got exactly one invoker having the least active value, return this invoker directly.
            return invokers.get(leastIndexs[0]);
        } // 当Invoker集合中,出现了不同的权重。通过totalWeight获取随机权重值,通过遍历最小活跃数Invoker集合,做减法,取权重值小于等于0的Invoker
        if (!sameWeight && totalWeight > 0) {
            // If (not every invoker has the same weight & at least one invoker's weight>0), select randomly based on totalWeight.
            int offsetWeight = random.nextInt(totalWeight) + 1;
            // Return a invoker based on the random value.
            for (int i = 0; i < leastCount; i++) {
                int leastIndex = leastIndexs[i];
                offsetWeight -= getWeight(invokers.get(leastIndex), invocation);
                if (offsetWeight <= 0)
                    return invokers.get(leastIndex);
            }
        } // 没有出现不同的权重,则根据相同最小活跃数的数量做随机,然后取一个Invoker
        // If all invokers have the same weight value or totalWeight=0, return evenly.
        return invokers.get(leastIndexs[random.nextInt(leastCount)]);
    }
}
