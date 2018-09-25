import React from "react";
import {observer} from 'mobx-react';
import {observable} from 'mobx';
import mySQLApiClient from "../../api-client/mysql-api-client";

@observer
export default class MySQLServerList extends React.Component {

    @observable serverList = [];

    componentDidMount() {
        mySQLApiClient.getServers().done(data => this.serverList = data);
    }

    render() {
        return (
            <div className="container mt-3">
                <table className="table">
                    <thead>
                    <tr>
                        <th scope="col">#</th>
                        <th scope="col">Name</th>
                        <th scope="col">Host</th>
                        <th scope="col">Port</th>
                        <th scope="col">Action</th>
                    </tr>
                    </thead>
                    <tbody>
                    {
                        this.serverList.map(server => <Row key={server.serverId} server={server}/>)
                    }
                    </tbody>
                </table>
            </div>
        )
    }
}

@observer
class Row extends React.Component {
    render() {
        const server = this.props.server;
        return (
            <tr>
                <th scope="row">{server.serverId}</th>
                <td>{server.name}</td>
                <td>{server.host}</td>
                <td>{server.port}</td>
                <td>
                    <a className="text-white btn btn-primary btn-sm">Edit</a>
                    <a className="text-white btn btn-danger btn-sm delete-server ml-1" href="#">Remove</a>
                </td>
            </tr>
        );
    }
}