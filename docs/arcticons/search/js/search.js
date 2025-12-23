// --- State & Config ---
let allIconNames = [];
let visibleCount = 100;
let currentMatches = [];
const ICON_BASE_URL = 'https://raw.githubusercontent.com/Arcticons-Team/Arcticons/main/icons/white/';

// 1. Initialize DOM as an empty object or let it be defined inside init
let DOM = {};

/**
 * 2. Data & UI Initialization
 */
async function init() {
  // Select elements now that the DOM is fully loaded
  DOM = {
    search: document.getElementById('search'),
    results: document.getElementById('results')
  };

  // Attach listener here, once DOM.search is guaranteed to exist
  const debouncedSearch = debounce(handleSearch, 300);
  DOM.search.addEventListener('input', debouncedSearch);

  try {
    const response = await fetch('https://raw.githubusercontent.com/Arcticons-Team/Arcticons/main/app/src/main/assets/drawable.xml');
    const xmlText = await response.text();
    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(xmlText, 'application/xml');

    const items = Array.from(xmlDoc.querySelectorAll('item'));
    allIconNames = [...new Set(items.map(i => i.getAttribute('drawable')))].sort();

    currentMatches = shuffleArray(allIconNames)
    renderGrid();
    checkDeepLink();
  } catch (err) {
    console.error("Initialization failed:", err);
  }
}

/**
 * 3. Search Logic
 */
function handleSearch() {
  if (DOM.search.value != ""){
  const query = DOM.search.value.toLowerCase().replace(/ /g, '_');

  currentMatches = allIconNames.filter(name => {
    return name.includes(query) || name.replace(/_/g, ' ').includes(query);
  });
}else{
  currentMatches = shuffleArray(allIconNames)
}

  visibleCount = 100;
  renderGrid();
}

/**
 * 4. Rendering logic
 */
function renderGrid() {
  DOM.results.innerHTML = '';

  // Only render the current "slice" for performance
  const slice = currentMatches.slice(0, visibleCount);
  const fragment = document.createDocumentFragment();

  slice.forEach(name => {
    const img = document.createElement('img');
    img.src = `${ICON_BASE_URL}${name}.svg`;
    img.alt = name;
    img.title = name;
    img.loading = "lazy";
    img.onclick = openPopup;
    fragment.appendChild(img);
  });

  DOM.results.appendChild(fragment);

  if (visibleCount < currentMatches.length) {
    renderLoadMore();
  }
}

function shuffleArray(arr) {
    for (let i = arr.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [arr[i], arr[j]] = [arr[j], arr[i]]; // Swap elements
    }
    return arr;
}

function renderLoadMore() {
  const btn = document.createElement('button');
  btn.innerText = `Load More (${currentMatches.length - visibleCount} remaining)`;
  btn.className = 'load-more-btn';
  btn.onclick = () => {
    visibleCount += 200;
    renderGrid();
  };
  DOM.results.appendChild(btn);
}

/**
 * 5. Deep Linking Logic
 */
function checkDeepLink() {
  const params = new URLSearchParams(window.location.search);
  const iconName = params.get('icon');
  if (iconName && allIconNames.includes(iconName)) {
    // Create a dummy object to satisfy openPopup's "this" or event expectation
    const dummy = { title: iconName, src: `${ICON_BASE_URL}${iconName}.svg`, alt: iconName };
    openPopup.call(dummy);
  }
}

// --- Utils ---
const debounce = (func, delay) => {
  let timeoutId;
  return (...args) => {
    if (timeoutId) clearTimeout(timeoutId);
    timeoutId = setTimeout(() => func.apply(null, args), delay);
  };
};

// Ensure this matches the function name in your clipboard utility
function copyToClipboard(text) {
  navigator.clipboard.writeText(text).then(() => {
    const note = document.createElement('div');
    note.className = 'copy-notification';
    note.textContent = 'Copied to clipboard!';
    document.body.appendChild(note);
    setTimeout(() => note.remove(), 2000);
  });
}

function openPopup(iconData) {
  closePopup();
  // Support both event and direct call
  let icon = iconData && iconData.target ? iconData.target : this;
  // Update URL with icon name
  if (icon.title) {
    history.replaceState(null, '', '?icon=' + encodeURIComponent(icon.title));
  }
  let fig = document.createElement('figure');
  fig.className = 'popup-figure';
  let img = document.createElement('img');
  img.src = icon.src;
  img.alt = icon.alt;
  // Create title with formatted icon name
  let titleLabel = document.createElement('h2');
  titleLabel.className = 'icon-title';
  // 1. Remove leading underscores
  let cleanTitle = icon.title.replace(/^_+/, '');
  // 2. Replace remaining underscores with spaces
  cleanTitle = cleanTitle.replace(/_/g, ' ');
  // 3. Capitalize the first letter of every word
  let formattedTitle = cleanTitle.replace(/(^\w|\s\w)/g, m => m.toUpperCase());
  titleLabel.textContent = formattedTitle;
  let nameLabel = document.createElement('div');
  nameLabel.className = 'icon-name';
  nameLabel.textContent = icon.title;
  // Create close button
  let closeBtn = document.createElement('button');
  closeBtn.className = 'popup-close-btn';
  closeBtn.innerHTML = '&times;';
  closeBtn.title = 'Close';
  closeBtn.onclick = function (e) {
    e.stopPropagation();
    closePopup();
  };
  // Escape key handler
  function escHandler(e) {
    if (e.key === "Escape") {
      closePopup();
    }
  }
  document.addEventListener('keydown', escHandler);
  // Store handler for removal
  fig._escHandler = escHandler;
  fig.addEventListener('click', (e) => {
    if (e.target === fig) {
      closePopup();
    } else if (e.target !== closeBtn) {
      copyToClipboard(icon.title);
    }
  });
  fig.appendChild(closeBtn);
  fig.appendChild(img);
  fig.appendChild(titleLabel);
  fig.appendChild(nameLabel);
  document.body.appendChild(fig);
}

function closePopup() {
  let fig = document.getElementsByTagName('figure')[0];
  if (fig && fig._escHandler) {
    document.removeEventListener('keydown', fig._escHandler);
  }
  if (fig && fig.parentNode) {
    fig.parentNode.removeChild(fig);
  }
  // Restore URL to default (remove ?icon)
  if (window.location.search.includes('icon=')) {
    history.replaceState(null, '', window.location.pathname);
  }
}

// Global start
document.addEventListener('DOMContentLoaded', init);