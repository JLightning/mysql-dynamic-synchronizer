export default class AbstractClient {

    get(uri, data) {
        let done = null;
        let error = message => showError(message);
        $.get(DOMAIN + uri, data)
            .done(data => {
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

    post(uri, data) {
        let done = null;
        let error = message => showError(message);
        $.post(DOMAIN + uri, data)
            .done(data => {
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

    postJson(uri, data) {
        let done = null;
        let error = message => showError(message);
        $.ajax(DOMAIN + uri, {
            data: JSON.stringify(data),
            contentType: 'application/json',
            type: 'POST'
        }).done(data => {
            if (data.success) {
                if (done != null) done(data.data);
            } else {
                error(data.errorMessage);
            }
        }).fail(() => {
            error("Network failed");
        });

        return {
            done: f => done = f,
            error: f => error = f
        }
    }
}