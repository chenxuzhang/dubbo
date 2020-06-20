    package com.alibaba.dubbo.cache;
    
    import com.alibaba.dubbo.cache.Cache;
    import com.alibaba.dubbo.cache.CacheFactory;
    import com.alibaba.dubbo.common.URL;
    import com.alibaba.dubbo.common.extension.ExtensionLoader;
    import com.alibaba.dubbo.rpc.Invocation;
    // CacheFactory代理类
    public class CacheFactory$Adaptive
    implements CacheFactory {
        @Override
        public Cache getCache(URL uRL, Invocation invocation) {
            if (uRL == null) {
                throw new IllegalArgumentException("url == null");
            }
            URL uRL2 = uRL;
            if (invocation == null) {
                throw new IllegalArgumentException("invocation == null");
            }
            String string = invocation.getMethodName();
            // 默认lru策略
            // 通过方法名+cache获取配置的缓存策略
            // 方法名.cache(方法级别)、cache(服务类级别)、default.cache(默认) 这三种获取方式
            String string2 = uRL2.getMethodParameter(string, "cache", "lru");
            if (string2 == null) {
                throw new IllegalStateException(new StringBuffer().append("Fail to get extension(com.alibaba.dubbo.cache.CacheFactory) name from url(").append(uRL2.toString()).append(") use keys([cache])").toString());
            }
            // 通过SPI方式获取缓存策略实现类
            CacheFactory cacheFactory = ExtensionLoader.getExtensionLoader(CacheFactory.class).getExtension(string2);
            return cacheFactory.getCache(uRL, invocation);
        }
    }
