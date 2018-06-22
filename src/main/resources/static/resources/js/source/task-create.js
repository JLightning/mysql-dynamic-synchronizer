import React from 'react';
import ReactDOM from 'react-dom';
import Select from "./common/select";

class TaskCreate extends React.Component {

    render() {
        return (
            <div className="container mt-3">
                <form>
                    <div className="form-group">
                        <label htmlFor="name">Name</label>
                        <input type="text" className="form-control" id="name" name="name"
                               placeholder="Enter Task Name"/>
                    </div>

                    <Select options={[]} btnTitle={'Select Server'}/>
                </form>
            </div>
        )
    }
}

if (document.getElementById('taskCreateWrapper') !== null) {
    ReactDOM.render(<TaskCreate/>, document.getElementById('taskCreateWrapper'));
}