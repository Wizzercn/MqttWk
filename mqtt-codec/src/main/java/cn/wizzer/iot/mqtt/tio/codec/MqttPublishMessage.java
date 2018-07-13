/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package cn.wizzer.iot.mqtt.tio.codec;

import org.nutz.lang.Lang;
import org.tio.core.utils.ByteBufferUtils;

import java.nio.ByteBuffer;

/**
 * See <a href="http://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html#publish">MQTTV3.1/publish</a>
 */
public class MqttPublishMessage extends MqttMessage {

    public MqttPublishMessage(
            MqttFixedHeader mqttFixedHeader,
            MqttPublishVariableHeader variableHeader,
            ByteBuffer payload) {
        super(mqttFixedHeader, variableHeader, payload);
    }

    @Override
    public MqttPublishVariableHeader variableHeader() {
        return (MqttPublishVariableHeader) super.variableHeader();
    }

    @Override
    public ByteBuffer payload() {
        return content();
    }

    public ByteBuffer content() {
        final ByteBuffer data = (ByteBuffer) super.payload();
        if (!data.hasRemaining()) {
            throw Lang.makeThrow("data size is %s", "0");
        }
        return data;
    }

    public MqttPublishMessage copy() {
        return replace(ByteBufferUtils.copy(content(), 0, content().remaining()));
    }

    public MqttPublishMessage replace(ByteBuffer content) {
        return new MqttPublishMessage(fixedHeader(), variableHeader(), content);
    }

}
