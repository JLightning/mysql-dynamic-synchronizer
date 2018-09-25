import React from 'react';
import {computed, observable} from 'mobx';
import {observer} from 'mobx-react';
import redisApiClient from "../../../api-client/redis-api-client";

@observer
export default class RedisServerAdd extends React.Component {

    @observable server = {
        name: '',
        host: '',
        port: '',
        username: '',
        password: '',
    };

    @computed get readyToSubmit() {
        return this.server.name !== '' && this.server.host !== '' && this.server.port !== '';
    }

    submit(e) {
        e.preventDefault();
        redisApiClient.create(this.server).done(data => location.href = '/server/redis/list/');
    }

    render() {
        return (
            <div className="container mt-3">
                <form>
                    <input type="hidden" name="serverId"/>
                    <div className="form-group">
                        <label htmlFor="name">Name</label>
                        <input type="text" className="form-control" id="name" name="name" value={this.server.name}
                               onChange={e => this.server.name = e.target.value}
                               placeholder="Enter Reminding Name"/>
                    </div>
                    <div className="form-group">
                        <label htmlFor="host">Host</label>
                        <input type="text" className="form-control" id="host" name="host" placeholder="Enter Host"
                               value={this.server.host} onChange={e => this.server.host = e.target.value}/>
                    </div>
                    <div className="form-group">
                        <label htmlFor="host">Port</label>
                        <input type="text" className="form-control" id="port" name="port" placeholder="Enter Port"
                               value={this.server.port} onChange={e => this.server.port = e.target.value}/>
                    </div>
                    <div className="form-group">
                        <label htmlFor="username">Username</label>
                        <input type="text" className="form-control" id="username" name="username"
                               value={this.server.username} onChange={e => this.server.username = e.target.value}
                               placeholder="Enter Username"/>
                    </div>
                    <div className="form-group">
                        <label htmlFor="password">Password</label>
                        <input type="password" className="form-control" id="password" name="password"
                               value={this.server.password} onChange={e => this.server.password = e.target.value}
                               placeholder="Enter Password"/>
                    </div>
                    <button onClick={e => this.submit(e)} type="submit" className="btn btn-primary" disabled={!this.readyToSubmit}>Submit</button>
                </form>
            </div>
        );
    }
}