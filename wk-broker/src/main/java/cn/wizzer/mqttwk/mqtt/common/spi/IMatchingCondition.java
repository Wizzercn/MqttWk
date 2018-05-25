package cn.wizzer.mqttwk.mqtt.common.spi;

import cn.wizzer.mqttwk.mqtt.common.spi.impl.subscriptions.Topic;

/**
 * Created by wizzer on 2018/5/13.
 */
public interface IMatchingCondition {

    boolean match(Topic key);
}