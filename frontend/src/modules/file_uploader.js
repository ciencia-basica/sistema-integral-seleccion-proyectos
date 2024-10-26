let uploadedFile = null;

export function handleFileSelect(event) {
    uploadedFile = event.target.files.length > 0 ? event.target.files[0] : null;
}

export function getFileContent(){
    if (!uploadedFile)
        return Promise.reject("Missing File");

    return new Promise((resolve, reject) => {
        const reader = new FileReader();

        reader.onload = () => resolve(reader.result);
        reader.onerror = () => reject("Error occurred reading the file.");

        reader.readAsText(uploadedFile);
    });
}
