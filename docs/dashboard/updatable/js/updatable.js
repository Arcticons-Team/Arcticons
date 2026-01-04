import { state } from "../../js/state/store.js";
import { DOM, TABLE_COLUMNS_Updates as TABLE_COLUMNS } from "../../js/const.js"
import { debounce, CopyAppfilter, copyToClipboard } from "../../js/functions.js";
import { updateTable, lazyLoadAndRender } from "./ui/tableRenderer.js";

async function initializeAppData() {
    const fetchJson = (url) => fetch(url).then(res => res.ok ? res.json() : null).catch(() => null);
    const [updatableJson, appfilterJson, requestsJson] = await Promise.all([
        fetchJson('/assets/updatable.json'),
        fetchJson('/assets/combined_appfilter.json'),
        fetchJson('/assets/requests.json')
    ])
    if (!updatableJson) {
        console.error("Critical error: updatable.json not found")
        notifyMessage("Failed to load updatable data. Please refresh.");
        return;
    }
    state.all = updatableJson;

    if (appfilterJson) {
        const filteredData = filterAppfilter(appfilterJson);
        state.all = filteredData;
    } else {
        console.warn("componentInfo.json missing: showing all entries without filtering.");
    }

    if (requestsJson) {
        const latestDate = new Date(requestsJson.stats.lastUpdate * 1000);
        DOM.dateHeader.innerText = latestDate.toLocaleString(undefined, {
            day: 'numeric', year: 'numeric', month: 'long'
        });
    }

    updateHeaderText(`${state.all.length} Updates Available`);
    state.view = state.all;
    state.ui.sort.column = 0;
    state.ui.sort.direction = 'asc'
    state.copy.appfilterName = false;
    recomputeView();
    initEventListeners();
}
initializeAppData();

// Filter state.all based on componentInfo content
function filterAppfilter(appfilterData) {
    const appfilterItems = new Set(appfilterData.components);
    console.log(appfilterItems)
    //const appfilterItems = new Set(parseAppfilter(appfilterContent)); // Convert to Set for fast lookups
    const filteredOutEntries = [];
    const filteredData = state.all.filter(entry => {
        if (appfilterItems.has(entry.componentInfo)) { // Check membership in O(1)
            filteredOutEntries.push(entry.componentInfo); // Track filtered out entries
            return false; // Exclude from filtered data
        }
        return true; // Include in filtered data
    });

    console.log("Filtered out entries:", filteredOutEntries);
    return filteredData;
}

// Update header text
function updateHeaderText(newHeader) {
    document.getElementById('header').innerText = newHeader;
}

// Scroll event listener for lazy loading
const tableContainer = document.querySelector('.table-container');
tableContainer.addEventListener('scroll', () => {
    const { scrollTop, scrollHeight, clientHeight } = tableContainer;
    if (scrollTop + clientHeight >= scrollHeight - 100) {
        lazyLoadAndRender();
    }
});

function showIconPreview(iconSrc) {
    const previewOverlay = document.getElementById('preview-overlay');
    const previewImage = document.getElementById('preview-image');

    // Set the preview image source to the clicked icon source
    previewImage.src = iconSrc;

    // Show the preview overlay
    previewOverlay.style.display = 'block';
    // Add click event listener to hide the preview when clicked on the overlay or close button
    previewOverlay.addEventListener('click', function (e) {
        if (e.target === this || e.target.classList.contains('close-button-class')) {
            // Hide the preview overlay
            this.style.display = 'none';
        }
    });
}

// Search function
const filterAppEntries = debounce(() => {
    state.ui.search = DOM.searchInput.value;
    recomputeView();
}, 200);

function updateSortMarkers() {
    DOM.sortableHeaders.forEach((th, index) => {
        th.classList.remove('asc', 'desc');

        if (index === state.ui.sort.column) {
            th.classList.add(state.ui.sort.direction);
        }
    });
}

function sortTable(columnIndex) {
    state.ui.sort.column = columnIndex;
    state.ui.sort.direction = DOM.sortableHeaders[columnIndex].classList.contains('asc') ? 'desc' : 'asc';
    recomputeView();
}
let computeWorker
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
    };
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

function initEventListeners() {
    DOM.searchInput.addEventListener('input', filterAppEntries);
    DOM.renameBtn.addEventListener('click', () => {
        CopyAppfilter(null, true);
    });
    DOM.imagePreviewOverlay.onclick = e => {
        console.log(e);
        if (e.target === DOM.imagePreviewOverlay || e.target.classList.contains('close-button-class')) {
            DOM.imagePreviewOverlay.classList.remove("show");
        }
    };
    bindPress(DOM.copySelectedBtn,
        () => DOM.renameOverlay.classList.add("show"),
        () => CopyAppfilter(null, false)
    );

    DOM.updatableButton.addEventListener("click", function () {
        const updatableURL = `/dashboard/requests`;
        window.location.href = updatableURL;
    });

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

    DOM.tableBody.addEventListener('click', (event) => {
        const target = event.target;
        const row = target.closest('tr');
        if (!row) return;

        const index = parseInt(row.dataset.index);
        const pkg = row.dataset.pkg;
        const componentInfo = row.dataset.componentInfo;
        const entry = state.view[index];

        // 1. Handle Copy Button
        if (target.closest('.copy-btn')) {
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
    });
}