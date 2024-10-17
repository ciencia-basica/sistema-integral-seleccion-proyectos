import { redirectTo } from "./user_fetch.js"

export class HttpError extends Error {
    constructor(statusCode, detail, ignoreUserValidation = false) {
        super(`Failed request with code ${statusCode}`);

        if (Error.captureStackTrace)
            Error.captureStackTrace(this, HttpError);

        this.name = "HttpError";
        this.statusCode = statusCode;
        this.detail = detail;
        this.date = new Date();

        if (!ignoreUserValidation) {
            const username = sessionStorage.getItem("username");
            if (detail == `usuario '${username}' no existe`) {
                // exit app
                redirectTo("index", "");
            }
        }
    }
}

export class ResponseFormatError extends Error {
    constructor(message) {
        super(message);

        if (Error.captureStackTrace)
            Error.captureStackTrace(this, ResponseFormatError);

        this.name = "ResponseFormatError";
        this.date = new Date();
    }
}
