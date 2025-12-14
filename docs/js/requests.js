import { filterAppfilter, shuffleArray, sortData } from './functions.js';
import { TABLE_COLUMNS_Requests as TABLE_COLUMNS, DOM, imagepath } from './const.js';
import { state } from './state/store.js';
import { updateTable, lazyLoadAndRender } from './ui/tableRenderer.js';
import { copyToClipboard } from './events/button.js';
import { renderCategories, initCategoryUI } from './ui/category.js';

const toggleBtn = document.getElementById('show-matching-drawables-btn');
const toggleCell = document.getElementById('show-matching-drawables');

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
        document.getElementById('date_header').innerText = latestRequestDate.toLocaleString(undefined, {
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

toggleBtn.addEventListener('click', () => {
    state.ui.showMatchingDrawables = !state.ui.showMatchingDrawables;
    toggleBtn.innerText = state.ui.showMatchingDrawables
        ? "Show All Entries"
        : "Show Matching Drawables";

    toggleBtn.classList.toggle("active-toggle", state.ui.showMatchingDrawables);
    toggleCell.classList.toggle("active", state.ui.showMatchingDrawables);

    recomputeView();
});

function updateSortMarkers() {
    const headers = document.querySelectorAll('thead th');

    headers.forEach((th, index) => {
        th.classList.remove('asc', 'desc');

        if (index === state.ui.sort.column) {
            th.classList.add(state.ui.sort.direction);
        }
    });
}

// Accessing the button element by its id
const updatableButton = document.getElementById("updatable-button");
const randomButton = document.getElementById("random-button");
const randomResetButton = document.getElementById(`random-reset-button`);
const randomNumberInput = document.getElementById("random-number-input");

// Add an event listener to the button
updatableButton.addEventListener("click", function () {
    // Define the URL to redirect to
    const updatableURL = `updatable.html`;
    // Redirect to the specified URL
    window.location.href = updatableURL;
});
randomButton.addEventListener("click", function () {
    randomIcons();
});
randomResetButton.addEventListener("click", function () {
    randomResetButton.style.display = "none";
    updateTable(state.all);
});
randomNumberInput.addEventListener("keypress", function (event) {
    // If the user presses the "Enter" key on the keyboard
    if (event.key === "Enter") {
        // Cancel the default action, if needed
        event.preventDefault();
        // Trigger the button element with a click
        randomButton.click();
    }
});


// Update header text
function updateHeaderText(newHeader) {
    document.getElementById('header').innerText = newHeader;
    document.getElementById('smallheader').innerText = newHeader;
}

// Scroll event listener for lazy loading
const tableContainer = document.querySelector('.table-container');
tableContainer.addEventListener('scroll', () => {
    const { scrollTop, scrollHeight, clientHeight } = tableContainer;
    if (scrollTop + clientHeight >= scrollHeight - 100) {
        lazyLoadAndRender();
    }
});



function showClearSearchIcon() {
    const clearSearch = document.querySelector('#clear-search');
    if (DOM.searchInput.value.trim() === "") {
        clearSearch.style.visibility = 'hidden'; // Hide the icon if the input is empty
    } else {
        clearSearch.style.visibility = 'visible'; // Show the icon if the input has text
    }
}

document.getElementById('clear-search').addEventListener('click', clearSearch);

function clearSearch() {
    showClearSearchIcon();
    filterAppEntries();
}

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

DOM.regexSwitch.addEventListener('change', filterAppEntries);
document.getElementById('closePopup').addEventListener('click', filterAppEntries);
DOM.searchInput.addEventListener('input', filterAppEntries);

const toggleCategoryModeBtn = document.getElementById('Category_Match');
toggleCategoryModeBtn.addEventListener('click', () => {
    const one = state.ui.categoryMode === 'all';
    state.ui.categoryMode = one ? 'one' : 'all';

    toggleCategoryModeBtn.innerText = one
        ? "Match One Category"
        : "Match All Categories";

    toggleCategoryModeBtn.classList.toggle("active-toggle", one);
    recomputeView();
});

// Runs when "I'm feelin' lucky" button is clicked on
function randomIcons() {
    const randomResetButton = document.getElementById(`random-reset-button`);
    const randomNumberInput = document.getElementById(`random-number-input`); // Number of requests to select randomly
    const totalRequests = state.all.length; // Total numbers of requests

    const defaultRandomCnt = 10;
    const minRandomCnt = 1;

    if (defaultRandomCnt >= totalRequests) {
        notifyMessage(`There are TOO FEW requests! (Yay!)`);
        return;
    }

    let randomCnt = defaultRandomCnt; // Default is used when the number in the input box is not numeric

    if (!isNaN(parseInt(randomNumberInput.value)) && isFinite(randomNumberInput.value)) {
        randomNumberInput.value = parseInt(randomNumberInput.value);
        if (randomNumberInput.value == totalRequests) {
            return;
        }
        if (randomNumberInput.value > totalRequests) {
            notifyMessage(`There are fewer requests than ` + randomNumberInput.value);
            randomNumberInput.value = defaultRandomCnt;
        }
        // If value is too low (e.g. 0, -1), set to default
        if (randomNumberInput.value < minRandomCnt)
            randomNumberInput.value = defaultRandomCnt;

        randomCnt = randomNumberInput.value;
    }
    else {
        randomNumberInput.value = defaultRandomCnt;
    }

    // Randomization part
    const numArr = Array(totalRequests).fill().map((element, index) => index + minRandomCnt - 1); // Initialize an array of 0 to the total number of requests
    let slicedRandomNumArr = shuffleArray(numArr).slice(0, randomCnt); // Choose the first N as the random indices
    let randomizedEntriesData = [];
    for (let i = 0; i < slicedRandomNumArr.length; i++) {
        randomizedEntriesData.push(state.all[slicedRandomNumArr[i]]);
    }

    updateTable(randomizedEntriesData);
    randomResetButton.style.display = "inline-block";
}

function showInfo() {
    var popup = document.getElementById("infotext");
    popup.classList.toggle("show");
}
window.showInfo = showInfo;



function notifyMessage(message) {
    document.getElementById('search-notification').innerText = message;
    document.getElementById('search-notification').style.display = 'block';
    // Hide the notification after a few seconds
    setTimeout(() => {
        document.getElementById('search-notification').style.display = 'none';
    }, 5000);
}

const showMultipeBtn = document.getElementById('show-multiple');
showMultipeBtn.addEventListener('click', () => {
    state.ui.showMatchingNames = !state.ui.showMatchingNames;
    if (state.ui.showMatchingNames) {
        state.ui.sort.direction = 'asc';
        state.ui.sort.column = 0;
    }
    showMultipeBtn.classList.toggle("active-toggle", state.ui.showMatchingNames);
    showMultipeBtn.innerText = state.ui.showMatchingNames
        ? "Show All"
        : "Show Matching Name";
    recomputeView();
});

function filterEntriesByAppNameFrequency(appEntriesData, minOccurrences) {
    const appNameCount = {};
    appEntriesData.forEach(entry => {
        const { appName } = entry;
        appNameCount[appName] = (appNameCount[appName] || 0) + 1;
    });
    return appEntriesData.filter(entry => appNameCount[entry.appName] >= minOccurrences);
}

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
    const table = document.querySelector('table');
    const headers = table.querySelectorAll('thead th');
    state.ui.sort.column = columnIndex;
    state.ui.sort.direction = headers[columnIndex].classList.contains('asc') ? 'desc' : 'asc';
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
                console.log('All app entries have been updated with colors.');
            })
            .catch(error => {
                console.error('Error processing entries:', error);
            });
    });
}

