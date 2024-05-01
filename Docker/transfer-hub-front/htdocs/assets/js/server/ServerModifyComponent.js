// All operations regarding the routesModify component.

var global_mode;
var global_server_id;

function onInitServerModifyComponent(mode, serverId) {
    global_mode = mode;
    global_server_id = serverId;
    initializeServerForm();
}
    
function initializeServerForm(){
        // Needs to load all standard data like credentials and protocols
        // In the request, upon success it needs to load the load transferrecord method.
        if (global_mode == 'edit') {
            // We are editing the transfer. So we need the information of this route.
            // TODO
            $('#serverModifyTitle').text('Editing server');
            loadServerRecord();
        } else {
            // We are generating a new transfer. So we need to have an empty form.
            $('#serverModifyTitle').text('Adding new server');
            $(".form-control").each(function(){
                $(this).val("");
            })
        }
    }

function loadServerRecord() {
    const token = checkToken();
    $.ajax({
        url: link+'/api/host/'+global_server_id,
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token 
        },
        success: function(response) {
            $('#server_hostname').val(response.hostname);
         $('#server_port').val(response.port);
         $('#server_description').val(response.description);
        },
        error: function(jqXHR, textStatus, errorThrown) {
        // Check the status code and take appropriate actions
        if (jqXHR.status === 401) {
            // Handle 401 Unauthorized error
            alert(jqXHR.responseText, "Unauthorized");
            window.location.href = 'login.html';
        } else if (jqXHR.status === 500) {
            // Handle 500 Internal Server Error
            alert(jqXHR.responseText, "Internal Server Error");
        } else {
            // Handle other errors
            alert(jqXHR.responseText, "Unexpected Error");
        }
    }
    });   
    }

function onServerSave(){
    if (global_mode == 'edit'){
        toApiServerService({
            id:global_server_id,
            hostname: $('#server_hostname').val() == "" ? null : $('#server_hostname').val(),
            port: $('#server_port').val() == "" ? null : $('#server_port').val(),
            description: $('#server_description').val() == "" ? null : $('#server_description').val(),
            scope: global_scope,
        });
    } else {
        toApiServerService({
            hostname: $('#server_hostname').val() == "" ? null : $('#server_hostname').val(),
            port: $('#server_port').val() == "" ? null : $('#server_port').val(),
            description: $('#server_description').val() == "" ? null : $('#server_description').val(),
            scope: global_scope,
        });
    }
}

function toApiServerService(serverRecord) {
    data = JSON.stringify(serverRecord)
    const token = checkToken();
    createOrUpdate = global_mode == 'edit' ? 'update' : 'create';
        $.ajax({ 
            url: link+'/api/host/'+createOrUpdate, 
            headers: {'Authorization': 'Bearer ' + token},
            type: global_mode == 'edit' ? 'PUT' : 'POST',
            contentType: "application/json",
            dataType: "json",
            data:data,
            success: function (response) { 
                // Do something with the result 
            alert(global_mode == 'edit' ? 'Your server has been updated.' : 'Your server has been created.', "Information Message");
            $('#serverModify').modal('hide');
            onInitServers();
            },
        error: function(jqXHR, textStatus, errorThrown) {
        // Check the status code and take appropriate actions
        if (jqXHR.status === 401) {
            // Handle 401 Unauthorized error
            alert(jqXHR.responseText, "Unauthorized");
            window.location.href = 'login.html';
        } else if (jqXHR.status === 500) {
            // Handle 500 Internal Server Error
            alert(jqXHR.responseText, "Internal Server Error");
        } else {
            // Handle other errors
            alert(jqXHR.responseText, "Unexpected Error");
        }
    }
        }); 
}

$('#btn-save-server').click(function(){
    onServerSave();
});