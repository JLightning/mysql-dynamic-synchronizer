import React from "react";
import PropTypes from 'prop-types';
import Select, {SelectOption} from "./select";
import mySQLApiClient from "../api-client/mysql-api-client";
import {observer} from 'mobx-react';
import {observable, reaction} from "mobx";

@observer
export default class TableSelector extends React.Component {

    @observable servers = [];
    @observable databases = [];
    @observable tables = [];
    @observable serverId = 0;
    @observable database = '';
    @observable table = '';

    constructor(props) {
        super(props);

        reaction(() => this.serverId, serverId => this.serverSelected(serverId));
        reaction(() => this.database, database => this.databaseSelected(database));

        if (this.props.table != null) {
            this.serverId = this.props.table.serverId;
            this.database = this.props.table.database;
            this.table = this.props.table.table;
        }

        reaction(() => this.table, table => {
            this.props.onSelected({
                serverId: this.serverId,
                database: this.database,
                table: table
            })
        });
    }

    componentDidMount() {
        this.getServers();
    }

    getServers() {
        mySQLApiClient.getServers().done(data => this.servers = data);
    }

    serverSelected(serverId) {
        mySQLApiClient.getDatabasesForServer(serverId).done(data => {
            this.databases = data;
        });
    }

    databaseSelected(database) {
        mySQLApiClient.getTablesForServerAndDatabase(this.serverId, database).done(data => {
            this.tables = data;
        });
    }

    renderServerAndDb() {
        return [
            <div className="row" key="server">
                <Select className='fullWidth col'
                        options={this.servers.map(server => new SelectOption(server.serverId, server.name + ' mysql://' + server.host + ':' + server.port))}
                        btnTitle={'Select Server'}
                        value={this.serverId}
                        onItemClick={option => this.serverId = option.id}/>
            </div>,
            <div className="row" key="db">
                <Select className='mt-3 fullWidth col'
                        options={this.databases.map(db => new SelectOption(db, db))}
                        btnTitle={'Select Database'}
                        value={this.database}
                        onItemClick={option => this.database = option.value}/>
            </div>
        ]
    }

    render() {
        return (
            <div>
                <p>{this.props.title}</p>
                {this.renderServerAndDb()}
                <div className="row">
                    <Select className='mt-3 fullWidth col'
                            options={this.tables.map(table => new SelectOption(table, table))}
                            btnTitle={'Select Table'}
                            value={this.table}
                            onItemClick={option => this.table = option.value}/>
                </div>
            </div>
        );
    }
}

TableSelector.propTypes = {
    onSelected: PropTypes.func.isRequired,
    table: PropTypes.object,
    title: PropTypes.string.isRequired
};