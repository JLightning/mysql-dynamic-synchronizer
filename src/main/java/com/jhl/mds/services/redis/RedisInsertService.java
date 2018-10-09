package com.jhl.mds.services.redis;

import com.jhl.mds.dto.migration.MySQL2RedisMigrationDTO;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.function.Consumer;

@Service
public class RedisInsertService implements PipeLineTaskRunner<MySQL2RedisMigrationDTO, Map<String, Object>, Boolean> {

    @Override
    public void execute(MySQL2RedisMigrationDTO context, Map<String, Object> input, Consumer<Boolean> next, Consumer<Exception> errorHandler) throws Exception {
        System.out.println("input = " + input);
        Jedis jedis = new Jedis(context.getTarget().getHost(), Integer.parseInt(context.getTarget().getPort()));
        jedis.set(String.valueOf(input.get("key")), String.valueOf(input.get("value")));

        jedis.close();
    }
}
