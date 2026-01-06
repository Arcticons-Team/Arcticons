// Array of Link Images
export const imagepath = {
    playStore: '/dashboard/img/icon-play.svg',
    fdroid: '/dashboard/img/icon-fdroid.svg',
    izzyOnDroid: '/dashboard/img/icon-izzy.svg',
    galaxyStore: '/dashboard/img/icon-galaxy.svg',
    wwwSearch: '/dashboard/img/icon-web.svg',
    download: '/dashboard/img/icon-download.svg',
    copy: '/dashboard/img/icon-copy.svg',
    more: '/dashboard/img/icon-more.svg'
};

export const urls = {
    playStore: 'https://play.google.com/store/apps/details?id=',
    fdroid: `https://f-droid.org/en/packages/`,
    izzyOnDroid: 'https://apt.izzysoft.de/fdroid/index/apk/',
    galaxyStore: 'https://galaxystore.samsung.com/detail/',
    wwwSearch: 'https://www.ecosia.org/search?q=',
}

export const TABLE_COLUMNS_Requests = [
    { key: 'appIconColor', type: 'number' },
    { key: 'appName', type: 'string' },
    { key: 'playStoreDownloads', type: 'downloads' },
    { key: 'requestedInfo', type: 'number' },
    { key: 'lastRequestedTime', type: 'number' }
];

export const TABLE_COLUMNS_Updates = [
    { key: 'appName', type: 'string' },
    { key: 'pkgName', type: 'string' },
    { key: 'componentInfo', type: 'string' }
];

export const DOM = {
    caseInsensitive: document.getElementById('caseInsensitive-switch'),
    caseUnicode: document.getElementById('caseUnicode-switch'),
    categoryModeBtn: document.getElementById("Category_Match"),
    clearCategoryBtn: document.getElementById('clear-category'),
    clearSearchBtn: document.getElementById("clear-search"),
    closePopupBtn: document.getElementById("closePopup"),
    closeRegexSettingsBtn: document.getElementById("closePopup"),
    copySelectedBtn: document.getElementById("copy-selected-button"),
    dateHeader: document.getElementById("date_header"),
    header: document.getElementById("header"),
    infoText: document.getElementById("infotext"),
    matchingDrawableColumn: document.getElementById('show-matching-drawables'),
    matchingDrawablesBtn: document.getElementById('btn-match-drawable'),
    matchingDrawablesBtnTxt: document.getElementById('btn-match-drawable-text'),
    matchingNameBtn: document.getElementById('btn-match-name'),
    matchingNameBtnTxt: document.getElementById('btn-match-name-text'),
    matchingNumberInput: document.getElementById(`matching-number-input`),
    randomButton: document.getElementById("random-button"),
    randomNumberInput: document.getElementById("random-number-input"),
    randomResetButton: document.getElementById(`random-reset-button`),
    regexPopup: document.getElementById("SettingsPopup"),
    regexSearchSettingsBtn: document.getElementById("RegexSearchSettings"),
    regexSwitch: document.getElementById('regex-btn'),
    renameOverlay: document.getElementById("renamer-overlay"),
    requestsTable: document.getElementById("requests-table"),
    requestsTableContainer: document.getElementById("requests-table-container"),
    requeststhead: document.getElementById("requests-table-head"),
    reverseSwitch: document.getElementById('reverse-switch'),
    searchInput: document.getElementById('search-input'),
    searchNotification: document.getElementById("search-notification"),
    sortableHeaders: document.querySelectorAll('#requests-table thead th.sortable-header'),
    tableBody: document.getElementById("app-entries"),
    updatableButton: document.getElementById("updatable-button"),
    imagePreviewOverlay: document.getElementById('preview-overlay'),
    imagePreview: document.getElementById('preview-image'),
    renameBtn: document.getElementById("rename-button"),
    sentinel: document.getElementById("table-sentinel"),
    categoriesDiv: document.getElementById('categories'),
    searchInputCategory: document.getElementById('search-input_category'),
    imagePreviewTitle: document.getElementById('preview-image-name'),
    floatingBtnContainer: document.getElementById('floating-action-btn'),
    downloadSelectedBtn: document.getElementById('download-selected-btn'),
    closePreview: document.getElementById('close-preview'),
    keepBtn: document.getElementById('keep-button')
};