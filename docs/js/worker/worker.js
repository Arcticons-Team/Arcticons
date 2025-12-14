// worker.js

// Function to filter entries by app name frequency
function filterEntriesByAppNameFrequency(appEntriesData, minOccurrences) {
    const appNameCount = {};
    appEntriesData.forEach(entry => {
        const { appName } = entry;
        appNameCount[appName] = (appNameCount[appName] || 0) + 1;
    });
    return appEntriesData.filter(entry => appNameCount[entry.appName] >= minOccurrences);
}

function sortData(direction, columnIndex, data, TABLE_COLUMNS) {
    const column = TABLE_COLUMNS[columnIndex];
    if (!column || column.type === 'none') return data;

    const factor = direction === 'asc' ? 1 : -1;

    return [...data].sort((a, b) => {
        const valA = getCellValue(a, column,direction);
        const valB = getCellValue(b, column,direction);

        if (valA === null || valB === null) return 0;
        if (valA > valB) return factor;
        if (valA < valB) return -factor;
        return 0;
    });
}
function getCellValue(row, column,direction) {
    const value = row[column.key];
    switch (column.type) {
        case 'number':
            return Number(value) || 0;

        case 'date':
            return new Date(value);

        case 'downloads':
            return parseDownloadValue(value,direction);

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
// Compute view data
onmessage = function(event) {
    const { data, state, TABLE_COLUMNS } = event.data;
    let filteredData = data;

    // Matching drawables
    if (state.ui.showMatchingDrawables && state.drawableSet) {
        filteredData = filteredData.reduce((acc, entry) => {
            const base = entry.baseDrawable || entry.drawable?.replace(/_\d+$/, '');

            if (state.drawableSet.has(entry.drawable)) {
                acc.push(entry);
            } else if (state.drawableSet.has(base)) {
                acc.push({
                    ...entry,
                    Arcticon: `<img src="https://raw.githubusercontent.com/Arcticons-Team/Arcticons/refs/heads/main/icons/white/${base}.svg" class="arcticon">`,
                    ArcticonPath: `https://raw.githubusercontent.com/Arcticons-Team/Arcticons/refs/heads/main/icons/white/${base}.svg`
                });
            }
            return acc;
        }, []);
    }

    // Matching names
    if (state.ui.showMatchingNames) {
        filteredData = filterEntriesByAppNameFrequency(filteredData, state.ui.matchingNameThreshold);
    }

    // Category filter
    if (state.ui.categories.size) {
        const cats = [...state.ui.categories];
        filteredData = filteredData.filter(e =>
            state.ui.categoryMode === 'one'
                ? cats.some(c => e.playStoreCategories.includes(c))
                : cats.every(c => e.playStoreCategories.includes(c))
        );
    }

    // search
    if (state.ui.search) {
        if (state.ui.regex) {
            const re = new RegExp(state.ui.search, state.ui.regexFlags);
            data = data.filter(e =>
                state.ui.reverse ? !re.test(e.searchText) : re.test(e.searchText)
            );
        } else {
            const s = state.ui.search.toLowerCase();
            data = data.filter(e => e.appName.toLowerCase().includes(s));
        }
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
