// @flow


export class MigrationType {

    /**
     * @type {MigrationType}
     */
    static FULL_MIGRATION : ?MigrationType = null;
    /**
     * @type {MigrationType}
     */
    static INCREMENTAL_MIGRATION : ?MigrationType = null;
    /**
     * @type {MigrationType}
     */
    static FULL_INCREMENTAL_MIGRATION : ?MigrationType = null;
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

MigrationType.FULL_MIGRATION = new MigrationType('FULL_MIGRATION');
MigrationType.INCREMENTAL_MIGRATION = new MigrationType('INCREMENTAL_MIGRATION');
MigrationType.FULL_INCREMENTAL_MIGRATION = new MigrationType('FULL_INCREMENTAL_MIGRATION');

