import React from 'react';
import ReactDOM from 'react-dom';
import Select, {SelectOption} from "./common/select";

class TaskCreate extends React.Component {

    render() {
        return (
            <div className="container mt-3">
                <form>
                    <div className="form-group">
                        <label htmlFor="name">Name</label>
                        <input type="text" className="form-control" id="name" name="name"
                               placeholder="Enter Task Name"/>
                    </div>

                    <div className="row">
                        <div className="col">
                            <TableSelector/>
                        </div>
                        <div className="col">
                            <TableSelector/>
                        </div>
                    </div>
                </form>
            </div>
        )
    }
}

class TableSelector extends React.Component {

    constructor(props) {
        super(props);
        this.state = {servers: [], databases: [], tables: []};
    }

    componentDidMount() {
        this.getServers();
    }

    getServers() {
        $.get('/api/mysql/servers').done((data) => {
            if (data.success) {
                this.setState({servers: data.data});
            }
        });
    }

    getDatabases(option) {
        $.get('/api/mysql/databases?serverId=' + option.id).done((data) => {
            if (data.success) {
                this.setState({databases: data.data, serverId: option.id});
            }
        });
    }

    getTables(option) {
        $.get('/api/mysql/tables?serverId=' + this.state.serverId + "&database=" + option.value).done((data) => {
            if (data.success) {
                this.setState({tables: data.data, databaseName: option.value});
            }
        });
    }

    render() {
        return (
            <div>
                <p>Source Server</p>
                <Select
                    options={this.state.servers.map(server => new SelectOption(server.serverId, server.name + ' mysql://' + server.host + ':' + server.port))}
                    btnTitle={'Select Server'}
                    onItemClick={option => this.getDatabases(option)}/>

                <p className="mt-3">Source Database</p>
                <Select
                    options={this.state.databases.map((db, idx) => new SelectOption(idx, db))}
                    btnTitle={'Select Database'}
                    onItemClick={option => this.getTables(option)}/>

                <p className="mt-3">Source Table</p>
                <Select
                    options={this.state.tables.map((db, idx) => new SelectOption(idx, db))}
                    btnTitle={'Select Database'}
                    onItemClick={option => this.setState({tableName: option.value})}/>
            </div>
        );
    }
}

if (document.getElementById('taskCreateWrapper') !== null) {
    ReactDOM.render(<TaskCreate/>, document.getElementById('taskCreateWrapper'));
}