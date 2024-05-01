// All operations regarding the routesModify component.

var global_mode;
var global_route_id;

function onInitRoutesModifyComponent(mode, routeId) {
    global_mode = mode;
    global_route_id = routeId;
    initializeForm();
    initializeTabs();
}

function initializeTabs() {
    const token = checkToken();
    if (global_mode == "edit") {
        $('#tab_in_progress').show();
        $('#tab_progress_events').show();
        // Just making sure that we are not trying to get data when the route does not exist.
        $.ajax({
            url: link + '/api/inprogress/' + global_route_id,
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token
            },
            success: function (response) {
                var inProgressTable = $('#inProgressOverviewTable').DataTable({
                    data: response,
                    select: true,
                    destroy: true,
                    columns: [
                        {"data": "id"},
                        {"data": "transfer"},
                        {"data": "file"},
                        {"data": "processed_at"}
                    ]
                });
                inProgressTable.on('select', function (e, dt, type, indexes) {
                    if (type === 'row') {
                        const routeId = inProgressTable.rows(indexes).data().pluck('id')[0];
                        const file = inProgressTable.rows(indexes).data().pluck('file')[0];
                        console.log(routeId + '/' + file);
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
        $.ajax({
            url: link + '/api/progressevent/' + global_route_id,
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token
            },
            success: function (response) {
                var progressEventTable = $('#progressEventOverviewTable').DataTable({
                    data: response,
                    select: true,
                    destroy: true,
                    columns: [
                        {"data": "caseNumber"},
                        {"data": "transfer"},
                        {"data": "fromHost"},
                        {"data": "toHost"},
                        {"data": "file"},
                        {"data": "progressType"},
                        {"data": "exception_message"},
                        {"data": "processed_at"}
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
    } else {
        $('#tab_in_progress').hide();
        $('#tab_progress_events').hide();
    }
}

function initializeForm() {
    // Needs to load all standard data like credentials and protocols
    // In the request, upon success it needs to load the load transferrecord method.
    const token = checkToken();
    const id = global_scope.id;
    $('#route_name').prop('disabled', global_mode == 'edit');
    $.ajax({
        url: link + '/api/credential/scope/'+id,
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function (response) {
            
            initializeCredentials(response);
            $.ajax({
                url: link + '/api/host/scope/'+id,
                method: 'GET',
                headers: {
                    'Authorization': 'Bearer ' + token
                },
                success: function (response) {
                    initializeServers(response);
                    if (global_mode == 'edit') {
                        // We are editing the transfer. So we need the information of this route.
                        // TODO
                        $('#routesModifyTitle').text('Editing transfer');
                        loadTransferRecord();
                    } else {
                        // We are generating a new transfer. So we need to have an empty form.
                        $('#routesModifyTitle').text('Adding new transfer');
                        $(".form-control").each(function () {
                            $(this).val("");
                        })
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

function initializeCredentials(credentialList) {
    $('#source_select_credential').empty();
    $('#destination_select_credential').empty();
    $('#source_select_credential').append(new Option("", null));
    $('#destination_select_credential').append(new Option("", null));
    for (var i in credentialList) {
        $('#source_select_credential').append(new Option(credentialList[i].username, credentialList[i].id));
        $('#destination_select_credential').append(new Option(credentialList[i].username, credentialList[i].id));
    }
}

function initializeServers(serverList) {
    $('#source_select_host').empty();
    $('#destination_select_host').empty();
    $('#source_select_host').append(new Option("", null));
    $('#destination_select_host').append(new Option("", null));
    for (var i in serverList) {
        $('#source_select_host').append(new Option(serverList[i].hostname, serverList[i].id));
        $('#destination_select_host').append(new Option(serverList[i].hostname, serverList[i].id));
    }
}

function loadTransferRecord() {
    const token = checkToken();
    $.ajax({
                url: link + '/api/transfer/' + global_route_id,
                method: 'GET',
                headers: {
                    'Authorization': 'Bearer ' + token
                },
                success: function (response) {
                    $('#route_name').val(response.id);
                    $('#route_description').val(response.description);
                    $('#route_status').val(response.status);
                    initializeEndpoints(response.endpoints);
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

function endpointToForm(endpoint) {
    const direction = endpoint.direction == 'FROM' ? 'source' : 'destination';
    $('#' + direction + '_select_protocol').val(endpoint.protocol);
    $('#' + direction + '_select_host').val(endpoint.host);
    $('#' + direction + '_select_host').prop('data-param', endpoint.id);
    $('#' + direction + '_input_path').val(endpoint.path);
    $('#' + direction + '_select_credential').val(endpoint.credential != null ? endpoint.credential.id : null);
    $('#' + direction + '_input_parameters').val(endpoint.parameter != null ? endpoint.parameter : null);
}

function initializeEndpoints(endpointList) {
    // Will fill the endpoint specific elements in a route in the form.
    // Needs to be called by a load transferrecord method.
    for (var i in endpointList) {
        endpointToForm({
            id: endpointList[i].id,
            protocol: endpointList[i].protocol,
            host: endpointList[i].host.id,
            path: endpointList[i].path,
            direction: endpointList[i].direction,
            parameter: endpointList[i].parameter,
            credential: endpointList[i].credential
        });

    }

}

function onSave() {
    endpointList = [];
    endpointList.push(formToEnpoint('FROM'));
    endpointList.push(formToEnpoint('TO'));
    toApiService({
        id: $('#route_name').val(),
        description: $('#route_description').val(),
        status: $('#route_status').val(),
        scope: global_scope,
        endpoints: endpointList
    });
}

function toApiService(transferRecord) {
    const token = checkToken();
    data = JSON.stringify(transferRecord)
    createOrUpdate = global_mode == 'edit' ? 'update' : 'create';
    $.ajax({
        url: link + '/api/transfer/' + createOrUpdate,
        headers: {
                    'Authorization': 'Bearer ' + token
                },
        type: global_mode == 'edit' ? 'PUT' : 'POST',
        contentType: "application/json",
        dataType: "json",
        data: data,
        success: function (response) {
            // Do something with the result
            alert(global_mode == 'edit' ? 'Your transfer has been updated.' : 'Your transfer has been created.', "Information Message");
            $('#routesModify').modal('hide');
            onInit();
        },
        error: function(jqXHR, textStatus, errorThrown) {
        // Check the status code and take appropriate actions
        if (jqXHR.status === 401) {
            // Handle 401 Unauthorized error
            alert(jqXHR.responseText, "Unauthorized");
            //window.location.href = 'login.html';
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

function formToEnpoint(direction_input) {
    const direction = direction_input == 'FROM' ? 'source' : 'destination';
    return {
        protocol: $('#' + direction + '_select_protocol').val(),
        host: $('#' + direction + '_select_host').val() == null ? null : {id: parseInt($('#' + direction + '_select_host').val())},
        port: parseInt($('#' + direction + '_input_port').val()) == 0 ? null : parseInt($('#' + direction + '_input_port').val()),
        path: $('#' + direction + '_input_path').val() == "" ? null : $('#' + direction + '_input_path').val(),
        direction: direction_input,
        parameter: $('#' + direction + '_input_parameters').val() == "" ? null : $('#' + direction + '_input_parameters').val(),
        credentialDTO: $('#' + direction + '_select_credential').val() == null ? null : {id: parseInt($('#' + direction + '_select_credential').val())}
    };
}

function removeInProgressRepo(id, file) {
    $.DELETE(link + '/api/inprogress/' + id + '/' + file).done(function (response) {

    })
}


$('#btn-save').click(function () {
    onSave();
});