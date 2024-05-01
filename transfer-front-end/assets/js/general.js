const link = "http://localhost:9090";
var global_scope;
function checkToken() {
    const token = localStorage.getItem('transfer-hub-token');
    if(!token) {
        window.location.href = 'login.html';
    } else {
        return token;
    }
}

function getActiveScopeObject() {
    const scope = {
        id:$('#select_scope option:selected').val(),
        name: $('#select_scope option:selected').text()
    }
    return scope;
}

$('#btn_login').click(function(){
    window.location.href = 'login.html';
});

$('#btn_login_user').click(function(){
    login();
});

$('#btn_register').click(function(){
    window.location.href = 'register.html';
});

$('#link_transfer').click(function(){
    $('#transferOverview').show();
    $('#credentialOverview').hide();
    $('#serverOverview').hide();
});

$('#link_credentials').click(function(){
    $('#transferOverview').hide();
    $('#credentialOverview').show();
    $('#serverOverview').hide();
});

$('#link_servers').click(function(){
    $('#transferOverview').hide();
    $('#credentialOverview').hide();
    $('#serverOverview').show();
});

$('#link_scope_administration').click(function(){
    initializeScopeListForAdmin();
});

function alert(message, title) {
    $('#toast-message').text(message);
    $('#toast-header').text(title);
    var toastElement = document.getElementById('toast-alert');
    var toast = new bootstrap.Toast(toastElement);
    toast.show();
}