RegexSearchSettings.addEventListener(
    "click",
    function () {
        myPopup.classList.add("show");
    }
);
closePopup.addEventListener(
    "click",
    function () {
        myPopup.classList.remove(
            "show"
        );
    }
);
window.addEventListener(
    "click",
    function (event) {
        if (event.target == myPopup) {
            myPopup.classList.remove(
                "show"
            );
        }
        if (event.target == document.getElementById("renamer-overlay")) {
            document.getElementById("renamer-overlay").classList.remove("show");
        }
    }
);

var node = document.getElementById("copy-selected-button");
var longpress = false;
var presstimer = null;
var longtarget = null;

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
    document.getElementById("renamer-overlay").classList.add("show");
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

node.addEventListener("mousedown", start);
node.addEventListener("touchstart", start);
node.addEventListener("click", click);
node.addEventListener("mouseout", cancel);
node.addEventListener("touchend", cancel);
node.addEventListener("touchleave", cancel);
node.addEventListener("touchcancel", cancel);

function recomputeView() {
    let data = state.all;
    // matching drawables
    if (state.ui.showMatchingDrawables && state.drawableSet) {
        data = data.reduce((acc, entry) => {
            const base = entry.baseDrawable || entry.drawable?.replace(/_\d+$/, '');

            if (state.drawableSet.has(entry.drawable)) {
                acc.push(entry);
            } else if (state.drawableSet.has(base)) {
                acc.push({
                    ...entry,
                    Arcticon: `<img src="https://raw.githubusercontent.com/Arcticons-Team/Arcticons/refs/heads/main/icons/white/${base}.svg" class="arcticon">`,
                    ArcticonPath: `https://raw.githubusercontent.com/Arcticons-Team/Arcticons/refs/heads/main/icons/white/${base}.svg`
                });
            }
            return acc;
        }, []);
    }
    // matching names
    if (state.ui.showMatchingNames) {
        const NumberInput = document.getElementById(`matching-number-input`); // Number of requests to select randomly
        NumberInput.value = parseInt(NumberInput.value)
        const threshold = NumberInput.value;
        data = filterEntriesByAppNameFrequency(data, threshold);
    }
    // category filter
    if (state.ui.categories.size) {
        DOM.clearCategoryBtn.style.visibility = 'visible';
        const cats = [...state.ui.categories];
        data = data.filter(e =>
            state.ui.categoryMode === 'one'
                ? cats.some(c => e.playStoreCategories.includes(c))
                : cats.every(c => e.playStoreCategories.includes(c))
        );
    } else {
        DOM.clearCategoryBtn.style.visibility = 'hidden';
    }
    // search
    if (state.ui.search) {
        showClearSearchIcon();
        if (state.ui.regex) {
            const re = new RegExp(state.ui.search, state.ui.regexFlags);
            data = data.filter(e =>
                state.ui.reverse
                    ? !re.test(e.searchText)
                    : re.test(e.searchText)
            );
        } else {
            const s = state.ui.search.toLowerCase();
            data = data.filter(e => e.appName.toLowerCase().includes(s));
        }
    }
    // sort
    data = sortData(
        state.ui.sort.direction,
        state.ui.sort.column,
        [...data],
        TABLE_COLUMNS
    );
    // submit
    state.startIndex = 0;
    state.view = data;
    updateTable(data);
    updateSortMarkers();
}