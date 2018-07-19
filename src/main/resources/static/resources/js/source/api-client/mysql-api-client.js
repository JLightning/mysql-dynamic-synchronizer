import AbstractClient from "./abstract-client";

export default class MySQLApiClient extends AbstractClient {

    getFields(tableInfo) {
        return this.get('/api/mysql/fields', tableInfo);
    }
}