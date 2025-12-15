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
    { key: 'appIconColor',         type: 'number'   },
    { key: 'playStoreDownloads',   type: 'downloads' },
    { key: 'requestedInfo',        type: 'number'   },
    { key: 'lastRequestedTime',    type: 'date'   }
];

export const DOM = {
    caseInsensitive: document.getElementById('caseInsensitive-switch'),
    caseUnicode: document.getElementById('caseUnicode-switch'),
    categoryModeBtn: document.getElementById("Category_Match"),
    clearCategoryBtn: document.getElementById('clear-category'),
    clearSearchBtn: document.getElementById("clear-search"),
    closePopupBtn: document.getElementById("closePopup"),
    copySelectedBtn: document.getElementById("copy-selected-button"),
    dateHeader: document.getElementById("date_header"),
    header: document.getElementById("header"),
    infoText: document.getElementById("infotext"),
    matchingDrawableColumn: document.getElementById('show-matching-drawables'),
    matchingDrawablesBtn: document.getElementById('show-matching-drawables-btn'),
    matchingNameBtn: document.getElementById('show-multiple'),
    matchingNumberInput: document.getElementById(`matching-number-input`),
    randomButton: document.getElementById("random-button"),
    randomNumberInput: document.getElementById("random-number-input"),
    randomResetButton: document.getElementById(`random-reset-button`),
    regexSwitch: document.getElementById('regex-switch'),
    renameOverlay: document.getElementById("renamer-overlay"),
    requestsTable: document.getElementById("requests-table"),
    requestsTableContainer: document.getElementById("requests-table-container"),
    reverseSwitch: document.getElementById('reverse-switch'),
    searchInput: document.getElementById('search-input'),
    searchNotification: document.getElementById("search-notification"),
    smallheader: document.getElementById("smallheader"),
    updatableButton: document.getElementById("updatable-button"),
    regexSearchSettingsBtn: document.getElementById("RegexSearchSettings"),
    closeRegexSettingsBtn: document.getElementById("closePopup"),
    regexPopup: document.getElementById("myPopup"),
    sortableHeaders: document.querySelectorAll('#requests-table thead th.sortable-header'),
    requeststhead: document.getElementById("requests-table-head")

};
