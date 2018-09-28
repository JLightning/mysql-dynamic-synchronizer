export default class MySQLInsertMode {

    /**
     * @type {string}
     */
    name = '';

    /**
     * @param name {string}
     */
    constructor(name) {
        this.name = name;
    }
}

MySQLInsertMode.INSERT = new MySQLInsertMode('INSERT');
MySQLInsertMode.INSERT_IGNORE = new MySQLInsertMode('INSERT_IGNORE');
MySQLInsertMode.REPLACE = new MySQLInsertMode('REPLACE');
MySQLInsertMode.prototype.toJSON = function() { return this.name;};

