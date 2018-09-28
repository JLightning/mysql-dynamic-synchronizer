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
export default class MigrationDTO {

    /**
     * @type {int}
     */
    taskId = 0;
    /**
     * @type {TableInfoDTO}
     */
    source = null;
    /**
     * @type {TableInfoDTO}
     */
    target = null;
    /**
     * @type {SimpleFieldMappingDTO[]}
     */
    mapping = null;
    /**
     * @type {MySQLInsertMode}
     */
    insertMode = null;
    /**
     * @type {string[]}
     */
    filters = null;
    /**
     * @type {string[]}
     */
    targetColumns = null;

    /**
     * @param taskId {int}
     * @param source {TableInfoDTO}
     * @param target {*}
     * @param mapping {SimpleFieldMappingDTO[]}
     * @param insertMode {MySQLInsertMode}
     * @param filters {string[]}
     * @param targetColumns {string[]}
     */
    constructor(taskId, source, target, mapping, insertMode, filters, targetColumns) {
        this.taskId = taskId;
        this.source = source;
        this.target = target;
        this.mapping = mapping;
        this.insertMode = insertMode;
        this.filters = filters;
        this.targetColumns = targetColumns;
    }
}
