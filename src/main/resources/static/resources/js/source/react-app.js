import React, {Fragment} from "react";
import ReactDOM from "react-dom";
import Layout from "./layout";
import {BrowserRouter, Route} from "react-router-dom";
import TaskCreateChooser from "./pages/task/task-create-chooser";
import TaskCreateMysql2Mysql from "./pages/task/mysql2mysql/task-create";
import TaskCreateMysal2Redis from "./pages/task/mysql2redis/task-create";
import TaskList from "./pages/task/task-list";
import TaskDetail from "./pages/task/mysql2mysql/task-detail";
import TableStructureSync from "./table-structure-sync";
import MysqlServerList from "./pages/servers/mysql/list";
import RedisServerAdd from "./pages/servers/redis/add";
import RedisServerList from "./pages/servers/redis/list";
import MySQLServerAdd from "./pages/servers/mysql/add";

class App extends React.Component {

    render() {
        return (
            <BrowserRouter>
                <Fragment>
                    <Layout>
                        <Route exact path="/task/list" component={TaskList}/>
                        <Route exact path="/task/create" component={TaskCreateChooser}/>
                        <Route path="/task/create/mysql-to-mysql" component={TaskCreateMysql2Mysql}/>
                        <Route path="/task/edit/mysql-to-mysql/:taskId" component={TaskCreateMysql2Mysql}/>

                        <Route path="/task/create/mysql-to-redis" component={TaskCreateMysal2Redis}/>

                        <Route path="/task/detail/:taskId" component={TaskDetail}/>

                        <Route path="/server/mysql/list" component={MysqlServerList}/>
                        <Route path="/server/mysql/add" component={MySQLServerAdd}/>
                        <Route path="/server/mysql/edit/:serverId" component={MySQLServerAdd}/>

                        <Route path="/server/redis/list" component={RedisServerList}/>
                        <Route path="/server/redis/add" component={RedisServerAdd}/>
                        <Route path="/server/redis/edit/:serverId" component={RedisServerAdd}/>

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