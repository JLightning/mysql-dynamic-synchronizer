// @flow
import {MySQLInsertMode} from './my-sqlinsert-mode';
import {SimpleFieldMappingDTO} from './simple-field-mapping-dto';
import {TaskType} from './task-type';
import {Table} from './table';
import {observable} from 'mobx';

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
