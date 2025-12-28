import { state } from './state/store.js';

// Function to shuffle an array
export function shuffleArray(arr) {
    for (let i = arr.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [arr[i], arr[j]] = [arr[j], arr[i]]; // Swap elements
    }
    return arr;
}

// Debounce function for search input
export const debounce = (func, delay) => {
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

export function sortData(direction, columnIndex, data, TABLE_COLUMNS) {
    const column = TABLE_COLUMNS[columnIndex];
    if (!column || column.type === 'none') return data;

    const factor = direction === 'asc' ? 1 : -1;

    return [...data].sort((a, b) => {
        const valA = getCellValue(a, column, direction);
        const valB = getCellValue(b, column, direction);

        if (valA === null || valB === null) return 0;
        if (valA > valB) return factor;
        if (valA < valB) return -factor;
        return 0;
    });
}
function getCellValue(row, column, direction) {
    const value = row[column.key];
    switch (column.type) {
        case 'number':
            return Number(value) || 0;

        case 'downloads':
            return parseDownloadValue(value, direction);

        case 'string':
            return String(value).toLowerCase().trim();

        default:
            return null;
    }
}

// Convert download string to a numeric value for sorting
function parseDownloadValue(value, sortingDirection) {
    if (value === "X") return sortingDirection === 'asc' ? 9999999999999999999999 : -1; // Assign a low value for "AppNotFound" to push it to the end
    if (value.endsWith("+")) value = value.slice(0, -1); // Remove the "+" at the end
    if (value.endsWith("K")) return parseFloat(value) * 1000; // Convert "k" to 1000
    if (value.endsWith("M")) return parseFloat(value) * 1000000; // Convert "M" to 1,000,000
    if (value.endsWith("B")) return parseFloat(value) * 1000000000; // Convert "B" to 1,000,000,000
    return parseFloat(value); // Return the numeric value for simple numbers like "100"
}

// Copy to clipboard function
export function CopyAppfilter(index, rename) {
    let copyText = "";
    const node = document.getElementById("drawableName-input");

    // Handle rename mode
    if (rename) {
        document.getElementById("renamer-overlay").classList.remove("show");
    }
    // Multi-row mode
    if (index === null) {
        copyText = getSelectedEntries()
            .map(entry => {
                const appfilterValue = getAppfilterValue(entry, rename, node.value);
                return buildCopyText(entry, appfilterValue, state.copy.appfilterName);
            })
            .join('\n');

        clearSelected();
    }

    // Single row mode
    else {
        const entry = state.view[index];
        const appfilterValue = getAppfilterValue(entry, rename, node.value);
        copyText = buildCopyText(entry, appfilterValue, state.copy.appfilterName);
    }

    copyToClipboard(copyText)
}

export function copyToClipboard(copyText) {
    // Copy to clipboard
    navigator.clipboard.writeText(copyText)
        .then(() => {
            const note = document.getElementById('copy-notification');
            note.innerText = `Copied:\n${copyText}`;
            note.style.display = 'block';

            setTimeout(() => {
                note.style.display = 'none';
            }, 3000);
        })
        .catch(error => {
            console.error('Unable to copy to clipboard:', error);
        });
}

function getSelectedEntries() {
    return state.view.filter(e => state.selectedRows.has(e.componentInfo));
}

function clearSelected() {
    document
        .querySelectorAll('tr.row-glow')
        .forEach(row => row.classList.remove('row-glow'));

    state.selectedRows.clear();
    console.log("All rows deselected.");
}

function getAppfilterValue(entry, rename, replacement) {
    if (!rename) {
        replacement = entry.drawable;
    }
    return `<item component="ComponentInfo{${entry.componentInfo}}" drawable="${replacement}"/>`;
}
function buildCopyText(entry, appfilterValue, mode) {
    return mode
        ? `<!-- ${entry.appName} -->\n${appfilterValue}\n`
        : appfilterValue;
}

export async function downloadImage(imageSrc, nameOfDownload) {
    const response = await fetch(imageSrc);
    const blobImage = await response.blob();
    const href = URL.createObjectURL(blobImage);

    const anchorElement = document.createElement('a');
    anchorElement.href = href;
    anchorElement.download = nameOfDownload;
    anchorElement.target = '_self'; 

    document.body.appendChild(anchorElement);
    anchorElement.click();

    setTimeout(() => {
        document.body.removeChild(anchorElement);
        window.URL.revokeObjectURL(href);
    }, 100);
}