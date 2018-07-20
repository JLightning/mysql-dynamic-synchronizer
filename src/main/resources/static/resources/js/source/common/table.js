import React from 'react';

export default class Table extends React.Component {

    render() {
        return (
            <table className={'table ' + (this.props.className || '')}>
                <thead>
                <tr>
                    {
                        this.props.th.map((_th, idx) => <th scope="col" key={idx}>{_th}</th>)
                    }
                </tr>
                </thead>
                <tbody>{this.props.children}</tbody>
            </table>
        )
    }
}