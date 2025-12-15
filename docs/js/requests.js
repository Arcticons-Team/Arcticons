import { filterAppfilter, shuffleArray } from './functions.js';
import { TABLE_COLUMNS_Requests as TABLE_COLUMNS, DOM, imagepath } from './const.js';
import { state } from './state/store.js';
import { updateTable, lazyLoadAndRender } from './ui/tableRenderer.js';
import { copyToClipboard } from './events/button.js';
import { renderCategories, initCategoryUI } from './ui/category.js';

function collectCategories(playStoreCategories) {
    playStoreCategories
        .filter(c => !/^#\d* top\b/.test(c))
        .forEach(c => state.allCategories.add(c));
}

function finalizeCategories() {
    const cats = [...state.allCategories];
    const pinned = cats.filter(c => c === 'App' || c === 'Game').sort();
    const rest = shuffleArray(cats.filter(c => !pinned.includes(c)));
    return new Set([...pinned, ...rest]);
}

// Debounce function for search input
const debounce = (func, delay) => {
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
initCategoryUI(recomputeView);
fetch(`assets/requests.json`)
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return response.json();
    })
    .then(JsonContent => {
        const iconcount = JsonContent.iconcount;
        // Set the latest request date
        const latestRequestDate = Object.values(JsonContent).reduce((latest, entry) => {
            const requestDate = new Date(parseFloat(entry.requestDate) * 1000); // Convert to milliseconds
            return requestDate > latest ? requestDate : latest;
        }, new Date(0));
        DOM.dateHeader.innerText = latestRequestDate.toLocaleString(undefined, {
            day: 'numeric', year: 'numeric', month: 'long'
        });

        // Process each app entry
        Object.entries(JsonContent).forEach(([componentInfo, entry]) => {
            const appName = entry.Name;
            const drawable = entry.drawable;
            const appIconPath = drawable ? `extracted_png/${drawable}.webp` : 'img/requests/default.svg';
            const appIcon = `<img src="${appIconPath}" alt="Icon">`;
            const playStoreDownloads = entry.PlayStore.Downloads.replace("no_data", "X");
            const playStoreCategories = entry.PlayStore.Categories;
            collectCategories(playStoreCategories);

            // Generate links (if available)
            const appLinks = [
                `<a href="https://play.google.com/store/apps/details?id=${componentInfo.split('/')[0]}" class="links" target="_blank"><img src="${imagepath.playStore}" alt="Image"></a>`,
                `<a href="https://f-droid.org/en/packages/${componentInfo.split('/')[0]}/" class="links" id='fdroid' target="_blank"><img src="${imagepath.fdroid}" alt="Image"></a>`,
                `<a href="https://apt.izzysoft.de/fdroid/index/apk/${componentInfo.split('/')[0]}" class="links" id='izzy' target="_blank"><img src="${imagepath.izzyOnDroid}" alt="Image"></a>`,
                `<a href="https://galaxystore.samsung.com/detail/${componentInfo.split('/')[0]}" class="links" id='galaxy' target="_blank"><img src="${imagepath.galaxyStore}" alt="Image"></a>`,
                `<a href="https://www.ecosia.org/search?q=${componentInfo.split('/')[0]}" class="links" target="_blank"><img src="${imagepath.wwwSearch}" alt="Image"></a>`
            ].join('\n');
            // Process each entry and store data    

            const appNameAppfilter = `<!-- ${entry.Name} -->`;
            const appfilter = `<item component="ComponentInfo{${componentInfo}}" drawable="${drawable}"/>`;
            const requestedInfo = entry.count;
            const lastRequestedTime = new Date(parseFloat(entry.requestDate) * 1000).toLocaleString().replace(',', '');
            const appIconColor = 0;
            const Arcticon = `<img src="https://raw.githubusercontent.com/Arcticons-Team/Arcticons/refs/heads/main/icons/white/${drawable}.svg" alt="Arcticon" class="arcticon">`;
            const ArcticonPath = `https://raw.githubusercontent.com/Arcticons-Team/Arcticons/refs/heads/main/icons/white/${drawable}.svg`;
            const searchText = (appNameAppfilter + appfilter).toLowerCase();
            state.all.push({
                appName,
                appIcon,
                Arcticon,
                appLinks,
                playStoreDownloads,
                requestedInfo,
                lastRequestedTime,
                appNameAppfilter,
                appfilter,
                appIconPath,
                appIconColor,
                playStoreCategories,
                drawable,
                ArcticonPath,
                searchText

            });
        });

        fetch(`assets/combined_appfilter.xml`)
            .then(response => {
                if (!response.ok) {
                    // If appfilter.xml cannot be loaded, render appEntriesData as is
                    console.error('Error fetching appfilter:', response.status);
                    recomputeView();
                    return;
                }
                return response.text();
            })
            .then(appfilterContent => {
                if (!appfilterContent) {
                    // If appfilterContent is empty, render appEntriesData as is
                    console.error('Empty appfilter content');
                    recomputeView();
                    return;
                }

                // Extract all drawable values from appfilterContent
                const parser = new DOMParser();
                const xmlDoc = parser.parseFromString(appfilterContent, "application/xml");
                const items = xmlDoc.querySelectorAll("item");

                items.forEach(item => {
                    const drawable = item.getAttribute("drawable");
                    if (drawable) {
                        state.drawableSet.add(drawable);
                    }
                });

                const filteredData = filterAppfilter(state.all, appfilterContent);
                state.all = filteredData;
                updateHeaderText(`${state.all.length} Requested Apps`);
                LoadColorData();
                // Initial render
                filterAppEntries();
                state.allCategories = finalizeCategories();
                renderCategories();
                //setCategory();
            })
            .catch(error => console.error('Error fetching or processing appfilter:', error));
    })
    .catch(error => console.error('Error fetching file:', error));

