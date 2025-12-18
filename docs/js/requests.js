import { filterAppfilter, shuffleArray } from './functions.js';
import { TABLE_COLUMNS_Requests as TABLE_COLUMNS, DOM, imagepath } from './const.js';
import { state } from './state/store.js';
import { updateTable, lazyLoadAndRender } from './ui/tableRenderer.js';
import { copyToClipboard } from './events/button.js';
import { renderCategories, initCategoryUI } from './ui/category.js';

function collectCategories(playStoreCategories) {
    playStoreCategories
        .filter(c => !/^#\d* top\b/.test(c))
        .forEach(c => state.allCategories.add(c));
}

function finalizeCategories() {
    const cats = [...state.allCategories];
    const pinned = cats.filter(c => c === 'App' || c === 'Game').sort();
    const rest = shuffleArray(cats.filter(c => !pinned.includes(c)));
    return new Set([...pinned, ...rest]);
}

// Debounce function for search input
const debounce = (func, delay) => {
    let timeoutId;
    return (...args) => {
        if (timeoutId) {
            clearTimeout(timeoutId);
        }
        timeoutId = setTimeout(() => {
            func.apply(null, args);
        }, delay);
    };
};

async function initializeAppData() {
    try {
        // 1. Start all fetches at once
        const [requestsRes, appfilterRes, colorsRes] = await Promise.all([
            fetch('assets/requests.json'),
            fetch('assets/combined_appfilter.json'),
            fetch('assets/image_color_counts.json')
        ]);

        // 2. Check for errors
        if (!requestsRes.ok || !appfilterRes.ok) throw new Error("Critical data failed to load");

        // 3. Parse all responses in parallel
        const [jsonContent, appfilterJson, colorsJson] = await Promise.all([
            requestsRes.json(),
            appfilterRes.json(),
            colorsRes.json()
        ]);

        // 4. Process Data (Sequential processing of the results)
        processRequests(jsonContent);
        processAppfilter(appfilterJson);
        processColors(colorsJson);

        // 5. Final Render
        state.allCategories = finalizeCategories();
        renderCategories();
        recomputeView();

        setTimeout(() => {
            initEventListeners();
        }, 0);

    } catch (error) {
        console.error("Initialization error:", error);
    }
}

initializeAppData();
initCategoryUI(recomputeView);

function processRequests(JsonContent) {
    // Set latest date header
    const latestDate = Object.values(JsonContent).reduce((latest, entry) => {
        const d = new Date(parseFloat(entry.requestDate) * 1000);
        return d > latest ? d : latest;
    }, new Date(0));

    DOM.dateHeader.innerText = latestDate.toLocaleString(undefined, {
        day: 'numeric', year: 'numeric', month: 'long'
    });

    state.all = Object.entries(JsonContent).map(([componentInfo, entry]) => {
        const pkgName = componentInfo.split('/')[0];
        const drawable = entry.drawable;
        const appIconPath = drawable ? `extracted_png/${drawable}.webp` : 'img/requests/default.svg';

        collectCategories(entry.PlayStore?.Categories ?? []);

        return {
            appName: entry.Name,
            componentInfo,
            appIcon: `<img src="${appIconPath}" alt="Icon">`,
            Arcticon: `<img src="https://raw.githubusercontent.com/Arcticons-Team/Arcticons/refs/heads/main/icons/white/${drawable}.svg" alt="Arcticon" class="arcticon">`,
            appLinks: [
                `<a href="https://play.google.com/store/apps/details?id=${pkgName}" class="links" target="_blank"><img src="${imagepath.playStore}" alt="P"></a>`,
                `<a href="https://f-droid.org/en/packages/${pkgName}/" class="links" id='fdroid' target="_blank"><img src="${imagepath.fdroid}" alt="F"></a>`,
                `<a href="https://apt.izzysoft.de/fdroid/index/apk/${pkgName}" class="links" id='izzy' target="_blank"><img src="${imagepath.izzyOnDroid}" alt="I"></a>`,
                `<a href="https://galaxystore.samsung.com/detail/${pkgName}" class="links" id='galaxy' target="_blank"><img src="${imagepath.galaxyStore}" alt="G"></a>`,
                `<a href="https://www.ecosia.org/search?q=${pkgName}" class="links" target="_blank"><img src="${imagepath.wwwSearch}" alt="S"></a>`
            ].join('\n'),
            playStoreDownloads: entry.PlayStore?.Downloads?.replace("no_data", "X") ?? "X",
            requestedInfo: entry.count,
            lastRequestedTime: new Date(parseFloat(entry.requestDate) * 1000).toLocaleString().replace(',', ''),
            appNameAppfilter: `<!-- ${entry.Name} -->`,
            appfilter: `<item component="ComponentInfo{${componentInfo}}" drawable="${drawable}"/>`,
            appIconPath,
            appIconColor: 0,
            playStoreCategories: entry.PlayStore?.Categories ?? [],
            drawable,
            searchText: `<!-- ${entry.Name} --><item component="ComponentInfo{${componentInfo}}" drawable="${drawable}"/>`
        };
    });
}

function processAppfilter(appfilterJson) {
    if (!appfilterJson || !Array.isArray(appfilterJson)) return;

    // 1. Create a Set of existing components for ultra-fast filtering
    const existingComponents = new Set();

    appfilterJson.forEach(item => {
        // Add drawable to state for global reference (if needed)
        if (item.drawable) {
            state.drawableSet.add(item.drawable);
        }

        // Store the component string to check against requests
        if (item.component) {
            // We strip "ComponentInfo{" and "}" if they exist to match your request keys
            const cleanComponent = item.component.replace('ComponentInfo{', '').replace('}', '').trim();
            existingComponents.add(cleanComponent);
        }
    });

    // 2. Filter state.all: Remove apps that already exist in your appfilter
    // This replaces the old filterAppfilter(state.all, content) function
    state.all = state.all.filter(entry => {
        return !existingComponents.has(entry.componentInfo);
    });

    updateHeaderText(`${state.all.length} Requested Apps`);
}

function processColors(colorJson) {
    // 1. Convert Array to Map for O(1) lookup performance
    const colorMap = new Map();
    colorJson.forEach(item => colorMap.set(item.filename, item.unique_colors));

    // 2. Patch state.all
    state.all.forEach(entry => {
        const filename = entry.appIconPath.split('/').pop();
        entry.appIconColor = colorMap.get(filename) || 0;
    });
}

function updateSortMarkers() {
    DOM.sortableHeaders.forEach((th, index) => {
        th.classList.remove('asc', 'desc');

        if (index === state.ui.sort.column) {
            th.classList.add(state.ui.sort.direction);
        }
    });
}

// Update header text
function updateHeaderText(newHeader) {
    DOM.header.innerText = newHeader;
    DOM.smallheader.innerText = newHeader;
}

// Search function
const filterAppEntries = debounce(() => {
    state.ui.search = DOM.searchInput.value;
    state.ui.regex = DOM.regexSwitch.checked;
    state.ui.reverse = DOM.reverseSwitch.checked;

    state.ui.regexFlags =
        (DOM.caseInsensitive.checked ? 'i' : '') +
        (DOM.caseUnicode.checked ? 'u' : '');

    recomputeView();
}, 500);

function showInfo() {
    DOM.infoText.classList.toggle("show");
}
window.showInfo = showInfo;

function notifyMessage(message) {
    DOM.searchNotification.innerText = message;
    DOM.searchNotification.style.display = 'block';
    // Hide the notification after a few seconds
    setTimeout(() => {
        DOM.searchNotification.style.display = 'none';
    }, 5000);
}

// Sort table function with optional sortingDirection parameter
function sortTable(columnIndex) {
    state.ui.sort.column = columnIndex;
    state.ui.sort.direction = DOM.sortableHeaders[columnIndex].classList.contains('asc') ? 'desc' : 'asc';
    recomputeView();
}

function bindPress(element, onClick, onLong) {
    let timer;
    let long = false;
    const start = (e) => {
        if (e.type === "mousedown" && e.button !== 0) return;
        long = false;
        element.classList.add("longpress");
        timer = setTimeout(() => { onLong(); long = true; }, 500);
    };
    const end = (e) => {
        clearTimeout(timer);
        element.classList.remove("longpress");
        if (!long && e.type === "click") onClick();
    };
    element.addEventListener("mousedown", start);
    element.addEventListener("touchstart", start, { passive: true });
    ["mouseup", "mouseleave", "touchend", "click"].forEach(ev => element.addEventListener(ev, end));
}

function updateUIState(state) {
    DOM.clearCategoryBtn.style.visibility =
        state.ui.categories.size ? 'visible' : 'hidden';

    DOM.clearSearchBtn.style.visibility =
        state.ui.search ? 'visible' : 'hidden';

    DOM.randomResetButton.style.display =
        state.ui.random.active ? "inline-block" : "none";
}

let computeWorker;

function recomputeView() {
    updateUIState(state);
    if (computeWorker) computeWorker.terminate();

    computeWorker = new Worker('./js/worker/worker.js', { type: 'module' });

    computeWorker.postMessage({
        data: state.all,
        state: state,
        TABLE_COLUMNS: TABLE_COLUMNS
    });

    computeWorker.onmessage = function (event) {
        const filteredData = event.data;
        state.view = filteredData;
        updateTable(filteredData);
        updateSortMarkers();
        computeWorker = null;
    };
}

function initEventListeners() {
    DOM.matchingNumberInput.addEventListener('input', () => {
        const value = parseInt(DOM.matchingNumberInput.value, 10);
        state.ui.matchingNameThreshold = isNaN(value) || value < 1 ? 1 : value;
        if (state.ui.showMatchingNames) recomputeView();
    });

    bindPress(DOM.copySelectedBtn,
        () => DOM.renameOverlay.classList.add("show"),
        () => copyToClipboard(null, false)
    );

    DOM.regexSearchSettingsBtn.addEventListener(
        "click",
        function () {
            DOM.regexPopup.classList.add("show");
        }
    );
    DOM.closeRegexSettingsBtn.addEventListener(
        "click",
        function () {
            DOM.regexPopup.classList.remove(
                "show"
            );
        }
    );
    window.addEventListener(
        "click",
        function (event) {
            if (event.target == myPopup) {
                DOM.regexPopup.classList.remove(
                    "show"
                );
            }
            if (event.target == DOM.renameOverlay) {
                DOM.renameOverlay.classList.remove("show");
            }
        }
    );

    DOM.requeststhead.addEventListener('click', (event) => {
        // Find the closest parent <th> element (the target header)
        const header = event.target.closest('th');

        // 1. Check if a header was clicked and if it is sortable
        if (header && header.classList.contains('sortable-header')) {
            // 2. Efficiently get the logical column index from the data attribute
            const columnIndex = header.dataset.sortIndex;

            // 3. Ensure the index is valid before proceeding
            if (columnIndex !== undefined) {
                // Pass the extracted logical index (as a number) to sortTable
                sortTable(parseInt(columnIndex, 10));
            }
        }
    });

    DOM.matchingNameBtn.addEventListener('click', () => {
        state.ui.showMatchingNames = !state.ui.showMatchingNames;
        if (state.ui.showMatchingNames) {
            state.ui.sort.direction = 'asc';
            state.ui.sort.column = 0;
        }
        DOM.matchingNameBtn.classList.toggle("active-toggle", state.ui.showMatchingNames);
        DOM.matchingNameBtn.innerText = state.ui.showMatchingNames
            ? "Show All"
            : "Show Matching Name";
        recomputeView();
    });

    DOM.clearSearchBtn.addEventListener('click', filterAppEntries);
    DOM.regexSwitch.addEventListener('change', filterAppEntries);
    DOM.closePopupBtn.addEventListener('click', filterAppEntries);
    DOM.searchInput.addEventListener('input', filterAppEntries);
    DOM.categoryModeBtn.addEventListener('click', () => {
        const one = state.ui.categoryMode === 'all';
        state.ui.categoryMode = one ? 'one' : 'all';

        DOM.categoryModeBtn.innerText = one
            ? "Match One Category"
            : "Match All Categories";

        DOM.categoryModeBtn.classList.toggle("active-toggle", one);
        recomputeView();
    });

    // Scroll event listener for lazy loading
    DOM.requestsTableContainer.addEventListener('scroll', () => {
        const { scrollTop, scrollHeight, clientHeight } = DOM.requestsTableContainer;
        if (scrollTop + clientHeight >= scrollHeight - 100) {
            lazyLoadAndRender();
        }
    });

    // Add an event listener to the button
    DOM.updatableButton.addEventListener("click", function () {
        // Define the URL to redirect to
        const updatableURL = `updatable.html`;
        // Redirect to the specified URL
        window.location.href = updatableURL;
    });
    DOM.randomButton.addEventListener("click", function () {
        state.ui.random.active = true;
        state.ui.random.count = parseInt(DOM.randomNumberInput.value, 10);
        if (isNaN(state.ui.random.count) || state.ui.random.count <= 0) {
            notifyMessage("Please enter a valid positive number for random selection.");
            return;
        }
        recomputeView();
    });
    DOM.randomResetButton.addEventListener("click", function () {
        state.ui.random.active = false;
        recomputeView();
    });
    DOM.randomNumberInput.addEventListener("keypress", function (event) {
        // If the user presses the "Enter" key on the keyboard
        if (event.key === "Enter") {
            // Cancel the default action, if needed
            event.preventDefault();
            // Trigger the button element with a click
            DOM.randomButton.click();
        }
    });

    DOM.matchingDrawablesBtn.addEventListener('click', () => {
        state.ui.showMatchingDrawables = !state.ui.showMatchingDrawables;
        DOM.matchingDrawablesBtn.innerText = state.ui.showMatchingDrawables
            ? "Show All Entries"
            : "Show Matching Drawables";

        DOM.matchingDrawablesBtn.classList.toggle("active-toggle", state.ui.showMatchingDrawables);
        DOM.matchingDrawableColumn.classList.toggle("active", state.ui.showMatchingDrawables);

        recomputeView();
    });
}