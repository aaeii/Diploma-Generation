async function uploadExcel() {
    const fileInput = document.getElementById('dataAboutParticipantsFile');
    const existingDataSelect = document.getElementById('existingDataFiles');
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
    const formData = new FormData();
    if (fileInput.files.length > 0) {
        formData.append('dataAboutParticipantsFile', fileInput.files[0]);
    } else if (existingDataSelect.value) {
        formData.append('existingDataFileId', existingDataSelect.value);
    } else {
        alert('Пожалуйста, выберите файл для загрузки.');
        return;
    }
    try {
        const response = await fetch('/uploadExcel', {
            method: 'POST',
            body: formData,
            headers: {
             [csrfHeader]: csrfToken
             }
        });
        if (response.ok) {
            const headers = await response.json();
            displayHeaders(headers);
        } else {
            alert('Ошибка загрузки файла: ' + response.statusText);
        }
    } catch (error) {
        alert('Произошла ошибка: ' + error.message);
    }
}

function displayHeaders(headers) {
    const headersContainer = document.getElementById('headersContainer');
    headersContainer.innerHTML = '';
    const ul = document.createElement('ol');
    headers.forEach(header => {
        const li = document.createElement('li');
        li.textContent = header;
        ul.appendChild(li);
    });
    headersContainer.appendChild(ul);
}


