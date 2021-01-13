package cn.wizzer.iot.model;

import org.nutz.dao.entity.annotation.*;
import org.nutz.dao.interceptor.annotation.PrevInsert;

import java.io.Serializable;

/**
 * @author wizzer@qq.com
 */
@Table("iot_dev")
public class IotDev implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column
    @Name
    @Comment("ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @PrevInsert(els = {@EL("uuid()")})
    private String id;

    @Column
    @Comment("设备ID")
    @ColDefine(type = ColType.VARCHAR, width = 255)
    private String devId;

    @Column
    @Comment("设备状态(0-在线/1-离线/2-异常")
    @ColDefine(type = ColType.INT)
    private int devStatus;

    @Column
    @Comment("电池容量")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String devBattery;

    @Column
    @Comment("JSON")
    @ColDefine(type = ColType.VARCHAR, width = 500)
    private String devData;

    @Column
    @Comment("创建时间")
    @PrevInsert(now = true)
    private Long createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public int getDevStatus() {
        return devStatus;
    }

    public void setDevStatus(int devStatus) {
        this.devStatus = devStatus;
    }

    public String getDevBattery() {
        return devBattery;
    }

    public void setDevBattery(String devBattery) {
        this.devBattery = devBattery;
    }

    public String getDevData() {
        return devData;
    }

    public void setDevData(String devData) {
        this.devData = devData;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
