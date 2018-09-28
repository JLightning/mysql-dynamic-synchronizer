export class SimpleFieldMappingDTO {

    /**
     * @type {string}
     */
    sourceField = null;
    /**
     * @type {string}
     */
    targetField = null;

    /**
    * @param sourceField {string}
    * @param targetField {string}
     */
    constructor(sourceField, targetField) {
        this.sourceField = sourceField;
        this.targetField = targetField;
    }
}
export default class TableFieldsMappingRequestDTO {

    /**
     * @type {int}
     */
    sourceServerId = 0;
    /**
     * @type {string}
     */
    sourceDatabase = null;
    /**
     * @type {string}
     */
    sourceTable = null;
    /**
     * @type {int}
     */
    targetServerId = 0;
    /**
     * @type {string}
     */
    targetDatabase = null;
    /**
     * @type {string}
     */
    targetTable = null;
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
