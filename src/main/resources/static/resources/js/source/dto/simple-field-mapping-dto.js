// @flow


export class SimpleFieldMappingDTO {

    /**
     * @type {string}
     */
    sourceField: ?string = '';
    /**
     * @type {string}
     */
    targetField: ?string = '';

    /**
     * @param sourceField {string}
     * @param targetField {string}
     */
    constructor(sourceField: ?string, targetField: ?string) {
        this.sourceField = sourceField;
        this.targetField = targetField;
    }


}
