document.addEventListener('DOMContentLoaded', () => {
    const loadScriptsButton = document.getElementById('load-scripts-button');
    const placeholderLoadButton = document.getElementById('placeholder-load-button');
    const uploadModal = document.getElementById('upload-modal');
    const closeModalButton = document.querySelector('.close-upload-modal');

    const openModal = () => {
        if (uploadModal) {
            uploadModal.style.display = 'block';
        }
    };

    const closeModal = () => {
        if (uploadModal) {
            uploadModal.style.display = 'none';
        }
    };

    if (loadScriptsButton) {
        loadScriptsButton.addEventListener('click', openModal);
    }
    if (placeholderLoadButton) {
        placeholderLoadButton.addEventListener('click', openModal);
    }
    if (closeModalButton) {
        closeModalButton.addEventListener('click', closeModal);
    }

    window.addEventListener('click', (event) => {
        if (event.target === uploadModal) {
            closeModal();
        }
    });

    const dropZone = document.getElementById('drop-zone');
    const fileInput = document.getElementById('file-input');
    const fileListContainer = document.getElementById('file-list-container');
    const fileList = document.getElementById('file-list');
    const fileOrderForm = document.getElementById('file-order-form');
    let selectedFiles = [];

    if (dropZone) {
        dropZone.addEventListener('click', () => fileInput.click());

        dropZone.addEventListener('dragover', (event) => {
            event.preventDefault();
            dropZone.classList.add('drag-over');
        });

        dropZone.addEventListener('dragleave', () => {
            dropZone.classList.remove('drag-over');
        });

        dropZone.addEventListener('drop', (event) => {
            event.preventDefault();
            dropZone.classList.remove('drag-over');
            const files = event.dataTransfer.files;
            handleFiles(files);
        });

        fileInput.addEventListener('change', () => {
            const files = fileInput.files;
            handleFiles(files);
        });
    }

    function handleFiles(files) {
        selectedFiles = Array.from(files);
        fileList.innerHTML = '';
        if (selectedFiles.length > 0) {
            fileListContainer.style.display = 'block';
            selectedFiles.forEach((file, index) => {
                const li = document.createElement('li');
                li.textContent = file.name;
                li.draggable = true;
                li.dataset.index = index;
                fileList.appendChild(li);
            });
        }
    }

    if (fileOrderForm) {
        fileOrderForm.addEventListener('submit', (event) => {
            event.preventDefault();

            const formData = new FormData();
            selectedFiles.forEach(file => {
                formData.append('files', file);
            });

            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

            fetch('/upload', {
                method: 'POST',
                headers: {
                    [csrfHeader]: csrfToken
                },
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                // Handle response data, e.g., display graph
                console.log(data);
                alert('Files uploaded successfully! (Check console for response)');
                closeModal();
            })
            .catch(error => {
                console.error('Error:', error);
                alert('An error occurred during file upload.');
            });
        });
    }
});
