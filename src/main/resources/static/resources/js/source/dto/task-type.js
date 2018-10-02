// @flow


export class TaskType {

    /**
     * @type {TaskType}
     */
    static FULL_MIGRATION : ?TaskType = null;
    /**
     * @type {TaskType}
     */
    static INCREMENTAL_MIGRATION : ?TaskType = null;
    /**
     * @type {TaskType}
     */
    static FULL_INCREMENTAL_MIGRATION : ?TaskType = null;
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

TaskType.FULL_MIGRATION = new TaskType('FULL_MIGRATION');
TaskType.INCREMENTAL_MIGRATION = new TaskType('INCREMENTAL_MIGRATION');
TaskType.FULL_INCREMENTAL_MIGRATION = new TaskType('FULL_INCREMENTAL_MIGRATION');

