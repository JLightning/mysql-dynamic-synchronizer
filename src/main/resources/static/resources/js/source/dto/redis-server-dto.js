export default class RedisServerDTO {

    /**
     * @type {int}
     */
    serverId = 0;
    /**
     * @type {string}
     */
    name = '';
    /**
     * @type {string}
     */
    host = '';
    /**
     * @type {string}
     */
    port = '';
    /**
     * @type {string}
     */
    username = '';
    /**
     * @type {string}
     */
    password = '';

    /**
     * @param serverId {int}
     * @param name {string}
     * @param host {string}
     * @param port {string}
     * @param username {string}
     * @param password {string}
     */
    constructor(serverId, name, host, port, username, password) {
        this.serverId = serverId;
        this.name = name;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }
}
