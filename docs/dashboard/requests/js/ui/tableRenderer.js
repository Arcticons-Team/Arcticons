// js/ui/tableRenderer.js
import { state } from '../../../js/state/store.js';
import { DOM, imagepath, urls } from '../../../js/const.js';


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
            <td class="arcticon-column" style="${state.ui.showMatchingDrawables ? 'display:table-cell;' : 'display:none;'}">
                ${state.ui.showMatchingDrawables
                ? `<a href="#" class="icon-preview" data-column="Arcticon">
                    <img src="https://raw.githubusercontent.com/Arcticons-Team/Arcticons/refs/heads/main/icons/white/${entry.Arcticon}.svg" alt="Arcticon" class="arcticon">
                    </a>`
                : '<span class="arcticon-placeholder">No Match</span>'
            }
            </td>
            <td class="app-name-cell" style="cursor: pointer;">${entry.appName}</br><span class="componentinfo">${entry.componentInfo}</span></td>
            <td>${entry.playStoreDownloads}</td>
            <td>${entry.requestedInfo}</td>
            <td>${formattedDate}</td>
            <td>
                <img src="${imagepath.copy}" title="Copy Appfilter" data-type="copy" class="btn-small" alt="Copy">
                <img src="${imagepath.download}" title="Download Icon" data-type="download" data-drawable="${entry.drawable}.webp" data-downloadpath="/extracted_png/${entry.drawable}.webp" class="btn-small" alt="Download">
            <img src="${imagepath.more}" title="More" data-type="more" class="btn-small" alt="More">
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
        <img src="${imagepath.playStore}" data-linktype="play" data-type="link" class="btn-small" alt="Play Store">
        <img src="${imagepath.fdroid}" data-linktype="fdroid" data-type="link" class="btn-small" alt="F-Droid">
        <img src="${imagepath.wwwSearch}" data-linktype="search" data-type="link" class="btn-small" alt="Websearch">
    `;
}

export function showIconPreview(iconSrc, Name, column) {
    DOM.imagePreview.src = iconSrc;
    DOM.imagePreviewTitle.textContent = Name
    DOM.imagePreview.classList.toggle('preview-arcticon', column === "Arcticon");
    DOM.imagePreviewOverlay.style.display = 'block';
}

export function getrowMenu(pkg) {
    return `
      <div class="btn-container" tabindex="0" role="menuitem"
        onclick="window.open('${urls.playStore}${pkg}')">
        <img src="${imagepath.playStore}"> <span>Play Store</span>
      </div>
      <div class="btn-container" tabindex="0" role="menuitem"
        onclick="window.open('${urls.fdroid}${pkg}')">
        <img src="${imagepath.fdroid}"> <span>F-Droid</span>
      </div>
      <div class="btn-container"tabindex="0" role="menuitem"
        onclick="window.open('${urls.izzyOnDroid}${pkg}')">
        <img src="${imagepath.izzyOnDroid}"> <span>IzzyOnDroid</span>
      </div>
      <div class="btn-container" tabindex="0" role="menuitem"
        onclick="window.open('${urls.galaxyStore}${pkg}')">
        <img src="${imagepath.galaxyStore}"> <span>Galaxy Store</span>
      </div>
      <div class="btn-container" tabindex="0" role="menuitem"
        onclick="window.open('${urls.wwwSearch}${pkg}')">
        <img src="${imagepath.wwwSearch}"> <span>Search</span>
      </div>
    `;
}