    package com.alibaba.dubbo.rpc.cluster;
    
    import com.alibaba.dubbo.common.URL;
    import com.alibaba.dubbo.common.extension.ExtensionLoader;
    import com.alibaba.dubbo.rpc.Invoker;
    import com.alibaba.dubbo.rpc.RpcException;
    import com.alibaba.dubbo.rpc.cluster.Cluster;
    import com.alibaba.dubbo.rpc.cluster.Directory;
    
    public class Cluster$Adaptive
    implements Cluster {
        public Invoker join(Directory directory) throws RpcException {
            if (directory == null) {
                throw new IllegalArgumentException("com.alibaba.dubbo.rpc.cluster.Directory argument == null");
            }
            if (directory.getUrl() == null) {
                throw new IllegalArgumentException("com.alibaba.dubbo.rpc.cluster.Directory argument getUrl() == null");
            }
            URL uRL = directory.getUrl();
            // 默认获取failover
            String string = uRL.getParameter("cluster", "failover");
            if (string == null) {
                throw new IllegalStateException(new StringBuffer().append("Fail to get extension(com.alibaba.dubbo.rpc.cluster.Cluster) name from url(").append(uRL.toString()).append(") use keys([cluster])").toString());
            }
            // 通过SPI方式获取Cluster
            Cluster cluster = ExtensionLoader.getExtensionLoader(Cluster.class).getExtension(string);
            return cluster.join(directory);
        }
    }
