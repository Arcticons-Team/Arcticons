// js/ui/tableRenderer.js
import { state } from '../state/store.js';
import { DOM, imagepath } from '../const.js';


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
            <td class="app-name-cell" style="cursor: pointer;">${entry.appName}</td>
            <td class="icon-preview" data-column="AppIcon">
                    <img src="extracted_png/${entry.drawable}.webp" alt="Icon">
            </td>
            <td class="arcticon-column" style="${state.ui.showMatchingDrawables ? 'display:table-cell;' : 'display:none;'}">
                ${state.ui.showMatchingDrawables
                ? `<a href="#" class="icon-preview" data-column="Arcticon">
                    <img src="https://raw.githubusercontent.com/Arcticons-Team/Arcticons/refs/heads/main/icons/white/${entry.Arcticon}.svg" alt="Arcticon" class="arcticon">
                    </a>`
                : '<span class="arcticon-placeholder">No Match</span>'
            }
            </td>
            <td class="links-cell">${createLinksHtml()}</td>
            <td>${entry.playStoreDownloads}</td>
            <td>${entry.requestedInfo}</td>
            <td>${formattedDate}</td>
            <td>
                <button class="green-button copy-button">
                    <img class="copy-icon" src="img/requests/copy.svg">
                    <span class="copy-text">Copy</span>
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

function createLinksHtml() {
    return `
        <img src="${imagepath.playStore}" data-type="play" class="links" alt="P">
        <img src="${imagepath.fdroid}" data-type="fdroid" class="links" alt="F">
        <img src="${imagepath.izzyOnDroid}" data-type="izzy" class="links" alt="I">
        <img src="${imagepath.galaxyStore}" data-type="galaxy" class="links" alt="G">
        <img src="${imagepath.wwwSearch}" data-type="search" class="links" alt="A">
    `;
}

export function showIconPreview(iconSrc, column) {
    DOM.imagePreview.src = iconSrc;
    DOM.imagePreview.classList.toggle('preview-arcticon', column === "Arcticon");
    DOM.imagePreviewOverlay.style.display = 'block';
}