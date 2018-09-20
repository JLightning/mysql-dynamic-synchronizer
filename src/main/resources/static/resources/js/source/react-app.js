// import './task/task-create-chooser.js';
// import './task/mysql2mysql/task-detail.js';
// import './table-structure-sync.js';

import * as React from "react";
import ReactDOM from "react-dom";
import Layout from "./layout";
import {BrowserRouter, Route} from "react-router-dom";
import TaskCreateChooser from "./task/task-create-chooser";
import TaskCreateMysql2Mysql from "./task/mysql2mysql/task-create";
import TaskList from "./task/task-list";

class App extends React.Component {

    render() {
        return (
            <BrowserRouter>
                <Layout>
                    <Route exact path="/task/list" component={TaskList}/>
                    <Route exact path="/task/create" component={TaskCreateChooser}/>
                    <Route path="/task/create/mysql-to-mysql" component={TaskCreateMysql2Mysql}/>
                </Layout>
            </BrowserRouter>
        )
    }
}

if (document.getElementById('reactWrapper') !== null) {
    ReactDOM.render(<App/>, document.getElementById('reactWrapper'));
}