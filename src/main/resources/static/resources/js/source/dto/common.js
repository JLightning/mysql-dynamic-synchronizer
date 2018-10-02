// @flow
import {observable} from 'mobx';

export class MySQLFieldDTO {

    /**
     * @type {string}
     */
    @observable field: ?string = '';
    /**
     * @type {string}
     */
    @observable type: ?string = '';
    /**
     * @type {string}
     */
    @observable collation: ?string = '';
    /**
     * @type {boolean}
     */
    @observable nullable: ?boolean = false;
    /**
     * @type {string}
     */
    @observable key: ?string = '';
    /**
     * @type {string}
     */
    @observable defaultValue: ?string = '';
    /**
     * @type {string}
     */
    @observable extra: ?string = '';
    /**
     * @type {string}
     */
    @observable comment: ?string = '';

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
    @observable sourceServerId: ?number = 0;
    /**
     * @type {string}
     */
    @observable sourceDatabase: ?string = '';
    /**
     * @type {string}
     */
    @observable sourceTable: ?string = '';
    /**
     * @type {number}
     */
    @observable targetServerId: ?number = 0;
    /**
     * @type {string}
     */
    @observable targetDatabase: ?string = '';
    /**
     * @type {string}
     */
    @observable targetTable: ?string = '';
    /**
     * @type {SimpleFieldMappingDTO[]}
     */
    @observable mapping: ?SimpleFieldMappingDTO[] = null;

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
    @observable sourceField: ?string = '';
    /**
     * @type {string}
     */
    @observable targetField: ?string = '';
    /**
     * @type {boolean}
     */
    @observable mappable: ?boolean = false;

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
    @observable serverId: ?number = 0;
    /**
     * @type {string}
     */
    @observable name: ?string = '';
    /**
     * @type {string}
     */
    @observable host: ?string = '';
    /**
     * @type {string}
     */
    @observable port: ?string = '';
    /**
     * @type {string}
     */
    @observable username: ?string = '';
    /**
     * @type {string}
     */
    @observable password: ?string = '';

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
    @observable running: ?boolean = false;
    /**
     * @type {number}
     */
    @observable progress: ?number = 0;

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
    @observable running: ?boolean = false;
    /**
     * @type {number}
     */
    @observable insertCount: ?number = null;
    /**
     * @type {number}
     */
    @observable updateCount: ?number = null;
    /**
     * @type {number}
     */
    @observable deleteCount: ?number = null;
    /**
     * @type {boolean}
     */
    @observable delta: ?boolean = false;

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
