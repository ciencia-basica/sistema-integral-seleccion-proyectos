import { HttpError, ResponseFormatError } from "./errors.js"

const LOCAL_HOST = "http://127.0.0.1:8000";
const GLOBAL_HOST = "https://decisionesanaliticas.com"
const API_URI = window.location.hostname == "127.0.0.1" ? LOCAL_HOST : GLOBAL_HOST;
// const API_URI = GLOBAL_HOST;

//Api on start validation
fetch(`${API_URI}/`, {
    method: "GET"
}).then(response => {
    if (!response.ok) {
        console.error("conexion con backend fallida");
        console.error({ "error code": response.status });
    }
    return response.json();
})
    .then(_ => { })
    .catch(error => {
        console.error(error);
    });

//Throws an expcetion if the format of the response does not match the expected one
//Otherwiise does nothing and returns undefined
function validateResponseFormat(response, expectedFormat) {
    if (!response || !expectedFormat || typeof response !== typeof expectedFormat)
        throw new ResponseFormatError("Null or undefined parameters");

    const expected = Object.keys(expectedFormat);
    if (Object.keys(response).length !== expected.length)
        throw new ResponseFormatError("Invalid response format structure");

    for (let key of expected) {
        if (!response.hasOwnProperty(key))
            throw new ResponseFormatError("Invalid response format structure");

        const responseVal = response[key];
        const expectedVal = expectedFormat[key];
        if (expectedVal === undefined)
            continue;
        if (typeof expectedVal !== typeof responseVal)
            throw new ResponseFormatError(`Invalid attribute "${key}" type`);
        if (typeof expectedVal === "object" && !Array.isArray(expectedVal))
            validateResponseFormat(responseVal, expectedVal);
    }
}

//Api Calls

export async function existsUser(username) {
    const fetchURI = `${API_URI}/user/${username}`;

    const response = await fetch(fetchURI, { method: "GET" });
    const parsedResponse = await response.json();

    if (!response.ok)
        throw new HttpError(response.status, parsedResponse, ignoreUserValidation = true);

    validateResponseFormat(parsedResponse, { "exists": true });

    return parsedResponse.exists;
}

export async function registerUser(username) {
    const fetchURI = `${API_URI}/user/${username}`;

    const response = await fetch(fetchURI, { method: "POST" });
    const parsedResponse = await response.json();

    if (!response.ok)
        throw new HttpError(response.status, parsedResponse);

    validateResponseFormat(parsedResponse, { "response": "" });

    return parsedResponse.response;
}

export async function deleteUser(username) {
    const fetchURI = `${API_URI}/user/${username}`;

    const response = await fetch(fetchURI, { method: "DELETE" });
    const parsedResponse = await response.json();

    if (!response.ok)
        throw new HttpError(response.status, parsedResponse);

    validateResponseFormat(parsedResponse, { "response": "" });

    return parsedResponse.response;
}

export async function resetUser(username) {
    const fetchURI = `${API_URI}/user/reset/${username}`;

    const response = await fetch(fetchURI, { method: "POST" });
    const parsedResponse = await response.json();

    if (!response.ok)
        throw new HttpError(response.status, parsedResponse);

    validateResponseFormat(parsedResponse, { "response": "" });

    return parsedResponse.response;
}


export async function getConfigFile(user, algorithmType, configType) {
    const fetchURI = `${API_URI}/config/${user}/${algorithmType}/${configType}`;

    const response = await fetch(fetchURI, { method: "GET" });
    const parsedResponse = await response.json();

    if (!response.ok)
        throw new HttpError(response.status, parsedResponse);

    validateResponseFormat(parsedResponse, { "config": "" });

    return parsedResponse.config;
}

export async function modifyConfig(user, algorithmType, configType, data) {
    const param = new URLSearchParams({ "config_data": data }).toString();
    const fetchURI = `${API_URI}/config/${user}/${algorithmType}/${configType}?${param}`;

    const response = await fetch(fetchURI, { method: "POST" });
    const parsedResponse = await response.json();

    if (!response.ok)
        throw new HttpError(response.status, parsedResponse);

    validateResponseFormat(parsedResponse, { "response": "" });

    return parsedResponse.response;
}

export async function getOutputs(user, algorithmType) {
    const fetchURI = `${API_URI}/outputs/${user}/${algorithmType}`;
    const response = await fetch(fetchURI, { method: "GET" });

    const parsedResponse = await response.json();

    if (!response.ok)
        throw new HttpError(response.status, parsedResponse);

    validateResponseFormat(parsedResponse, { "outputs": undefined });

    return parsedResponse.outputs;
}

export async function getOutput(user, algorithmType, outputType) {
    const fetchURI = `${API_URI}/output/${user}/${algorithmType}/${outputType}`;

    const response = await fetch(fetchURI, { method: "GET" });
    const parsedResponse = await response.json();

    if (!response.ok)
        throw new HttpError(response.status, parsedResponse);

    validateResponseFormat(parsedResponse, { "output": "" });

    return parsedResponse.output;
}

export async function execute(user, algorithmType) {
    const fetchURI = `${API_URI}/execute/${user}/${algorithmType}`;

    const response = await fetch(fetchURI, { method: "POST" });
    const parsedResponse = await response.json();

    if (!response.ok)
        throw new HttpError(response.status, parsedResponse);

    validateResponseFormat(parsedResponse, { "response": "" });

    return parsedResponse.response;
}
