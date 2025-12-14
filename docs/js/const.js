// Array of Link Images
export const imagepath = {
    playStore: 'img/requests/google-play-store.svg',
    fdroid: 'img/requests/f-droid.svg',
    izzyOnDroid: 'img/requests/izzyondroid.svg', 
    galaxyStore: 'img/requests/galaxystore.svg', 
    wwwSearch: 'img/requests/search-globe.svg'
};

export const TABLE_COLUMNS_Requests = [
    { key: 'appName',              type: 'string' },
    { key: 'appIconColor',         type: 'number'   }, // not sortable
    { key: 'Arcticon',             type: 'none'   }, // not sortable
    { key: 'appLinks',             type: 'none'   },
    { key: 'playStoreDownloads',   type: 'downloads' },
    { key: 'requestedInfo',        type: 'number'   },
    { key: 'lastRequestedTime',    type: 'date'   }
];

export const DOM = {
    searchInput: document.getElementById('search-input'),
    regexSwitch: document.getElementById('regex-switch'),
    reverseSwitch: document.getElementById('reverse-switch'),
    caseInsensitive: document.getElementById('caseInsensitive-switch'),
    caseUnicode: document.getElementById('caseUnicode-switch'),
    clearCategoryBtn: document.getElementById('clear-category'),
    matchingNumberInput: document.getElementById(`matching-number-input`),
    matchingNameBtn: document.getElementById('show-multiple')
};