DOM.matchingDrawablesBtn.addEventListener('click', () => {
    state.ui.showMatchingDrawables = !state.ui.showMatchingDrawables;
    DOM.matchingDrawablesBtn.innerText = state.ui.showMatchingDrawables
        ? "Show All Entries"
        : "Show Matching Drawables";

    DOM.matchingDrawablesBtn.classList.toggle("active-toggle", state.ui.showMatchingDrawables);
    DOM.matchingDrawableColumn.classList.toggle("active", state.ui.showMatchingDrawables);

    recomputeView();
});

function updateSortMarkers() {
    DOM.sortableHeaders.forEach((th, index) => {
        th.classList.remove('asc', 'desc');

        if (index === state.ui.sort.column) {
            th.classList.add(state.ui.sort.direction);
        }
    });
}
// Add an event listener to the button
DOM.updatableButton.addEventListener("click", function () {
    // Define the URL to redirect to
    const updatableURL = `updatable.html`;
    // Redirect to the specified URL
    window.location.href = updatableURL;
});
DOM.randomButton.addEventListener("click", function () {
    randomIcons();
});
DOM.randomResetButton.addEventListener("click", function () {
    DOM.randomResetButton.style.display = "none";
    updateTable(state.all);
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


// Update header text
function updateHeaderText(newHeader) {
    DOM.header.innerText = newHeader;
    DOM.smallheader.innerText = newHeader;
}

// Scroll event listener for lazy loading
DOM.requestsTableContainer.addEventListener('scroll', () => {
    const { scrollTop, scrollHeight, clientHeight } = DOM.requestsTableContainer;
    if (scrollTop + clientHeight >= scrollHeight - 100) {
        lazyLoadAndRender();
    }
});

// Search function
const filterAppEntries = debounce(() => {
    state.ui.search = DOM.searchInput.value;
    state.ui.regex = DOM.regexSwitch.checked;
    state.ui.reverse = DOM.reverseSwitch.checked;

    state.ui.regexFlags =
        (DOM.caseInsensitive.checked ? 'i' : '') +
        (DOM.caseUnicode.checked ? 'u' : '');

    recomputeView();
}, 500);

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

// Runs when "I'm feelin' lucky" button is clicked on
function randomIcons() {
    const totalRequests = state.all.length; // Total numbers of requests

    const defaultRandomCnt = 10;
    const minRandomCnt = 1;

    if (defaultRandomCnt >= totalRequests) {
        notifyMessage(`There are TOO FEW requests! (Yay!)`);
        return;
    }

    let randomCnt = defaultRandomCnt; // Default is used when the number in the input box is not numeric

    if (!isNaN(parseInt(DOM.randomNumberInput.value)) && isFinite(DOM.randomNumberInput.value)) {
        DOM.randomNumberInput.value = parseInt(DOM.randomNumberInput.value);
        if (DOM.randomNumberInput.value == totalRequests) {
            return;
        }
        if (DOM.randomNumberInput.value > totalRequests) {
            notifyMessage(`There are fewer requests than ` + DOM.randomNumberInput.value);
            DOM.randomNumberInput.value = defaultRandomCnt;
        }
        // If value is too low (e.g. 0, -1), set to default
        if (DOM.randomNumberInput.value < minRandomCnt)
            DOM.randomNumberInput.value = defaultRandomCnt;

        randomCnt = DOM.randomNumberInput.value;
    }
    else {
        DOM.randomNumberInput.value = defaultRandomCnt;
    }

    // Randomization part
    const numArr = Array(totalRequests).fill().map((element, index) => index + minRandomCnt - 1); // Initialize an array of 0 to the total number of requests
    let slicedRandomNumArr = shuffleArray(numArr).slice(0, randomCnt); // Choose the first N as the random indices
    let randomizedEntriesData = [];
    for (let i = 0; i < slicedRandomNumArr.length; i++) {
        randomizedEntriesData.push(state.all[slicedRandomNumArr[i]]);
    }

    updateTable(randomizedEntriesData);
    DOM.randomResetButton.style.display = "inline-block";
}

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

// Function to parse XML and return color data as an object
function loadColorsFromXML(xmlFilePath, callback) {
    // Fetch the XML file
    fetch(xmlFilePath)
        .then(response => response.text())  // Get the text content of the XML file
        .then(xmlText => {
            // Parse the XML text into a DOM object
            const parser = new DOMParser();
            const xmlDoc = parser.parseFromString(xmlText, "application/xml");

            const colorData = {};

            // Loop through each <image> element in the XML
            const images = xmlDoc.getElementsByTagName('image');
            for (let i = 0; i < images.length; i++) {
                const image = images[i];
                const filename = image.getElementsByTagName('filename')[0].textContent;
                const uniqueColors = image.getElementsByTagName('unique_colors')[0].textContent;
                colorData[filename] = uniqueColors; // Store the color by filename
            }

            // Call the callback with the color data
            callback(colorData);
        })
        .catch(error => {
            console.error('Error loading XML:', error);
        });
}

// Sort table function with optional sortingDirection parameter
function sortTable(columnIndex) {
    state.ui.sort.column = columnIndex;
    state.ui.sort.direction = DOM.sortableHeaders[columnIndex].classList.contains('asc') ? 'desc' : 'asc';
    recomputeView();
}
window.sortTable = sortTable;

function LoadColorData() {
    const xmlFilePath = 'assets/image_color_counts.xml';
    // Load the color data from the XML file
    loadColorsFromXML(xmlFilePath, function (colorData) {
        if (!colorData) {
            console.error('No color data available.');
            return;
        }
        const promises = state.all.map(entry => {
            const appIconName = entry.appIconPath.split('/').pop();  // Get the filename from the path
            const newColor = colorData[appIconName];
            if (newColor) {
                entry.appIconColor = newColor;
            } else {
                entry.appIconColor = 0; // Default to 0 if no color data found
            }
        });
        // Wait for all promises to resolve
        Promise.all(promises)
            .then(() => {
            })
            .catch(error => {
                console.error('Error processing entries:', error);
            });
    });
}

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

DOM.regexSearchSettingsBtn.addEventListener(
    "click",
    function () {
        regexPopup.classList.add("show");
    }
);
DOM.closeRegexSettingsBtn.addEventListener(
    "click",
    function () {
        regexPopup.classList.remove(
            "show"
        );
    }
);
window.addEventListener(
    "click",
    function (event) {
        if (event.target == myPopup) {
            regexPopup.classList.remove(
                "show"
            );
        }
        if (event.target == DOM.renameOverlay) {
            DOM.renameOverlay.classList.remove("show");
        }
    }
);


var longpress = false;
var presstimer = null;

var cancel = function (e) {
    console.log("cancel");

    if (presstimer !== null) {
        clearTimeout(presstimer);
        presstimer = null;
    }

    this.classList.remove("longpress");
};

var click = function (e) {
    console.log("click");
    if (presstimer !== null) {
        clearTimeout(presstimer);
        presstimer = null;
    }

    this.classList.remove("longpress");

    if (longpress) {
        return false;
    }
    DOM.renameOverlay.classList.add("show");
};

var start = function (e) {
    console.log("start");
    console.log(e);

    if (e.type === "click" && e.button !== 0) {
        return;
    }

    longpress = false;

    this.classList.add("longpress");

    if (presstimer === null) {
        presstimer = setTimeout(function () {
            copyToClipboard(null, false);
            longpress = true;
        }, 500);
    }

    return false;
};

DOM.copySelectedBtn.addEventListener("mousedown", start);
DOM.copySelectedBtn.addEventListener("touchstart", start);
DOM.copySelectedBtn.addEventListener("click", click);
DOM.copySelectedBtn.addEventListener("mouseout", cancel);
DOM.copySelectedBtn.addEventListener("touchend", cancel);
DOM.copySelectedBtn.addEventListener("touchleave", cancel);
DOM.copySelectedBtn.addEventListener("touchcancel", cancel);

function updateUIState(state) {
    DOM.clearCategoryBtn.style.visibility =
        state.ui.categories.size ? 'visible' : 'hidden';

    DOM.clearSearchBtn.style.visibility =
        state.ui.search ? 'visible' : 'hidden';
}

let computeWorker;

function recomputeView() {
    updateUIState(state);

    if (!computeWorker) {
        computeWorker = new Worker('./js/worker/worker.js');
    }

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
    };
}

DOM.matchingNumberInput.addEventListener('input', () => {
    const value = parseInt(DOM.matchingNumberInput.value, 10);
    // Validate the value
    state.ui.matchingNameThreshold = isNaN(value) || value < 1 ? 1 : value;
    // Optionally recompute the view
    if (state.ui.showMatchingNames) recomputeView();
});
