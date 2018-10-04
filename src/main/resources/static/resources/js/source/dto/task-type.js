// @flow


export class TaskType {

    /**
     * @type {TaskType}
     */
    static MYSQL_TO_MYSQL : ?TaskType = null;
    /**
     * @type {TaskType}
     */
    static MYSQL_TO_REDIS : ?TaskType = null;
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

TaskType.MYSQL_TO_MYSQL = new TaskType('MYSQL_TO_MYSQL');
TaskType.MYSQL_TO_REDIS = new TaskType('MYSQL_TO_REDIS');

