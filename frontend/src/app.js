import * as backend from "./modules/backend_connection.js"
import { handleFileSelect, getFileContent } from "./modules/file_uploader.js"
import { downloadDataAsFile } from "./modules/downloader.js"
import { requestFeedback } from "./modules/ui_feedback.js"
import { visualizeOutput } from "./modules/output_visualizer.js"
import { getUser, redirectTo } from "./modules/user_fetch.js"

const ALGORITHMS = [
    "Calculate credibility matrix",
    "Calculate sorting",
    "Project level",
    "Portfolio level",
];

const algorithmCols = ["#8495b9","#c9b14a","#66cc99","#aa6fd1"]

const algorithmPageColors = ALGORITHMS.reduce((acc, key, idx) => {
    acc[key] = algorithmCols[idx];
    return acc;
}, {});

const CONFIGS = [
    "Additional criteria parameters",
    "Credibility criteria",
    "Criteria directions",
    "Criteria hierarchy",
    "Criteria interactions",
    "Criteria parameters",
    "Performance matrix",
    "Use value function",
    "Veto thresholds for supercriteria",
    "Weights",
    "Sorting criteria"
];

//CSS vars
const ALGORITHM_COL_CSSVAR = "--algorithm-color";
const OUTPUT_DISPLAY_CSSVAR = "--get-output-display";

//DOM Ids
const IDs = {
    algorithmTitle : "algorithm-title",
    algorithmSelector : "algorithm-selector",
    executeButton : "execute-button",

    configSelector : "config-selector",
    configTextInput : "config-text",
    configFileInput : "config-file",
    sendTextButton : "send-text-button",
    sendFileButton : "send-file-button",
    getConfigButton : "get-config-button",

    outputContainer : "get-output",
    outputSelector : "output-selector",
    getOutputButton : "get-output-button",
    outputTableContainer : "output-table",

    returnLoginButton : "return-to-login",
};

function fillSelectorOptions(selectorElem, options){
    selectorElem.innerHTML = "";
    options.forEach(opt => selectorElem.innerHTML += `<option value="${opt}">${opt}</option>\n`);
    selectorElem.value = options[0];
    selectorElem.dispatchEvent(new Event("change"));
}

function updateOutputSelect(outputSelectorElem, outputContainerElem, selectedAlgorithm){
    const request = backend.getOutputs(getUser(), selectedAlgorithm);
    request.then(outputs => {
        fillSelectorOptions(outputSelectorElem, outputs);
        const cssvarDisplay = window.getComputedStyle(outputContainerElem)
                                    .getPropertyValue(OUTPUT_DISPLAY_CSSVAR);
        outputContainerElem.style.display = outputs.length == 0 ? "none" : cssvarDisplay;
    });

    return request;
}

document.addEventListener("DOMContentLoaded", _ => {
    const elems = Object.keys(IDs).reduce((output, id) => {
        output[IDs[id]] = document.getElementById(IDs[id]);
        return output;
    }, {});

    elems[IDs.algorithmSelector].addEventListener("change", _ => {
        const selectedAlgorithm = elems[IDs.algorithmSelector].value;
        elems[IDs.algorithmTitle].textContent = selectedAlgorithm;

        const color = algorithmPageColors[selectedAlgorithm];
        document.documentElement.style.setProperty(ALGORITHM_COL_CSSVAR, color);

        elems[IDs.outputTableContainer].innerHTML = "";
        const outputSelectorElem = elems[IDs.outputSelector];
        const outputContainerElem = elems[IDs.outputContainer];
        updateOutputSelect(outputSelectorElem, outputContainerElem, selectedAlgorithm);
    });

    fillSelectorOptions(elems[IDs.algorithmSelector], ALGORITHMS);
    fillSelectorOptions(elems[IDs.configSelector], CONFIGS);

    elems[IDs.configFileInput].addEventListener("change", handleFileSelect);
    elems[IDs.returnLoginButton].addEventListener("click", _ => { redirectTo("index", ""); });

    elems[IDs.sendTextButton].addEventListener("click", _ => {
        const selectedAlgorithm = elems[IDs.algorithmSelector].value;
        const selectedConfig = elems[IDs.configSelector].value;
        const inputText = elems[IDs.configTextInput].value;
        const request = backend.modifyConfig(getUser(), selectedAlgorithm, selectedConfig, inputText);
        requestFeedback(request, elems[IDs.sendTextButton], "Enviado", "Fallido");
    });
    elems[IDs.sendFileButton].addEventListener("click", _ => {
        getFileContent().then(fileContent => {
            const selectedAlgorithm = elems[IDs.algorithmSelector].value;
            const selectedConfig = elems[IDs.configSelector].value;
            const request = backend.modifyConfig(getUser(), selectedAlgorithm, selectedConfig, fileContent);
            requestFeedback(request, elems[IDs.sendFileButton], "Enviado", "Fallido");
        }).catch(_ => {
            const noEvent = new Promise(resolve => resolve());
            requestFeedback(noEvent,elems[IDs.sendFileButton], "Archivo No Seleccionado", "");   
        });
    });

    elems[IDs.getConfigButton].addEventListener("click", _ => {
        const selectedAlgorithm = elems[IDs.algorithmSelector].value;
        const selectedConfig = elems[IDs.configSelector].value;
        const request = backend.getConfigFile(getUser(), selectedAlgorithm, selectedConfig);
        request.then(config => downloadDataAsFile(config, selectedConfig));
        requestFeedback(request, elems[IDs.getConfigButton], "Obtenido", "Fallido");
    });

    elems[IDs.executeButton].addEventListener("click", _ => {
        const selectedAlgorithm = elems[IDs.algorithmSelector].value;
        const request = backend.execute(getUser(), selectedAlgorithm);

        elems[IDs.outputTableContainer].innerHTML = "";
        const outputSelectorElem = elems[IDs.outputSelector];
        const outputContainerElem = elems[IDs.outputContainer];
        updateOutputSelect(outputSelectorElem, outputContainerElem, selectedAlgorithm).then(_ => {
            request.then(response => {
                alert(response);
            }); 
        });

        requestFeedback(request, elems[IDs.executeButton], "Ejecutado", "Fallido");
    });

    elems[IDs.getOutputButton].addEventListener("click", _ => {
        const selectedAlgorithm = elems[IDs.algorithmSelector].value;
        const selectedOutput = elems[IDs.outputSelector].value;

        const request = backend.getOutput(getUser(), selectedAlgorithm, selectedOutput);
        request.then(output => {
            downloadDataAsFile(output, `${selectedOutput}-resultado-${selectedAlgorithm}`);
            if (selectedAlgorithm == ALGORITHMS[1])
                visualizeOutput(output, elems[IDs.outputTableContainer]);
        });

        requestFeedback(request, elems[IDs.getOutputButton], "Obtenido", "Fallido");
    });
});
