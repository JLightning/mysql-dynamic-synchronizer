// @flow
import {SimpleFieldMappingDTO} from './simple-field-mapping-dto';
import {observable} from 'mobx';

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
