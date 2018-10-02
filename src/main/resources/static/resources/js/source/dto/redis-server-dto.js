// @flow
export class RedisServerDTO {

    /**
     * @type {number}
     */
    serverId : ?number = 0;
    /**
     * @type {string}
     */
    name : ?string = '';
    /**
     * @type {string}
     */
    host : ?string = '';
    /**
     * @type {string}
     */
    port : ?string = '';
    /**
     * @type {string}
     */
    username : ?string = '';
    /**
     * @type {string}
     */
    password : ?string = '';

    /**
     * @param serverId {number}
     * @param name {string}
     * @param host {string}
     * @param port {string}
     * @param username {string}
     * @param password {string}
     */
    constructor(serverId : ?number, name : ?string, host : ?string, port : ?string, username : ?string, password : ?string) {
        this.serverId = serverId;
        this.name = name;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }


}
