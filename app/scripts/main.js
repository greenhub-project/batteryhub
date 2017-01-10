google.charts.load('current', {'packages':['gantt']});
google.charts.setOnLoadCallback(drawChart);

function daysToMilliseconds(days) {
      return days * 24 * 60 * 60 * 1000;
}

function drawChart() {

  var data = new google.visualization.DataTable();
  data.addColumn('string', 'Task ID');
  data.addColumn('string', 'Task Name');
  data.addColumn('string', 'Resource');
  data.addColumn('date', 'Start Date');
  data.addColumn('date', 'End Date');
  data.addColumn('number', 'Duration');
  data.addColumn('number', 'Percent Complete');
  data.addColumn('string', 'Dependencies');

  data.addRows([
    ['repo-docs', 'Repository documentation', 'docs',
      new Date(2017, 0, 1), null, daysToMilliseconds(30), 60, null],
    ['tests', 'Tests Implementation', 'testing',
      new Date(2017, 0, 1), null, daysToMilliseconds(30), 50, null],
    ['alpha', 'Alpha stage', 'alpha',
      new Date(2017, 0, 1), null, daysToMilliseconds(14), 65, null],
    ['beta', 'Beta stage', 'beta',
      new Date(2017, 0, 15), null, daysToMilliseconds(21), 0, 'alpha'],
    ['release-stable', 'Stable release', 'release',
      new Date(2017, 1, 5), null, daysToMilliseconds(7), 0, 'beta']
  ]);

  var options = {
    height: 260
  };

  var chart = new google.visualization.Gantt(document.getElementById('chart_roadmap'));

  chart.draw(data, options);
}
