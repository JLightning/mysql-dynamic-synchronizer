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
export default class TaskDTO {

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
     * @param target {*}
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
