// Parse appfilter content
function parseAppfilter(appfilterContent) {
    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(appfilterContent, 'text/xml');
    const items = xmlDoc.querySelectorAll('item');
    const appfilterItems = [];
    items.forEach(item => {
        const component = item.getAttribute('component');
        if (component) {
            appfilterItems.push(component.trim());
        }
    });
    return appfilterItems;
}

// Function to shuffle an array
export function shuffleArray(arr) {
    for (let i = arr.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [arr[i], arr[j]] = [arr[j], arr[i]]; // Swap elements
    }
    return arr;
}
export function getAppfilterValue(entry, rename, replacement) {
    if (!rename) return entry.appfilter;
    return entry.appfilter.replace(/(?<=drawable=")[^"]+(?="\/>)/, replacement);
}
export function buildCopyText(entry, appfilterValue, mode) {
    return mode
        ? appfilterValue
        : `${entry.appNameAppfilter}\n${appfilterValue}\n`;
}

// Filter appEntriesData based on appfilter content
export function filterAppfilter(appEntriesData, appfilterContent) {
    const appfilterItems = new Set(parseAppfilter(appfilterContent)); // Convert to Set for fast lookups
    const filteredOutEntries = [];

    const filteredData = appEntriesData.filter(entry => {
        const entryAppfilter = entry.appfilter.trim().split('"')[1].trim();
        if (appfilterItems.has(entryAppfilter)) { // Check membership in O(1)
            filteredOutEntries.push(entryAppfilter); // Track filtered out entries
            return false; // Exclude from filtered data
        }
        return true; // Include in filtered data
    });

    console.log("Filtered out entries:", filteredOutEntries);
    return filteredData;
}

export function sortData(direction, columnIndex, data, TABLE_COLUMNS) {
    const column = TABLE_COLUMNS[columnIndex];
    if (!column || column.type === 'none') return data;

    const factor = direction === 'asc' ? 1 : -1;

    return data.sort((a, b) => {
        const valA = getCellValue(a, column);
        const valB = getCellValue(b, column);

        if (valA === null || valB === null) return 0;
        if (valA > valB) return factor;
        if (valA < valB) return -factor;
        return 0;
    });
}

function getCellValue(row, column) {
    const value = row[column.key];
    switch (column.type) {
        case 'number':
            return Number(value) || 0;

        case 'date':
            return new Date(value);

        case 'downloads':
            return parseDownloadValue(value, state.ui.sort.direction);

        case 'string':
            return String(value).toLowerCase().trim();

        default:
            return null;
    }
}

// Convert download string to a numeric value for sorting
function parseDownloadValue(value, sortingDirection) {
    console
    if (value === "no_data") return sortingDirection === 'asc' ? 9999999999999999999999 : -1; // Assign a low value for "AppNotFound" to push it to the end
    if (value === "X") return sortingDirection === 'asc' ? 9999999999999999999999 : -1; // Assign a low value for "AppNotFound" to push it to the end
    if (value.endsWith("+")) value = value.slice(0, -1); // Remove the "+" at the end
    if (value.endsWith("K")) return parseFloat(value) * 1000; // Convert "k" to 1000
    if (value.endsWith("M")) return parseFloat(value) * 1000000; // Convert "M" to 1,000,000
    if (value.endsWith("B")) return parseFloat(value) * 1000000000; // Convert "B" to 1,000,000,000
    return parseFloat(value); // Return the numeric value for simple numbers like "100"
}