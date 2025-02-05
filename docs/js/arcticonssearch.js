let lazyImageObserver = new IntersectionObserver(function (entries, observer) {
  entries.forEach(function (entry) {
    if (entry.isIntersecting) {
      let lazyImage = entry.target;
      lazyImage.addEventListener('error', function () { this.src = this.src.replace('icons/black', 'todo').replace('icons/white', 'todo'); });
      lazyImage.src = lazyImage.dataset.src;
      lazyImage.className = '';
      lazyImageObserver.unobserve(lazyImage);
    }
  });
});

function openPopup() {
  let fig = document.createElement('figure');
  let title = document.createElement('figcaption');
  title.appendChild(document.createTextNode(this.title));
  let img = document.createElement('img');
  img.src = this.src;
  img.alt = this.alt;
  fig.addEventListener('click', closePopup);
  fig.appendChild(title);
  fig.appendChild(img);
  document.body.appendChild(fig);
}

function closePopup() {
  let fig = document.getElementsByTagName('figure')[0];
  fig.parentNode.removeChild(fig);
}

function search() {
  document.getElementById('results').setAttribute('aria-disabled', 'false');

  let searchTerm = this.value.toLowerCase();
  let allImages = Array.from(document.querySelectorAll('.tab img')); // Convert NodeList to Array
  let uniqueTitles = new Set(); // Store unique titles
  let validImages = new Set(); // Store unique images

  allImages.forEach(img => {
    let title = img.title.toLowerCase();
    if ((!searchTerm || title.includes(searchTerm)) && !uniqueTitles.has(title)) {
      uniqueTitles.add(title); // Add title to Set to ensure uniqueness
      validImages.add(img); // Add corresponding image
    }
  });

  // Update visibility of images
  allImages.forEach(img => {
    img.style.display = validImages.has(img) ? 'inline-block' : 'none';
  });
}




function sortIcons(a, b) {
  let nameA = a.getAttribute('drawable');
  let nameB = b.getAttribute('drawable');
  if (nameA < nameB) { return -1; }
  if (nameA > nameB) { return 1; }
  return 0;
}

function genImageGrid() {
  let parse = new DOMParser();
  let xmldoc = parse.parseFromString(this.responseText, 'application/xml');
  // Generate carrousel
  let docs = Array.prototype.slice.call(xmldoc.querySelectorAll('item'));
  let latest = docs.slice(-8);
  let carrousel = document.createElement('section');
  carrousel.id = 'carrousel';
  carrousel.innerHTML = '<h2>Latest icons</h2><div class="latest content"></div>';
  for (let i of latest) {
    let im = document.createElement('img');
    im.src = 'https://raw.githubusercontent.com/Donnnno/Arcticons/main/icons/black/' + i.attributes.drawable.value + '.svg';
    im.addEventListener('error', function () { this.src = this.src.replace('icons/black', 'todo'); });
    im.alt = i.attributes.drawable.value;
    im.title = i.attributes.drawable.value;
    carrousel.children[1].appendChild(im);
  }

  for (let i of docs.sort(sortIcons)) {
    let im = document.createElement('img');
    im.className = 'lazy';
    lazyImageObserver.observe(im);
    //im.src = 'c.svg';
    im.dataset.src = 'https://raw.githubusercontent.com/Donnnno/Arcticons/main/icons/black/' + i.attributes.drawable.value + '.svg';
    im.alt = i.attributes.drawable.value;
    im.title = i.attributes.drawable.value;
    im.addEventListener('click', openPopup);
    document.getElementsByClassName('tab')[0].appendChild(im);
  }
}
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
  document.getElementsByClassName('tab')[1].style.display = 'none';
  document.getElementById('search').oninput = search;
  let a = new XMLHttpRequest();
  a.open('GET', 'https://raw.githubusercontent.com/Donnnno/Arcticons/main/app/src/main/assets/drawable.xml');
  a.onload = function () {
    const count = countDrawableEntries(a.responseText);
    console.log("Number of drawable entries:", count);
    updateIconCount(count); // Update the count in the HTML content
    genImageGrid.call(a);
  };
  a.send();
});