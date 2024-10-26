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
            const email = sessionStorage.getItem("userEmail"); // Cambiado a email en lugar de username
            if (detail === `usuario con correo '${email}' no existe`) {
                // Mostrar un mensaje de error si el usuario no existe
                alert("Este correo no está registrado.");
            } else if (detail === `usuario con correo '${email}' no verificado`) {
                // Mostrar un mensaje de error si el correo no está verificado
                alert("Por favor, verifica tu correo electrónico antes de iniciar sesión.");
            } else {
                // Otros errores posibles (contraseña incorrecta, etc.)
                alert("Error en la solicitud: " + detail);
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
