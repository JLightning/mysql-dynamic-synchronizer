import * as React from "react";
import taskListApiClient from "../api-client/task-list-api-client";
import {observer} from 'mobx-react';
import {observable} from 'mobx';

@observer
export default class TaskList extends React.Component {

    @observable taskList = [];

    componentDidMount() {
        taskListApiClient.getAllTasks().done(data => this.taskList = data);
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
                        {this.taskList.map(task => <Row key={task.taskId} task={task}/>)}
                    </tbody>
                </table>
            </div>
        )
    }
}

@observer
class Row extends React.Component {

    render() {
        const task = this.props.task;
        return (
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
                        <a className="text-white btn btn-primary btn-sm">Detail</a>
                        <a className="btn btn-danger btn-sm delete-task ml-1" href="#">Remove</a>
                    </div>
                </td>
            </tr>
        );
    }
}