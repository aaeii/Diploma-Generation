// Функция для добавления нового блока
function addSection(event) {
    if (event) {
        event.preventDefault();
    }
    const container = document.getElementById('section-one-diploma');
    const newSection = document.createElement('div');
    newSection.innerHTML = `
    <div class="one-diploma" style="margin-top:5px">
                        <label>Введите название команды:</label>
                        <input class="input-form-name-team" type="text" name="nameTeam"
                            placeholder="Team" />
                        <br>
                        <label>Введите степень:</label>
                        <input class="diploma-input degree" name="degreeNumber" placeholder="1" type="number" min="1" max="3" />
                    </div>
            `;

    container.appendChild(newSection);
}