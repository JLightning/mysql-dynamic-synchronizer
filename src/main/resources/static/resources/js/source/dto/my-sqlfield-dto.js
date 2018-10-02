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
