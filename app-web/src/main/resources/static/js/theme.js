document.addEventListener('DOMContentLoaded', () => {
    const themeToggle = document.getElementById('theme-toggle');
    const htmlElement = document.documentElement;

    function applyTheme(theme) {
        if (theme === 'dark') {
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
        // Dispatch a custom event to notify other parts of the app
        const event = new CustomEvent('themeChanged', { detail: { theme } });
        document.dispatchEvent(event);
    }

    // On page load, apply the saved theme. Default to light mode.
    const savedTheme = localStorage.getItem('theme') || 'light';
    applyTheme(savedTheme);


    // When the toggle is clicked, update the theme and save the preference
    if (themeToggle) {
        themeToggle.addEventListener('change', () => {
            const newTheme = themeToggle.checked ? 'dark' : 'light';
            localStorage.setItem('theme', newTheme);
            applyTheme(newTheme);
        });
    }
});
