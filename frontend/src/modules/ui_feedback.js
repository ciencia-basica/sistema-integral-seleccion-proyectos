import { HttpError } from "./errors.js"

export function requestFeedback(request, htmlElem, onSuccesMsg, onErrorMsg){
    const prevText = htmlElem.innerHTML;
    htmlElem.disabled = true;
    htmlElem.innerHTML = "...";

    request.then(_ => {
        htmlElem.innerHTML = onSuccesMsg;
        setTimeout(_ => {
            htmlElem.innerHTML = prevText;
            htmlElem.disabled = false;
        }, 800);
    }).catch(error => {
        if (error instanceof HttpError)
            alert(error.detail);

        htmlElem.innerHTML = onErrorMsg;
        setTimeout(_ => {
            htmlElem.innerHTML = prevText;
            htmlElem.disabled = false;
        }, 1500);
    });
}