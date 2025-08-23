let targetIconToOpen = null;

let lazyImageObserver = new IntersectionObserver(function(entries, observer){
  entries.forEach(function(entry){
    if (entry.isIntersecting) {
      let lazyImage = entry.target;
      lazyImage.addEventListener('error', function(){this.src = this.src.replace('icons/black', 'todo').replace('icons/white', 'todo');});
      
      // If this is the target icon we want to open, add load listener before setting src
      if (targetIconToOpen && lazyImage.title === targetIconToOpen) {
        lazyImage.addEventListener('load', function() {
          openPopup.call(this);
          targetIconToOpen = null; // Reset after opening
        }, { once: true });
      }
      
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
  let formattedTitle = icon.title.charAt(0).toUpperCase() + 
                       icon.title.slice(1).replace(/_/g, ' ');
  titleLabel.textContent = formattedTitle;
  let nameLabel = document.createElement('div');
  nameLabel.className = 'icon-name';
  nameLabel.textContent = icon.title;
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
      copyToClipboard(icon.title);
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
  // Restore URL to default (remove ?icon)
  if (window.location.search.includes('icon=')) {
    history.replaceState(null, '', window.location.pathname);
  }
}

function sortIcons(a, b){
  let nameA = a.getAttribute('drawable');
  let nameB = b.getAttribute('drawable');
  if (nameA < nameB){ return -1; }
  if (nameA > nameB){ return 1; }
  return 0;
}

function openPopupWithInlineSvg(svgText, iconTitle) {
  closePopup();
  if (iconTitle) {
    history.replaceState(null, '', '?icon=' + encodeURIComponent(iconTitle));
  }

  let fig = document.createElement('figure');
  fig.className = 'popup-figure';

  // Create a container and insert raw SVG
  let svgContainer = document.createElement('div');
  svgContainer.className = 'svg-inline-container';
  svgContainer.innerHTML = svgText;

  // Title and name labels
  let titleLabel = document.createElement('h2');
  titleLabel.className = 'icon-title';
  let formattedTitle = iconTitle.charAt(0).toUpperCase() + iconTitle.slice(1).replace(/_/g, ' ');
  titleLabel.textContent = formattedTitle;

  let nameLabel = document.createElement('div');
  nameLabel.className = 'icon-name';
  nameLabel.textContent = iconTitle;

  // Close button
  let closeBtn = document.createElement('button');
  closeBtn.className = 'popup-close-btn';
  closeBtn.innerHTML = '&times;';
  closeBtn.title = 'Close';
  closeBtn.onclick = function(e) { e.stopPropagation(); closePopup(); };

  function escHandler(e) { if (e.key === 'Escape') closePopup(); }
  document.addEventListener('keydown', escHandler);
  fig._escHandler = escHandler;

  // Click handlers: clicking the figure closes; clicking svg copies name
  fig.addEventListener('click', function(e) {
    if (e.target === fig) { closePopup(); }
    else if (e.target !== closeBtn) { copyToClipboard(iconTitle); }
  });

  fig.appendChild(closeBtn);
  fig.appendChild(svgContainer);
  fig.appendChild(titleLabel);
  fig.appendChild(nameLabel);
  document.body.appendChild(fig);
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

  // Set the target icon from URL params
  const params = new URLSearchParams(window.location.search);
  const iconFromUrl = params.get('icon');
  if (iconFromUrl) {
    targetIconToOpen = iconFromUrl;
  }
  
  for (let i of uniqueDocs.sort(sortIcons)){
    let im = document.createElement('img');
    im.className = 'lazy';
    im.dataset.src = 'https://raw.githubusercontent.com/Arcticons-Team/Arcticons/main/icons/white/' + i.attributes.drawable.value + '.svg';
    im.alt = i.attributes.drawable.value;
    im.title = i.attributes.drawable.value;
    im.addEventListener('click', openPopup);

    // Append first so layout and selectors work
    document.getElementsByClassName('tab')[0].appendChild(im);

    // If this is the icon requested via URL, try fetching the raw SVG and open inline
    if (iconFromUrl && i.attributes.drawable.value === iconFromUrl) {
      fetch(im.dataset.src).then(resp => {
        if (!resp.ok) throw new Error('Network response was not ok');
        return resp.text();
      }).then(svgText => {
        // If fetch succeeded, open popup with inline SVG for reliability
        openPopupWithInlineSvg(svgText, im.title);
        // Also set the img src so it appears in the grid
        im.src = im.dataset.src;
        im.className = '';
      }).catch(err => {
        // Fallback: set src and open popup when image loads
        im.addEventListener('load', function() { openPopup.call(this); }, { once: true });
        im.addEventListener('error', function(){ this.src = this.src.replace('icons/black', 'todo').replace('icons/white', 'todo'); }, { once: true });
        im.src = im.dataset.src;
        im.className = '';
      });
    } else {
      // Otherwise observe for lazy loading
      lazyImageObserver.observe(im);
    }
  }

  // After building grid, try to open target icon robustly (handles slow loads or production differences)
  function attemptOpenIcon(iconName, retries = 10, delay = 200) {
    if (!iconName) return;
    let imgs = document.querySelectorAll('.tab img');
    for (let img of imgs) {
      if (img.title === iconName) {
        // If image already loaded, open immediately
        if (img.complete && img.naturalWidth && img.naturalWidth > 0) {
          openPopup.call(img);
        } else {
          // otherwise wait for load
          img.addEventListener('load', function() { openPopup.call(this); }, { once: true });
          img.addEventListener('error', function() { /* ignore */ }, { once: true });
          // ensure it is loading
          if (!img.src) {
            img.src = img.dataset.src;
            img.className = '';
          }
        }
        return;
      }
    }
    if (retries > 0) {
      setTimeout(function() { attemptOpenIcon(iconName, retries - 1, delay); }, delay);
    }
  }

  // Support both query param and hash (#icon=...)
  let initialIcon = null;
  const q = new URLSearchParams(window.location.search).get('icon');
  if (q) initialIcon = q;
  const h = (window.location.hash || '').replace(/^#/, '');
  if (!initialIcon && h.startsWith('icon=')) initialIcon = decodeURIComponent(h.split('=')[1]);
  if (initialIcon) {
    attemptOpenIcon(initialIcon);
  }
}
document.addEventListener("DOMContentLoaded", function(){
  document.getElementsByClassName('tab')[1].style.display = 'none';
  document.getElementById('search').oninput = search;
  let a = new XMLHttpRequest();
  a.open('GET', 'https://raw.githubusercontent.com/Arcticons-Team/Arcticons/main/app/src/main/assets/drawable.xml');
  a.onload = genImageGrid;
  a.send();
});