import AbstractClient from "./abstract-client";

class RedisApiClient extends AbstractClient {

    createFlat(serverId, name, host, port, username, password) {
        return super.put('/api/redis/', {serverId, name, host, port, username, password});
    }

    create(dto) {
        return super.put('/api/redis/', dto);
    }

    delete(serverId) {
        return super.delete('/api/redis/' + serverId + '', {});
    }

    list() {
        return super.get('/api/redis/', {});
    }
}

const redisApiClient = new RedisApiClient();
export default redisApiClient;
