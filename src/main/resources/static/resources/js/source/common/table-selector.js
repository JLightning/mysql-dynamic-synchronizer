import React from "react";
import PropTypes from 'prop-types';
import Select, {SelectOption} from "./select";
import mySQLApiClient from "../api-client/mysql-api-client";
import {observer} from 'mobx-react';
import {autorun, observable} from "mobx";

@observer
export default class TableSelector extends React.Component {

    @observable servers = [];
    @observable databases = [];
    @observable tables = [];
    autorunDisposer = [];

    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.getServers();

        this.autorunDisposer.push(autorun(() => this.serverSelected(this.props.table.serverId)));
        this.autorunDisposer.push(autorun(() => this.databaseSelected(this.props.table.database)));
    }

    componentWillUnmount() {
        this.autorunDisposer.forEach(disposer => disposer());
    }

    getServers() {
        mySQLApiClient.getServers().done(data => this.servers = data);
    }

    serverSelected(serverId) {
        if (typeof serverId === 'undefined' || serverId === null || serverId === 0) return;

        mySQLApiClient.getDatabasesForServer(serverId).done(data => {
            this.databases = data;
        });
    }

    databaseSelected(database) {
        if (typeof database === 'undefined' || database === null || database === '') return;

        mySQLApiClient.getTablesForServerAndDatabase(this.props.table.serverId, database).done(data => {
            this.tables = data;
        });
    }

    renderServerAndDb() {
        return [
            <div className="row" key="server">
                <Select className='fullWidth col'
                        options={this.servers.map(server => new SelectOption(server.serverId, server.name + ' mysql://' + server.host + ':' + server.port))}
                        btnTitle={'Select Server'}
                        value={this.props.table.serverId}
                        onItemClick={option => this.props.table.serverId = option.id}/>
            </div>,
            <div className="row" key="db">
                <Select className='mt-3 fullWidth col'
                        options={this.databases.map(db => new SelectOption(db, db))}
                        btnTitle={'Select Database'}
                        value={this.props.table.database}
                        onItemClick={option => this.props.table.database = option.value}/>
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
                            value={this.props.table.table}
                            onItemClick={option => this.props.table.table = option.value}/>
                </div>
            </div>
        );
    }
}

TableSelector.propTypes = {
    table: PropTypes.object,
    title: PropTypes.string.isRequired
};