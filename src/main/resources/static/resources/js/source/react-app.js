import React from "react";
import {Fragment} from "react";
import ReactDOM from "react-dom";
import Layout from "./layout";
import {BrowserRouter, Route} from "react-router-dom";
import TaskCreateChooser from "./task/task-create-chooser";
import TaskCreateMysql2Mysql from "./task/mysql2mysql/task-create";
import TaskList from "./task/task-list";
import TaskDetail from "./task/mysql2mysql/task-detail";
import TableStructureSync from "./table-structure-sync";
import MySQLServerList from "./servers/mysql-server-list";

class App extends React.Component {

    render() {
        return (
            <BrowserRouter>
                <Fragment>
                    <Layout>
                        <Route exact path="/task/list" component={TaskList}/>
                        <Route exact path="/task/create" component={TaskCreateChooser}/>
                        <Route path="/task/create/mysql-to-mysql" component={TaskCreateMysql2Mysql}/>
                        <Route path="/task/detail/:taskId" component={TaskDetail}/>

                        <Route path="/server/mysql-server-list" component={MySQLServerList}/>

                        <Route path="/util/table-structure-sync" component={TableStructureSync}/>
                    </Layout>
                </Fragment>
            </BrowserRouter>
        )
    }
}

if (document.getElementById('reactWrapper') !== null) {
    ReactDOM.render(<App/>, document.getElementById('reactWrapper'));
}