import AbstractClient from "./abstract-client";

class MessageApiClient extends AbstractClient {

    /**

     * @returns {{done: (function(function(string[]): *): *), error: (function(*): *)}}
     */
    getErrorMessages() {
        return super.get('/api/message/errors', {});
    }
}

const messageApiClient = new MessageApiClient();
export default messageApiClient;
