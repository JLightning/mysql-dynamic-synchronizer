// @flow


export class MySQLInsertMode {

    /**
     * @type {MySQLInsertMode}
     */
    static INSERT : ?MySQLInsertMode = null;
    /**
     * @type {MySQLInsertMode}
     */
    static INSERT_IGNORE : ?MySQLInsertMode = null;
    /**
     * @type {MySQLInsertMode}
     */
    static REPLACE : ?MySQLInsertMode = null;
    /**
     * @type {string}
     */
    name : string = '';

    /**
     * @param name {string}
     */
    constructor(name : string) {
        this.name = name;
    }

     toJSON() {
          return this.name;
     }
}

MySQLInsertMode.INSERT = new MySQLInsertMode('INSERT');
MySQLInsertMode.INSERT_IGNORE = new MySQLInsertMode('INSERT_IGNORE');
MySQLInsertMode.REPLACE = new MySQLInsertMode('REPLACE');

