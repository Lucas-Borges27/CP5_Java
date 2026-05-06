document.addEventListener('DOMContentLoaded', function() {
    const userMenuButton = document.getElementById('userMenuButton');

    if (userMenuButton) {
        userMenuButton.addEventListener('click', function() {
            const menu = document.getElementById('userMenu');
            if (menu) {
                menu.classList.toggle('hidden');
            }
        });
    }
});

function changeLang(lang) {
    const target = `${window.location.pathname}?lang=${encodeURIComponent(lang)}`;
    window.location.assign(target);
}
