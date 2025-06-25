let lazyImageObserver = new IntersectionObserver(function(entries, observer){
  entries.forEach(function(entry){
    if (entry.isIntersecting) {
      let lazyImage = entry.target;
      lazyImage.addEventListener('error', function(){this.src = this.src.replace('icons/black', 'todo').replace('icons/white', 'todo');});
      lazyImage.src = lazyImage.dataset.src;
      lazyImage.className = '';
      lazyImageObserver.unobserve(lazyImage);
    }
  });
});

let searchTimeout;

function search(){
  // Clear any existing timeout
  clearTimeout(searchTimeout);
  
  // Set new timeout for 1 second delay
  searchTimeout = setTimeout(() => {
    document.getElementById('results').setAttribute('aria-disabled', 'false');
    
    // Replace spaces with underscores in search term
    let searchTerm = this.value.toLocaleLowerCase().replace(/ /g, '_');
    let busq = '.tab img';
    let todos = Array.prototype.slice.call(document.querySelectorAll(busq));
    
    for (let i of todos){
      if (this.value){
        // Get icon title and normalize it for comparison
        let iconTitle = i.title.toLocaleLowerCase();
        // Check if search term matches either with underscores or spaces
        let matchesSearch = iconTitle.includes(searchTerm) || 
                           iconTitle.replace(/_/g, ' ').includes(this.value.toLocaleLowerCase());
        
        i.style.display = matchesSearch ? 'inline-block' : 'none';
      } else {
        i.style.display = 'inline-block';
      }
    }
  }, 1000);
}

function copyToClipboard(text) {
  navigator.clipboard.writeText(text).then(() => {
    showCopyNotification();
  });
}

function showCopyNotification() {
  const notification = document.createElement('div');
  notification.className = 'copy-notification';
  notification.textContent = 'Copied to clipboard!';
  document.body.appendChild(notification);
  
  setTimeout(() => {
    notification.remove();
  }, 2000);
}

function openPopup(){
  let fig = document.createElement('figure');
  fig.className = 'popup-figure';

  let img = document.createElement('img');
  img.src = this.src;
  img.alt = this.alt;

  // Create title with formatted icon name
  let titleLabel = document.createElement('h2');
  titleLabel.className = 'icon-title';
  let formattedTitle = this.title.charAt(0).toUpperCase() + 
                       this.title.slice(1).replace(/_/g, ' ');
  titleLabel.textContent = formattedTitle;

  let nameLabel = document.createElement('div');
  nameLabel.className = 'icon-name';
  nameLabel.textContent = this.title;

  // Create close button
  let closeBtn = document.createElement('button');
  closeBtn.className = 'popup-close-btn';
  closeBtn.innerHTML = '&times;';
  closeBtn.title = 'Close';
  closeBtn.onclick = function(e) {
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
      copyToClipboard(this.title);
    }
  });

  fig.appendChild(closeBtn);
  fig.appendChild(img);
  fig.appendChild(titleLabel);
  fig.appendChild(nameLabel);
  document.body.appendChild(fig);
}

function closePopup(){
  let fig = document.getElementsByTagName('figure')[0];
  if (fig && fig._escHandler) {
    document.removeEventListener('keydown', fig._escHandler);
  }
  if (fig && fig.parentNode) {
    fig.parentNode.removeChild(fig);
  }
}

function sortIcons(a, b){
  let nameA = a.getAttribute('drawable');
  let nameB = b.getAttribute('drawable');
  if (nameA < nameB){ return -1; }
  if (nameA > nameB){ return 1; }
  return 0;
}

function genImageGrid(){
  let parse = new DOMParser();
  let xmldoc = parse.parseFromString(this.responseText, 'application/xml');
  let docs = Array.prototype.slice.call(xmldoc.querySelectorAll('item'));

  // Filter out duplicates by drawable name
  let seen = new Set();
  let uniqueDocs = docs.filter(i => {
    let name = i.attributes.drawable.value;
    if (seen.has(name)) return false;
    seen.add(name);
    return true;
  });

  for (let i of uniqueDocs.sort(sortIcons)){
    let im = document.createElement('img');
    im.className = 'lazy';
    lazyImageObserver.observe(im);
    im.dataset.src = 'https://raw.githubusercontent.com/Donnnno/Arcticons/main/icons/white/' + i.attributes.drawable.value + '.svg';
    im.alt = i.attributes.drawable.value;
    im.title = i.attributes.drawable.value;
    im.addEventListener('click', openPopup);
    document.getElementsByClassName('tab')[0].appendChild(im);
  }
}
document.addEventListener("DOMContentLoaded", function(){
  document.getElementsByClassName('tab')[1].style.display = 'none';
  document.getElementById('search').oninput = search;
  let a = new XMLHttpRequest();
  a.open('GET', 'https://raw.githubusercontent.com/Donnnno/Arcticons/main/app/src/main/assets/drawable.xml');
  a.onload = genImageGrid;
  a.send();
});