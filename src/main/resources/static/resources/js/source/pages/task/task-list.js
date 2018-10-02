import * as React from "react";
import {Fragment} from "react";
import {observer} from 'mobx-react';
import {observable} from 'mobx';
import {Link} from "react-router-dom";
import YesNoModal from "../../common/yes-no-modal";
import taskApiClient from "../../api-client/task-api-client";

@observer
export default class TaskList extends React.Component {

    @observable taskList = [];

    componentDidMount() {
        this.reload();
    }

    reload() {
        taskApiClient.list().done(data => this.taskList = data);
    }

    render() {
        return (
            <div className="container-fluid mt-3">
                <table className="table">
                    <thead>
                    <tr>
                        <th scope="col">#</th>
                        <th scope="col">Name</th>
                        <th scope="col">Source Server</th>
                        <th scope="col">Source Database</th>
                        <th scope="col">Source Table</th>
                        <th scope="col">Target Server</th>
                        <th scope="col">Target Database</th>
                        <th scope="col">Target Table</th>
                        <th scope="col">Mapping</th>
                        <th scope="col">Action</th>
                    </tr>
                    </thead>
                    <tbody>
                    {this.taskList.map(task => <Row key={task.taskId} task={task} reload={this.reload.bind(this)}/>)}
                    </tbody>
                </table>
            </div>
        )
    }
}

@observer
class Row extends React.Component {

    @observable showModal = false;

    render() {
        const task = this.props.task;
        return (
            <Fragment>
                <tr>
                    <th scope="row">{task.taskId}</th>
                    <td>{task.taskName}</td>
                    <td>{task.source.serverId}</td>
                    <td>{task.source.database}</td>
                    <td>{task.source.table}</td>
                    <td>{task.target.serverId}</td>
                    <td>{task.target.database}</td>
                    <td>{task.target.table}</td>
                    <td>TMP</td>
                    <td>
                        <div>
                            <Link to={"/task/detail/" + task.taskId}
                                  className="text-white btn btn-primary btn-sm">Detail</Link>
                            <a className="text-white btn btn-danger btn-sm delete-task ml-1"
                               onClick={e => this.showModal = true}>Remove</a>
                        </div>
                    </td>
                </tr>

                {
                    this.showModal ? <YesNoModal title="Confirm Delete" onHide={e => this.showModal = false}
                                                 onYes={e => this.props.reload()}>
                        Are you sure you want to delete task {task.taskName} (#{task.taskId})?
                    </YesNoModal> : null
                }
            </Fragment>
        );
    }
}