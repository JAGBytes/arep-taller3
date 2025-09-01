
function loadGetMsg() {
    let nameVar = document.getElementById("name").value.trim();
    
    // Validación del nombre: solo letras, números y espacios
    if (!isValidName(nameVar)) {
        document.getElementById("getrespmsg").innerHTML = "Nombre inválido. Solo letras, números y espacios.";
        return;
    }
    
    const xhttp = new XMLHttpRequest();

    // Callback que se ejecuta cuando llega la respuesta del servidor
    xhttp.onload = function () {
        let data = JSON.parse(this.responseText); // Convertir la respuesta JSON
        document.getElementById("getrespmsg").innerHTML = data.message;
    };
    
    // Configurar y enviar la petición GET con el nombre como parámetro
    xhttp.open("GET", "/app/hello?name=" + nameVar);
    xhttp.send();
}

function loadPostMsg() {
    let name = document.getElementById("postname").value.trim();
    
    // Validación del nombre: solo letras, números y espacios
    if (!isValidName(name)) {
        document.getElementById("postrespmsg").innerHTML = "Nombre inválido. Solo letras, números y espacios.";
        return;
    }

    const xhttp = new XMLHttpRequest();

    // Cuando se reciba la respuesta del servidor
    xhttp.onload = function () {
        let data = JSON.parse(this.responseText);
        // Actualizamos el div correspondiente al POST
        document.getElementById("postrespmsg").innerHTML = data.message;
    };

    // Abre la conexión tipo POST hacia el endpoint del servidor
    xhttp.open("POST", "/app/hello");

    // Indicamos que el contenido enviado es de tipo JSON
    xhttp.setRequestHeader("Content-Type", "application/json");

    // Envia los datos como JSON
    xhttp.send(JSON.stringify({ name: name }));
}

function isValidName(name) {
    const regex = /^[a-zA-Z0-9 ]+$/;
    return regex.test(name);
}


function loadPiMsg() {
    const xhttp = new XMLHttpRequest();

    xhttp.onload = function () {
        document.getElementById("pirespmsg").innerHTML = "Valor de π: " + this.responseText;
    };

    // GET al endpoint /pi
    xhttp.open("GET", "/pi");
    xhttp.send();
}

function loadEMsg() {
    const xhttp = new XMLHttpRequest();

    xhttp.onload = function () {
        document.getElementById("erespmsg").innerHTML = "Valor de e: " + this.responseText;
    };

    // GET al endpoint /e
    xhttp.open("GET", "/e");
    xhttp.send();
}