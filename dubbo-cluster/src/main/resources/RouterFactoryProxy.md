    package com.alibaba.dubbo.rpc.cluster;
    
    import com.alibaba.dubbo.common.URL;
    import com.alibaba.dubbo.common.extension.ExtensionLoader;
    import com.alibaba.dubbo.rpc.cluster.Router;
    import com.alibaba.dubbo.rpc.cluster.RouterFactory;
    
    public class RouterFactory$Adaptive
    implements RouterFactory {
        @Override
        public Router getRouter(URL uRL) {
            if (uRL == null) {
                throw new IllegalArgumentException("url == null");
            }
            URL uRL2 = uRL;
            String string = uRL2.getProtocol();
            if (string == null) {
                throw new IllegalStateException(new StringBuffer().append("Fail to get extension(com.alibaba.dubbo.rpc.cluster.RouterFactory) name from url(").append(uRL2.toString()).append(") use keys([protocol])").toString());
            }
            // 获取url中的协议头,SPI通过协议头进行查找路由工厂类实现(在调用getRouter方法之前,就已经设定好了协议头)
            RouterFactory routerFactory = ExtensionLoader.getExtensionLoader(RouterFactory.class).getExtension(string);
            return routerFactory.getRouter(uRL);
        }
    }
