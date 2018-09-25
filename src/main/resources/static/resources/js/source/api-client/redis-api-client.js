import AbstractClient from "./abstract-client";

class RedisApiClient extends AbstractClient {

    createFlat(serverId, name, host, port, username, password) {
        return this.put('/api/redis/', {serverId, name, host, port, username, password});
    }

    create(dto) {
        return this.put('/api/redis/', dto);
    }

    list() {
        return this.get('/api/redis/', {});
    }
}

const redisApiClient = new RedisApiClient();
export default redisApiClient;
