/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.common.message;

import java.util.List;

/**
 * 消息存储服务接口
 */
public interface IRetainMessageStoreService {

	/**
	 * 存储retain标志消息
	 */
	void put(String topic, RetainMessageStore retainMessageStore);

	/**
	 * 获取retain消息
	 */
	RetainMessageStore get(String topic);

	/**
	 * 删除retain标志消息
	 */
	void remove(String topic);

	/**
	 * 判断指定topic的retain消息是否存在
	 */
	boolean containsKey(String topic);

	/**
	 * 获取retain消息集合
	 */
	List<RetainMessageStore> search(String topicFilter);

}
