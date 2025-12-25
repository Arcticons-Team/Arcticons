// worker.js
import { shuffleArray, sortData } from '../../../js/functions.js';
// Function to filter entries by app name frequency
function filterEntriesByAppNameFrequency(appEntriesData, minOccurrences) {
    const appNameCount = {};
    appEntriesData.forEach(entry => {
        const { appName } = entry;
        appNameCount[appName] = (appNameCount[appName] || 0) + 1;
    });
    return appEntriesData.filter(entry => appNameCount[entry.appName] >= minOccurrences);
}
// Compute view data
onmessage = function (event) {
    const { data, state, TABLE_COLUMNS } = event.data;
    let filteredData = data;

    // Matching drawables
    if (state.ui.showMatchingDrawables && state.drawableSet) {
        filteredData = filteredData.reduce((acc, entry) => {
            if (state.drawableSet.has(entry.Arcticon)) {
                acc.push(entry);
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
            filteredData = filteredData.filter(e => {
                const searchTarget = `${e.appName} ${e.componentInfo} ${e.drawable}`;
                const matches = re.test(searchTarget);
                return state.ui.reverse ? !matches : matches;
            });
        } else {
            const s = state.ui.search.toLowerCase();
            filteredData = filteredData.filter(e => e.appName.toLowerCase().includes(s));
        }
    }
    // Random selection
    if (state.ui.random.active) {
        const dataLength = filteredData.length;
        if (state.ui.random.count != dataLength && state.ui.random.count < dataLength) {
            const numArr = Array.from({ length: dataLength }, (_, i) => i);
            const slicedRandomNumArr = shuffleArray(numArr).slice(0, state.ui.random.count);
            filteredData = slicedRandomNumArr.map(index => filteredData[index]);
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
