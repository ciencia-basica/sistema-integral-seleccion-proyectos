// Cambiamos la clave para el correo electrónico
export const EMAIL_KEY = "userEmail";  // Antes era USERNAME_KEY

// Obtener el email del usuario de sessionStorage
export function getUser() {
    return sessionStorage.getItem(EMAIL_KEY);
}

// Redirigir a una página y almacenar el email
export function redirectTo(html, email) {
    sessionStorage.setItem(EMAIL_KEY, email);  // Almacenamos el correo en sessionStorage
    window.location.href = `${html}.html`;  // Redirigimos a la página correspondiente
}
