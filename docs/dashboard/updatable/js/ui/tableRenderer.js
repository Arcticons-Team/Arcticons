// js/ui/tableRenderer.js
import { state } from '../../../js/state/store.js';
import { DOM, imagepath } from '../../../js/const.js';


/* ---------- Core table operations ---------- */
export function clearTable() {
    DOM.tableBody.innerHTML = ''; // Faster than deleting rows one by one
}

export function renderTableBatch(data) {
    const fragment = document.createDocumentFragment(); // Use a fragment for a single reflow

    data.forEach((entry, i) => {
        const index = i + state.startIndex;
        const row = document.createElement('tr');

        // Attach metadata to the row itself
        row.dataset.index = index;
        row.dataset.componentInfo = entry.componentInfo;
        row.dataset.pkg = entry.pkgName;

        if (state.selectedRows.has(entry.appfilter)) {
            row.classList.add('row-glow');
        }
        const formattedDate = new Date(entry.lastRequestedTime * 1000)
            .toLocaleDateString(undefined, {
                day: 'numeric',
                month: 'short',
                year: 'numeric'
            });
        // Generate the entire row HTML at once - Much faster than insertCell()
        row.innerHTML = `
            <td class="icon-preview" data-column="AppIcon">
                    <img src="/extracted_png/${entry.drawable}.webp" alt="Icon">
            </td>
            <td class="app-name-cell" style="cursor: pointer;">${entry.appName}</br><span class="componentinfo">${entry.componentInfo}</span></td>
            <td>
                <button class="btn copy-btn">
                    <img src="${imagepath.copy}">
                </button>
            </td>
        `;
        fragment.appendChild(row);
    });
    DOM.tableBody.appendChild(fragment);
}

export function lazyLoadAndRender() {
    const batch = state.view.slice(
        state.startIndex,
        state.startIndex + state.batchSize
    );
    renderTableBatch(batch);
    state.startIndex += state.batchSize;
}

export function updateTable(data = state.view) {
    state.startIndex = 0;
    state.view = data;
    clearTable();
    lazyLoadAndRender();
}

export function showIconPreview(iconSrc, column) {
    DOM.imagePreview.src = iconSrc;
    DOM.imagePreview.classList.toggle('preview-arcticon', column === "Arcticon");
    DOM.imagePreviewOverlay.style.display = 'block';
}