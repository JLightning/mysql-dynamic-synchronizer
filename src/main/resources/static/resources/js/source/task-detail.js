import React from 'react';
import ReactDOM from 'react-dom';

class TaskDetail extends React.Component {

    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount() {
        $.get(DOMAIN + '/api/task/detail/' + taskId).done(data => {
            if (data.success) {
                this.setState({task: data.data});
            }
        });
    }

    render() {
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
                    <div className="col-3 pl-3">
                        <p>Full migration progress:</p>
                    </div>
                    <div className="col-9">
                        <div className="progress ml-1" style={{height: "24px"}}>
                            <div className="progress-bar progress-bar-striped progress-bar-animated" role="progressbar"
                                 aria-valuenow="75" aria-valuemin="0" aria-valuemax="100" style={{width: '75%'}}>75%
                            </div>
                        </div>
                    </div>
                </div>
                <div className="row mt-3">
                    <div className="col-6">
                        <p className="h4">Mapping:</p>
                        <table className="table">
                            <thead>
                            <tr>
                                <th scope="col">Source</th>
                                <th scope="col">Target</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td>id</td>
                                <td>id</td>
                            </tr>
                            <tr>
                                <td>created_at</td>
                                <td>created_at</td>
                            </tr>
                            <tr>
                                <td>updated_at</td>
                                <td>updated_at</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        );
    }
}

if (document.getElementById('taskDetailWrapper') !== null) {
    ReactDOM.render(<TaskDetail/>, document.getElementById('taskDetailWrapper'));
}