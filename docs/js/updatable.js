var appEntriesDataGlobal = []; // Store the original data for sorting
// Lazy loading and virtualization
const batchSize = 50; // Number of rows to load at a time
let startIndex = 0; // Start index for lazy loading
let appEntriesData = []; // Store the original data for sorting
// Global variables to track sorting column and direction
let sortingColumnIndex = 3;
let sortingDirection = 'desc';

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

// Fetch and process data
fetch(`assets/updatable.txt`)
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return response.text();
    })
    .then(fileContent => {
        const appEntries = fileContent.split(/(?=<!--[^]*?-->)/).filter(entry => entry.trim() !== '');

        // Process each entry and store data
        appEntries.slice(0).forEach(entry => {
            const lines = entry.trim().split('\n');
            const appName = lines[0].trim().split('--')[1].trim();
            const appNameAppfilter = lines[0].trim();
            const appfilter = lines[1].trim().split('\n').join(' ').trim();
            const packageName = appfilter.split('ComponentInfo{')[1].split('/')[0].trim();
            const drawable = extractDrawable(appfilter);
            const appIconPath = drawable ? `extracted_png/${drawable}.webp` : 'img/requests/default.svg'; // Adjust path accordingly
            const appIcon = `<img src="${appIconPath}" alt="App Icon" style="width:50px;height:50px;">`;

            appEntriesData.push({
                appName,
                appIcon,
                packageName,
                appNameAppfilter,
                appfilter,
                appIconPath
            });
        });
        appEntriesDataGlobal = appEntriesData;
        updateHeaderText(`${appEntriesData.length} Possible Appfilter Updates`);

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
                const filteredData = filterAppfilter(appEntriesData, appfilterContent);
                appEntriesData = filteredData;
                appEntriesDataGlobal = filteredData;
                updateHeaderText(`${appEntriesData.length} Possible Appfilter Updates`);
                const table = document.querySelector('table');
                const headers = table.querySelectorAll('thead th');
                // headers[sortingColumnIndex].classList.add(sortingDirection);
                // Initial render
                lazyLoadAndRender();
            })
            .catch(error => console.error('Error fetching or processing appfilter:', error));
    })
    .catch(error => console.error('Error fetching file:', error));

// Function to extract the drawable attribute from appfilter
function extractDrawable(appfilter) {
    const regex = /drawable="([^"]+)"/;
    const match = appfilter.match(regex);
    if (match && match.length > 1) {
        return match[1]; // Return the value inside the quotes
    }
    return null; // Return null if no match found
}

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

// Function to render the table based on provided data
function renderTable(data) {
    const table = document.getElementById("app-entries");
    data.forEach((entry, index) => {
        let row = table.insertRow();
        let cell1 = row.insertCell(0);
        let cell2 = row.insertCell(1);
        let cell3 = row.insertCell(2);
        let cell4 = row.insertCell(3);
        let cell5 = row.insertCell(4);
        index = index + startIndex;
        cell1.innerHTML = entry.appName;
        cell2.innerHTML = `<a href="#" class="icon-preview" data-index="${index}">${entry.appIcon}</a>`;
        cell3.innerHTML = `<div class="package-name"><div id="packagename">` + entry.packageName + `</div><div id="package-copy"><button class="copy-package" onclick="copyToClipboard(${index}, 'package')"><img src="img/requests/copy.svg"></button></div></div>`;
        cell4.innerHTML = entry.appfilter.replace('<', '&lt;').replace('>', '&gt;').replace(/"/g, '&quot;').trim();
        cell5.innerHTML = `<button class="copy-button" onclick="copyToClipboard(${index}, 'appfilter')">Copy</button>`;
    });
        // Add event listeners to the icon previews
        const iconPreviews = document.querySelectorAll('.icon-preview');
        iconPreviews.forEach(icon => {
            icon.addEventListener('click', function(event) {
                event.preventDefault();
                const index = parseInt(this.getAttribute('data-index'));
                const entry = appEntriesDataGlobal[index];
                showIconPreview(entry.appIconPath);
            });
        });
}


function showIconPreview(iconSrc) {
    const previewOverlay = document.getElementById('preview-overlay');
    const previewImage = document.getElementById('preview-image');

    // Set the preview image source to the clicked icon source
    previewImage.src = iconSrc;

    // Show the preview overlay
    previewOverlay.style.display = 'block';
    // Add click event listener to hide the preview when clicked on the overlay or close button
previewOverlay.addEventListener('click', function(e) {
    if (e.target === this || e.target.classList.contains('close-button')) {
        // Hide the preview overlay
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
function copyToClipboard(index, event) {
    const entry = appEntriesDataGlobal[index];
    let copyText = "";

    if (event == "package") {
        copyText = `${entry.packageName}`;
    } else if (event == "appfilter") {
        copyText = `${entry.appfilter}`;
    }

    navigator.clipboard.writeText(copyText).then(() => {
        // Show the copy notification
        document.getElementById('copy-notification').innerText = `Copied: ${copyText}`;
        document.getElementById('copy-notification').style.display = 'block';

        // Hide the notification after a few seconds
        setTimeout(() => {
            document.getElementById('copy-notification').style.display = 'none';
        }, 3000);
    }).catch(error => {
        console.error('Unable to copy to clipboard:', error);
    });
}

// Accessing the button element by its id
const updatableButton = document.getElementById("updatable-button");

// Add an event listener to the button
updatableButton.addEventListener("click", function () {
    // Define the URL to redirect to
    const updatableURL = `requests.html`;
    // Redirect to the specified URL
    window.location.href = updatableURL;
});

// Search function
const filterAppEntries = debounce(() => {
    const searchInput = document.getElementById('search-input').value.toLowerCase();
    const filteredData = appEntriesData.filter(entry =>
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
    } else {
        document.getElementById('search-notification').style.display = 'none';
        const filteredandsortedData = sortData(sortingDirection, sortingColumnIndex, [...filteredData])
        updateTable(filteredandsortedData);
    }
}, 500);

// Sort table function
function sortTable(columnIndex) {
    const table = document.querySelector('table');
    const headers = table.querySelectorAll('thead th');

    // Determine the sorting direction
    sortingDirection = headers[columnIndex].classList.contains('asc') ? 'desc' : 'asc';

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
        // Default to string comparison
        const cellA = a[Object.keys(a)[columnIndex]].toLowerCase();
        const cellB = b[Object.keys(b)[columnIndex]].toLowerCase();
        return sortingDirection === 'asc' ? cellA.localeCompare(cellB) : cellB.localeCompare(cellA);
    });
    return sortedData;
}

// Initial table rendering
function initializeTable() {
    renderTable(appEntriesData);
}
