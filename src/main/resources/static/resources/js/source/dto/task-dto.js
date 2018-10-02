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


export class TaskType {

    /**
     * @type {TaskType}
     */
    static FULL_MIGRATION : ?TaskType = null;
    /**
     * @type {TaskType}
     */
    static INCREMENTAL_MIGRATION : ?TaskType = null;
    /**
     * @type {TaskType}
     */
    static FULL_INCREMENTAL_MIGRATION : ?TaskType = null;
    /**
     * @type {string}
     */
    name : string = '';

    /**
     * @param name {string}
     */
    constructor(name : string) {
        this.name = name;
    }

     toJSON() {
          return this.name;
     }
}

TaskType.FULL_MIGRATION = new TaskType('FULL_MIGRATION');
TaskType.INCREMENTAL_MIGRATION = new TaskType('INCREMENTAL_MIGRATION');
TaskType.FULL_INCREMENTAL_MIGRATION = new TaskType('FULL_INCREMENTAL_MIGRATION');



export class MySQLInsertMode {

    /**
     * @type {MySQLInsertMode}
     */
    static INSERT : ?MySQLInsertMode = null;
    /**
     * @type {MySQLInsertMode}
     */
    static INSERT_IGNORE : ?MySQLInsertMode = null;
    /**
     * @type {MySQLInsertMode}
     */
    static REPLACE : ?MySQLInsertMode = null;
    /**
     * @type {string}
     */
    name : string = '';

    /**
     * @param name {string}
     */
    constructor(name : string) {
        this.name = name;
    }

     toJSON() {
          return this.name;
     }
}

MySQLInsertMode.INSERT = new MySQLInsertMode('INSERT');
MySQLInsertMode.INSERT_IGNORE = new MySQLInsertMode('INSERT_IGNORE');
MySQLInsertMode.REPLACE = new MySQLInsertMode('REPLACE');

import {SimpleFieldMappingDTO} from './simple-field-mapping-dto';

export class TaskDTO {

    /**
     * @type {number}
     */
    @observable taskId: ?number = 0;
    /**
     * @type {string}
     */
    @observable taskName: ?string = '';
    /**
     * @type {SimpleFieldMappingDTO[]}
     */
    @observable mapping: ?SimpleFieldMappingDTO[] = null;
    /**
     * @type {Table}
     */
    @observable source: ?Table = null;
    /**
     * @type {Table}
     */
    @observable target: ?Table = null;
    /**
     * @type {TaskType}
     */
    @observable taskType: ?TaskType = null;
    /**
     * @type {MySQLInsertMode}
     */
    @observable insertMode: ?MySQLInsertMode = null;
    /**
     * @type {string[]}
     */
    @observable filters: ?string[] = null;

    /**
     * @param taskId {number}
     * @param taskName {string}
     * @param mapping {SimpleFieldMappingDTO[]}
     * @param source {Table}
     * @param target {Table}
     * @param taskType {TaskType}
     * @param insertMode {MySQLInsertMode}
     * @param filters {string[]}
     */
    constructor(taskId: ?number, taskName: ?string, mapping: ?SimpleFieldMappingDTO[], source: ?Table, target: ?Table, taskType: ?TaskType, insertMode: ?MySQLInsertMode, filters: ?string[]) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.mapping = mapping;
        this.source = source;
        this.target = target;
        this.taskType = taskType;
        this.insertMode = insertMode;
        this.filters = filters;
    }


}
