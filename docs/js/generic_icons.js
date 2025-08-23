async function loadIcons() {
    const response = await fetch('assets/generic_icons.txt');
    const text = await response.text();
    const icons = text.split('\n').filter(line => line.trim());

    const grid = document.getElementById('icon-grid');
    icons.forEach(iconName => {
        if (iconName) {
            const item = document.createElement('div');
            item.className = 'icon-item';
            item.innerHTML = `
              <img src="https://raw.githubusercontent.com/Arcticons-Team/Arcticons/main/icons/white/${iconName}.svg" 
                   alt="${iconName}" 
                   onerror="this.src='img/arcticons/icon.png'">
              <div class="icon-name">${iconName}</div>
            `;
            item.onclick = () => copyIconName(iconName);
            grid.appendChild(item);
        }
    });
}

function copyIconName(name) {
    navigator.clipboard.writeText(name).then(() => {
        const notification = document.getElementById('copy-notification');
        notification.style.display = 'block';
        setTimeout(() => {
            notification.style.display = 'none';
        }, 2000);
    });
}

loadIcons()