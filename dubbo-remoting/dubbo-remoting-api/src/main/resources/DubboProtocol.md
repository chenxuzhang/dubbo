#### Dubbo协议结构
    由请求头、响应头 + 请求体、响应体 组成
    
    请求头、响应头(占用16字节)
   
    2字节:协议标记
    1字节:请求类型 + 序列化类型[第1位:请求(1) or 响应(0),第2位:单向(0) or 双向(1),第3位:事件(1),后5位:序列化类型]
    1字节:响应状态
    8字节:请求id
    4字节:请求体序列化后的长度,按字节计数
    
    请求体、响应体(占用有大小限制,默认8M,可设置)
    Request对象中Data属性值 or Response对象中Result属性值 序列化后
    
    Request
    Dubbo的RPC协议版本 服务接口全路径 服务的版本号 方法名称 参数类型字符串 实参1 实参n attachments集合
    
    Response
    正常: 
        空值: code码(1个字节) attachments集合信息
        有值: code码(1个字节) 返回结果对象 attachments集合信息
    异常: code码(1个字节) Throwable信息 attachments集合信息