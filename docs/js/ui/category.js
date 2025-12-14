import { state } from '../state/store.js';

/* ---------- Category UI ---------- */

// Cache DOM elements
const categoriesDiv = document.querySelector('.categories');
const clearCategoryBtn = document.querySelector('#clear-category');
const searchInputCategory = document.getElementById('search-input_category');
const clearSearchCategoryBtn = document.getElementById('clear-search_category');

let onCategoryChange = null; // Callback for recomputeView

// Initialize Category UI with a callback
export function initCategoryUI(recomputeCallback) {
    onCategoryChange = recomputeCallback;
    renderCategories();
}

// Render category buttons
export function renderCategories() {
    categoriesDiv.innerHTML = ''; // Clear existing buttons
    state.allCategories.forEach(category => {
        const button = document.createElement('button');
        button.textContent = category;
        button.id = 'category-button';
        button.className = 'green-button';
        button.onclick = () => toggleCategory(button, category);
        categoriesDiv.appendChild(button);
    });
}

// Toggle category selection
function toggleCategory(button, category) {
    if (state.ui.categories.has(category)) {
        state.ui.categories.delete(category);
        button.removeAttribute('activated');
    } else {
        state.ui.categories.add(category);
        button.setAttribute('activated', 'true');
    }
    if (onCategoryChange) onCategoryChange();
}

// Filter categories by search input
export function findCategory() {
    showClearSearchCategory();
    const search = searchInputCategory.value.toLowerCase();
    document.querySelectorAll('#category-button').forEach(button => {
        button.style.display = button.textContent.toLowerCase().includes(search)
            ? 'inline-block'
            : 'none';
    });
}

// Show/hide clear icon
export function showClearSearchCategory() {
    clearSearchCategoryBtn.style.visibility =
        searchInputCategory.value.trim() === '' ? 'hidden' : 'visible';
}

// Clear category search input
export function clearSearchCategory() {
    searchInputCategory.value = '';
    showClearSearchCategory();
    findCategory();
}

// Clear all selected categories
export function clearCategorySelection() {
    document.querySelectorAll('#category-button').forEach(btn => btn.removeAttribute('activated'));
    state.ui.categories.clear();
    if (onCategoryChange) onCategoryChange();
}

// Event listeners
searchInputCategory.addEventListener('input', findCategory);
clearSearchCategoryBtn.addEventListener('click', clearSearchCategory);
clearCategoryBtn.addEventListener('click', clearCategorySelection);
