import * as React from "react";
import {Fragment} from "react";
import {Link} from "react-router-dom";
import {observable} from 'mobx';
import {observer} from 'mobx-react';
import DevTools from 'mobx-react-devtools';

export default class Layout extends React.Component {

    render() {
        return (
            <Fragment>
                <div className="container-fluid">
                    <nav className="navbar navbar-expand-lg navbar-light bg-light">
                        <a className="navbar-brand">MySQL Dynamic Synchronizer</a>
                        <button className="navbar-toggler" type="button" data-toggle="collapse"
                                data-target="#navbarSupportedContent"
                                aria-controls="navbarSupportedContent" aria-expanded="false"
                                aria-label="Toggle navigation">
                            <span className="navbar-toggler-icon"></span>
                        </button>

                        <div className="collapse navbar-collapse" id="navbarSupportedContent">
                            <ul className="navbar-nav mr-auto">
                                <li className="nav-item active">
                                    <a className="nav-link">Dashboard <span
                                        className="sr-only">(current)</span></a>
                                </li>
                                <li className="nav-item dropdown">
                                    <a className="nav-link dropdown-toggle" href="#" id="taskDropdown" role="button"
                                       data-toggle="dropdown"
                                       aria-haspopup="true" aria-expanded="false">
                                        Tasks
                                    </a>
                                    <div className="dropdown-menu" aria-labelledby="navbarDropdown">
                                        <Link to="/task/list/" className="dropdown-item">List</Link>
                                        <Link to="/task/create/" className="dropdown-item">Create task</Link>
                                    </div>
                                </li>
                                <li className="nav-item dropdown">
                                    <a className="nav-link dropdown-toggle" href="#" id="serverDropdown" role="button"
                                       data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                        MySQL Servers
                                    </a>
                                    <div className="dropdown-menu" aria-labelledby="navbarDropdown">
                                        <a className="dropdown-item"
                                        >List</a>
                                        <a className="dropdown-item">Add
                                            Server</a>
                                    </div>
                                </li>
                                <li className="nav-item dropdown">
                                    <a className="nav-link dropdown-toggle" href="#" id="toolDropdown" role="button"
                                       data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                        Tools
                                    </a>
                                    <div className="dropdown-menu" aria-labelledby="navbarDropdown">
                                        <Link to="/util/table-structure-sync/" className="dropdown-item">Table Structure Sync</Link>
                                    </div>
                                </li>
                            </ul>
                        </div>
                    </nav>
                    <div className="content">
                        {this.props.children}
                    </div>
                </div>
                <Modal/>
                <DevTools/>
            </Fragment>
        )
    }
}

@observer
class Modal extends React.Component {

    modalElm = null;
    @observable modalContent = '';

    componentDidMount() {
        window.showError = this.showError.bind(this);
    }

    componentWillUnmount() {
        window.showError = message => alert(message);
    }

    showError(message) {
        this.modalContent = message;
        $(this.modalElm).modal();
    }

    render() {
        return (
            <div className="modal" tabIndex="-1" id="errorMessageModal" role="dialog"
                 ref={modalElm => this.modalElm = modalElm}>
                <div className="modal-dialog" role="document">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 className="modal-title">Error Messages</h5>
                            <button type="button" className="close" data-dismiss="modal" aria-label="Close">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>
                        <div className="modal-body modal-error-message">
                            <p>{this.modalContent}</p>
                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-secondary" data-dismiss="modal">Close
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}