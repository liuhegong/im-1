# 分布式IM
## 一、概述
使用netty开发分布式Im，用于加深对netty的理解与im通讯技术的学习
## 二、集群架构
![架构图](https://img-blog.csdnimg.cn/27c34099715546f2945239a4688708d5.png)
### 1.客户端
用户聊天客户端，客户端连接IM服务需要进行用户认证。用户认证成功之后，开始连接上线。
### 2.服务路由
服务路由负责将客户端的连接请求按照不同的负载均衡策略路由到不同的IM服务，建立长链接。负载均衡策略分为以下四种：
- 一致性HASH负载均衡策略
- 最少活跃数负载均衡策略
- 随机调用负载均衡策略
- 轮询调用负载均衡策略
### 3.IM服务集群
为了避免单节点故障，IM服务采用集群模式。集群内各个IM服务又互为对方的客户端，用于转发远程消息（消息接收客户端连接其他IM服务节点）。
### 4.ZK集群
ZK集群作为IM服务的注册中心，用户IM服务的注册与发现以及服务上线、下线的事件监听通知。通过node事件，控制IM服务之间连接的建立与断开。
### 5.消息队列
消息队列用户发送离线消息、聊天消息。
### 6.MongoDB集群
存储离线消息及聊天消息。
### 7.Redis集群
存储客户端的连接session信息（客户端与服务端连接的信息）
## 三、netty集群方案
首先需要明确一个问题，netty的channel是无法存储到redis里面的。netty的channel是一个连接，是和机器的硬件绑定的，无法序列化，计算存到redis里面，取出来也无法使用。
### 1.ZK作为注册中心实现
（1）channel无法存储的问题

channel是无法存储到redis里面的，但是客户端和服务端的连接信息（例如：127.0.0.1:8080的服务端是127.0.0.1:9090）是可以存储到redis里面的，因此可以通过redis存储连接信息。key为客户端标识，value为服务端地址信息，获取客户端的连接时，直接通过客户端信息即可获取其服务新
![channel存储](https://img-blog.csdnimg.cn/74d482dbd4cc49db8520e50630bb8dd6.png)

（2）服务端连接的问题

客户端连接服务端时，客户端如何知道当前服务端有哪些，需要要连接哪个？这个问题可以通过ZK解决。使用ZK作为注册中心，服务端上线后在ZK中创建node，连接服务端时，从ZK获取在线节点信息，根据负载均衡策略选择服务端连接。
![ZK注册中心](https://img-blog.csdnimg.cn/b2534128090a462bb9217e104de27996.png)

（3）消息转发的问题

连接相同服务的客户端，可以直接通过获连接当前服取客户端信息进行消息的转发，那连接不同服务端消息如何转发？我们可以通过监听ZK中node的事件（node创建代表新的服务上线，node销毁代表服务下线），通过不同的事件方法，实现服务端之间的互相连接。
![消息转发](https://img-blog.csdnimg.cn/22951f45d82a41a39fb00065239fd5d6.png)

### 2.redis订阅与广播实现
## 四、核心功能
### 1.netty服务节点的注册与发现
### 2.netty服务节点的负载均衡策略
### 2.netty服务节点的消息转发
