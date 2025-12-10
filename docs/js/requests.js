// Array of Link Images
const imageNames = ['img/requests/google-play-store.svg', 'img/requests/f-droid.svg', 'img/requests/izzyondroid.svg', 'img/requests/galaxystore.svg', 'img/requests/search-globe.svg'];
let appEntriesData = []; // Store the original data for sorting
var appEntriesDataGlobal = []; // Currently displayed data
var appEntriesDataFiltered = []; // Filtered data based on category
var appEntriesDataMatched = []; //  Matched data based on drawable
// Lazy loading and virtualization
const batchSize = 30; // Number of rows to load at a time
let startIndex = 0; // Start index for lazy loading
// Global variables to track sorting column and direction
let sortingColumnIndex = 5;
let sortingDirection = 'desc';
var selectedRows = new Set();
var AllCategories = new Set();

let isShowingMatches = false; // Toggle state

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
            const filteredCategories = playStoreCategories.filter(category => {
                return !/^#\d* top\b/.test(category); // Exclude based on the regex
            });
            filteredCategories.forEach(category => AllCategories.add(category));
            // Convert Set to Array
            let categoriesArray = Array.from(AllCategories);
            // Extract 'App' and 'Game' from the array
            let specialCategories = categoriesArray.filter(cat => cat === "App" || cat === "Game");
            specialCategories.sort(); // Sort 'App' and 'Game' alphabetically
            // Remove 'App' and 'Game' from the original array
            categoriesArray = categoriesArray.filter(cat => cat !== "App" && cat !== "Game");
            // Shuffle the remaining categories
            categoriesArray = shuffleArray(categoriesArray);
            // Combine 'App' and 'Game' at the front
            categoriesArray = [...specialCategories, ...categoriesArray];
            // Convert the array back to a Set
            AllCategories = new Set(categoriesArray);

            // Generate links (if available)
            const appLinks = [
                `<a href="https://play.google.com/store/apps/details?id=${componentInfo.split('/')[0]}" class="links" target="_blank"><img src="img/requests/google-play-store.svg" alt="Image"></a>`,
                `<a href="https://f-droid.org/en/packages/${componentInfo.split('/')[0]}/" class="links" id='fdroid' target="_blank"><img src="img/requests/f-droid.svg" alt="Image"></a>`,
                `<a href="https://apt.izzysoft.de/fdroid/index/apk/${componentInfo.split('/')[0]}" class="links" id='izzy' target="_blank"><img src="img/requests/izzyondroid.svg" alt="Image"></a>`,
                `<a href="https://galaxystore.samsung.com/detail/${componentInfo.split('/')[0]}" class="links" id='galaxy' target="_blank"><img src="img/requests/galaxystore.svg" alt="Image"></a>`,
                `<a href="https://www.ecosia.org/search?q=${componentInfo.split('/')[0]}" class="links" target="_blank"><img src="img/requests/search-globe.svg" alt="Image"></a>`
            ].join('\n');
            // Process each entry and store data    

            const appNameAppfilter = `<!-- ${entry.Name} -->`;
            const appfilter = `<item component="ComponentInfo{${componentInfo}}" drawable="${drawable}"/>`;
            const requestedInfo = entry.count;
            const lastRequestedTime = new Date(parseFloat(entry.requestDate) * 1000).toLocaleString().replace(',', '');
            const appIconColor = 0;
            const Arcticon = `<img src="https://raw.githubusercontent.com/Arcticons-Team/Arcticons/refs/heads/main/icons/white/${drawable}.svg" alt="Arcticon" class="arcticon">`;
            const ArcticonPath = `https://raw.githubusercontent.com/Arcticons-Team/Arcticons/refs/heads/main/icons/white/${drawable}.svg`;
            appEntriesData.push({
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
                ArcticonPath

            });
        });
        appEntriesDataGlobal = appEntriesData;
        appEntriesDataFiltered = appEntriesData;
        appEntriesDataMatched = appEntriesData;
        console.log("All Categories:", AllCategories);



        // Example usage:
        fetch(`assets/combined_appfilter.xml`)
            .then(response => {
                if (!response.ok) {
                    // If appfilter.xml cannot be loaded, render appEntriesData as is
                    console.error('Error fetching appfilter:', response.status);
                    lazyLoadAndRender();
                    return;
                }
                return response.text();
            })
            .then(appfilterContent => {
                if (!appfilterContent) {
                    // If appfilterContent is empty, render appEntriesData as is
                    console.error('Empty appfilter content');
                    lazyLoadAndRender();
                    return;
                }

                // 🔽 Extract all drawable values from appfilterContent
                const parser = new DOMParser();
                const xmlDoc = parser.parseFromString(appfilterContent, "application/xml");
                const items = xmlDoc.querySelectorAll("item");

                // Create a Set to store unique drawable names
                const drawableSet = new Set();

                items.forEach(item => {
                    const drawable = item.getAttribute("drawable");
                    if (drawable) {
                        drawableSet.add(drawable);
                    }
                });

                // Now drawableSet contains all unique drawable names
                console.log(`Total unique drawables found: ${drawableSet.size}`);
                console.log(drawableSet);
                window.drawableSet = drawableSet;
                // 🔼

                const filteredData = filterAppfilter(appEntriesData, appfilterContent);
                appEntriesData = filteredData;
                appEntriesDataGlobal = filteredData;
                appEntriesDataFiltered = filteredData;
                appEntriesDataMatched = filteredData;
                updateHeaderText(`${appEntriesData.length} Requested Apps`);
                const table = document.querySelector('table');
                const headers = table.querySelectorAll('thead th');
                headers[sortingColumnIndex].classList.add(sortingDirection);

                LoadColorData();
                // Initial render
                lazyLoadAndRender();
                // Optionally, trigger the function immediately if needed (e.g., if the page is loaded with a default state):
                filterAppEntries();
                setCategory();
            })
            .catch(error => console.error('Error fetching or processing appfilter:', error));
    })
    .catch(error => console.error('Error fetching file:', error));

