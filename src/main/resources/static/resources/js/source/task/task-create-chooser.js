import React from 'react';
import ReactDOM from "react-dom";
import TaskCreate from "./mysql2mysql/task-create";

const type = {
    MYSQL2MYSQL: 'mysql2mysql'
}

class TaskCreateChooser extends React.Component {

    constructor(props) {
        super(props);
        this.state = {type: ''};
    }

    setType(e, type) {
        e.preventDefault();
        this.setState({type});
    }

    render() {
        if (this.state.type === type.MYSQL2MYSQL) {
            return <TaskCreate/>
        }
        return (
            <div className="list-group mt-3">
                <a href="#" className="list-group-item list-group-item-action" onClick={e => this.setType(e, type.MYSQL2MYSQL)}>MySQL to MySQL</a>
                <a href="#" className="list-group-item list-group-item-action">MySQL to Redis</a>
            </div>
        );
    }
}

if (document.getElementById('taskCreateWrapper') !== null) {
    ReactDOM.render(<TaskCreateChooser/>, document.getElementById('taskCreateWrapper'));
}