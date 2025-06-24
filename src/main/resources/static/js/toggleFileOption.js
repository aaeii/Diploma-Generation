function toggleFileOption(selectedId) {
    const newFileUploadSection = document.getElementById('newFileUploadSection');
    const existingFilesSelect = document.getElementById('existingFiles');

    const inputs = newFileUploadSection.querySelectorAll('input');
    const options = existingFilesSelect.querySelectorAll('option');

    if (selectedId === 'uploadNewFile') {
        newFileUploadSection.classList.remove('passive');
        existingFilesSelect.classList.add('passive');
        inputs.forEach(input => input.disabled = false);
        options.forEach(option => {
            option.disabled = true;
        });

    } else if (selectedId === 'useExistingFile') {
        newFileUploadSection.classList.add('passive');
        existingFilesSelect.classList.remove('passive');
        inputs.forEach(input => input.disabled = true);
        options.forEach(option => {
            option.disabled = false;
        });

    }
}

// При загрузке страницы инициализируем видимость разделов
document.addEventListener('DOMContentLoaded', function () {
    const uploadNewFileRadio = document.getElementById('uploadNewFile');
    const useExistingFileRadio = document.getElementById('useExistingFile');
    if (!uploadNewFileRadio.checked && !useExistingFileRadio.checked) {
        uploadNewFileRadio.checked = true;
    }

    let selectedId = null;
    if (uploadNewFileRadio.checked) {
        selectedId = 'uploadNewFile';
    } else if (useExistingFileRadio.checked) {
        selectedId = 'useExistingFile';
    }

    toggleFileOption(selectedId);
});

document.getElementById('uploadNewFile').addEventListener('change', function () {
    toggleFileOption('uploadNewFile');
});
document.getElementById('useExistingFile').addEventListener('change', function () {
    toggleFileOption('useExistingFile');
});