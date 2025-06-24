function toggleDataFileOption(selectedId) {
    const newFileUploadSection = document.getElementById('newDataFileUploadSection');
    const existingFilesSelect = document.getElementById('existingDataFiles');

    const inputs = newFileUploadSection.querySelectorAll('input');
    const options = existingFilesSelect.querySelectorAll('option');

    if (selectedId === 'uploadNewDataFile') {
        newFileUploadSection.classList.remove('passive');
        existingFilesSelect.classList.add('passive');
        inputs.forEach(input => input.disabled = false);
        options.forEach(option => {
            option.disabled = true;
        });

    } else if (selectedId === 'useExistingDataFile') {
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
    const uploadNewFileRadio = document.getElementById('uploadNewDataFile');
    const useExistingFileRadio = document.getElementById('useExistingDataFile');
    if (!uploadNewFileRadio.checked && !useExistingFileRadio.checked) {
        uploadNewFileRadio.checked = true;
    }

    let selectedId = null;
    if (uploadNewFileRadio.checked) {
        selectedId = 'uploadNewDataFile';
    } else if (useExistingFileRadio.checked) {
        selectedId = 'useExistingDataFile';
    }

    toggleDataFileOption(selectedId);
});

document.getElementById('uploadNewDataFile').addEventListener('change', function () {
    toggleDataFileOption('uploadNewDataFile');
});
document.getElementById('useExistingDataFile').addEventListener('change', function () {
    toggleDataFileOption('useExistingDataFile');
});