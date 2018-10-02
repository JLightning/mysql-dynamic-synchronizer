// @flow
import AbstractClient from "./abstract-client";


class MessageApiClient extends AbstractClient {

    /**

     * @returns {{done: (function(function(string[]): *): *), error: (function(*): *)}}
     */
    getErrorMessages(): {done: (string[] => void) => void, error: () => void} {
        return super.get('/api/message/errors', {});
    }
}

const messageApiClient = new MessageApiClient();
export default messageApiClient;
