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

    detail(serverId) {
        return super.get('/api/redis/' + serverId + '', {});
    }

    list() {
        return super.get('/api/redis/', {});
    }

    updateFlat(serverId, name, host, port, username, password) {
        return super.postJson('/api/redis/', {serverId, name, host, port, username, password});
    }

    update(dto) {
        return super.postJson('/api/redis/', dto);
    }
}

const redisApiClient = new RedisApiClient();
export default redisApiClient;
