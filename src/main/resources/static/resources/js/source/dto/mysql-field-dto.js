export default class MySQLFieldDTO {

    /**
     * @type {string}
     */
    field = '';
    /**
     * @type {string}
     */
    type = '';
    /**
     * @type {string}
     */
    collation = '';
    /**
     * @type {boolean}
     */
    nullable = false;
    /**
     * @type {string}
     */
    key = '';
    /**
     * @type {string}
     */
    defaultValue = '';
    /**
     * @type {string}
     */
    extra = '';
    /**
     * @type {string}
     */
    comment = '';

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
    constructor(field, type, collation, nullable, key, defaultValue, extra, comment) {
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
