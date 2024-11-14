import * as backend from "./modules/backend_connection.js"
import { requestFeedback } from "./modules/ui_feedback.js"
import { redirectTo } from "./modules/user_fetch.js"

//DOM Ids
const IDs = {
    loginEmail: "login-email",
    loginPassword: "login-password",
    loginButton: "login-app",

    createAccountEmail: "create-account-email",
    createAccountPassword: "create-account-password",
    createAccountButton: "create-account-button",

    deleteAccountEmail: "delete-account-email",
    deleteAccountPassword: "delete-account-password",
    deleteAccountButton: "delete-account",

    guestAccessButton: "guest-access"
};

document.addEventListener("DOMContentLoaded", _ => {
    const elems = Object.keys(IDs).reduce((output, id) => {
        output[IDs[id]] = document.getElementById(IDs[id]);
        return output;
    }, {});

    // Login
    elems[IDs.loginButton].addEventListener("click", _ => {
        const email = elems[IDs.loginEmail].value;
        const password = elems[IDs.loginPassword].value;

        if (email === "" || password === "") {
            alert("Por favor, rellena todos los campos.");
            return;
        }

        const request = backend.loginUser(email, password); // Cambiar a función que maneje login en backend
        requestFeedback(request, elems[IDs.loginButton], "", "Error");
        request.then(success => {
            if (!success) {
                alert("Correo o contraseña invalido");
                return;
            }
            redirectTo("app", email);
        });
    });

    // Crear cuenta
    elems[IDs.createAccountButton].addEventListener("click", _ => {
        const email = elems[IDs.createAccountEmail].value;
        const password = elems[IDs.createAccountPassword].value;

        if (email === "" || password === "") {
            alert("Por favor, rellena todos los campos.");
            return;
        }

        const request = backend.registerUser(email, password);
        requestFeedback(request, elems[IDs.createAccountButton], "", "Error");

        request.then(response => {
            alert("Por favor, confirma tu correo electrónico.");
        }).catch(error => {
            alert("Error: " + error.detail);
        });
    });

    // Eliminar cuenta
    elems[IDs.deleteAccountButton].addEventListener("click", _ => {
        const email = elems[IDs.deleteAccountEmail].value;
        const password = elems[IDs.deleteAccountPassword].value;
        const request = backend.deleteUser(email, password); // Cambiar a función que elimine en backend
        requestFeedback(request, elems[IDs.deleteAccountButton], "", "Error");
        request.then(response => {
            alert("Cuenta eliminada exitosamente.");
        });
    });

    // Acceso como invitado
    elems[IDs.guestAccessButton].addEventListener("click", _ => {
        redirectTo("app", "guest");
    });
});
