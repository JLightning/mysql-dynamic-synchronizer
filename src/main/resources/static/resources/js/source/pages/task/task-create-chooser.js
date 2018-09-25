import React from 'react';
import ReactDOM from "react-dom";
import {observable} from 'mobx';
import {observer} from 'mobx-react';
import {Link} from "react-router-dom";

@observer
export default class TaskCreateChooser extends React.Component {

    @observable type;

    render() {
        return (
            <div>
                <div className="list-group mt-3">
                    <Link to="mysql-to-mysql" className="list-group-item list-group-item-action">MySQL to MySQL</Link>
                    <Link to="mysql-to-redis" className="list-group-item list-group-item-action">MySQL to Redis</Link>
                </div>
            </div>
        )
    }
}

if (document.getElementById('taskCreateWrapper') !== null) {
    ReactDOM.render(<TaskCreateChooser/>, document.getElementById('taskCreateWrapper'));
}