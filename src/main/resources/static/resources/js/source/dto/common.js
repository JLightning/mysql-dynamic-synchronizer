export class SimpleFieldMappingDTO {

    /**
     * @type {string}
     */
    sourceField = '';
    /**
     * @type {string}
     */
    targetField = '';

    /**
     * @param sourceField {string}
     * @param targetField {string}
     */
    constructor(sourceField, targetField) {
        this.sourceField = sourceField;
        this.targetField = targetField;
    }
}
export class TableFieldsMappingRequestDTO {

    /**
     * @type {int}
     */
    sourceServerId = 0;
    /**
     * @type {string}
     */
    sourceDatabase = '';
    /**
     * @type {string}
     */
    sourceTable = '';
    /**
     * @type {int}
     */
    targetServerId = 0;
    /**
     * @type {string}
     */
    targetDatabase = '';
    /**
     * @type {string}
     */
    targetTable = '';
    /**
     * @type {SimpleFieldMappingDTO[]}
     */
    mapping = null;

    /**
     * @param sourceServerId {int}
     * @param sourceDatabase {string}
     * @param sourceTable {string}
     * @param targetServerId {int}
     * @param targetDatabase {string}
     * @param targetTable {string}
     * @param mapping {SimpleFieldMappingDTO[]}
     */
    constructor(sourceServerId, sourceDatabase, sourceTable, targetServerId, targetDatabase, targetTable, mapping) {
        this.sourceServerId = sourceServerId;
        this.sourceDatabase = sourceDatabase;
        this.sourceTable = sourceTable;
        this.targetServerId = targetServerId;
        this.targetDatabase = targetDatabase;
        this.targetTable = targetTable;
        this.mapping = mapping;
    }
}
export class RedisServerDTO {

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
export class Table {

    /**
     * @type {int}
     */
    serverId = 0;
    /**
     * @type {string}
     */
    database = '';
    /**
     * @type {string}
     */
    table = '';

    /**
     * @param serverId {int}
     * @param database {string}
     * @param table {string}
     */
    constructor(serverId, database, table) {
        this.serverId = serverId;
        this.database = database;
        this.table = table;
    }
}
export class TaskDTO {

    /**
     * @type {int}
     */
    taskId = 0;
    /**
     * @type {string}
     */
    taskName = '';
    /**
     * @type {SimpleFieldMappingDTO[]}
     */
    mapping = null;
    /**
     * @type {Table}
     */
    source = null;
    /**
     * @type {Table}
     */
    target = null;
    /**
     * @type {TaskType}
     */
    taskType = null;
    /**
     * @type {MySQLInsertMode}
     */
    insertMode = null;
    /**
     * @type {string[]}
     */
    filters = null;

    /**
     * @param taskId {int}
     * @param taskName {string}
     * @param mapping {SimpleFieldMappingDTO[]}
     * @param source {Table}
     * @param target {Table}
     * @param taskType {TaskType}
     * @param insertMode {MySQLInsertMode}
     * @param filters {string[]}
     */
    constructor(taskId, taskName, mapping, source, target, taskType, insertMode, filters) {
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
