import { state } from './state/store.js';

// Function to shuffle an array
export function shuffleArray(arr) {
    for (let i = arr.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [arr[i], arr[j]] = [arr[j], arr[i]]; // Swap elements
    }
    return arr;
}

// Copy to clipboard function
export function copyToClipboard(index, rename) {
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
                return buildCopyText(entry, appfilterValue, state.ui.showMatchingNames);
            })
            .join('\n');

        clearSelected();
    }

    // Single row mode
    else {
        const entry = state.view[index];
        const appfilterValue = getAppfilterValue(entry, rename, node.value);
        copyText = buildCopyText(entry, appfilterValue, state.ui.showMatchingNames);
    }

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
        ? appfilterValue
        : `<!-- ${entry.appName} -->\n${appfilterValue}\n`;
}