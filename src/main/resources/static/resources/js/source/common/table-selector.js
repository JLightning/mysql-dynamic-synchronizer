import React from "react";
import PropTypes from 'prop-types';
import Select, {SelectOption} from "./select";
import mySQLApiClient from "../api-client/mysql-api-client";

export default class TableSelector extends React.Component {

    constructor(props) {
        super(props);
        this.state = {servers: [], databases: [], tables: [], serverId: 0, database: '', table: ''};
        if (this.props.table != null) {
            this.state.serverId = this.props.table.serverId;
            this.state.database = this.props.table.database;
            this.state.table = this.props.table.table;

            this.serverSelected(this.state.serverId);
            this.databaseSelected(this.state.database);
        }
    }

    componentDidMount() {
        this.getServers();
    }

    getServers() {
        mySQLApiClient.getServers().done(data => this.setState({servers: data}));
    }

    serverSelected(serverId) {
        mySQLApiClient.getDatabasesForServer(serverId).done(data => this.setState({
            databases: data,
            serverId: serverId
        }));
    }

    databaseSelected(database) {
        mySQLApiClient.getTablesForServerAndDatabase(this.state.serverId, database).done(data => this.setState({
            tables: data,
            database: database
        }));
    }

    renderServerAndDb() {
        return [
            <div className="row" key="server">
                <Select className='fullWidth col'
                        options={this.state.servers.map(server => new SelectOption(server.serverId, server.name + ' mysql://' + server.host + ':' + server.port))}
                        btnTitle={'Select Server'}
                        value={this.state.serverId}
                        onItemClick={option => this.serverSelected(option.id)}/>
            </div>,
            <div className="row" key="db">
                <Select className='mt-3 fullWidth col'
                        options={this.state.databases.map(db => new SelectOption(db, db))}
                        btnTitle={'Select Database'}
                        value={this.state.database}
                        onItemClick={option => this.databaseSelected(option.value)}/>
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
                            options={this.state.tables.map(table => new SelectOption(table, table))}
                            btnTitle={'Select Table'}
                            value={this.state.table}
                            onItemClick={option => {
                                this.setState({table: option.value});
                                if (this.props.onSelected !== undefined) {
                                    this.props.onSelected({
                                        serverId: this.state.serverId,
                                        database: this.state.database,
                                        table: option.value
                                    })
                                }
                            }}/>
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