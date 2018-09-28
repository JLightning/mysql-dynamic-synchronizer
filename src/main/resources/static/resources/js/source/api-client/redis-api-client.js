import AbstractClient from "./abstract-client";

class RedisApiClient extends AbstractClient {

    /**
     * @param serverId {int}
     * @param name {string}
     * @param host {string}
     * @param port {string}
     * @param username {string}
     * @param password {string}
     */
    createFlat(serverId, name, host, port, username, password) {
        return super.putJson('/api/redis/', {serverId, name, host, port, username, password});
    }

    /**
     * @param dto {RedisServerDTO}
     */
    create(dto) {
        return super.putJson('/api/redis/', dto);
    }

    /**
     * @param serverId {int}
     */
    delete(serverId) {
        return super.delete('/api/redis/' + serverId + '', {});
    }

    /**
     * @param serverId {int}
     */
    detail(serverId) {
        return super.get('/api/redis/' + serverId + '', {});
    }

    /**

     */
    list() {
        return super.get('/api/redis/', {});
    }

    /**
     * @param serverId {int}
     * @param name {string}
     * @param host {string}
     * @param port {string}
     * @param username {string}
     * @param password {string}
     */
    updateFlat(serverId, name, host, port, username, password) {
        return super.postJson('/api/redis/', {serverId, name, host, port, username, password});
    }

    /**
     * @param dto {RedisServerDTO}
     */
    update(dto) {
        return super.postJson('/api/redis/', dto);
    }
}

const redisApiClient = new RedisApiClient();
export default redisApiClient;
