import { state } from '../../../js/state/store.js';
import { DOM } from '../../../js/const.js';

/* ---------- Category UI ---------- */

let onCategoryChange = null; // Callback for recomputeView

export function initCategoryUI(recomputeCallback) {
    onCategoryChange = recomputeCallback;

    // ONE listener for all buttons (past, present, and future)
    DOM.categoriesDiv.addEventListener('click', (event) => {
        const button = event.target.closest('.btn');
        if (!button) return;

        const category = button.textContent;
        toggleCategory(button, category);
    });

    renderCategories();
    DOM.clearSearchCategoryBtn.addEventListener('click', clearSearchCategory);
    DOM.clearCategoryBtn.addEventListener('click', clearCategorySelection);
}

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
let categoryButtons = []; // Cache references here

export function renderCategories() {
    const fragment = document.createDocumentFragment();
    categoryButtons = []; // Reset cache

    state.allCategories.forEach(category => {
        const button = document.createElement('button');
        button.textContent = category;
        button.dataset.name = category.toLowerCase();
        button.className = 'btn';
        if (state.ui.categories.has(category)) {
            button.setAttribute('activated', 'true');
        }
        fragment.appendChild(button);
        categoryButtons.push(button); // Store reference
    });

    DOM.categoriesDiv.innerHTML = '';
    DOM.categoriesDiv.appendChild(fragment);
}

export function findCategory() {
    showClearSearchCategory();
    const search = DOM.searchInputCategory.value.toLowerCase();
    categoryButtons.forEach(button => {
        const matches = button.dataset.name.includes(search);
        button.classList.toggle('hidden', !matches);
    });
}

// Show/hide clear icon
export function showClearSearchCategory() {
    DOM.clearSearchCategoryBtn.style.visibility =
        DOM.searchInputCategory.value.trim() === '' ? 'hidden' : 'visible';
}

// Clear category search input
export function clearSearchCategory() {
    DOM.searchInputCategory.value = '';
    showClearSearchCategory();
    findCategory();
}

// Clear all selected categories
export function clearCategorySelection() {
    categoryButtons.forEach(btn => btn.removeAttribute('activated'));
    state.ui.categories.clear();
    if (onCategoryChange) onCategoryChange();
}