document.addEventListener('DOMContentLoaded', () => {
    const themeToggle = document.getElementById('theme-toggle');
    const htmlElement = document.documentElement;

    // On page load, apply the saved theme. Default to light mode.
    if (localStorage.getItem('theme') === 'dark') {
        htmlElement.classList.add('dark');
        if (themeToggle) {
            themeToggle.checked = true;
        }
    } else {
        htmlElement.classList.remove('dark');
        if (themeToggle) {
            themeToggle.checked = false;
        }
    }

    // When the toggle is clicked, update the theme and save the preference
    if (themeToggle) {
        themeToggle.addEventListener('change', () => {
            if (themeToggle.checked) {
                htmlElement.classList.add('dark');
                localStorage.setItem('theme', 'dark');
            } else {
                htmlElement.classList.remove('dark');
                localStorage.setItem('theme', 'light');
            }
        });
    }
});
