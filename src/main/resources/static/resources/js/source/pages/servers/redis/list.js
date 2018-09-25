import React, {Fragment} from "react";
import {observer} from 'mobx-react';
import {observable} from 'mobx';
import redisApiClient from "../../../api-client/redis-api-client";
import YesNoModal from "../../../common/yes-no-modal";
import {Link} from "react-router-dom";

@observer
export default class ReidsServerList extends React.Component {

    @observable serverList = [];

    componentDidMount() {
        this.reload();
    }

    reload() {
        redisApiClient.list().done(data => this.serverList = data);
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
                        this.serverList.map(server => <Row key={server.serverId} server={server}
                                                           reload={this.reload.bind(this)}/>)
                    }
                    </tbody>
                </table>
            </div>
        )
    }
}

@observer
class Row extends React.Component {

    @observable showDeleteModal = false;

    render() {
        const server = this.props.server;
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