function setCategory() {
    // Find the div where the buttons will be added
    const categoriesDiv = document.querySelector('.categories');

    // Add each category as a button
    AllCategories.forEach(category => {
        const button = document.createElement('button'); // Create a button element
        button.textContent = category; // Set button text to the category name
        button.id = 'category-button'; // Add a class for styling
        button.className = 'green-button'; // Add a class for styling
        button.onclick = () => toggleCategory(button, category); // Set an onclick handler
        categoriesDiv.appendChild(button); // Add the button to the div
    });

}


const toggleBtn = document.getElementById('show-matching-drawables-btn');
const toggleCell = document.getElementById('show-matching-drawables');

toggleBtn.addEventListener('click', () => {
    if (!window.drawableSet || window.drawableSet.size === 0) {
        console.warn('Drawable set not initialized or empty');
        return;
    }
    if (!isShowingMatches) {
        showMatchingDrawables();
    } else {
        // 🔁 Revert to full data
        revertMatchingDrawables();
    }
});

function showMatchingDrawables() {
    toggleBtn.innerText = "Show All Entries";
    toggleBtn.classList.add("active-toggle");
    toggleCell.classList.add("active");
    isShowingMatches = true;

    const matchingEntries = appEntriesData.filter(entry => {
        let baseDrawable = entry.drawable.replace(/_\d+$/, '');

        if (window.drawableSet.has(entry.drawable)) {
            //entry.arcticon = entry.drawable; // exact match
            return true;
        } else if (window.drawableSet.has(baseDrawable)) {
            let Arcticon = `<img src="https://raw.githubusercontent.com/Arcticons-Team/Arcticons/refs/heads/main/icons/white/${baseDrawable}.svg" alt="Arcticon" class="arcticon">`;
            let ArcticonPath = `https://raw.githubusercontent.com/Arcticons-Team/Arcticons/refs/heads/main/icons/white/${baseDrawable}.svg`;
            entry.Arcticon = Arcticon; // matched base
            entry.ArcticonPath = ArcticonPath; // matched base
            return true;
        }

        return false;
    });
    console.log(`Total matches found: ${matchingEntries.length}`);
    appEntriesDataMatched = matchingEntries;
    if (matchingEntries.length === 0) {
        document.getElementById('search-notification').innerText = `No matching drawable entries found.`;
        document.getElementById('search-notification').style.display = 'block';
        setTimeout(() => {
            document.getElementById('search-notification').style.display = 'none';
        }, 5000);
        updateTable([]);
    } else {
        document.getElementById('search-notification').style.display = 'none';
        const filteredandsortedData = sortData(sortingDirection, sortingColumnIndex, [...matchingEntries]);
        updateTable(filteredandsortedData);
        filterCategory();
    }
}

