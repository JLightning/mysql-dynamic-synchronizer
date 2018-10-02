// @flow
import {observable} from 'mobx';

export class IncrementalMigrationProgressDTO {

    /**
     * @type {boolean}
     */
    @observable running: ?boolean = false;
    /**
     * @type {number}
     */
    @observable insertCount: ?number = null;
    /**
     * @type {number}
     */
    @observable updateCount: ?number = null;
    /**
     * @type {number}
     */
    @observable deleteCount: ?number = null;
    /**
     * @type {boolean}
     */
    @observable delta: ?boolean = false;

    /**
     * @param running {boolean}
     * @param insertCount {number}
     * @param updateCount {number}
     * @param deleteCount {number}
     * @param isDelta {boolean}
     */
    constructor(running: ?boolean, insertCount: ?number, updateCount: ?number, deleteCount: ?number, delta: ?boolean) {
        this.running = running;
        this.insertCount = insertCount;
        this.updateCount = updateCount;
        this.deleteCount = deleteCount;
        this.delta = delta;
    }


}
