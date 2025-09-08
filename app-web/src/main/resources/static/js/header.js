document.addEventListener('DOMContentLoaded', function () {
    const header = document.querySelector('.header-guest');
    if (!header) return;

    // --- Hide header on scroll ---
    let lastScrollTop = 0;
    window.addEventListener('scroll', function () {
        let scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        if (scrollTop > lastScrollTop && scrollTop > header.offsetHeight) {
            // Scroll Down
            header.classList.add('header-guest--hidden');
        } else {
            // Scroll Up
            header.classList.remove('header-guest--hidden');
        }
        lastScrollTop = scrollTop <= 0 ? 0 : scrollTop;
    });

    // --- Mobile menu toggle ---
    const mobileMenuToggle = document.getElementById('mobile-menu-toggle');
    const mobileMenuPanel = document.getElementById('mobile-menu-panel');

    if (mobileMenuToggle && mobileMenuPanel) {
        mobileMenuToggle.addEventListener('click', function () {
            mobileMenuPanel.classList.toggle('is-open');
        });
    }
});
