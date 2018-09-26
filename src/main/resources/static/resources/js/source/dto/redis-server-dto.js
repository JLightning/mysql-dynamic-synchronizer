export default class RedisServerDTO {

    /**
     * @type {int}
     */
    serverId = 0;
    /**
     * @type {String}
     */
    name = null;
    /**
     * @type {String}
     */
    host = null;
    /**
     * @type {String}
     */
    port = null;
    /**
     * @type {String}
     */
    username = null;
    /**
     * @type {String}
     */
    password = null;

    /**
    * @param serverId {int}
    * @param name {String}
    * @param host {String}
    * @param port {String}
    * @param username {String}
    * @param password {String}
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
