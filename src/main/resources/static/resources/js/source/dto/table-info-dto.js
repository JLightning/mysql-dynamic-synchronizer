export class MySQLServerDTO {

    /**
     * @type {int}
     */
    serverId = 0;
    /**
     * @type {string}
     */
    name = null;
    /**
     * @type {string}
     */
    host = null;
    /**
     * @type {string}
     */
    port = null;
    /**
     * @type {string}
     */
    username = null;
    /**
     * @type {string}
     */
    password = null;

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
export default class TableInfoDTO {

    /**
     * @type {MySQLServerDTO}
     */
    server = null;
    /**
     * @type {string}
     */
    database = null;
    /**
     * @type {string}
     */
    table = null;

    /**
    * @param server {MySQLServerDTO}
    * @param database {string}
    * @param table {string}
     */
    constructor(server, database, table) {
        this.server = server;
        this.database = database;
        this.table = table;
    }
}
