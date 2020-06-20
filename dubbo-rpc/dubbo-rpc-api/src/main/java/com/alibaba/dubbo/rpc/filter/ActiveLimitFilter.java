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
package com.alibaba.dubbo.rpc.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcStatus;
/**
 * 服务消费者每服务每方法最大并发调用数,超过则使用wait进行阻塞。只能用到消费者端
 * LimitInvokerFilter
 */
@Activate(group = Constants.CONSUMER, value = Constants.ACTIVES_KEY)
public class ActiveLimitFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        URL url = invoker.getUrl();
        String methodName = invocation.getMethodName();
        int max = invoker.getUrl().getMethodParameter(methodName, Constants.ACTIVES_KEY, 0); // 设置最高并发数,可针对服务配置,也可针对服务的方法进行单独配置
        RpcStatus count = RpcStatus.getStatus(invoker.getUrl(), invocation.getMethodName()); // 从缓存(ConcurrentMap)中获取方法对应的连接数信息
        if (max > 0) {
            long timeout = invoker.getUrl().getMethodParameter(invocation.getMethodName(), Constants.TIMEOUT_KEY, 0); // 配置的超时时间
            long start = System.currentTimeMillis(); // 线程开始等待时间,用于计算超时
            long remain = timeout;
            int active = count.getActive(); // 正在执行的并发数量
            if (active >= max) {
                synchronized (count) {
                    while ((active = count.getActive()) >= max) { // 当前并发执行的数量 大于等于 配置的最大并发数
                        try { // 则使当前线程wait,并设置等待时间,最多等待timeout时间
                            count.wait(remain);
                        } catch (InterruptedException e) {
                        } // 当前线程被唤醒之后,判断是否超时(当前时间-当前线程等待之前的时间)。超时则抛出异常
                        long elapsed = System.currentTimeMillis() - start;
                        remain = timeout - elapsed; // 当前线程被唤醒后,如果未抢到执行权限(其他线程首先处理count.getActive值),则接着等待(等待时间为[超时时间-已经等待的时间])
                        if (remain <= 0) {
                            throw new RpcException("Waiting concurrent invoke timeout in client-side for service:  "
                                    + invoker.getInterface().getName() + ", method: "
                                    + invocation.getMethodName() + ", elapsed: " + elapsed
                                    + ", timeout: " + timeout + ". concurrent invokes: " + active
                                    + ". max concurrent invoke limit: " + max);
                        }
                    }
                }
            }
        }
        try {
            long begin = System.currentTimeMillis();
            RpcStatus.beginCount(url, methodName); // 记录状态
            try { // 执行调用链
                Result result = invoker.invoke(invocation);
                RpcStatus.endCount(url, methodName, System.currentTimeMillis() - begin, true);
                return result;
            } catch (RuntimeException t) {
                RpcStatus.endCount(url, methodName, System.currentTimeMillis() - begin, false);
                throw t;
            }
        } finally {
            if (max > 0) {
                synchronized (count) {
                    count.notify(); // 唤醒等待的线程
                }
            }
        }
    }

}
