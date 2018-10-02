// @flow
import {observable} from 'mobx';

export class FullMigrationProgressDTO {

    /**
     * @type {boolean}
     */
    @observable running: ?boolean = false;
    /**
     * @type {number}
     */
    @observable progress: ?number = 0;

    /**
     * @param running {boolean}
     * @param progress {number}
     */
    constructor(running: ?boolean, progress: ?number) {
        this.running = running;
        this.progress = progress;
    }


}
