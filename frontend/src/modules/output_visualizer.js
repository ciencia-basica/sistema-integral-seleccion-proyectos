function addRowToTable(tableElem, ...cells){
    const tr = document.createElement("tr");
    
    cells.forEach(cell => {
        const td = document.createElement("td");
        td.textContent = cell;
        tr.appendChild(td);
    })

    tableElem.appendChild(tr);
}

export function visualizeOutput(output, outputTableContainerElem){
    const table = document.createElement("table");
    addRowToTable(table, "Proyecto", "rango");

    const rows = output.split("\n");
    rows.forEach(row => {
        if (row.length == 0)
            return;

        const [project, ...cells] = row.split("\t");
        const parsedCells = cells.map(c => c = c.match(/[a-zA-Z]+/)[0]).join(" - ");
        addRowToTable(table, project, parsedCells);
    });
    outputTableContainerElem.innerHTML = "";
    outputTableContainerElem.appendChild(table);
}