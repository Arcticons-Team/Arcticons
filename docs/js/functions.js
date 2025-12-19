// Parse appfilter content
function parseAppfilter(appfilterContent) {
    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(appfilterContent, 'text/xml');
    const items = xmlDoc.querySelectorAll('item');
    const appfilterItems = [];
    items.forEach(item => {
        const component = item.getAttribute('component');
        if (component) {
            appfilterItems.push(component.trim());
        }
    });
    return appfilterItems;
}

// Function to shuffle an array
export function shuffleArray(arr) {
    for (let i = arr.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [arr[i], arr[j]] = [arr[j], arr[i]]; // Swap elements
    }
    return arr;
}
export function getAppfilterValue(entry, rename, replacement) {
    if (!rename) {
        replacement = entry.drawable;
    }
    return `<item component="ComponentInfo{${entry.componentInfo}}" drawable="${replacement}"/>`;
}
export function buildCopyText(entry, appfilterValue, mode) {
    return mode
        ? appfilterValue
        : `<!-- ${entry.appName} -->\n${appfilterValue}\n`;
}