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
package com.alibaba.dubbo.cache.filter;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.cache.CacheFactory;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;

/**
 * 缓存过滤器
 */
@Activate(group = {Constants.CONSUMER, Constants.PROVIDER}, value = Constants.CACHE_KEY)
public class CacheFilter implements Filter {
    // 缓存工厂接口,默认lru:最少使用
    private CacheFactory cacheFactory; // 类名:CacheFactory$Adaptive 实现CacheFactory,由动态生成,实现类代码可参考dubbo-filter/dubbo-filter-cache/src/main/resources/CacheFactoryProxy.md
    // 通过SPI进行依赖注入(ExtensionLoader-->injectExtension)。支持SPI方式,也支持Spring方式
    public void setCacheFactory(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory; // 此处注入的是SPI的自适应拓展代理类,代理类通过URL中设置的参数来获取具体的缓存规则类
    }
    // 是否启用缓存,可针对服务具体方法、或者具体服务
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (cacheFactory != null && ConfigUtils.isNotEmpty(invoker.getUrl().getMethodParameter(invocation.getMethodName(), Constants.CACHE_KEY))) {
            Cache cache = cacheFactory.getCache(invoker.getUrl(), invocation); // 通过URL Parameters获取缓存的策略,未配置默认使用lru(最少使用)
            if (cache != null) {
                String key = StringUtils.toArgumentString(invocation.getArguments()); // 方法的参数作为缓存的key
                Object value = cache.get(key);
                if (value != null) {
                    return new RpcResult(value);
                } // 执行调用链
                Result result = invoker.invoke(invocation);
                if (!result.hasException() && result.getValue() != null) {
                    cache.put(key, result.getValue());
                }
                return result;
            }
        }
        return invoker.invoke(invocation);
    }

}
