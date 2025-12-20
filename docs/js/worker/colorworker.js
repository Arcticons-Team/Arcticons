// js/worker/colorWorker.js
self.onmessage = function(event) {
    const { allEntries, colorData } = event.data;

    // 1. Create Lookup Map
    const colorMap = new Map();
    for (let i = 0; i < colorData.length; i++) {
        colorMap.set(colorData[i].filename, colorData[i].unique_colors);
    }

    // 2. Patch entries
    for (let i = 0; i < allEntries.length; i++) {
        const entry = allEntries[i];
        const filename = `${entry.drawable}.webp`;
        entry.appIconColor = colorMap.get(filename) || 0;
    }

    // 3. Return the enriched list
    self.postMessage(allEntries);
};