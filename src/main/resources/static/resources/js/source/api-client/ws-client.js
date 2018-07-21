const SockJS = require('../lib/sockjs.min');
const Stomp = require('../lib/stomp.min').Stomp;

class WsClient {

    constructor() {
        const socket = new SockJS('/ws');
        this.successCallback = [];
        this.errorCallback = [];
        this.connected = false;
        this.stompClient = Stomp.over(socket);
        this.stompClient.connect('guest', 'guest', this.onWsConnected.bind(this), this.onWsError.bind(this));
    }

    onWsConnected() {
        this.connected = true;
        this.successCallback.forEach(cb => cb());
    }

    onWsError() {
        this.connected = false;
        this.errorCallback.forEach(cb => cb());
    }

    connect(successCb, errorCb) {
        if (this.connected) {
            successCb();
        }
        this.successCallback.push(successCb);
        this.errorCallback.push(errorCb);
    }

    subscribe(channel, callback) {
        const r = () => this.stompClient.subscribe(channel, payload => {
            const data = JSON.parse(payload.body);
            callback(data);
        });
        if (this.connected) {
            r();
        } else {
            this.successCallback.push(r);
        }
    }

    send(channel, body1, body2) {
        this.stompClient.send(channel, body1, body2);
    }
}

const wsClient = new WsClient();
export default wsClient;