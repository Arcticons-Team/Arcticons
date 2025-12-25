import { shuffleArray, CopyAppfilter, debounce } from '../../js/functions.js';
import { TABLE_COLUMNS_Requests as TABLE_COLUMNS, DOM } from '../../js/const.js';
import { state } from '../../js/state/store.js';
import { updateTable, lazyLoadAndRender, showIconPreview } from './ui/tableRenderer.js';
import { renderCategories, initCategoryUI, findCategory } from './ui/category.js';

function finalizeCategories() {
    const cats = [...state.allCategories];
    const pinned = cats.filter(c => c === 'App' || c === 'Game').sort();
    const rest = shuffleArray(cats.filter(c => !pinned.includes(c)));
    return new Set([...pinned, ...rest]);
}

async function initializeAppData() {
    try {
        // --- PHASE 1: Fast Load with Fallbacks ---
        // We wrap fetches in a catch-all to prevent one failure from stopping the site
        const fetchJson = (url) => fetch(url).then(res => res.ok ? res.json() : null).catch(() => null);

        const [jsonContent, appfilterJson, colorsJson] = await Promise.all([
            fetchJson('/assets/requests.json'),
            fetchJson('/assets/combined_appfilter.json'),
            fetchJson('/assets/image_color_counts.json')
        ]);
        // Critical Data Check: If requests.json is missing, we can't do anything.
        if (!jsonContent) {
            console.error("Critical error: requests.json not found.");
            notifyMessage("Failed to load request data. Please refresh.");
            return;
        }
        processRequests(jsonContent);
        if (appfilterJson) {
            processAppfilter(appfilterJson);
        } else {
            console.warn("appfilter.json missing: showing all entries without filtering.");
        }
        state.allCategories = finalizeCategories();
        recomputeView();
        renderCategories();
        if (colorsJson) {
            const colorWorker = new Worker('./js/worker/colorworker.js');
            colorWorker.postMessage({
                allEntries: state.all,
                colorData: colorsJson
            });
            colorWorker.onmessage = function (e) {
                state.all = e.data; 
                colorWorker.terminate();
            };
        }
        setTimeout(() => initEventListeners(), 0);
    } catch (error) {
        console.error("Initialization error:", error);
    }
}

initializeAppData();
initCategoryUI(recomputeView);

function processRequests(jsonResponse) {
    const latestDate = new Date(jsonResponse.stats.lastUpdate * 1000);
    DOM.dateHeader.innerText = latestDate.toLocaleString(undefined, {
        day: 'numeric', year: 'numeric', month: 'long'
    });
    jsonResponse.categories.forEach(c => state.allCategories.add(c));
    state.all = jsonResponse.entries;
    updateHeaderText(`${jsonResponse.stats.totalCount} Requested Apps`);
}

function processAppfilter(appfilterData) {
    if (!appfilterData || !appfilterData.components) return;

    if (appfilterData.drawables) {
        appfilterData.drawables.forEach(d => state.drawableSet.add(d));
    }
    const existingComponents = new Set(appfilterData.components);
    state.all = state.all.filter(entry => !existingComponents.has(entry.componentInfo));
    updateHeaderText(`${state.all.length} Requested Apps`);
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
}, 200);

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
        timer = setTimeout(() => { onLong(); long = true; }, 300);
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

    DOM.matchingDrawableColumn.style.display =
        state.ui.showMatchingDrawables ? 'table-cell' : 'none';
}


let computeWorker;

function recomputeView() {

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
        updateUIState(state);
    };
}

function initEventListeners() {
    const debouncedFindCategory = debounce(findCategory, 300);
    DOM.searchInputCategory.addEventListener('input', debouncedFindCategory);

    DOM.matchingNumberInput.addEventListener('input', () => {
        const value = parseInt(DOM.matchingNumberInput.value, 10);
        state.ui.matchingNameThreshold = isNaN(value) || value < 1 ? 1 : value;
        if (state.ui.showMatchingNames) recomputeView();
    });

    bindPress(DOM.copySelectedBtn,
        () => DOM.renameOverlay.classList.add("show"),
        () => CopyAppfilter(null, false)
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

    const observer = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting) {
            lazyLoadAndRender(); // Load next batch
        }
    }, {
        root: DOM.requestsTableContainer,
        rootMargin: '200px' // Start loading 200px before reaching the bottom
    });

    observer.observe(DOM.sentinel);

    // Add an event listener to the button
    DOM.updatableButton.addEventListener("click", function () {
        // Define the URL to redirect to
        const updatableURL = `/dashboard/updatable`;
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

        state.copy.appfilterName = false;
        recomputeView();
    });

    DOM.imagePreviewOverlay.onclick = e => {
        if (e.target === DOM.imagePreviewOverlay || e.target.classList.contains('close-button-class')) {
            DOM.imagePreviewOverlay.style.display = 'none';
        }
    };

    DOM.renameBtn.addEventListener('click', () => {
        CopyAppfilter(null, true);
    });

    DOM.tableBody.addEventListener('click', (event) => {
        const target = event.target;
        const row = target.closest('tr');
        if (!row) return;

        const index = parseInt(row.dataset.index);
        const pkg = row.dataset.pkg;
        const componentInfo = row.dataset.componentInfo;
        const entry = state.view[index];

        // 1. Handle Copy Button
        if (target.closest('.copy-button')) {
            CopyAppfilter(index, false);
            return;
        }

        // 2. Handle App Name (Row Selection)
        if (target.classList.contains('app-name-cell')) {
            const active = state.selectedRows.has(componentInfo);
            active ? state.selectedRows.delete(componentInfo) : state.selectedRows.add(componentInfo);
            row.classList.toggle('row-glow', !active);
            return;
        }

        // 3. Handle Icon Previews
        const previewLink = target.closest('.icon-preview');
        if (previewLink) {
            event.preventDefault();
            const col = previewLink.dataset.column;
            const path = col === "AppIcon" ? `/extracted_png/${entry.drawable}.webp` : `https://raw.githubusercontent.com/Arcticons-Team/Arcticons/refs/heads/main/icons/white/${entry.Arcticon}.svg`;
            showIconPreview(path, col);
            return;
        }

        // 4. Handle Store Links (On Demand)
        if (target.classList.contains('links')) {
            const type = target.dataset.type;
            const urls = {
                play: `https://play.google.com/store/apps/details?id=${pkg}`,
                fdroid: `https://f-droid.org/en/packages/${pkg}/`,
                izzy: `https://apt.izzysoft.de/fdroid/index/apk/${pkg}`,
                galaxy: `https://galaxystore.samsung.com/detail/${pkg}`,
                search: `https://www.ecosia.org/search?q=${pkg}`
            };
            if (urls[type]) window.open(urls[type], '_blank');
        }
    });
}