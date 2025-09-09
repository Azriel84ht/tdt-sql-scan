document.addEventListener('DOMContentLoaded', function () {
    const header = document.querySelector('.header-authenticated');
    if (!header) return;

    // --- Hide header on scroll ---
    let lastScrollTop = 0;
    window.addEventListener('scroll', function () {
        let scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        if (scrollTop > lastScrollTop && scrollTop > header.offsetHeight) {
            // Scroll Down
            header.classList.add('header-authenticated--hidden');
        } else {
            // Scroll Up
            header.classList.remove('header-authenticated--hidden');
        }
        lastScrollTop = scrollTop <= 0 ? 0 : scrollTop;
    });

    // --- Mobile menu toggle ---
    const mobileMenuToggle = document.getElementById('mobile-menu-toggle-auth');
    const mobileMenuPanel = document.getElementById('mobile-menu-panel-auth');

    if (mobileMenuToggle && mobileMenuPanel) {
        mobileMenuToggle.addEventListener('click', function () {
            mobileMenuPanel.classList.toggle('is-open');
        });
    }

    // --- Profile dropdown toggle ---
    const profileToggle = document.getElementById('profile-toggle');
    const profileMenu = document.getElementById('profile-menu');

    if (profileToggle && profileMenu) {
        profileToggle.addEventListener('click', function (event) {
            event.stopPropagation();
            profileMenu.classList.toggle('is-open');
        });

        // Close dropdown if clicking outside
        window.addEventListener('click', function (event) {
            if (!profileMenu.contains(event.target) && !profileToggle.contains(event.target)) {
                profileMenu.classList.remove('is-open');
            }
        });
    }
});
