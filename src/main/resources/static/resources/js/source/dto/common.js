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
import {SimpleFieldMappingDTO} from './simple-field-mapping-dto';

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
