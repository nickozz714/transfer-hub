function onInitCredentials(){
    const token = checkToken();
    const id = global_scope.id;
    $.ajax({
        url: link+'/api/credential/scope/'+id,
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token 
        },
        success: function(response) {
            var credentialTable = $('#credentialOverviewTable').DataTable({
            data:response,
            select:true,
            destroy:true,
            columns: [
                {"data":"id"},
                {"data":"username"},
                {"data":"type"},
                {"data":"last_updated_at"}
            ]
        });
        credentialTable.on( 'select', function ( e, dt, type, indexes ) {
    if ( type === 'row' ) {
        var data = credentialTable.rows( indexes ).data().pluck( 'id' );
        openCredentialDialog('edit', data[0]);
            }
        });
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

function openCredentialDialog(mode, credentialId) {
    $('#credentialModify').modal('show');
    onInitCredentialModifyComponent(mode, credentialId);
}

$('#btn-new-credential').click(function(){
    openCredentialDialog('new',null);
});

$('#btn_refresh_credentials').click(function(){
    onInitCredentials();
});