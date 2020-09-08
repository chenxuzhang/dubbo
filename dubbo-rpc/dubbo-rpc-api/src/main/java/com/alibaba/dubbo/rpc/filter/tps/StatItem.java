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
package com.alibaba.dubbo.rpc.filter.tps;

import java.util.concurrent.atomic.AtomicInteger;

class StatItem {

    private String name;
    // 重置时间
    private long lastResetTime;
    // 监控多长时间的tps
    private long interval;

    private AtomicInteger token;
    // tps
    private int rate;
    // name:服务接口名+分组+版本号。rate:tps上限。interval:单位时间内,不能超过tps上限
    StatItem(String name, int rate, long interval) {
        this.name = name;
        this.rate = rate;
        this.interval = interval;
        this.lastResetTime = System.currentTimeMillis();
        this.token = new AtomicInteger(rate);
    }

    public boolean isAllowable() {
        long now = System.currentTimeMillis();
        if (now > lastResetTime + interval) { // 判断当前时间是否超过tps单位时间。true:超过
            token.set(rate); // 重置tps上限量,单位时间内没处理一个请求,token则会减一
            lastResetTime = now; // 更新单位时间
        } // 问题:单位时间结束到开始。如果请求堆积到这两个时间点,会造成成倍的请求。解决办法是"滑动窗口"
        // 轮询 + 原子操作 保证了值修改的原子性
        int value = token.get();
        boolean flag = false;
        while (value > 0 && !flag) {
            flag = token.compareAndSet(value, value - 1);
            value = token.get();
        }

        return flag;
    }

    long getLastResetTime() {
        return lastResetTime;
    }

    int getToken() {
        return token.get();
    }

    @Override
    public String toString() {
        return new StringBuilder(32).append("StatItem ")
                .append("[name=").append(name).append(", ")
                .append("rate = ").append(rate).append(", ")
                .append("interval = ").append(interval).append("]")
                .toString();
    }

}
