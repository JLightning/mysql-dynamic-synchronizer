// @flow
import AbstractClient from "./abstract-client";
import {MySQLFieldDTO, SimpleFieldMappingDTO, TableFieldsMappingRequestDTO, MySQLFieldWithMappingDTO, MySQLServerDTO, RedisServerDTO} from '../dto/common';

class RedisApiClient extends AbstractClient {

    /**
     * @param serverId {number}
     * @param name {string}
     * @param host {string}
     * @param port {string}
     * @param username {string}
     * @param password {string}
     * @returns {{done: (function(function(RedisServerDTO): *): *), error: (function(*): *)}}
     */
    createFlat(serverId : number, name : string, host : string, port : string, username : string, password : string): {done: (RedisServerDTO => void) => void, error: () => void} {
        return super.putJson('/api/redis/', {serverId, name, host, port, username, password});
    }

    /**
     * @param dto {RedisServerDTO}
     * @returns {{done: (function(function(RedisServerDTO): *): *), error: (function(*): *)}}
     */
    create(dto : RedisServerDTO): {done: (RedisServerDTO => void) => void, error: () => void} {
        return super.putJson('/api/redis/', dto);
    }

    /**
     * @param serverId {number}
     * @returns {{done: (function(function(boolean): *): *), error: (function(*): *)}}
     */
    delete(serverId : number): {done: (boolean => void) => void, error: () => void} {
        return super.delete('/api/redis/' + serverId + '', {});
    }

    /**
     * @param serverId {number}
     * @returns {{done: (function(function(RedisServerDTO): *): *), error: (function(*): *)}}
     */
    detail(serverId : number): {done: (RedisServerDTO => void) => void, error: () => void} {
        return super.get('/api/redis/' + serverId + '', {});
    }

    /**

     * @returns {{done: (function(function(RedisServerDTO[]): *): *), error: (function(*): *)}}
     */
    list(): {done: (RedisServerDTO[] => void) => void, error: () => void} {
        return super.get('/api/redis/', {});
    }

    /**
     * @param serverId {number}
     * @param name {string}
     * @param host {string}
     * @param port {string}
     * @param username {string}
     * @param password {string}
     * @returns {{done: (function(function(RedisServerDTO): *): *), error: (function(*): *)}}
     */
    updateFlat(serverId : number, name : string, host : string, port : string, username : string, password : string): {done: (RedisServerDTO => void) => void, error: () => void} {
        return super.postJson('/api/redis/', {serverId, name, host, port, username, password});
    }

    /**
     * @param dto {RedisServerDTO}
     * @returns {{done: (function(function(RedisServerDTO): *): *), error: (function(*): *)}}
     */
    update(dto : RedisServerDTO): {done: (RedisServerDTO => void) => void, error: () => void} {
        return super.postJson('/api/redis/', dto);
    }
}

const redisApiClient = new RedisApiClient();
export default redisApiClient;
