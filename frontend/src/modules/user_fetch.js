export const USERNAME_KEY = "username";

export function getUser() {
    return sessionStorage.getItem(USERNAME_KEY);
}

export function redirectTo(html, username) {
    sessionStorage.setItem(USERNAME_KEY, username);
    window.location.href = `${html}.html`;
}
