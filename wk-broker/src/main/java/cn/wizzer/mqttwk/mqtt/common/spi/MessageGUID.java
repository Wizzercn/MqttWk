package cn.wizzer.mqttwk.mqtt.common.spi;

import java.io.Serializable;

/**
 * Created by wizzer on 2018/5/13.
 */
public class MessageGUID implements Serializable {

    private static final long serialVersionUID = 4315161987111542406L;
    private final String guid;

    public MessageGUID(String guid) {
        this.guid = guid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MessageGUID that = (MessageGUID) o;

        return guid != null ? guid.equals(that.guid) : that.guid == null;
    }

    @Override
    public int hashCode() {
        return guid != null ? guid.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "MessageGUID{" + "guid='" + guid + '\'' + '}';
    }
}
