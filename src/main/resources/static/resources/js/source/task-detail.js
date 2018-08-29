import React from 'react';
import ReactDOM from 'react-dom';
import taskApiClient from "./api-client/task-api-client";
import Table from "./common/table";

class TaskDetail extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            fullMigrationProgress: 0,
            fullMigrationRunning: false,
            incrementalMigrationRunning: false,
            incrementalMigrationProgress: {insertCount: 0, updateCount: 0, deleteCount: 0}
        };
    }

    componentDidMount() {
        taskApiClient.getFullMigrationTaskProgressWs(taskId, event => {
            if (this.state.fullMigrationProgress !== event.progress) {
                this.setState({fullMigrationProgress: event.progress})
            }
            if (this.state.fullMigrationRunning !== event.running) {
                this.setState({fullMigrationRunning: event.running})
            }
        });

        taskApiClient.getIncrementalMigrationProgressWs(taskId, event => {
            if (event.delta) {
                this.setState({
                    incrementalMigrationRunning: event.running, incrementalMigrationProgress: {
                        insertCount: event.insertCount + this.state.incrementalMigrationProgress.insertCount,
                        updateCount: event.updateCount + this.state.incrementalMigrationProgress.updateCount,
                        deleteCount: event.deleteCount + this.state.incrementalMigrationProgress.deleteCount
                    }
                });
            } else {
                this.setState({
                    incrementalMigrationRunning: event.running, incrementalMigrationProgress: {
                        insertCount: event.insertCount || this.state.incrementalMigrationProgress.insertCount,
                        updateCount: event.updateCount || this.state.incrementalMigrationProgress.updateCount,
                        deleteCount: event.deleteCount || this.state.incrementalMigrationProgress.deleteCount
                    }
                });
            }
        });

        taskApiClient.getTaskAction(taskId).done(data => this.setState({task: data}));
    }

    render() {
        const task = this.state.task;
        if (this.state.task == null) return <p>Loading...</p>;
        return (
            <div className="container-fluid">
                <div className="row mt-3">
                    <div className="col-4">
                        <p className="h2">Task: {this.state.task.taskName}</p>
                        <p>Task type: MySQL to MySQL full migration and incremental migration</p>
                    </div>
                </div>
                <div className="row mt-1">
                    <div className="col-3 pl-3 vertial-center">
                        <div>Full migration progress:</div>
                    </div>
                    <div className="col-8 vertial-center">
                        <div className="progress ml-1" style={{height: "24px"}}>
                            <div className="progress-bar progress-bar-striped progress-bar-animated" role="progressbar"
                                 aria-valuenow="75" aria-valuemin="0" aria-valuemax="100"
                                 style={{width: this.state.fullMigrationProgress + '%'}}>{this.state.fullMigrationProgress}%
                            </div>
                        </div>
                    </div>
                    <div className="col-1 vertial-center">
                        {
                            (() => {
                                if (this.state.fullMigrationRunning)
                                    return <button type="button"
                                                   className="float-right btn btn-primary btn-sm ml-1">Stop</button>;
                                return (
                                    <button type="button" className="float-right btn btn-primary btn-sm"
                                            onClick={() => taskApiClient.startFullMigrationTask(taskId)}>
                                        Start
                                    </button>
                                );
                            })()
                        }
                    </div>
                </div>
                <div className="row mt-3">
                    <div className="col-6">
                        <p className="h4">Mapping:</p>
                        <Table
                            th={["Source: " + task.source.database + ":" + task.source.table, "Target: " + task.target.database + ":" + task.target.table]}>
                            {this.state.task.mapping.map(o => (
                                <tr>
                                    <td>{o.sourceField}</td>
                                    <td>{o.targetField}</td>
                                </tr>
                            ))}
                        </Table>
                        <p className="h4">Filters:</p>
                        <Table th={["Filter"]}>
                            {this.state.task.filters.map(o => (
                                <tr>
                                    <td>{o}</td>
                                </tr>
                            ))}
                        </Table>
                    </div>
                    <div className="col-6">
                        <div className="row">
                            <div className="col-6">
                                <p className="h4">Incremental migration:</p>
                            </div>
                            <div className="col-6">
                                {
                                    (() => {
                                        if (this.state.incrementalMigrationRunning)
                                            return (
                                                <button type="button"
                                                        className="float-right btn btn-primary btn-sm ml-1"
                                                        onClick={() => taskApiClient.stopIncrementalMigrationTask(taskId)}>Stop
                                                </button>
                                            );
                                        return (
                                            <button type="button" className="float-right btn btn-primary btn-sm"
                                                    onClick={() => taskApiClient.startIncrementalMigrationTask(taskId)}>
                                                Start
                                            </button>
                                        )
                                    })()
                                }
                            </div>
                        </div>
                        <Table th={["Insert", "Update", "Delete"]}>
                            <tr>
                                <td>{this.state.incrementalMigrationProgress.insertCount}</td>
                                <td>{this.state.incrementalMigrationProgress.updateCount}</td>
                                <td>{this.state.incrementalMigrationProgress.deleteCount}</td>
                            </tr>
                        </Table>
                    </div>
                </div>
            </div>
        );
    }
}

if (document.getElementById('taskDetailWrapper') !== null) {
    ReactDOM.render(<TaskDetail/>, document.getElementById('taskDetailWrapper'));
}