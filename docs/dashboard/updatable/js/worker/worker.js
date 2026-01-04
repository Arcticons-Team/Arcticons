// worker.js
import { sortData } from '../../../js/functions.js';
// Compute view data
onmessage = function (event) {
    const { data, state, TABLE_COLUMNS } = event.data;
    let filteredData = data;
    // search
    if (state.ui.search) {
        const s = state.ui.search.toLowerCase();
        filteredData = filteredData.filter(e => {
            const searchTarget = `${e.appName} ${e.componentInfo}`;
            return searchTarget.toLowerCase().includes(s);
        });
    }
    // Sort
    filteredData = sortData(
        state.ui.sort.direction,
        state.ui.sort.column,
        filteredData,
        TABLE_COLUMNS
    );
    postMessage(filteredData);
};
