import wsClient from './ws-client';

export default class AbstractClient {

    baseHttpRequest(request) {
        let done = null;
        let error = message => showError(message);
        request.done(data => {
            if (data.success) {
                if (done != null) done(data.data);
            } else {
                error(data.errorMessage);
            }
        })
            .fail(() => {
                error("Network failed");
            });

        return {
            done: f => done = f,
            error: f => error = f
        }
    }

    get(uri, data) {
        return this.baseHttpRequest($.get(DOMAIN + uri, data));
    }

    post(uri, data) {
        return this.baseHttpRequest($.post(DOMAIN + uri, data));
    }

    delete(uri, data) {
        return this.baseHttpRequest($.ajax(DOMAIN + uri, {
            data: JSON.stringify(data),
            contentType: 'application/json',
            type: 'DELETE'
        }));
    }

    postJson(uri, data) {
        return this.baseHttpRequest($.ajax(DOMAIN + uri, {
            data: JSON.stringify(data),
            contentType: 'application/json',
            type: 'POST'
        }));
    }

    subscribe(uri, cb) {
        wsClient.subscribe(uri, cb)
    }
}