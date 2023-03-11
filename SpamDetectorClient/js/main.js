// TODO: onload function should retrieve the data needed to populate the UI

// When the document has loaded, execute the following code
$(document).ready(function() {
  // Call the RESTful server to retrieve data
  $.ajax({
    url: 'http://localhost:8080/spamDetector-1.0/api/spam',
    type: 'GET',
    dataType: 'json',
    success: function(data) {
      // Loop through the data and create a table row for each item
      $.each(data, function(index, item) {
        var tr = $('<tr>');
        tr.append('<td>' + item.filename + '</td>');
        tr.append('<td>' + item.spamProbability + '</td>');
        tr.append('<td>' + item.actualClass + '</td>');
        $('#resultsTable').append(tr);
      });
      
      // After populating the table, calculate the width of the header cells
      var table = document.querySelector('#resultsTable');
      var tbody = table.querySelector('tbody');
      var thead = table.querySelector('thead');
      var tr = thead.querySelector('tr');
      var ths = tr.querySelectorAll('th');
      var tds = tbody.querySelector('tr').querySelectorAll('td');
      for (var i = 0; i < ths.length; i++) {
        console.log(tds[i].offsetWidth);
        var width = tds[i].offsetWidth + 15;
        tds[i].style.width = width + 'px'
        ths[i].style.width = width + 'px';
      }

    },
    error: function() {
      alert('Error retrieving data');
    }
  });

  // Call the RESTful server to retrieve accuracy data
  $.ajax({
    url: 'http://localhost:8080/spamDetector-1.0/api/spam/accuracy',
    type: 'GET',
    dataType: 'json',
    success: function(data) {
      // Display accuracy data
      $('#accuracy').html(data.accuracy);
    },
    error: function() {
      alert('Error retrieving accuracy data');
    }
  });

  // Call the RESTful server to retrieve precision data
  $.ajax({
    url: 'http://localhost:8080/spamDetector-1.0/api/spam/precision',
    type: 'GET',
    dataType: 'json',
    success: function(data) {
      // Display precision data
      $('#precision').html(data.precision);
    },
    error: function() {
      alert('Error retrieving precision data');
    }
  });
});
