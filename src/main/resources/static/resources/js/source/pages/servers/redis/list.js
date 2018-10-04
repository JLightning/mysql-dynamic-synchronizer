import React, {Fragment} from "react";
import {observer} from 'mobx-react';
import {observable} from 'mobx';
import redisApiClient from "../../../api-client/redis-api-client";
import YesNoModal from "../../../common/yes-no-modal";
import {Link} from "react-router-dom";
import {RedisServerDTO} from "../../../dto/redis-server-dto";
import Table from "../../../common/table";

@observer
export default class ReidsServerList extends React.Component {

    @observable serverList: RedisServerDTO[] = [];

    componentDidMount() {
        this.reload();
    }

    reload() {
        redisApiClient.list().done(data => this.serverList = data);
    }

    render() {
        return (
            <div className="container mt-3">
                <Table th={["#", "Name", "Host", "Port", "Action"]}>
                    {
                        this.serverList.map(server => <Row key={server.serverId} server={server}
                                                           reload={this.reload.bind(this)}/>)
                    }
                </Table>
            </div>
        )
    }
}

@observer
class Row extends React.Component {

    @observable showDeleteModal = false;

    render() {
        const server: RedisServerDTO = this.props.server;
        return (
            <Fragment>
                <tr>
                    <th scope="row">{server.serverId}</th>
                    <td>{server.name}</td>
                    <td>{server.host}</td>
                    <td>{server.port}</td>
                    <td>
                        <Link to={"/server/redis/edit/" + server.serverId}
                              className="text-white btn btn-primary btn-sm">Edit</Link>
                        <a className="text-white btn btn-danger btn-sm delete-server ml-1" href="#"
                           onClick={e => this.showDeleteModal = true}>Delete</a>
                    </td>
                </tr>
                {
                    this.showDeleteModal ? <YesNoModal
                        onYes={e => redisApiClient.delete(server.serverId).done(data => this.props.reload())}
                        title="Delete Confirm" onHide={e => this.showDeleteModal = false}>
                        Are you sure you want to delete server {server.serverId}: {server.host}:{server.port}?
                    </YesNoModal> : null
                }
            </Fragment>
        );
    }
}