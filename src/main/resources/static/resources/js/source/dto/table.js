// @flow
import {observable} from 'mobx';

export class Table {

    /**
     * @type {number}
     */
    @observable serverId: ?number = 0;
    /**
     * @type {string}
     */
    @observable database: ?string = '';
    /**
     * @type {string}
     */
    @observable table: ?string = '';

    /**
     * @param serverId {number}
     * @param database {string}
     * @param table {string}
     */
    constructor(serverId: ?number, database: ?string, table: ?string) {
        this.serverId = serverId;
        this.database = database;
        this.table = table;
    }


}
