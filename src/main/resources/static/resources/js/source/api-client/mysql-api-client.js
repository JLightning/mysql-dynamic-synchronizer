import AbstractClient from "./abstract-client";

export default class MySQLApiClient extends AbstractClient {

    getFields(serverId, database, table) {
        return this.get('/api/mysql/fields', {serverId, database, table});
    }

    validateFilter(serverId, database, table, filter) {
        return this.post('/api/mysql/validate-filter', {serverId, database, table, filter});
    }
}