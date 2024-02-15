// Array of Link Images
const imageNames = ['img/requests/google-play-store.svg', 'img/requests/f-droid.svg', 'img/requests/izzyondroid.svg', 'img/requests/galaxystore.svg', 'img/requests/search-globe.svg'];
var appEntriesDataGlobal = []; // Store the original data for sorting
// Lazy loading and virtualization
const batchSize = 50; // Number of rows to load at a time
let startIndex = 0; // Start index for lazy loading

let appEntriesData = []; // Store the original data for sorting

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
fetch('https://raw.githubusercontent.com/Arcticons-Team/Arcticons/main/generated/requests.txt')
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return response.text();
    })
    .then(fileContent => {
        const appEntries = fileContent.split(/(?=<!--[^]*?-->)/).filter(entry => entry.trim() !== '');
        // Call this function to change the header text
        updateHeaderText(appEntries[0].trim().split('\n')[1].trim());

        // Process each entry and store data
        appEntries.slice(1).forEach(entry => {
            const lines = entry.trim().split('\n');
            const appName = lines[0].trim().split('--')[1].trim();
            const appNameAppfilter = lines[0].trim();
            const appfilter = lines[1].trim().split('\n').join(' ').trim();
            const appLinks = lines.slice(2, lines.length - 2).map((line, index) => {
                const imageName = index < imageNames.length ? imageNames[index] : 'img/requests/default.svg';
                return `<a href="${line.trim()}" target="_blank"><img src="${imageName}" alt="Image" style="width:50px;height:50px;"></a>`;
            }).join('\n');
            const requestedTimestamp = parseInt(lines.slice(lines.length - 2)[1].trim().split(' ')[2]);
            const requestedInfo = lines.slice(lines.length - 2)[0].trim().split(' ')[1].trim();
            const lastRequestedTime = new Date(requestedTimestamp * 1000).toLocaleString();

            appEntriesData.push({
                appName,
                appLinks,
                requestedInfo,
                lastRequestedTime,
                appNameAppfilter,
                appfilter
            });
        });
        appEntriesDataGlobal = appEntriesData;

        // Example usage:
        fetch('https://raw.githubusercontent.com/Arcticons-Team/Arcticons/icon-requests/docs/assets/combined_appfilter.xml')
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
                // Initial render
                lazyLoadAndRender();
            })
            .catch(error => console.error('Error fetching or processing appfilter:', error));
    })
    .catch(error => console.error('Error fetching file:', error));



    // Filter appEntriesData based on appfilter content
    function filterAppfilter(appEntriesData, appfilterContent) {
        const appfilterItems = parseAppfilter(appfilterContent);
        const filteredOutEntries = [];
    
        const filteredData = appEntriesData.filter(entry => {
            const entryAppfilter = entry.appfilter.trim().split('"')[1].trim();
            // Check if the entry is filtered out
            const isFiltered = appfilterItems.some(component => component === entryAppfilter);  
            if (isFiltered) {
                filteredOutEntries.push(entryAppfilter);
            } 
            return !isFiltered;
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
        cell2.innerHTML = entry.appLinks;
        cell3.innerHTML = entry.requestedInfo;
        cell4.innerHTML = entry.lastRequestedTime;
        cell5.innerHTML = `<button class="copy-button" onclick="copyToClipboard(${index})">Copy</button>`;
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
function copyToClipboard(index) {
    const entry = appEntriesDataGlobal[index];
    const copyText = `${entry.appNameAppfilter}\n${entry.appfilter}`;
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

// Search function
const filterAppEntries = debounce(() => {
    const searchInput = document.getElementById('search-input').value.toLowerCase();
    const filteredData = appEntriesData.filter(entry =>
        entry.appName.toLowerCase().includes(searchInput)
    );
    console.log(filteredData)
    const appEntriesContainer = document.getElementById('app-entries-container');

    if (filteredData.length === 0) {
        document.getElementById('search-notification').innerText = `No results found.`;
        document.getElementById('search-notification').style.display = 'block';
        // Hide the notification after a few seconds
        setTimeout(() => {
            document.getElementById('search-notification').style.display = 'none';
        }, 5000);
    } else {
        document.getElementById('search-notification').style.display = 'none';
        updateTable(filteredData);
    }
}, 1000);

// Sort table function
function sortTable(columnIndex) {
    const table = document.querySelector('table');
    const headers = table.querySelectorAll('thead th');

    // Determine the sorting direction
    const sortingDirection = headers[columnIndex].classList.contains('asc') ? 'desc' : 'asc';

    // Remove sorting indicators from all headers
    headers.forEach(header => {
        header.classList.remove('asc', 'desc');
    });

    // Add the appropriate sorting class to the clicked header
    headers[columnIndex].classList.add(sortingDirection);

    const sortedData = [...appEntriesData];
    sortedData.sort((a, b) => {
        if (columnIndex === 3) { // Check if sorting the 'Last Requested' column
            const cellA = getCellValue(a, columnIndex);
            const cellB = getCellValue(b, columnIndex);

            // Handle dates
            return sortingDirection === 'asc' ? cellA - cellB : cellB - cellA;
        } else if (columnIndex === 2) {
            const cellA = getCellValue(a, columnIndex);
            const cellB = getCellValue(b, columnIndex);

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
    updateTable(sortedData);
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
        const dateString = row[key].split(',')[0]; // Extract date part from the string
        const [day, month, year] = dateString.split('/').map(Number); // Split the date string and convert parts to numbers
        const timeString = row[key].split(',')[1].trim(); // Extract time part from the string
        const [hour, minute, second] = timeString.split(':').map(Number); // Split the time string and convert parts to numbers
        return new Date(year, month - 1, day, hour, minute, second); // Return a Date object with year, month, day, hour, minute, second
    }
    return isNaN(row[key]) ? row[key] : parseFloat(row[key]);
}