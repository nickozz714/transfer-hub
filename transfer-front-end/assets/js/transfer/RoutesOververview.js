// The overview section of the transfers.
    $(document).ready(function() {
    initializeScopeListForPrincipal();
});

function onInit(){
    const token = checkToken();
    const id = global_scope.id;
    $.ajax({
        url: link+'/api/transfer/scope/'+id,
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token 
        },
        success: function(response) {
            var routesTable = $('#transferOverviewTable').DataTable({
            data:response,
            select:false,
            destroy:true,
            columns: [
                {"data":"id"},
                {"data":"description"},
                {"data":"status"},
                {"data":"last_updated_at"},
                {data: null,
                render: function(data, type, row) {
                  // Render action menu dropdown with buttons
                  return '<div class="dropdown">' +
                    '<button class="btn btn-dark dropdown-toggle" aria-expanded="false" data-bs-toggle="dropdown" type="button">Menu</button>' +
                      '<div class="dropdown-menu">'+
                    '<a class="dropdown-item" onclick="editTransfer(\'' + row.id + '\')">Configuration</a>'+
                    '<a class="dropdown-item" onclick="enableTransfer(\'' + row.id + '\')">Enable</a>' +
                    '<a class="dropdown-item" onclick="disableTransfer(\'' + row.id + '\')">Disable</a>' +
                    '<a class="dropdown-item" onclick="deleteTransfer(\'' + row.id + '\')">Delete</a>' +
                  '</div></div>';
                }
              }
            ]
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

function newTransfer() {
      $('#routesModify').modal('show');
    onInitRoutesModifyComponent("new", null);
}
// Define functions for actions
function editTransfer(id) {
  // Implement edit logic
  $('#routesModify').modal('show');
    onInitRoutesModifyComponent("edit", id);
}

function enableTransfer(id) {
  const token = checkToken();
    $.ajax({
        url: link+'/api/transfer/active/'+id,
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + token 
        },
        success: function(response) {
            onInit();
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

function disableTransfer(id) {
    const token = checkToken();
    $.ajax({
        url: link+'/api/transfer/inactive/'+id,
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + token 
        },
        success: function(response) {
            onInit();
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

function deleteTransfer(id) {
  // Implement delete logic
  alert("Removing a transfer is not yet possible", "Unfortunate Message")
}


$('#btn-new-transfer').click(function(){
    newTransfer();
});

$('#btn_refresh').click(function(){
    onInit();
});