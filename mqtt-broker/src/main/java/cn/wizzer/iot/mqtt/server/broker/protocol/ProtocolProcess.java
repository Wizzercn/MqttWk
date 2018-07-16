/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.protocol;

import cn.wizzer.iot.mqtt.server.broker.internal.InternalCommunication;
import cn.wizzer.iot.mqtt.server.broker.service.TioService;
import cn.wizzer.iot.mqtt.server.common.auth.IAuthService;
import cn.wizzer.iot.mqtt.server.common.message.IDupPubRelMessageStoreService;
import cn.wizzer.iot.mqtt.server.common.message.IDupPublishMessageStoreService;
import cn.wizzer.iot.mqtt.server.common.message.IMessageIdService;
import cn.wizzer.iot.mqtt.server.common.message.IRetainMessageStoreService;
import cn.wizzer.iot.mqtt.server.common.session.ISessionStoreService;
import cn.wizzer.iot.mqtt.server.common.subscribe.ISubscribeStoreService;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

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
    private TioService tioService;

    private Connect connect;

    private Subscribe subscribe;

    private UnSubscribe unSubscribe;

    private Publish publish;

    private DisConnect disConnect;

    private PingReq pingReq;

    private PubRel pubRel;

    private PubAck pubAck;

    private PubRec pubRec;

    private PubComp pubComp;

    public Connect connect() {
        if (connect == null) {
            connect = new Connect(sessionStoreService, subscribeStoreService, dupPublishMessageStoreService, dupPubRelMessageStoreService, authService, tioService);
        }
        return connect;
    }

    public Subscribe subscribe() {
        if (subscribe == null) {
            subscribe = new Subscribe(subscribeStoreService, messageIdService, messageStoreService);
        }
        return subscribe;
    }

    public UnSubscribe unSubscribe() {
        if (unSubscribe == null) {
            unSubscribe = new UnSubscribe(subscribeStoreService);
        }
        return unSubscribe;
    }

    public Publish publish() {
        if (publish == null) {
            publish = new Publish(sessionStoreService, subscribeStoreService, messageIdService, messageStoreService, dupPublishMessageStoreService, internalCommunication, tioService);
        }
        return publish;
    }

    public DisConnect disConnect() {
        if (disConnect == null) {
            disConnect = new DisConnect(sessionStoreService, subscribeStoreService, dupPublishMessageStoreService, dupPubRelMessageStoreService);
        }
        return disConnect;
    }

    public PingReq pingReq() {
        if (pingReq == null) {
            pingReq = new PingReq();
        }
        return pingReq;
    }

    public PubRel pubRel() {
        if (pubRel == null) {
            pubRel = new PubRel();
        }
        return pubRel;
    }

    public PubAck pubAck() {
        if (pubAck == null) {
            pubAck = new PubAck(messageIdService, dupPublishMessageStoreService);
        }
        return pubAck;
    }

    public PubRec pubRec() {
        if (pubRec == null) {
            pubRec = new PubRec(dupPublishMessageStoreService, dupPubRelMessageStoreService);
        }
        return pubRec;
    }

    public PubComp pubComp() {
        if (pubComp == null) {
            pubComp = new PubComp(messageIdService, dupPubRelMessageStoreService);
        }
        return pubComp;
    }

    public ISessionStoreService getSessionStoreService() {
        return sessionStoreService;
    }

}
