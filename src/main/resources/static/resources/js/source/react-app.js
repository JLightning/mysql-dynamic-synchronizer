import React from "react";
import {Fragment} from "react";
import ReactDOM from "react-dom";
import Layout from "./layout";
import {BrowserRouter, Route} from "react-router-dom";
import TaskCreateChooser from "./pages/task/task-create-chooser";
import TaskCreateMysql2Mysql from "./pages/task/mysql2mysql/task-create";
import TaskList from "./pages/task/task-list";
import TaskDetail from "./pages/task/mysql2mysql/task-detail";
import TableStructureSync from "./table-structure-sync";
import MysqlServerList from "./pages/servers/mysql/list";
import RedisServerAdd from "./pages/servers/redis/add";

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

                        <Route path="/server/mysql/list" component={MysqlServerList}/>

                        <Route path="/server/redis/add" component={RedisServerAdd}/>

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