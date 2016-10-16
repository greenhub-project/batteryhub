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
      new Date(2016, 9, 1), null, daysToMilliseconds(21), 30, null],
    ['workflow', 'Workflow', 'task',
      new Date(2016, 9, 1), null, daysToMilliseconds(14), 75, null],
    ['travisCI', 'Continuous integration', 'tool',
      new Date(2016, 9, 1), null, daysToMilliseconds(7), 100, null],
    ['docker', 'Docker containerization', 'tool',
      null, null, daysToMilliseconds(7), 75, 'travisCI'],
    ['tests', 'Tests Implementation', 'testing',
      new Date(2016, 9, 15), null, daysToMilliseconds(30), 10, null],
    ['code-guidelines', 'Change project guidelines', 'docs',
      new Date(2016, 9, 15), null, daysToMilliseconds(14), 5, null],
    ['retrofit', 'Migrate HTTP library', 'development',
      null, null, daysToMilliseconds(7), 0, 'code-guidelines'],
    ['alpha', 'Alpha stage', 'alpha',
      new Date(2016, 10, 5), null, daysToMilliseconds(14), 0, null],
    ['beta', 'Beta stage', 'beta',
      new Date(2016, 10, 19), null, daysToMilliseconds(30), 0, 'alpha'],
    ['release-stable', 'Stable release', 'release',
      new Date(2016, 11, 19), null, daysToMilliseconds(7), 0, 'beta']
  ]);

  var options = {
    height: 500
  };

  var chart = new google.visualization.Gantt(document.getElementById('chart_roadmap'));

  chart.draw(data, options);
}
