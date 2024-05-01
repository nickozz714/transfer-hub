function login() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    // Make an API request to your authentication endpoint
    // Use fetch or another AJAX library
    $.ajax({
        url: link+'/authentication/login',
        method: 'POST',
        headers: {'Content-Type' : 'application/json'},
        data: JSON.stringify({username:username, password:password}),
        success: function(data) {
            // Handle the response, which should contain the JWT token
            const token = data.token;
            alert(data.message, "Information Message");

            // Store the token securely (e.g., in localStorage or sessionStorage)
            localStorage.setItem('transfer-hub-token', token);

            window.location.href = 'module.html';
            // Now you can use this token for other API calls
            // Example: makeAPICallUsingToken(token);
        },
        error: function(jqXHR, textStatus, errorThrown) {
        // Check the status code and take appropriate actions
        if (jqXHR.status === 401) {
            // Handle 401 Unauthorized error
            alert(jqXHR.responseText, "Unauthorized");
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

function register() {
  const email = document.getElementById('registerEmail').value
  const username = document.getElementById('registerUsername').value;
  const password = document.getElementById('registerPassword').value;

  // Additional registration data can be collected similarly

  // Make an API request to your registration endpoint
      $.ajax({
        url: link+'/authentication/register',
        method: 'POST',
        headers: {'Content-Type' : 'application/json'},
        data: JSON.stringify({username:username, password:password, email:email}),
        success: function(data) {
            // Handle the response, which should contain the JWT token
            const token = data.token;
            alert(data.message, "Information Message");
            window.location.href = 'login.html';
            // Now you can use this token for other API calls
            // Example: makeAPICallUsingToken(token);
        },
        error: function(jqXHR, textStatus, errorThrown) {
        // Check the status code and take appropriate actions
        if (jqXHR.status === 401) {
            // Handle 401 Unauthorized error
            alert(jqXHR.responseText, "Unauthorized");
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