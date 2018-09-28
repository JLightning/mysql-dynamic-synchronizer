export default class TaskType {

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

TaskType.FULL_MIGRATION = new TaskType('FULL_MIGRATION');
TaskType.INCREMENTAL_MIGRATION = new TaskType('INCREMENTAL_MIGRATION');
TaskType.FULL_INCREMENTAL_MIGRATION = new TaskType('FULL_INCREMENTAL_MIGRATION');
TaskType.prototype.toJSON = function() { return this.name;};

