import React from 'react';
import ReactDOM from "react-dom";
import {default as TaskCreateMysql2Mysql} from "./mysql2mysql/task-create";
import {default as TaskCreateMysql2Redis} from "./mysql2redis/task-create";
import {observable} from 'mobx';
import {observer} from 'mobx-react';

const type = {
    MYSQL_2_MYSQL: 'mysql2mysql',
    MYSQL_2_REDIS: 'mysql2redis'
};

@observer class TaskCreateChooser extends React.Component {

    @observable type;

    setType(e, type) {
        e.preventDefault();
        this.type = type;
    }

    render() {
        switch (this.type) {
            case type.MYSQL_2_MYSQL:
                return <TaskCreateMysql2Mysql/>;
            case type.MYSQL_2_REDIS:
                return <TaskCreateMysql2Redis/>;
            default:
                return (
                    <div className="list-group mt-3">
                        <a href="#" className="list-group-item list-group-item-action"
                           onClick={e => this.setType(e, type.MYSQL_2_MYSQL)}>MySQL to MySQL</a>
                        <a href="#" className="list-group-item list-group-item-action"
                           onClick={e => this.setType(e, type.MYSQL_2_REDIS)}>MySQL to Redis</a>
                    </div>
                );
        }
    }
}

if (document.getElementById('taskCreateWrapper') !== null) {
    ReactDOM.render(<TaskCreateChooser/>, document.getElementById('taskCreateWrapper'));
}