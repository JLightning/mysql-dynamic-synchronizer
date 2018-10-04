import React from "react";
import {observer} from 'mobx-react';
import {observable} from 'mobx';
import mySQLApiClient from "../../../api-client/mysql-api-client";
import {MySQLServerDTO} from "../../../dto/my-sqlserver-dto";
import Table from "../../../common/table";

@observer
export default class MysqlServerList extends React.Component {

    @observable serverList: MySQLServerDTO[] = [];

    componentDidMount() {
        mySQLApiClient.getServers().done(data => this.serverList = data);
    }

    render() {
        return (
            <div className="container mt-3">
                <Table th={["#", "Name", "Host", "Port", "Action"]}>
                    {
                        this.serverList.map(server => <Row key={server.serverId} server={server}/>)
                    }
                </Table>
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