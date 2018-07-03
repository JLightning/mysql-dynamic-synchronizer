import React from 'react';
import ReactDOM from 'react-dom';

class TaskDetail extends React.Component {

    componentDidMount() {
        $.get(DOMAIN + '/api/task/detail/' + taskId).done(data => {
            if (data.success) {
                console.log(data.data);
                this.setState({task: data.data});
            }
        });
    }

    render() {
        return (<p>Test</p>);
    }
}

if (document.getElementById('taskDetailWrapper') !== null) {
    ReactDOM.render(<TaskDetail/>, document.getElementById('taskDetailWrapper'));
}