function revertMatchingDrawables() {
    toggleBtn.innerText = "Show Matching Drawables";
    toggleBtn.classList.remove("active-toggle");
    toggleCell.classList.remove("active");
    isShowingMatches = false;

    document.getElementById('search-notification').style.display = 'none';
    const fullDataSorted = sortData(sortingDirection, sortingColumnIndex, [...appEntriesData]);
    appEntriesDataMatched = fullDataSorted;
    updateTable(fullDataSorted);
    filterCategory();
}

// Function to shuffle an array
function shuffleArray(arr) {
    for (let i = arr.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [arr[i], arr[j]] = [arr[j], arr[i]]; // Swap elements
    }
    return arr;
}

// Function to toggle button activation
function toggleCategory(button, category) {
    const isActive = button.hasAttribute('activated');

    if (isActive) {
        // Deactivate the button if already active
        button.removeAttribute('activated');
        console.log(`Deactivated category: ${category}`);
    } else {
        // Activate the button if not active
        button.setAttribute('activated', 'true');
        console.log(`Activated category: ${category}`);
    }
    filterCategory()
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
    updateTable(appEntriesData);
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

// Filter appEntriesData based on appfilter content
function filterAppfilter(appEntriesData, appfilterContent) {
    const appfilterItems = new Set(parseAppfilter(appfilterContent)); // Convert to Set for fast lookups
    const filteredOutEntries = [];

    const filteredData = appEntriesData.filter(entry => {
        const entryAppfilter = entry.appfilter.trim().split('"')[1].trim();
        if (appfilterItems.has(entryAppfilter)) { // Check membership in O(1)
            filteredOutEntries.push(entryAppfilter); // Track filtered out entries
            return false; // Exclude from filtered data
        }
        return true; // Include in filtered data
    });

    console.log("Filtered out entries:", filteredOutEntries);
    return filteredData;
}


// Function to extract the drawable attribute from appfilter
function extractDrawable(appfilter) {
    const regex = /drawable="([^"]+)"/;
    const match = appfilter.match(regex);
    if (match && match.length > 1) {
        return match[1]; // Return the value inside the quotes
    }
    return null; // Return null if no match found
}

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

// Update header text
function updateHeaderText(newHeader) {
    header = newHeader;
    document.getElementById('header').innerText = newHeader;
    document.getElementById('smallheader').innerText = newHeader;
}

function lazyLoadAndRender() {
    const dataBatch = appEntriesDataGlobal.slice(startIndex, startIndex + batchSize);
    renderTable(dataBatch);
    startIndex += batchSize;
}

// Scroll event listener for lazy loading
const tableContainer = document.querySelector('.table-container');
tableContainer.addEventListener('scroll', () => {
    const { scrollTop, scrollHeight, clientHeight } = tableContainer;
    if (scrollTop + clientHeight >= scrollHeight - 100) {
        lazyLoadAndRender();
    }
});

// Function to clear existing table rows
function clearTable() {
    const table = document.getElementById("app-entries");
    while (table.rows.length > 0) {
        table.deleteRow(0);
    }
}

