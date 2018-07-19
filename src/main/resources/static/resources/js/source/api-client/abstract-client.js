export default class AbstractClient {

    get(uri, data) {
        let done = null;
        let error = message => {
            if (message != null) showError(message);
            else showError("Network error");
        };
        $.get(DOMAIN + uri, data)
            .done(data => {
                if (data.success) {
                    if (done != null) done(data.data);
                } else {
                    error(data.errorMessage);
                }
            })
            .fail(() => {
                error(null);
            });

        return {
            done: f => done = f,
            error: f => error = f
        }
    }
}