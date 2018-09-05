import React from 'react';
import PropTypes from 'prop-types';
import Select, {SelectOption} from "./select";
import mySQLApiClient from "../api-client/mysql-api-client";

export default class RedisServerSelector extends React.Component {

    constructor(props) {
        super(props);
        this.state = {servers: []};
    }

    componentDidMount() {
        this.getServers();
    }

    getServers() {
        mySQLApiClient.getServers().done(data => this.setState({servers: data}));
    }

    render() {
        return (
            <div>
                <p>{this.props.title}</p>
                <div className="row" key="server">
                    <Select className='fullWidth col'
                            options={this.state.servers.map(server => new SelectOption(server.serverId, server.name + ' mysql://' + server.host + ':' + server.port))}
                            btnTitle={'Select Server'}
                            value={this.state.serverId}
                            onItemClick={option => {
                                this.props.onSelected(option.id);
                            }}/>
                </div>
            </div>
        )
    }
}

RedisServerSelector.propTypes = {
    title: PropTypes.string.isRequired,
    onSelected: PropTypes.func.isRequired,
}