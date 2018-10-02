// @flow
import {observable} from 'mobx';

export class SimpleFieldMappingDTO {

    /**
     * @type {string}
     */
    @observable sourceField: ?string = '';
    /**
     * @type {string}
     */
    @observable targetField: ?string = '';

    /**
     * @param sourceField {string}
     * @param targetField {string}
     */
    constructor(sourceField: ?string, targetField: ?string) {
        this.sourceField = sourceField;
        this.targetField = targetField;
    }


}
