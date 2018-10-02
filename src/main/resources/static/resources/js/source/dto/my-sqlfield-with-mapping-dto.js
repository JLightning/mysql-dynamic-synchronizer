// @flow
import {observable} from 'mobx';

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
