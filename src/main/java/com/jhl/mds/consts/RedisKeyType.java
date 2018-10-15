package com.jhl.mds.consts;

public enum RedisKeyType {

    STRING(0),
    LIST(1);

    private int code;

    RedisKeyType(int code) {
        this.code = code;
    }

    public RedisKeyType findByCode(int code) {
        for (RedisKeyType redisKeyType: values()) {
            if (redisKeyType.code == code) {
                return redisKeyType;
            }
        }
        throw new RuntimeException("Not found");
    }
}
