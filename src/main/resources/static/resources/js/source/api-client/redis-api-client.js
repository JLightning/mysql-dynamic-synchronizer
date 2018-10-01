import AbstractClient from "./abstract-client";

class RedisApiClient extends AbstractClient {

    /**
     * @param serverId {number}
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
     * @returns {{done: (function(function(RedisServerDTO): *): *), error: (function(*): *)}}
     */
    create(dto) {
        return super.putJson('/api/redis/', dto);
    }

    /**
     * @param serverId {number}
     * @returns {{done: (function(function(*): *): *), error: (function(*): *)}}
     */
    delete(serverId) {
        return super.delete('/api/redis/' + serverId + '', {});
    }

    /**
     * @param serverId {number}
     * @returns {{done: (function(function(RedisServerDTO): *): *), error: (function(*): *)}}
     */
    detail(serverId) {
        return super.get('/api/redis/' + serverId + '', {});
    }

    /**

     * @returns {{done: (function(function(RedisServerDTO[]): *): *), error: (function(*): *)}}
     */
    list() {
        return super.get('/api/redis/', {});
    }

    /**
     * @param serverId {number}
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
     * @returns {{done: (function(function(RedisServerDTO): *): *), error: (function(*): *)}}
     */
    update(dto) {
        return super.postJson('/api/redis/', dto);
    }
}

const redisApiClient = new RedisApiClient();
export default redisApiClient;
