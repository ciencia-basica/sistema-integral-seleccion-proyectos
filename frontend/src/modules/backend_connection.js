    import { HttpError, ResponseFormatError } from "./errors.js"

    const LOCAL_HOST = "http://127.0.0.1:8000";
    const GLOBAL_HOST = "missing"//"http://18.224.67.125";
    const API_URI = "http://127.0.0.1:8000";
    // const API_URI = GLOBAL_HOST;

    //Api on start validation
    fetch(`${API_URI}/`, {
        method : "GET"
    }).then(response => {
        if (!response.ok){
            console.error("conexion con backend fallida");
            console.error({"error code":response.status});
        }
        return response.json();
    })
    .then(_ => {})
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

    export async function existsUser(email, password){
        const fetchURI = `${API_URI}/user/${email}`;

        const response = await fetch(fetchURI, {method:"GET"});
        const parsedResponse = await response.json();

        if (!response.ok)
            throw new HttpError(response.status, parsedResponse, ignoreUserValidation=true);

        validateResponseFormat(parsedResponse, {"exists":true});

        return parsedResponse.exists;
    }

    export async function loginUser(email, password) {
        const fetchURI = `${API_URI}/user/login`;
        const body = JSON.stringify({ email, password });
    
        const response = await fetch(fetchURI, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: body
        });
    
        const parsedResponse = await response.json();
        if (!response.ok) 
            throw new HttpError(response.status, parsedResponse);
        
        validateResponseFormat(parsedResponse, { "login_success": true });
    
        return parsedResponse.login_success;
    }

    //Register user with email and password
    export async function registerUser(email, password){
        const fetchURI = `${API_URI}/user/register`;
        const body = JSON.stringify({ email, password });

        const response = await fetch(fetchURI, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: body
        });
        const parsedResponse = await response.json();

        if (!response.ok)
            throw new HttpError(response.status, parsedResponse);

        validateResponseFormat(parsedResponse, {"response":""});

        return parsedResponse.response;
    }



    export async function deleteUser(email, password){
        const fetchURI = `${API_URI}/user/delete`;
        const body = JSON.stringify({email, password});

        const response = await fetch(fetchURI, {
            method:"DELETE",
            headers: {
                "Content-Type": "application/json"
            },
            body:body
        });
        const parsedResponse = await response.json();

        if (!response.ok)
            throw new HttpError(response.status, parsedResponse);

        validateResponseFormat(parsedResponse, {"response":""});

        return parsedResponse.response;
    }

    export async function resetUser(username){
        const fetchURI = `${API_URI}/user/reset/${username}`;

        const response = await fetch(fetchURI, {method:"POST"});
        const parsedResponse = await response.json();

        if (!response.ok)
            throw new HttpError(response.status, parsedResponse);

        validateResponseFormat(parsedResponse, {"response":""});

        return parsedResponse.response;
    }


    export async function getConfigFile(user, algorithmType, configType){
        const fetchURI = `${API_URI}/config/${user}/${algorithmType}/${configType}`;

        const response = await fetch(fetchURI, {method:"GET"});
        const parsedResponse = await response.json();

        if (!response.ok)
            throw new HttpError(response.status, parsedResponse);

        validateResponseFormat(parsedResponse, {"config":""});

        return parsedResponse.config;
    }

    export async function modifyConfig(user, algorithmType, configType, data){
        const param = new URLSearchParams({"config_data":data}).toString();
        const fetchURI = `${API_URI}/config/${user}/${algorithmType}/${configType}?${param}`;

        const response = await fetch(fetchURI, {method:"POST"});
        const parsedResponse = await response.json();

        if (!response.ok)
            throw new HttpError(response.status, parsedResponse);

        validateResponseFormat(parsedResponse, {"response":""});

        return parsedResponse.response;
    }

    export async function getOutputs(user, algorithmType){
        const fetchURI = `${API_URI}/outputs/${user}/${algorithmType}`;
        const response = await fetch(fetchURI, {method:"GET"});

        const parsedResponse = await response.json();

        if (!response.ok)
            throw new HttpError(response.status, parsedResponse);

        validateResponseFormat(parsedResponse, {"outputs":undefined});

        return parsedResponse.outputs;
    }

    export async function getOutput(user, algorithmType, outputType){
        const fetchURI = `${API_URI}/output/${user}/${algorithmType}/${outputType}`;

        const response = await fetch(fetchURI, {method:"GET"});
        const parsedResponse = await response.json();

        if (!response.ok)
            throw new HttpError(response.status, parsedResponse);

        validateResponseFormat(parsedResponse, {"output":""});

        return parsedResponse.output;
    }

    export async function execute(user, algorithmType){
        const fetchURI = `${API_URI}/execute/${user}/${algorithmType}`;

        const response = await fetch(fetchURI, {method:"POST"});
        const parsedResponse = await response.json();

        if (!response.ok)
            throw new HttpError(response.status, parsedResponse);

        validateResponseFormat(parsedResponse, {"response":""});

        return parsedResponse.response;
    }
