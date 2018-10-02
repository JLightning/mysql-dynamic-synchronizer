// @flow
export class MySQLFieldDTO {

    /**
     * @type {string}
     */
    field: ?string = '';
    /**
     * @type {string}
     */
    type: ?string = '';
    /**
     * @type {string}
     */
    collation: ?string = '';
    /**
     * @type {boolean}
     */
    nullable: ?boolean = false;
    /**
     * @type {string}
     */
    key: ?string = '';
    /**
     * @type {string}
     */
    defaultValue: ?string = '';
    /**
     * @type {string}
     */
    extra: ?string = '';
    /**
     * @type {string}
     */
    comment: ?string = '';

    /**
     * @param field {string}
     * @param type {string}
     * @param collation {string}
     * @param nullable {boolean}
     * @param key {string}
     * @param defaultValue {string}
     * @param extra {string}
     * @param comment {string}
     */
    constructor(field: ?string, type: ?string, collation: ?string, nullable: ?boolean, key: ?string, defaultValue: ?string, extra: ?string, comment: ?string) {
        this.field = field;
        this.type = type;
        this.collation = collation;
        this.nullable = nullable;
        this.key = key;
        this.defaultValue = defaultValue;
        this.extra = extra;
        this.comment = comment;
    }


}
export class SimpleFieldMappingDTO {

    /**
     * @type {string}
     */
    sourceField: ?string = '';
    /**
     * @type {string}
     */
    targetField: ?string = '';

    /**
     * @param sourceField {string}
     * @param targetField {string}
     */
    constructor(sourceField: ?string, targetField: ?string) {
        this.sourceField = sourceField;
        this.targetField = targetField;
    }


}
export class TableFieldsMappingRequestDTO {

    /**
     * @type {number}
     */
    sourceServerId: ?number = 0;
    /**
     * @type {string}
     */
    sourceDatabase: ?string = '';
    /**
     * @type {string}
     */
    sourceTable: ?string = '';
    /**
     * @type {number}
     */
    targetServerId: ?number = 0;
    /**
     * @type {string}
     */
    targetDatabase: ?string = '';
    /**
     * @type {string}
     */
    targetTable: ?string = '';
    /**
     * @type {SimpleFieldMappingDTO[]}
     */
    mapping: ?SimpleFieldMappingDTO[] = null;

    /**
     * @param sourceServerId {number}
     * @param sourceDatabase {string}
     * @param sourceTable {string}
     * @param targetServerId {number}
     * @param targetDatabase {string}
     * @param targetTable {string}
     * @param mapping {SimpleFieldMappingDTO[]}
     */
    constructor(sourceServerId: ?number, sourceDatabase: ?string, sourceTable: ?string, targetServerId: ?number, targetDatabase: ?string, targetTable: ?string, mapping: ?SimpleFieldMappingDTO[]) {
        this.sourceServerId = sourceServerId;
        this.sourceDatabase = sourceDatabase;
        this.sourceTable = sourceTable;
        this.targetServerId = targetServerId;
        this.targetDatabase = targetDatabase;
        this.targetTable = targetTable;
        this.mapping = mapping;
    }


}
export class MySQLFieldWithMappingDTO {

    /**
     * @type {string}
     */
    sourceField: ?string = '';
    /**
     * @type {string}
     */
    targetField: ?string = '';
    /**
     * @type {boolean}
     */
    mappable: ?boolean = false;

    /**
     * @param sourceField {string}
     * @param targetField {string}
     * @param mappable {boolean}
     */
    constructor(sourceField: ?string, targetField: ?string, mappable: ?boolean) {
        this.sourceField = sourceField;
        this.targetField = targetField;
        this.mappable = mappable;
    }


}
export class MySQLServerDTO {

    /**
     * @type {number}
     */
    serverId: ?number = 0;
    /**
     * @type {string}
     */
    name: ?string = '';
    /**
     * @type {string}
     */
    host: ?string = '';
    /**
     * @type {string}
     */
    port: ?string = '';
    /**
     * @type {string}
     */
    username: ?string = '';
    /**
     * @type {string}
     */
    password: ?string = '';

    /**
     * @param serverId {number}
     * @param name {string}
     * @param host {string}
     * @param port {string}
     * @param username {string}
     * @param password {string}
     */
    constructor(serverId: ?number, name: ?string, host: ?string, port: ?string, username: ?string, password: ?string) {
        this.serverId = serverId;
        this.name = name;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }


}
export class Table {

    /**
     * @type {number}
     */
    serverId: ?number = 0;
    /**
     * @type {string}
     */
    database: ?string = '';
    /**
     * @type {string}
     */
    table: ?string = '';

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

export class TaskDTO {

    /**
     * @type {number}
     */
    taskId: ?number = 0;
    /**
     * @type {string}
     */
    taskName: ?string = '';
    /**
     * @type {SimpleFieldMappingDTO[]}
     */
    mapping: ?SimpleFieldMappingDTO[] = null;
    /**
     * @type {Table}
     */
    source: ?Table = null;
    /**
     * @type {Table}
     */
    target: ?Table = null;
    /**
     * @type {TaskType}
     */
    taskType: ?TaskType = null;
    /**
     * @type {MySQLInsertMode}
     */
    insertMode: ?MySQLInsertMode = null;
    /**
     * @type {string[]}
     */
    filters: ?string[] = null;

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
export class FullMigrationProgressDTO {

    /**
     * @type {boolean}
     */
    running: ?boolean = false;
    /**
     * @type {number}
     */
    progress: ?number = 0;

    /**
     * @param running {boolean}
     * @param progress {number}
     */
    constructor(running: ?boolean, progress: ?number) {
        this.running = running;
        this.progress = progress;
    }


}
export class IncrementalMigrationProgressDTO {

    /**
     * @type {boolean}
     */
    running: ?boolean = false;
    /**
     * @type {number}
     */
    insertCount: ?number = null;
    /**
     * @type {number}
     */
    updateCount: ?number = null;
    /**
     * @type {number}
     */
    deleteCount: ?number = null;
    /**
     * @type {boolean}
     */
    delta: ?boolean = false;

    /**
     * @param running {boolean}
     * @param insertCount {number}
     * @param updateCount {number}
     * @param deleteCount {number}
     * @param isDelta {boolean}
     */
    constructor(running: ?boolean, insertCount: ?number, updateCount: ?number, deleteCount: ?number, delta: ?boolean) {
        this.running = running;
        this.insertCount = insertCount;
        this.updateCount = updateCount;
        this.deleteCount = deleteCount;
        this.delta = delta;
    }


}
