// @flow
import {observable} from 'mobx';

export class RedisKeyTypeDTO {

    /**
     * @type {number}
     */
    @observable serverId: ?number = 0;
    /**
     * @type {string}
     */
    @observable keyType: ?string = '';

    /**
     * @param serverId {number}
     * @param keyType {string}
     */
    constructor(serverId: ?number, keyType: ?string) {
        this.serverId = serverId;
        this.keyType = keyType;
    }


}
