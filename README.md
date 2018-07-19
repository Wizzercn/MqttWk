# MqttWk

基于 nutzboot + t-io + redis + kafka 实现的MQTT服务broker

本项目代码主要来源于 netty/t-io/iot-mqtt-server 等众多项目，开源免费，欢迎交流学习

# 参考项目

* [https://github.com/netty/netty](https://github.com/netty/netty)
* [https://gitee.com/tywo45/tio-showcase/](https://gitee.com/tywo45/tio-showcase/)
* [https://gitee.com/recallcode/iot-mqtt-server](https://gitee.com/recallcode/iot-mqtt-server)

# 使用说明

#### 软件架构说明

1. 使用t-io实现通信及协议解析
2. 使用nutzboot提供依赖注入及属性配置
3. 使用redis实现消息缓存,集群
4. 使用kafka实现消息代理

#### 项目结构
```
MqttWk
  ├── mqtt-codec -- MQTT协议解析的t-io实现
  ├── mqtt-auth -- MQTT服务连接时用户名和密码认证
  ├── mqtt-broker -- MQTT服务器功能的核心实现
  ├── mqtt-common -- 公共类及其他模块使用的服务接口及对象
  ├── mqtt-store -- MQTT服务器会话信息(redis缓存及kafka加载)
  ├── mqtt-zoo -- 教程文档或文件
    ├── mqtt-test-kafka -- kafka消费者接收消息
    ├── mqtt-test-websocket -- websocket通信测试示例
```

#### 功能说明
1. 参考MQTT3.1.1规范实现
2. 完整的QoS服务质量等级实现
3. 遗嘱消息, 保留消息及消息分发重试
4. 心跳机制
5. MQTT连接认证(可选择是否开启)
5. SSL方式连接(可选择是否开启)
6. 主题过滤(支持单主题订阅如 /mqtt/test --不可以/结尾, 通配符订阅 /mqtt/# --以/#结尾)
7. Websocket支持(可选择是否开启)
8. 集群功能

#### 快速开始
- [下载已打包好的可运行的jar文件](https://gitee.com/wizzer/MqttWk/releases)
- 运行jar文件(如果需要修改配置项,可以在application.properties修改)
- 打开mqtt-spy客户端, 填写相应配置[下载](https://github.com/eclipse/paho.mqtt-spy/wiki/Downloads)
- 连接端口:8885, websocket 端口: 9995 websocket path: /mqtt
- 连接使用的用户名: demo
- 连接使用的密码: 8F3B8DE2FDC8BD3D792BE77EAC412010971765E5BDD6C499ADCEE840CE441BDEF17E30684BD95CA708F55022222CC6161D0D23C2DFCB12F8AC998F59E7213393
- 连接使用的证书在 `mqtt-zoo`\keystore\server.cer

#### 集群使用
- 多机环境集群:
  - `mqttwk.broker.kafka.bootstrap.servers=192.168.1.101:9092,192.168.1.102:9093`
  - `redis.mode=cluster` 
  - `redis.nodes=192.168.1.103:16379,192.168.1.104:26379`
- 单机环境集群: 
  - `mqttwk.broker.kafka.bootstrap.servers=127.0.0.1:9092,127.0.0.1:9093`
  - `redis.mode=normal`
  - `redis.host=127.0.0.1`

#### 自定义 - 连接认证
- 默认只是简单使用对用户名进行RSA密钥对加密生成密码, 连接认证时对密码进行解密和相应用户名进行匹配认证
- 使用中如果需要实现连接数据库或其他方式进行连接认证, 只需要重写`mqtt-auth`模块下的相应方法即可

#### 自定义 - 服务端证书
- 服务端证书存储在`mqtt-broker`的`resources/keystore/server.jks`
- 用户可以制作自己的证书, 但存储位置和文件名必须使用上述描述的位置及文件名

#### 生成环境部署
- 生成环境部署建议使用`keepalived+nginx+mqtt-broker`方式
- 使用nginx的tcp和websocket反向代理mqtt-broker集群实现负载均衡
- 使用keepalived实现nginx的高可用    
![输入图片说明](https://images.gitee.com/uploads/images/2018/0712/112559_e5f8401d_1081719.png "1.png")
- `mqtt-broker`模块中包含`Dockerfile`文件可以直接生成镜像
- 需要注意: 基于集群的实现机制, 在通过`docker run`部署容器时,需要添加--net=host参数
- `docker run --name=mqtt-broker-service --net=host --restart=always --env-file=/home/rancher/mqtt-broker/env.list -v /home/rancher/mqtt-broker/config/:/opt/mqtt-broker/config/ -v /home/rancher/mqtt-broker/persistence/:/opt/mqtt-broker/persistence/ -d mqtt-broker:1.0`