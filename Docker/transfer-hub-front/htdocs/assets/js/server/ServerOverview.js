function onInitServers(){
    const token = checkToken();
    const id = global_scope.id;
    $.ajax({
        url: link+'/api/host/scope/'+id,
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token 
        },
        success: function(response) {
            var serverTable = $('#serverOverviewTable').DataTable({
            data:response,
            select:true,
            destroy:true,
            columns: [
                {"data":"id"},
                {"data":"hostname"},
                {"data":"port"},
                {"data":"description"},
                {"data":"last_updated_at"}
            ]
        });
        serverTable.on( 'select', function ( e, dt, type, indexes ) {
    if ( type === 'row' ) {
        var data = serverTable.rows( indexes ).data().pluck( 'id' );
        openServerDialog('edit', data[0]);
            }
        });
        },
        error: function(jqXHR, textStatus, errorThrown) {
        // Check the status code and take appropriate actions
        if (jqXHR.status === 401) {
            // Handle 401 Unauthorized error
            alert("Unauthorized: " + jqXHR.responseText);
            window.location.href = 'login.html';
        } else if (jqXHR.status === 500) {
            // Handle 500 Internal Server Error
            alert("Internal server error: " + jqXHR.responseText);
        } else {
            // Handle other errors
            alert("Unexpected error: " + jqXHR.responseText);
        }
    }
    });
}


function openServerDialog(mode, serverId) {
    $('#serverModify').modal('show');
    onInitServerModifyComponent(mode, serverId);
}

$('#btn-new-server').click(function(){
    openServerDialog('new',null);
});

$('#btn_refresh_server').click(function(){
    onInitServers();
});