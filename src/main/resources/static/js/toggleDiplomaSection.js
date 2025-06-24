function toggleDiplomaSection(sectionId) {
    const sectionAllDiplomas = document.getElementById('section-all-diplomas');
    const sectionOneDiploma = document.getElementById('section-one-diploma');
    const checkboxA = document.getElementById('checkboxA');
    const checkboxB = document.getElementById('checkboxB');
    if (sectionId === 'all') {
        const inputsAll = sectionAllDiplomas.querySelectorAll('input');
        if (checkboxA.checked) {
            sectionAllDiplomas.classList.remove('passive');
            inputsAll.forEach(input => input.disabled = false);
        } else {
            sectionAllDiplomas.classList.add('passive');
            inputsAll.forEach(input => input.disabled = true);
        }
    } else if (sectionId === 'one') {
        const inputsOne = sectionOneDiploma.querySelectorAll('input');
        if (checkboxB.checked) {
            sectionOneDiploma.classList.remove('passive');
            inputsOne.forEach(input => input.disabled = false);
        } else {
            sectionOneDiploma.classList.add('passive');
            inputsOne.forEach(input => input.disabled = true);
        }
    }
}

// При загрузке страницы проверяем состояние чекбоксов, чтобы применить стили
document.addEventListener('DOMContentLoaded', function () {
    const checkboxA = document.getElementById('checkboxA');
    const checkboxB = document.getElementById('checkboxB');
    if (!checkboxA.checked && !checkboxB.checked) {
        checkboxA.checked = true;
    }
    toggleDiplomaSection('all');
    toggleDiplomaSection('one');
});