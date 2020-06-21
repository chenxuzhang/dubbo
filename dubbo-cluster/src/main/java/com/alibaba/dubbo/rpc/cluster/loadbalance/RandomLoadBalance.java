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

import java.util.List;
import java.util.Random;

/**
 * random load balance.
 * 随机负载均衡策略
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    public static final String NAME = "random";

    private final Random random = new Random();

    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        int length = invokers.size(); // Number of invokers
        int totalWeight = 0; // The sum of weights
        boolean sameWeight = true; // Every invoker has the same weight? // true:所有提供者权重相同,false:所有提供者的权重有不相同的
        for (int i = 0; i < length; i++) {
            int weight = getWeight(invokers.get(i), invocation); // 计算每个服务提供者的权重
            totalWeight += weight; // Sum
            if (sameWeight && i > 0
                    && weight != getWeight(invokers.get(i - 1), invocation)) {
                sameWeight = false;
            }
        } // 所有提供者中,有权重不同的情况,需要通过计算权重来获取Invoker
        if (totalWeight > 0 && !sameWeight) {
            // If (not every invoker has the same weight & at least one invoker's weight>0), select randomly based on totalWeight.
            int offset = random.nextInt(totalWeight); // 通过权重总和来随机其中的数值
            // Return a invoker based on the random value.
            for (int i = 0; i < length; i++) { // 随机后的值,同每个Invoker权重做减法操作,小于0表示该下标对应的Invoker命中
                offset -= getWeight(invokers.get(i), invocation); // TODO 预热的服务节点,随着时间的增长,权重会越来越大,命中的概率会越来越高
                if (offset < 0) {
                    return invokers.get(i);
                }
            }
        } // 1、所有服务端节点权重相同。2、所有服务节点权重数值都为0  这两种情况 会通过服务提供者数量进行随机
        // If all invokers have the same weight value or totalWeight=0, return evenly.
        return invokers.get(random.nextInt(length));
    }

}
