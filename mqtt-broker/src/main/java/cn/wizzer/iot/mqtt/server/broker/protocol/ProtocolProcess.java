/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.protocol;

import cn.wizzer.iot.mqtt.server.broker.config.BrokerProperties;
import cn.wizzer.iot.mqtt.server.broker.internal.InternalCommunication;
import cn.wizzer.iot.mqtt.server.common.auth.IAuthService;
import cn.wizzer.iot.mqtt.server.common.message.IDupPubRelMessageStoreService;
import cn.wizzer.iot.mqtt.server.common.message.IDupPublishMessageStoreService;
import cn.wizzer.iot.mqtt.server.common.message.IMessageIdService;
import cn.wizzer.iot.mqtt.server.common.message.IRetainMessageStoreService;
import cn.wizzer.iot.mqtt.server.common.session.ISessionStoreService;
import cn.wizzer.iot.mqtt.server.common.subscribe.ISubscribeStoreService;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.Map;

/**
 * 协议处理
 */
@IocBean
public class ProtocolProcess {

    @Inject
    private ISessionStoreService sessionStoreService;

    @Inject
    private ISubscribeStoreService subscribeStoreService;

    @Inject
    private IAuthService authService;

    @Inject
    private IMessageIdService messageIdService;

    @Inject
    private IRetainMessageStoreService messageStoreService;

    @Inject
    private IDupPublishMessageStoreService dupPublishMessageStoreService;

    @Inject
    private IDupPubRelMessageStoreService dupPubRelMessageStoreService;

    @Inject
    private InternalCommunication internalCommunication;

    @Inject
    private BrokerProperties brokerProperties;

    @Inject
    private ChannelGroup channelGroup;

    @Inject
    private Map<String, ChannelId> channelIdMap;

    private volatile Connect connect;

    private volatile Subscribe subscribe;

    private volatile UnSubscribe unSubscribe;

    private volatile Publish publish;

    private volatile DisConnect disConnect;

    private volatile PingReq pingReq;

    private volatile PubRel pubRel;

    private volatile PubAck pubAck;

    private volatile PubRec pubRec;

    private volatile PubComp pubComp;

    public Connect connect() {
        if (connect == null) {
            synchronized (this) {
                if (connect == null) {
                    connect = new Connect(sessionStoreService, subscribeStoreService, dupPublishMessageStoreService, dupPubRelMessageStoreService, authService, brokerProperties, channelGroup, channelIdMap);
                }
            }
        }
        return connect;
    }

    public Subscribe subscribe() {
        if (subscribe == null) {
            synchronized (this) {
                if (subscribe == null) {
                    subscribe = new Subscribe(subscribeStoreService, messageIdService, messageStoreService);
                }
            }
        }
        return subscribe;
    }

    public UnSubscribe unSubscribe() {
        if (unSubscribe == null) {
            synchronized (this) {
                if (unSubscribe == null) {
                    unSubscribe = new UnSubscribe(subscribeStoreService);
                }
            }
        }
        return unSubscribe;
    }

    public Publish publish() {
        if (publish == null) {
            synchronized (this) {
                if (publish == null) {
                    publish = new Publish(sessionStoreService, subscribeStoreService, messageIdService, messageStoreService, dupPublishMessageStoreService, internalCommunication, channelGroup, channelIdMap, brokerProperties);
                }
            }
        }
        return publish;
    }

    public DisConnect disConnect() {
        if (disConnect == null) {
            synchronized (this) {
                if (disConnect == null) {
                    disConnect = new DisConnect(sessionStoreService, subscribeStoreService, dupPublishMessageStoreService, dupPubRelMessageStoreService);
                }
            }
        }
        return disConnect;
    }

    public PingReq pingReq() {
        if (pingReq == null) {
            synchronized (this) {
                if (pingReq == null) {
                    pingReq = new PingReq(sessionStoreService, brokerProperties, channelGroup, channelIdMap);
                }
            }
        }
        return pingReq;
    }

    public PubRel pubRel() {
        if (pubRel == null) {
            synchronized (this) {
                if (pubRel == null) {
                    pubRel = new PubRel();
                }
            }
        }
        return pubRel;
    }

    public PubAck pubAck() {
        if (pubAck == null) {
            synchronized (this) {
                if (pubAck == null) {
                    pubAck = new PubAck(dupPublishMessageStoreService);
                }
            }
        }
        return pubAck;
    }

    public PubRec pubRec() {
        if (pubRec == null) {
            synchronized (this) {
                if (pubRec == null) {
                    pubRec = new PubRec(dupPublishMessageStoreService, dupPubRelMessageStoreService);
                }
            }
        }
        return pubRec;
    }

    public PubComp pubComp() {
        if (pubComp == null) {
            synchronized (this) {
                if (pubComp == null) {
                    pubComp = new PubComp(dupPubRelMessageStoreService);
                }
            }
        }
        return pubComp;
    }

    public ISessionStoreService getSessionStoreService() {
        return sessionStoreService;
    }

}
