// state/store.js
export const state = {
  all: [],
  view: [],
  startIndex: 0,
  batchSize: 30,

  selectedRows: new Set(),
  allCategories: new Set(),
  drawableSet: new Set(),

  ui: {
    showMatchingDrawables: false,
    showMatchingNames: false,
    categories: new Set(),
    categoryMode: 'all',
    search: '',
    regex: false,
    regexFlags: '',
    reverse: false,
    sort: {
      column: 5,
      direction: 'desc'
    }
  }
};
