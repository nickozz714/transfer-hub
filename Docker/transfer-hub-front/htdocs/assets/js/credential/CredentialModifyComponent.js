// All operations regarding the routesModify component.

var global_mode;
var global_credential_id;

function onInitCredentialModifyComponent(mode, credentialId) {
    global_mode = mode;
    global_credential_id = credentialId;
    initializeCredentialForm();
    setForm();
}
    
function initializeCredentialForm(){
        // Needs to load all standard data like credentials and protocols
        // In the request, upon success it needs to load the load transferrecord method.
        if (global_mode == 'edit') {
            // We are editing the transfer. So we need the information of this route.
            // TODO
            $('#credentialModifyTitle').text('Editing credential');
            loadCredentialRecord();
        } else {
            // We are generating a new transfer. So we need to have an empty form.
            $('#credentialModifyTitle').text('Adding new credential');
            $(".form-control").each(function(){
                $(this).val("");
            })
        }
    }

function loadCredentialRecord() {
        const token = checkToken();
        $.ajax({ 
            url: link+'/api/credential/'+global_credential_id, 
            headers: {'Authorization': 'Bearer ' + token},
            type: 'GET',
            contentType: "application/json",
            dataType: "json",
            success: function (response) {
                 $('#credential_username').val(response.username);
                 $('#credential_password').val(response.password);
                 $('#credential_public_key').val(response.public_key);
                 $('#credential_private_key').val(response.private_key);
                 $('#credential_keyphrase').val(response.keyphrase);
                 $('#credential_token').val(response.token);
                 $('#credential_client_id').val(response.client_id);
                 $('#credential_client_secret').val(response.client_secret);
                 $('#credential_tenant_id').val(response.tenant_id);
                 $('#credential_type').val(response.type);
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
            alert(jqXHR.responseText, "Unexpected error");
        }
    }
        }); 
    }

function onCredentialSave(){
    if (global_mode == 'edit'){
        toApiCredentialService({
            id:global_credential_id,
            username: $('#credential_username').val() == "" ? null : $('#credential_username').val(),
            password: $('#credential_password').val() == "" ? null : $('#credential_password').val(),
            public_key: $('#credential_public_key').val() == "" ? null : $('#credential_public_key').val(),
            private_key: $('#credential_private_key').val() == "" ? null : $('#credential_private_key').val(),
            keyphrase: $('#credential_keyphrase').val() == "" ? null : $('#credential_keyphrase').val(),
            token: $('#credential_token').val() == "" ? null : $('#credential_token').val(),
            client_id: $('#credential_client_id').val() == "" ? null : $('#credential_client_id').val(),
            client_secret: $('#credential_client_secret').val() == "" ? null : $('#credential_client_Secret').val(),
            tenant_id: $('#credential_tenant_id').val() == "" ? null : $('#credential_tenant_id').val(),
            credentialType:$('#credential_type').val(),
            scope: global_scope,
        });
    } else {
        toApiCredentialService({
            username: $('#credential_username').val() == "" ? null : $('#credential_username').val(),
            password: $('#credential_password').val() == "" ? null : $('#credential_password').val(),
            public_key: $('#credential_public_key').val() == "" ? null : $('#credential_public_key').val(),
            private_key: $('#credential_private_key').val() == "" ? null : $('#credential_private_key').val(),
            keyphrase: $('#credential_keyphrase').val() == "" ? null : $('#credential_keyphrase').val(),
            token: $('#credential_token').val() == "" ? null : $('#credential_token').val(),
            client_id: $('#credential_client_id').val() == "" ? null : $('#credential_client_id').val(),
            client_secret: $('#credential_client_secret').val() == "" ? null : $('#credential_client_Secret').val(),
            tenant_id: $('#credential_tenant_id').val() == "" ? null : $('#credential_tenant_id').val(),
            credentialType:$('#credential_type').val(),
            scope: global_scope,
        });
    }
}

function toApiCredentialService(credentialRecord) {
    const token = checkToken();
    data = JSON.stringify(credentialRecord);
    createOrUpdate = global_mode == 'edit' ? 'update' : 'create';
        $.ajax({ 
            url: link+'/api/credential/'+createOrUpdate,
            headers: {'Authorization': 'Bearer ' + token},
            type: global_mode == 'edit' ? 'PUT' : 'POST',
            contentType: "application/json",
            dataType: "json",
            data:data,
            statusCode: {
                201: function(response) {
                    // Handle status code 201
                    alert(global_mode == 'edit' ? 'Your credential has been updated.' : 'Your credential has been created.', "Information Message");
                    $('#credentialModify').modal('hide');
                    onInitCredentials();
                }
                // Add other specific status code handlers if needed...
            },
            success: function (response) { 
                // Do something with the result 
            alert(global_mode == 'edit' ? 'Your credential has been updated.' : 'Your credential has been created.', "Information Message");
            $('#credentialModify').modal('hide');
            onInitCredentials();
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

$('#btn-save-credential').click(function(){
    onCredentialSave();
});

$('#credential_type').change(function(){
    setForm();
});

function setForm() {
    const selectedOption = $('#credential_type option:selected').val();
    const classToSelect = "."+selectedOption.toLowerCase();
    $(".credential").hide();
    $(classToSelect).show();
}