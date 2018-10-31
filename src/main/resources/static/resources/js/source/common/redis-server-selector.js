import React from 'react';
import PropTypes from 'prop-types';
import Select, {SelectOption} from "./select";
import redisApiClient from "../api-client/redis-api-client";
import {observable} from "mobx";
import {observer} from 'mobx-react';

@observer
export default class RedisServerSelector extends React.Component {

    @observable servers = [];

    constructor(props) {
        super(props);
        this.state = {servers: []};
    }

    componentDidMount() {
        this.getServers();
    }

    getServers() {
        redisApiClient.list().done(data => this.servers = data);
    }

    render() {
        return (
            <div>
                <p>{this.props.title}</p>
                <div className="row">
                    <Select className='fullWidth col'
                            options={this.servers.map(server => new SelectOption(server.serverId, server.name + ' mysql://' + server.host + ':' + server.port))}
                            btnTitle={'Select Server'}
                            value={this.serverId}
                            onItemClick={option => this.props.table.serverId = option.id}
                    />
                </div>
                <div className="row">
                    <Select className='fullWidth col mt-3'
                            options={[new SelectOption('string', 'String'), new SelectOption('list', 'List')]}
                            btnTitle={'Select key type'}
                            onItemClick={option => this.props.table.keyType = option.id}
                    />
                </div>
            </div>
        )
    }
}

RedisServerSelector.propTypes = {
    title: PropTypes.string.isRequired,
}