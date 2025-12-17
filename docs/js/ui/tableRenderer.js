// js/ui/tableRenderer.js
import { state } from '../state/store.js';
import { copyToClipboard } from '../events/button.js';
import { DOM } from '../const.js';


/* ---------- Core table operations ---------- */

export function clearTable() {
    while (DOM.tableBody.rows.length) {
        DOM.tableBody.deleteRow(0);
    }
}

export function renderTableBatch(data) {

    data.forEach((entry, i) => {
        const index = i + state.startIndex;
        const row = DOM.tableBody.insertRow();

        row.dataset.id = entry.appfilter;
        row.classList.toggle(
            'row-glow',
            state.selectedRows.has(entry.appfilter)
        );

        const cellAppName = row.insertCell(0);
        const cellAppIcon = row.insertCell(1);
        const cellArcticon = row.insertCell(2);
        const cellLinks = row.insertCell(3);
        const cellDownloads = row.insertCell(4);
        const cellReqInfo = row.insertCell(5);
        const cellReqTime = row.insertCell(6);
        const cellCopy = row.insertCell(7);

        cellAppName.textContent = entry.appName;
        cellAppName.style.cursor = 'pointer';
        cellAppName.addEventListener('click', () =>
            toggleRowSelection(row, entry.appfilter)
        );
        cellAppIcon.innerHTML = `<a href="#" class="icon-preview" data-index="${index}" column="AppIcon">${entry.appIcon}</a>`;
        cellLinks.innerHTML = entry.appLinks;
        cellDownloads.innerHTML = entry.playStoreDownloads;
        cellReqInfo.innerHTML = entry.requestedInfo;
        cellReqTime.innerHTML = entry.lastRequestedTime;
        cellCopy.innerHTML =
            `<button class="green-button copy-button">
        <img class="copy-icon" src="img/requests/copy.svg">
        <span class="copy-text">Copy</span>
      </button>`;
        cellCopy.querySelector('button').addEventListener('click', () => copyToClipboard(index, false));
        if (state.ui.showMatchingDrawables) {
            cellArcticon.innerHTML =
                `<a href="#" class="icon-preview" data-index="${index}" column="Arcticon">${entry.Arcticon}</a>`;
        } else {
            cellArcticon.innerHTML = `<span class="arcticon-placeholder">No Match</span>`;
        }
        toggleArcticonColumn(state.ui.showMatchingDrawables);
        // Also update the header visibility
        const arctIconHeader = document.querySelector('th:nth-child(3)');
        if (arctIconHeader) {
            arctIconHeader.style.display = state.ui.showMatchingDrawables ? 'table-cell' : 'none';
        }
    });
    // Add event listeners to the icon previews
    const iconPreviews = document.querySelectorAll('.icon-preview');
    iconPreviews.forEach(icon => {
        icon.addEventListener('click', function (event) {
            event.preventDefault();
            const index = parseInt(this.getAttribute('data-index'));
            const column = this.getAttribute('column');
            const entry = state.view[index];
            if (column === "AppIcon") {
                // Show the icon preview
                showIconPreview(entry.appIconPath, column);
            } else if (column === "Arcticon") {
                // Show the Arcticon preview
                showIconPreview(entry.ArcticonPath, column);
            }
        });
    });
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

function toggleArcticonColumn(show) {
    document.querySelectorAll('td:nth-child(3), th:nth-child(3)')
        .forEach(el => el.style.display = show ? 'table-cell' : 'none');
}

function toggleRowSelection(row, entryId) {
    const active = state.selectedRows.has(entryId);
    active ? state.selectedRows.delete(entryId) : state.selectedRows.add(entryId);
    row.classList.toggle('row-glow', !active);
}

const previewOverlay = document.getElementById('preview-overlay');
function showIconPreview(iconSrc, column) {
    const previewImage = document.getElementById('preview-image');

    // Set the preview image source to the clicked icon source
    previewImage.src = iconSrc;
    if (column === "Arcticon") {
        previewImage.classList.add('preview-arcticon'); // Set the ID for the preview image
    }
    else {
        previewImage.classList.remove('preview-arcticon');
    }
    // Show the preview overlay
    previewOverlay.style.display = 'block';
}

previewOverlay.onclick = e => {
  if (e.target === previewOverlay ||
      e.target.classList.contains('close-button-class')) {
    previewOverlay.style.display = 'none';
  }
};