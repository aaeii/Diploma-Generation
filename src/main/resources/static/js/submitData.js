async function submitData() {
    const formData = new FormData();
    collectFormData(formData);
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
    try {
        const response = await fetch('/submitData', {
            method: 'POST',
            body: formData,
            headers: {
                    [csrfHeader]: csrfToken
            }
        });
        if (response.ok) {
            const result = await response.text();
            if (result.includes("Файлы успешно сохранены")) {
                alert('Файлы успешно сохранены в личном кабинете');
            } else {
                alert(result);
            }
        } else {
            const errorText = await response.text();
            throw new Error(errorText || 'Ошибка сервера');
        }
    } catch (error) {
        console.error('Ошибка при обработке данных: ', error);
        alert('Ошибка при обработке данных: ' + error.message);
        document.getElementById("htmlFile").style.display = "block";
    }
}
function collectFormData(formData) {
    const url = document.getElementById('url').value;
    formData.append("url", url);
    const urlFile = document.getElementById('htmlFile').files[0];
    formData.append("htmlFile", urlFile);

    const data = Array.from(document.querySelectorAll('.input-field')).map(input => {
        const value = input.value.trim();
        return value ? parseInt(value, 10) : 10000;
    });

    data.forEach(value => {
        if (value !== null && !isNaN(value)) {
            formData.append("data", value);
        } else {
            formData.append("data", 10000);
        }
    });
    console.log(data)
    appendNumbers(formData);
    appendParticipantData(formData);
    appendTemplate(formData);
    appendNameTeam(formData);
    const diplomaTypeAllTeams = document.getElementById('checkboxA').checked;
    formData.append("diplomaTypeAllTeams", diplomaTypeAllTeams);

    const diplomaTypeSelectedTeam = document.getElementById('checkboxB').checked;
    formData.append("diplomaTypeSelectedTeam", diplomaTypeSelectedTeam);


    const fileFormat = document.getElementById('fileFormat').value;
    formData.append("fileFormat", fileFormat);
    const existingFileId = document.getElementById('existingFiles').value; // ID выбранного файла из выпадающего списка
    if (existingFileId) {
        formData.append("existingFileId", existingFileId);
    }
}

function appendNumbers(formData) {
    const num1_1 = document.getElementById('num1_1').value;
    const num1_2 = document.getElementById('num1_2').value;
    const num2_1 = document.getElementById('num2_1').value;
    const num2_2 = document.getElementById('num2_2').value;
    const num3_1 = document.getElementById('num3_1').value;
    const num3_2 = document.getElementById('num3_2').value;
    formData.append("num1_1", num1_1);
    formData.append("num1_2", num1_2);
    formData.append("num2_1", num2_1);
    formData.append("num2_2", num2_2);
    formData.append("num3_1", num3_1);
    formData.append("num3_2", num3_2);
}

function appendTemplate(formData) {
    const diplomaFile = document.getElementById('diplomaTemplateFile').files[0];
    let saveDiplomaFile = false;
    if (diplomaFile) {
        const confirmation = confirm("Сохранить шаблон в личный кабинет?");
        if (confirmation) {
            saveDiplomaFile = true;
        }
        formData.append("diplomaTemplateFile", diplomaFile);
        formData.append("saveDiplomaFile", saveDiplomaFile);
    }
}

function appendParticipantData(formData) {
    let saveDataFile = false;
    const fileInput = document.getElementById('dataAboutParticipantsFile');
    const existingDataSelect = document.getElementById('existingDataFiles');

    if (fileInput.files.length > 0) {
        const confirmation = confirm("Сохранить файл в личный кабинет?");
        if (confirmation) {
            saveDataFile = true;
        }
        formData.append('dataAboutParticipantsFile', fileInput.files[0]);
        formData.append("saveDataFile", saveDataFile);
    } else if (existingDataSelect.value) {
        formData.append('existingDataFileId', existingDataSelect.value);
    } else {
        alert('Пожалуйста, выберите файл для загрузки.');
        return;
    }
}
function appendNameTeam(formData) {
    const nameTeams = document.getElementsByName('nameTeam');
    const degreeNumbers = document.getElementsByName('degreeNumber');
    for (let i = 0; i < nameTeams.length; i++) {
        const team = nameTeams[i].value.trim();
        const degree = degreeNumbers[i] ? degreeNumbers[i].value.trim() : '';
        if (team) {
            formData.append('nameTeam', team);
            formData.append('degreeNumber', degree);
        }
    }
}