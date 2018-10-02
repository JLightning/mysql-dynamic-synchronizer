import React from 'react';
import taskApiClient from "../../../api-client/task-api-client";
import Table from "../../../common/table";
import {observable} from 'mobx';
import {observer} from 'mobx-react';
import YesNoModal from "../../../common/yes-no-modal";
import {TaskDTO} from "../../../dto/task-dto";
import {FullMigrationProgressDTO, IncrementalMigrationProgressDTO} from "../../../dto/common";

@observer
export default class TaskDetail extends React.Component {

    taskId = 0;
    @observable task: ?TaskDTO = null;
    @observable incrementalMigrationProgress: IncrementalMigrationProgressDTO = new IncrementalMigrationProgressDTO(false, 0, 0, 0, false);
    @observable incrementalMigrationRunning = false;
    @observable fullMigrationProgress: FullMigrationProgressDTO = new FullMigrationProgressDTO(false, 0);
    @observable showTruncateModal = false;

    constructor(props) {
        super(props);
        this.taskId = props.match.params.taskId;
    }

    componentDidMount() {
        taskApiClient.getFullMigrationTaskProgressWs(this.taskId, (event: FullMigrationProgressDTO) => this.fullMigrationProgress = event);

        taskApiClient.getIncrementalMigrationProgressWs(this.taskId, event => {
            if (event.delta) {
                this.incrementalMigrationRunning = event.running;
                this.incrementalMigrationProgress.insertCount += event.insertCount;
                this.incrementalMigrationProgress.updateCount += event.updateCount;
                this.incrementalMigrationProgress.deleteCount += event.deleteCount;
            } else {
                this.incrementalMigrationRunning = event.running;
                this.incrementalMigrationProgress.insertCount = this.incrementalMigrationProgress.insertCount || event.insertCount;
                this.incrementalMigrationProgress.updateCount = this.incrementalMigrationProgress.updateCount || event.updateCount;
                this.incrementalMigrationProgress.deleteCount = this.incrementalMigrationProgress.deleteCount || event.deleteCount;
            }
        });

        taskApiClient.getTaskAction(this.taskId).done(data => this.task = data);
    }

    render() {
        if (this.task == null) return <p>Loading...</p>;
        return (
            <div className="container-fluid">
                {
                    this.showTruncateModal ? <YesNoModal title="Confirm Truncate" onHide={e => this.showTruncateModal = false}
                                                 onYes={e => taskApiClient.truncateAndStartFullMigrationTask(this.taskId)}>
                        Are you sure you want to truncate and migrate?
                    </YesNoModal> : null
                }
                <div className="row mt-3">
                    <div className="col-4">
                        <p className="h2">Task: {this.task.taskName}</p>
                        <p>Task type: MySQL to MySQL full migration and incremental migration</p>
                    </div>
                </div>
                <div className="row mt-1">
                    <div className="col-3 pl-3 vertial-center">
                        <div>Full migration progress:</div>
                    </div>
                    <div className="col-7 vertial-center">
                        <div className="progress ml-1" style={{height: "24px"}}>
                            <div className="progress-bar progress-bar-striped progress-bar-animated" role="progressbar"
                                 aria-valuenow="75" aria-valuemin="0" aria-valuemax="100"
                                 style={{width: this.fullMigrationProgress.progress + '%'}}>{this.fullMigrationProgress.progress}%
                            </div>
                        </div>
                    </div>
                    <div className="col-2 vertial-center">
                        {
                            (() => {
                                if (this.fullMigrationProgress.running)
                                    return <button type="button"
                                                   className="float-right btn btn-primary btn-sm ml-1">Stop</button>;
                                return (
                                    <div>
                                        <button type="button" className="float-right btn btn-primary btn-sm"
                                                onClick={() => taskApiClient.startFullMigrationTask(this.taskId)}>
                                            Start
                                        </button>
                                        <button type="button" className="float-right btn btn-primary btn-sm mr-1"
                                                onClick={() => this.showTruncateModal = true}>
                                            Truncate and start
                                        </button>
                                    </div>
                                );
                            })()
                        }
                    </div>
                </div>
                <div className="row mt-3">
                    <div className="col-6">
                        <p className="h4">Mapping:</p>
                        <Table
                            th={["Source: " + this.task.source.database + ":" + this.task.source.table, "Target: " + this.task.target.database + ":" + this.task.target.table]}>
                            {this.task.mapping.map(o => (
                                <tr>
                                    <td>{o.sourceField}</td>
                                    <td>{o.targetField}</td>
                                </tr>
                            ))}
                        </Table>
                        <p className="h4">Filters:</p>
                        <Table th={["Filter"]}>
                            {this.task.filters.map((o, idx) => (
                                <tr key={idx}>
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
                                        if (this.incrementalMigrationRunning)
                                            return (
                                                <button type="button"
                                                        className="float-right btn btn-primary btn-sm ml-1"
                                                        onClick={() => taskApiClient.stopIncrementalMigrationTask(this.taskId)}>Stop
                                                </button>
                                            );
                                        return (
                                            <button type="button" className="float-right btn btn-primary btn-sm"
                                                    onClick={() => taskApiClient.startIncrementalMigrationTask(this.taskId)}>
                                                Start
                                            </button>
                                        )
                                    })()
                                }
                            </div>
                        </div>
                        <Table th={["Insert", "Update", "Delete"]}>
                            <tr>
                                <td>{this.incrementalMigrationProgress.insertCount}</td>
                                <td>{this.incrementalMigrationProgress.updateCount}</td>
                                <td>{this.incrementalMigrationProgress.deleteCount}</td>
                            </tr>
                        </Table>
                    </div>
                </div>
            </div>
        );
    }
}