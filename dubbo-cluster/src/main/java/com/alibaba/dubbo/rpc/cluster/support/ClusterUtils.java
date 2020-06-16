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
package com.alibaba.dubbo.rpc.cluster.support;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;

import java.util.HashMap;
import java.util.Map;

/**
 * ClusterUtils
 * URL参数合并
 */
public class ClusterUtils {

    private ClusterUtils() {
    }

    public static URL mergeUrl(URL remoteUrl, Map<String, String> localMap) {
        Map<String, String> map = new HashMap<String, String>();
        Map<String, String> remoteMap = remoteUrl.getParameters();


        if (remoteMap != null && remoteMap.size() > 0) {
            map.putAll(remoteMap);
            // 服务提供者的URL参数,选择性删除。有些设置由服务提供者影响,不应该由消费者影响
            // Remove configurations from provider, some items should be affected by provider.
            map.remove(Constants.THREAD_NAME_KEY); // threadname 线程名称
            map.remove(Constants.DEFAULT_KEY_PREFIX + Constants.THREAD_NAME_KEY); // default.threadname

            map.remove(Constants.THREADPOOL_KEY); // threadpool 线程池类型
            map.remove(Constants.DEFAULT_KEY_PREFIX + Constants.THREADPOOL_KEY); // default.threadpool

            map.remove(Constants.CORE_THREADS_KEY); // corethreads 核心线程数
            map.remove(Constants.DEFAULT_KEY_PREFIX + Constants.CORE_THREADS_KEY); // default.corethreads

            map.remove(Constants.THREADS_KEY); // threads io线程池大小
            map.remove(Constants.DEFAULT_KEY_PREFIX + Constants.THREADS_KEY); // default.threads

            map.remove(Constants.QUEUES_KEY); // queues 线程池队列大小
            map.remove(Constants.DEFAULT_KEY_PREFIX + Constants.QUEUES_KEY); // default.queues

            map.remove(Constants.ALIVE_KEY); // alive
            map.remove(Constants.DEFAULT_KEY_PREFIX + Constants.ALIVE_KEY); // default.alive

            map.remove(Constants.TRANSPORTER_KEY); // transporter 协议类型,默认为netty
            map.remove(Constants.DEFAULT_KEY_PREFIX + Constants.TRANSPORTER_KEY); // default.transporter
        }
        // 相同的参数,本地覆盖远程的。本地设置的参数优先
        if (localMap != null && localMap.size() > 0) {
            map.putAll(localMap);
        }
        if (remoteMap != null && remoteMap.size() > 0) {
            // 使用从提供程序端传递的版本
            String dubbo = remoteMap.get(Constants.DUBBO_VERSION_KEY);
            if (dubbo != null && dubbo.length() > 0) {
                map.put(Constants.DUBBO_VERSION_KEY, dubbo);
            }
            String version = remoteMap.get(Constants.VERSION_KEY);
            if (version != null && version.length() > 0) {
                map.put(Constants.VERSION_KEY, version);
            }
            String group = remoteMap.get(Constants.GROUP_KEY);
            if (group != null && group.length() > 0) {
                map.put(Constants.GROUP_KEY, group);
            }
            String methods = remoteMap.get(Constants.METHODS_KEY);
            if (methods != null && methods.length() > 0) {
                map.put(Constants.METHODS_KEY, methods);
            }
            // 保留提供程序url的时间戳
            String remoteTimestamp = remoteMap.get(Constants.TIMESTAMP_KEY);
            if (remoteTimestamp != null && remoteTimestamp.length() > 0) {
                map.put(Constants.REMOTE_TIMESTAMP_KEY, remoteMap.get(Constants.TIMESTAMP_KEY));
            }
            // Combine filters and listeners on Provider and Consumer
            String remoteFilter = remoteMap.get(Constants.REFERENCE_FILTER_KEY);
            String localFilter = localMap.get(Constants.REFERENCE_FILTER_KEY);
            if (remoteFilter != null && remoteFilter.length() > 0
                    && localFilter != null && localFilter.length() > 0) {
                localMap.put(Constants.REFERENCE_FILTER_KEY, remoteFilter + "," + localFilter);
            }
            String remoteListener = remoteMap.get(Constants.INVOKER_LISTENER_KEY);
            String localListener = localMap.get(Constants.INVOKER_LISTENER_KEY);
            if (remoteListener != null && remoteListener.length() > 0
                    && localListener != null && localListener.length() > 0) {
                localMap.put(Constants.INVOKER_LISTENER_KEY, remoteListener + "," + localListener);
            }
        }

        return remoteUrl.clearParameters().addParameters(map);
    }

}