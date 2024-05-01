function initializeScopeListForPrincipal() {
    const token = checkToken();
    $.ajax({
        url: link + '/api/scope',
        method: 'GET',
                headers: {
                    'Authorization': 'Bearer ' + token
                },
        success: function (response) {
            // Do something with the result
            initializeScopes(response);
            if (response.length > 0) {
            global_scope = getActiveScopeObject();
            onInit();
            onInitCredentials();
            onInitServers();
            }
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

function initializeScopeListForAdmin() {
    const token = checkToken();
    $.ajax({
        url: link + '/api/scope/admin',
        method: 'GET',
                headers: {
                    'Authorization': 'Bearer ' + token
                },
        success: function (response) {
            // Do something with the result
            initializeAdminScopes(response);
            if (response.length > 0) {
            outOfScopePrincipals();
            inOfScopePrincipals();
            }
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

function initializeScopes(scopeList) {
    $('#select_scope').empty();
    for (var i in scopeList) {
        $('#select_scope').append(new Option(scopeList[i].name, scopeList[i].id));
    }
}

function initializeAdminScopes(scopeList) {
    $('#select_scope_management').empty();
    $('#select_scope_registration').empty();
    for (var i in scopeList) {
        $('#select_scope_management').append(new Option(scopeList[i].name, scopeList[i].id));
        $('#select_scope_registration').append(new Option(scopeList[i].name, scopeList[i].id));
    }
}

$('#select_scope').change(function(){
    global_scope = getActiveScopeObject();
    onInit();
    onInitCredentials();
    onInitServers();
})

$('#btn_create_scope').click(function(){
    const token = checkToken();
    const scopeName = $('#input_scope_name').val();
    $.ajax({
        url: link + '/api/scope/create',
        method: 'POST',
        data: JSON.stringify({name: scopeName}),
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Content-Type' : 'application/json'
                },
        success: function (response) {
            // Do something with the result
            initializeScopeListForAdmin();
            initializeScopeListForPrincipal();
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
});

$('#btn_save_scope').click(function(){
    const token = checkToken();
    const scopeId = $('#select_scope_management option:selected').val();
    const scopeName = $('#input_management_scope_name').val();
    $.ajax({
        url: link + '/api/scope/update',
        method: 'PUT',
        data: JSON.stringify({id: scopeId, name: scopeName}),
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Content-Type' : 'application/json'
                },
        success: function (response) {
            // Do something with the result
            initializeScopeListForAdmin();
            initializeScopeListForPrincipal();
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
});

$("#select_scope_registration").change(function(){
    outOfScopePrincipals();
});

function outOfScopePrincipals(){
    const token = checkToken();
    const id = $("#select_scope_registration option:selected").val();
    $.ajax({
        url: link + '/api/scope/principal/out/'+id,
        method: 'GET',
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Content-Type' : 'application/json'
                },
        success: function (response) {
            // Do something with the result
            $("#select_scope_registration_principals").empty();
            for (var i in response) {
                $('#select_scope_registration_principals').append(new Option(response[i].username, response[i].id));
            }
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

$("#select_scope_registration").change(function(){
    inOfScopePrincipals();
});

function inOfScopePrincipals(){
    const token = checkToken();
    const id = $("#select_scope_registration option:selected").val();
    $.ajax({
        url: link + '/api/scope/principal/scoped/'+id,
        method: 'GET',
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Content-Type' : 'application/json'
                },
        success: function (response) {
            // Do something with the result
            $("#table_scope_registration tbody").empty();
            for (var i in response) {
                console.log(response);
               $("#table_scope_registration tbody").append('<tr><td data-id="'+response[i].principle_id+'" data-role="'+response[i].role+'">'+response[i].principle_name+'</td><td>'+response[i].role+'</td></tr>') 
            }
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

$('#table_scope_registration').on('click', 'tr', function(){
        // Remove the selected-row class from all rows
        $('#table_scope_registration tr').removeClass('selected-row');

        // Add the selected-row class to the clicked row
        $(this).addClass('selected-row');
      });

$('#btn_change').click(function(){
    var selectedRow = $('#table_scope_registration tr.selected-row');
    if (selectedRow.length > 0) {
          // Retrieve the data-id attribute from the first cell in the selected row
          var dataId = selectedRow.find('td:first').attr('data-id');
          var dataRole = selectedRow.find('td:first').attr('data-role');
          var role = $('#select_scope_role_principal option:selected').val();
            const token = checkToken();
            const id = $("#select_scope_registration option:selected").val();
            const name = $("#select_scope_registration option:selected").text();
            $.ajax({
                url: link + '/api/scope/join',
                method: 'PUT',
                data: JSON.stringify({
                    principle_id: dataId,
                    scopeDTO: {id:id, name:name},
                    role: role
                }),
                        headers: {
                            'Authorization': 'Bearer ' + token,
                            'Content-Type' : 'application/json'
                        },
                success: function (response) {
                    // Do something with the result
                    if (response.length) {
                    initializeScopeListForPrincipal();
                    initializeScopeListForAdmin();
                    inOfScopePrincipals();
                    outOfScopePrincipals();
                    alert("Principal role has been changed succesfully.", "Information Message");
                    }
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
        } else {
          alert('Please select a row first.', "Warning");
        }
});

$('#btn_add_principal').click(function(){
        const data = JSON.stringify({
            principle_id:$('#select_scope_registration_principals option:selected').val(),
            scopeDTO:getActiveScopeObject(),
            role:$('#select_scope_role option:selected').val()
        });
        const token = checkToken();
    $.ajax({
        url: link + '/api/scope/join',
        method: 'PUT',
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Content-Type' : 'application/json'
                },
        data: data,
        success: function (response) {
            // Do something with the result
            if (response.length > 0) {
            initializeScopes(response);
            global_scope = getActiveScopeObject();
            onInit();
            onInitCredentials();
            onInitServers();
            }
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
});