function renderTable(data) {
    const table = document.getElementById("app-entries");
    data.forEach((entry, index) => {
        let row = table.insertRow();
        let cellAppName = row.insertCell(0);
        let cellAppIcon = row.insertCell(1);
        let cellArcticon = row.insertCell(2);
        let cellLinks = row.insertCell(3);
        let cellDownloads = row.insertCell(4);
        let cellReqInfo = row.insertCell(5);
        let cellReqTime = row.insertCell(6);
        let cellCopy = row.insertCell(7);
        index = index + startIndex;
        // Make cell1 clickable
        cellAppName.textContent = entry.appName;
        cellAppName.style.cursor = "pointer";
        cellAppName.addEventListener("click", () => {
            if (selectedRows.has(index)) {
                selectedRows.delete(index);
                row.classList.remove("row-glow");
            } else {
                selectedRows.add(index);
                row.classList.add("row-glow");
            }
            console.log("Selected Rows:", Array.from(selectedRows));
        });
        // Render the app icon as a clickable image
        cellAppIcon.innerHTML = `<a href="#" class="icon-preview" data-index="${index}" column="AppIcon">${entry.appIcon}</a>`;
        cellLinks.innerHTML = entry.appLinks;
        cellDownloads.innerHTML = entry.playStoreDownloads;
        cellReqInfo.innerHTML = entry.requestedInfo;
        cellReqTime.innerHTML = entry.lastRequestedTime;
        cellCopy.innerHTML = `<button class="green-button" id="copy-button" onclick="copyToClipboard(${index},false)"><img class="copy-icon" src="img/requests/copy.svg" alt="Copy"><span class="copy-text">Copy</span></button>`;
        if (isShowingMatches) {
            cellArcticon.innerHTML = `<a href="#"class="icon-preview" data-index="${index}" column="Arcticon">${entry.Arcticon}</a>`;
        } else {
            cellArcticon.innerHTML = `<span class="arcticon-placeholder">No Match</span>`;
        }
        // Show/hide all Arcticon cells (3rd column) and adjust other cells accordingly
        document.querySelectorAll('td:nth-child(3)').forEach(td => {
            td.style.display = isShowingMatches ? 'table-cell' : 'none';
        });
        // Also update the header visibility
        const arctIconHeader = document.querySelector('th:nth-child(3)');
        if (arctIconHeader) {
            arctIconHeader.style.display = isShowingMatches ? 'table-cell' : 'none';
        }
    });

    // Add event listeners to the icon previews
    const iconPreviews = document.querySelectorAll('.icon-preview');
    iconPreviews.forEach(icon => {
        icon.addEventListener('click', function (event) {
            event.preventDefault();
            const index = parseInt(this.getAttribute('data-index'));
            const column = this.getAttribute('column');
            const entry = appEntriesDataGlobal[index];
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

function showIconPreview(iconSrc, column) {
    const previewOverlay = document.getElementById('preview-overlay');
    const previewImage = document.getElementById('preview-image');

    // Set the preview image source to the clicked icon source
    previewImage.src = iconSrc;
    if (column === "Arcticon") {
        previewImage.id = 'preview-arcticon'; // Set the ID for the preview image
    }

    // Show the preview overlay
    previewOverlay.style.display = 'block';
    // Add click event listener to hide the preview when clicked on the overlay or close button
    previewOverlay.addEventListener('click', function (e) {
        if (e.target === this || e.target.classList.contains('close-button-class')) {
            // Hide the preview overlay
            previewImage.id = 'preview-image';
            this.style.display = 'none';
        }
    });
}

// Update the table with filtered or sorted data
function updateTable(data) {
    startIndex = 0;
    clearTable(); // Clear existing table rows
    appEntriesDataGlobal = data;
    lazyLoadAndRender();
}

// Copy to clipboard function
function copyToClipboard(index, rename) {
    let copyText = "";
    const regex = /(?<=drawable=")[^"]+(?="\/>)/;
    let replacementText = null;

    // Handle rename mode
    if (rename) {
        const node = document.getElementById("drawableName-input");
        replacementText = node.value;
        document.getElementById("renamer-overlay").classList.remove("show");
    }

    function getAppfilterValue(entry) {
        if (!rename) return entry.appfilter;
        return entry.appfilter.replace(regex, replacementText);
    }

    // Multi-row mode
    if (index === null) {
        for (const rowIndex of selectedRows) {
            const entry = appEntriesDataGlobal[rowIndex];
            const appfilterValue = getAppfilterValue(entry);

            if (isShowingMatches) {
                copyText += `${appfilterValue}\n`;
            } else {
                copyText += `${entry.appNameAppfilter}\n${appfilterValue}\n\n`;
            }
        }

        clearSelected();
    }

    // Single row mode
    else {
        const entry = appEntriesDataGlobal[index];
        const appfilterValue = getAppfilterValue(entry);

        if (isShowingMatches) {
            copyText = `${appfilterValue}`;
        } else {
            copyText = `${entry.appNameAppfilter}\n${appfilterValue}`;
        }
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

function clearSelected() {
    const table = document.querySelector('table');

    selectedRows.forEach((rowIndex) => {
        const row = table.tBodies[0].rows[rowIndex]; // Adjust index if needed
        row.classList.remove("row-glow");
    });
    selectedRows.clear();
    console.log("All rows deselected.");
}

function findCategory() {
    showClearSearchCategory(); // Assuming this function clears previous results or manages the UI

    // Get the search input value and convert it to lowercase
    const searchInput = document.getElementById('search-input_category').value.toLowerCase();

    // Select all category buttons
    const categoryButtons = document.querySelectorAll('#category-button');

    // Loop through the buttons and toggle visibility based on the search input
    categoryButtons.forEach(button => {
        const categoryText = button.textContent.toLowerCase(); // Get the category text
        if (categoryText.includes(searchInput)) {
            button.style.display = 'inline-block'; // Show the button if it matches
        } else {
            button.style.display = 'none'; // Hide the button if it doesn't match
        }
    });
}

function showClearSearchIcon() {
    const clearSearch = document.querySelector('#clear-search');
    if (document.getElementById('search-input').value.trim() === "") {
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

function showClearSearchCategory() {
    const clearSearch = document.querySelector('#clear-search_category');
    if (document.getElementById('search-input_category').value.trim() === "") {
        clearSearch.style.visibility = 'hidden'; // Hide the icon if the input is empty
    } else {
        clearSearch.style.visibility = 'visible'; // Show the icon if the input has text
    }
}

document.getElementById('clear-search_category').addEventListener('click', clearSearchCategory);

function clearSearchCategory() {
    console.log("Clearing search category");
    showClearSearchCategory();
    findCategory();
}

function showClearCategory() {
    const clearSearch = document.querySelector('#clear-category');
    const activatedCategories = Array.from(document.querySelectorAll('#category-button[activated]'))

    if (activatedCategories.length === 0) {
        clearSearch.style.visibility = 'hidden'; // Hide the icon if the input is empty
    } else {
        clearSearch.style.visibility = 'visible'; // Show the icon if the input has text
    }
}

document.getElementById('clear-category').addEventListener('click', clearCategory);

function clearCategory() {
    showClearCategory();
    document.querySelectorAll('#category-button').forEach(btn => {
        btn.removeAttribute('activated');
    });
    filterCategory();
}

// Search function
const filterAppEntries = debounce(() => {

    showClearSearchIcon();
    if (document.getElementById('regex-switch').checked) {
        const searchInput = document.getElementById('search-input').value;
        const regexFlagInsensitive = document.getElementById('caseInsensitive-switch').checked ? 'i' : '';
        const regexFlagUnicode = document.getElementById('caseUnicode-switch').checked ? 'u' : '';
        const regexFlags = regexFlagInsensitive + regexFlagUnicode;
        // Create a regex from the search input, escaping special characters if necessary
        let regex;
        try {
            // This allows for user input to be interpreted as a regex pattern
            regex = new RegExp(searchInput, regexFlags); // 'i' for case-insensitive matching
        } catch (e) {
            // If the input is not a valid regex, treat it as a normal string search
            regex = new RegExp(searchInput.replace(/[.*+?^=!:${}()|\[\]\/\\]/g, '\\$&'), 'i');
        }
        let filteredData; // Declare filteredData outside of the if-else block
        if (document.getElementById('reverse-switch').checked) {
            //Put entries that don't match into filteredData
            filteredData = appEntriesDataFiltered.filter(entry =>
                !regex.test(entry.appNameAppfilter + entry.appfilter) // Use the regex to test the appName

            );
        } else {
            filteredData = appEntriesDataFiltered.filter(entry =>
                regex.test(entry.appNameAppfilter + entry.appfilter) // Use the regex to test the appName

            );
        }

        // If no results are found, show a notification
        if (filteredData.length === 0) {
            document.getElementById('search-notification').innerText = `No results found.`;
            document.getElementById('search-notification').style.display = 'block';
            // Hide the notification after a few seconds
            setTimeout(
                () => {
                    document.getElementById('search-notification').style.display = 'none';
                },
                5000
            );
            updateTable([]);
        } else {
            document.getElementById('search-notification').style.display = 'none';
            const filteredandsortedData = sortData(sortingDirection, sortingColumnIndex, [
                ...filteredData
            ])
            updateTable(filteredandsortedData);
        }
    } else {
        const searchInput = document.getElementById('search-input').value.toLowerCase();
        const filteredData = appEntriesDataFiltered.filter(entry =>
            entry.appName.toLowerCase().includes(searchInput)
        );
        // If no results are found, show a notification
        if (filteredData.length === 0) {
            document.getElementById('search-notification').innerText = `No results found.`;
            document.getElementById('search-notification').style.display = 'block';
            // Hide the notification after a few seconds
            setTimeout(() => {
                document.getElementById('search-notification').style.display = 'none';
            }, 5000);
            updateTable([]);
        } else {
            document.getElementById('search-notification').style.display = 'none';
            const filteredandsortedData = sortData(sortingDirection, sortingColumnIndex, [...filteredData])
            updateTable(filteredandsortedData);
        }
    }
}, 500);

document.getElementById('regex-switch').addEventListener('change', filterAppEntries);
document.getElementById('closePopup').addEventListener('click', filterAppEntries);
document.getElementById('rename-button').addEventListener('click', () => {
    copyToClipboard(null, true);
});

// Sort table function with optional sortingDirection parameter
function sortTable(columnIndex, localsortingDirection = null) {
    const table = document.querySelector('table');
    const headers = table.querySelectorAll('thead th');

    // If sortingDirection is provided, use it; otherwise, determine it based on the current header class
    if (localsortingDirection === null) {
        sortingDirection = headers[columnIndex].classList.contains('asc') ? 'desc' : 'asc';
    }
    else {
        sortingDirection = localsortingDirection;
    }

    // Remove sorting indicators from all headers
    headers.forEach(header => {
        header.classList.remove('asc', 'desc');
    });

    // Add the appropriate sorting class to the clicked header
    headers[columnIndex].classList.add(sortingDirection);
    sortingColumnIndex = columnIndex;
    // Sort the data
    const sortedData = sortData(sortingDirection, columnIndex, [...appEntriesDataGlobal]);

    updateTable(sortedData);
}

function sortData(sortingDirection, columnIndex, sortedData) {
    sortedData.sort((a, b) => {
        if (columnIndex === 6) { // Check if sorting the 'Last Requested' column
            const cellA = getCellValue(a, columnIndex);
            const cellB = getCellValue(b, columnIndex);

            // Handle dates
            return sortingDirection === 'asc' ? cellA - cellB : cellB - cellA;
        } else if (columnIndex === 5) {
            const cellA = getCellValue(a, columnIndex);
            const cellB = getCellValue(b, columnIndex);

            // Handle numerical values
            if (!isNaN(cellA) && !isNaN(cellB)) {
                return sortingDirection === 'asc' ? cellA - cellB : cellB - cellA;
            }
        } else if (columnIndex === 1) {
            const offset = 9;
            const cellA = getCellValue(a, columnIndex + offset);
            const cellB = getCellValue(b, columnIndex + offset);
            // Handle numerical values
            if (!isNaN(cellA) && !isNaN(cellB)) {
                return sortingDirection === 'asc' ? cellA - cellB : cellB - cellA;
            }

        } else if (columnIndex === 4) {
            const cellA = parseDownloadValue(getCellValue(a, columnIndex), sortingDirection);
            const cellB = parseDownloadValue(getCellValue(b, columnIndex), sortingDirection);
            // Handle numerical values
            if (!isNaN(cellA) && !isNaN(cellB)) {
                return sortingDirection === 'asc' ? cellA - cellB : cellB - cellA;
            }
        } else {
            // Default to string comparison
            const cellA = a[Object.keys(a)[columnIndex]].toLowerCase();
            const cellB = b[Object.keys(b)[columnIndex]].toLowerCase();
            return sortingDirection === 'asc' ? cellA.localeCompare(cellB) : cellB.localeCompare(cellA);
        }
    });
    return sortedData;
}

// Convert download string to a numeric value for sorting
function parseDownloadValue(value, sortingDirection) {
    console
    if (value === "no_data") return sortingDirection === 'asc' ? 9999999999999999999999 : -1; // Assign a low value for "AppNotFound" to push it to the end
    if (value === "X") return sortingDirection === 'asc' ? 9999999999999999999999 : -1; // Assign a low value for "AppNotFound" to push it to the end
    if (value.endsWith("+")) value = value.slice(0, -1); // Remove the "+" at the end
    if (value.endsWith("K")) return parseFloat(value) * 1000; // Convert "k" to 1000
    if (value.endsWith("M")) return parseFloat(value) * 1000000; // Convert "M" to 1,000,000
    if (value.endsWith("B")) return parseFloat(value) * 1000000000; // Convert "B" to 1,000,000,000
    return parseFloat(value); // Return the numeric value for simple numbers like "100"
}

const toggleCategoryModeBtn = document.getElementById('Category_Match');
toggleCategoryModeBtn.addEventListener('click', () => {
    let CategoryMode = toggleCategoryModeBtn.classList.contains('active-toggle');
    if (!CategoryMode) {
        toggleCategoryModeBtn.innerText = "Match One Category";
        toggleCategoryModeBtn.classList.add("active-toggle");
        CategoryMode = true;
        filterCategory();
    } else {
        // ?? Revert to full data
        toggleCategoryModeBtn.innerText = "Match All Categories";
        toggleCategoryModeBtn.classList.remove("active-toggle");
        CategoryMode = false;
        filterCategory();
    }
});

function filterCategory() {
    showClearCategory();
    let filteredData;
    let CategoryMode = toggleCategoryModeBtn.classList.contains('active-toggle');
    // Get all the activated categories
    const activatedCategories = Array.from(
        document.querySelectorAll('#category-button[activated]')
    ).map(button => button.textContent);

    if (CategoryMode) {
        // Show apps that have one of the activated categories in playStoreCategories
        filteredData = appEntriesDataMatched.filter(entry =>
            entry.playStoreCategories.some(category =>
                activatedCategories.includes(category)
            )
        );
    } else {
        // Show apps that have all of the activated categories in playStoreCategories
        filteredData = appEntriesDataMatched.filter(entry =>
            activatedCategories.every(category =>
                entry.playStoreCategories.includes(category)
            )
        );
    }
    appEntriesDataFiltered = filteredData; // Update the global filtered data

    // If no results are found, show a notification
    if (filteredData.length === 0) {
        const notification = document.getElementById('search-notification');
        notification.innerText = `No results found.`;
        notification.style.display = 'block';

        // Hide the notification after a few seconds
        setTimeout(() => {
            notification.style.display = 'none';
        }, 5000);

        // Update table with empty data
        updateTable([]);
    } else {
        document.getElementById('search-notification').style.display = 'none';

        // Sort the filtered data (replace sortData with your sorting logic)
        const filteredAndSortedData = sortData(
            sortingDirection,
            sortingColumnIndex,
            [...filteredData]
        );

        // Update the table with the sorted data
        updateTable(filteredAndSortedData);
        filterAppEntries(); // Apply search filter on top of category filter
    }
}

// Initial table rendering
function initializeTable() {
    renderTable(appEntriesData);
}

// Helper function to get cell value by column index
function getCellValue(row, columnIndex) {
    const key = Object.keys(row)[columnIndex];
    if (key === 'lastRequestedTime') {
        // Parse date strings to Date objects for sorting
        const dateString = row[key].split(' ')[0]; // Extract date part from the string
        const [day, month, year] = dateString.split('/').map(Number); // Split the date string and convert parts to numbers
        const timeString = row[key].split(' ')[1].trim(); // Extract time part from the string
        const [hour, minute, second] = timeString.split(':').map(Number); // Split the time string and convert parts to numbers
        return new Date(year, month - 1, day, hour, minute, second); // Return a Date object with year, month, day, hour, minute, second
    }
    return isNaN(row[key]) ? row[key] : parseFloat(row[key]);
}

// Runs when "I'm feelin' lucky" button is clicked on
function randomIcons() {
    const randomResetButton = document.getElementById(`random-reset-button`);
    const randomNumberInput = document.getElementById(`random-number-input`); // Number of requests to select randomly
    const totalRequests = appEntriesData.length; // Total numbers of requests

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
    shuffle(numArr); // Shuffle the entire array
    let slicedRandomNumArr = numArr.slice(0, randomCnt); // Choose the first N as the random indices
    let randomizedEntriesData = [];
    for (let i = 0; i < slicedRandomNumArr.length; i++) {
        randomizedEntriesData.push(appEntriesData[slicedRandomNumArr[i]]);
    }

    updateTable(randomizedEntriesData);
    randomResetButton.style.display = "inline-block";
}

function showInfo() {
    var popup = document.getElementById("infotext");
    popup.classList.toggle("show");
}

function shuffle(array) {
    let currentIndex = array.length;

    // While there remain elements to shuffle...
    while (currentIndex != 0) {

        // Pick a remaining element...
        let randomIndex = Math.floor(Math.random() * currentIndex);
        currentIndex--;

        // And swap it with the current element.
        [array[currentIndex], array[randomIndex]] = [
            array[randomIndex], array[currentIndex]];
    }
}

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
    if (isShowingMatches) {
        revertMatchingDrawables();
    }

    let MultipleMode = showMultipeBtn.classList.contains('active-toggle');
    if (MultipleMode) {
        showMultipeBtn.innerText = "Show Matching Name";
        showMultipeBtn.classList.remove("active-toggle");
        appEntriesDataMatched = appEntriesData;
        sortTable(5, 'desc')
    } else {
        showMultipeBtn.classList.add("active-toggle");
        showMultipeBtn.innerText = "Show All";
        const threshold = 2;
        const filteredEntries = filterEntriesByAppNameFrequency(appEntriesData, threshold);
        appEntriesDataMatched = filteredEntries;
        sortTable(0, 'asc')
    }
    filterCategory();
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

function LoadColorData() {
    const xmlFilePath = 'assets/image_color_counts.xml';

    // Load the color data from the XML file
    loadColorsFromXML(xmlFilePath, function (colorData) {
        if (!colorData) {
            console.error('No color data available.');
            return;
        }
        // Now that we have the color data, we can process the app entries

        // Assuming appEntriesDataGlobal is an array of objects with `appIconPath` and `appIconColor` properties
        const promises = appEntriesDataGlobal.map(entry => {
            // Extract the app icon filename from the path (assuming appIconPath is the full path or URL)
            const appIconName = entry.appIconPath.split('/').pop();  // Get the filename from the path

            // Look up the color in the loaded color data
            const newColor = colorData[appIconName];

            // If a color exists in the XML data, assign it
            if (newColor) {
                entry.appIconColor = newColor;
            } else {
                // Default or error handling if color is not found in XML
                entry.appIconColor = 0; // Or some default value
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
            copyToClipboard(null,false);
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