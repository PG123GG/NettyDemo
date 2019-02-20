package com.protocol.io;

import com.alibaba.fastjson.JSON;

/**
 * JSON序列化
 */
public class JSONSerializer implements Serializer {

    /**
     * 序列化算法
     */
    @Override
    public byte getSerializerAlgorithm() {
        return SerializerAlgorithm.JSON;
    }

    /**
     * java 对象转换成二进制
     *
     * @param object
     */
    @Override
    public byte[] serialize(Object object) {
        return JSON.toJSONBytes(object);
    }

    /**
     * 二进制转换成 java 对象
     *
     * @param clazz
     * @param bytes
     */
    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        return JSON.parseObject(bytes,clazz);
    }
}
