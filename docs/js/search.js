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

function openPopup(){
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

function closePopup(){
  let fig = document.getElementsByTagName('figure')[0];
  fig.parentNode.removeChild(fig);
}

function search(){
  document.getElementById('results').setAttribute('aria-disabled', 'false');
  let busq = '.tab img[title*="'+this.value.toLocaleLowerCase()+'"]';
  let todo = '.tab img';
  let validos = Array.prototype.slice.call(document.querySelectorAll(busq));
  let todos = Array.prototype.slice.call(document.querySelectorAll(todo));
  for (let i of todos){
    if (this.value){
      if (validos.indexOf(i) == -1){
        i.style.display = 'none';
      }else{
        i.style.display = 'inline-block';
      }
    }else{
      i.style.display = 'inline-block';
    }
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
  // Generate carrousel
  let docs = Array.prototype.slice.call(xmldoc.querySelectorAll('item'));
  let latest = docs.slice(-8);
  let carrousel = document.createElement('section');
  carrousel.id = 'carrousel';
  carrousel.innerHTML = '<h2>Latest icons</h2><div class="latest content"></div>';
  for (let i of latest){
    let im = document.createElement('img');
    im.src = 'https://raw.githubusercontent.com/Donnnno/Arcticons/main/icons/white/' + i.attributes.drawable.value + '.svg';
    im.addEventListener('error', function(){this.src = this.src.replace('icons/white', 'todo');});
    im.alt = i.attributes.drawable.value;
    im.title = i.attributes.drawable.value;
    carrousel.children[1].appendChild(im);
  }

  for (let i of docs.sort(sortIcons)){
    let im = document.createElement('img');
    im.className = 'lazy';
    lazyImageObserver.observe(im);
    im.src = 'c.svg';
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