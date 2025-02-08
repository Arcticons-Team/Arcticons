function countDrawableEntries(xmlText) {
    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(xmlText, 'application/xml');
    const items = xmlDoc.querySelectorAll('item');
    // Create a Set to store unique entries
    const uniqueEntries = new Set();
    // Iterate through the items and add their unique value to the Set
    items.forEach((item) => {
      const uniqueValue = item.getAttribute('drawable'); 
      uniqueEntries.add(uniqueValue);
    });
    return uniqueEntries.size;
  }
  
  // Function to round down the count to the nearest multiple of 100
  function roundDownToNearest100(count) {
    return Math.floor(count / 100) * 100;
  }
  
  // Function to update the number in the HTML content without grouping separators
  function updateIconCount(count) {
    const roundedCount = roundDownToNearest100(count);
    const iconCountElement = document.querySelector('.grid-content-3 p b');
    if (iconCountElement) {
      iconCountElement.textContent = roundedCount.toLocaleString(undefined, { useGrouping: false });
    }
  }
  
  
  document.addEventListener("DOMContentLoaded", function () {
    let a = new XMLHttpRequest();
    a.open('GET', 'https://raw.githubusercontent.com/Donnnno/Arcticons/main/app/src/main/assets/drawable.xml');
    a.onload = function () {
      const count = countDrawableEntries(a.responseText);
      console.log("Number of drawable entries:", count);
      updateIconCount(count); // Update the count in the HTML content
    };
    a.send();
  });