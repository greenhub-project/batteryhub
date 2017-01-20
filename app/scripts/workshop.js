const pager = document.getElementById('page-counter');
const num_slides = 15;
let currentSlide = 1;

window.onkeydown = e => doScrolling(e);

// Blocking the Mouse Wheel
/* IE7, IE8 */
window.onmousewheel = () => stopWheel()
/* Chrome, Safari, Firefox */
if (document.addEventListener) {
    document.addEventListener('DOMMouseScroll', stopWheel, false);
}

function animate(id, animation='fadeInUp') {
  document.getElementById(id).classList.add('animated', animation);
}

function animateWithDelay(id, animation='fadeInUp') {
  document.getElementById(id).classList.add('animated', animation, 'delayed');
}

function stopWheel(e) {
    if (!e) { e = window.event; } /* IE7, IE8, Chrome, Safari */
    if (e.preventDefault) { e.preventDefault(); } /* Chrome, Safari, Firefox */
    e.returnValue = false; /* IE7, IE8 */
}

function doScrolling(e) {
  if([32, 33, 34, 35, 36, 37, 38, 39, 40].indexOf(e.keyCode) > -1) {
    e.preventDefault();
  }
  if (e.keyCode === 40) {
    if (currentSlide === num_slides) return;
    currentSlide += 1;
  } else if (e.keyCode === 38) {
    if (currentSlide === 1) return;
    currentSlide -= 1;
  }
  pager.textContent = (currentSlide < 10) ? '0' + currentSlide : currentSlide;
  document.querySelector('#s' + currentSlide).scrollIntoView({ behavior: 'smooth' });
}

new Chart(document.getElementById('chart-devices'), {
    type: 'line',
    data: {
        labels: ['7 Jan', '8 Jan', '9 Jan', '10 Jan', '11 Jan', '12 Jan', '13 Jan', '14 Jan', '15 Jan', '16 Jan', '17 Jan', '18 Jan', '19 Jan'],
        datasets: [{
          label: '# Devices Installs per day',
          fill: true,
          lineTension: 0.1,
          backgroundColor: 'rgba(75,192,192,0.4)',
          borderColor: 'rgba(75,192,192,1)',
          borderCapStyle: 'butt',
          borderDash: [],
          borderDashOffset: 0.0,
          borderJoinStyle: 'miter',
          pointBorderColor: 'rgba(75,192,192,1)',
          pointBackgroundColor: '#fff',
          pointBorderWidth: 1,
          pointHoverRadius: 5,
          pointHoverBackgroundColor: 'rgba(75,192,192,1)',
          pointHoverBorderColor: 'rgba(220,220,220,1)',
          pointHoverBorderWidth: 2,
          pointRadius: 1,
          pointHitRadius: 10,
          data: [1, 6, 1, 11, 13, 11, 1, 0, 0, 0, 0, 0, 0],
          spanGaps: false,
        }]
    }
});

new Chart(document.getElementById('chart-samples'), {
    type: 'line',
    data: {
        labels: ['7 Jan', '8 Jan', '9 Jan', '10 Jan', '11 Jan', '12 Jan', '13 Jan', '14 Jan', '15 Jan', '16 Jan', '17 Jan', '18 Jan', '19 Jan'],
        datasets: [{
          label: '# Samples uploaded per day',
          fill: true,
          lineTension: 0.1,
          backgroundColor: 'rgba(75,192,192,0.4)',
          borderColor: 'rgba(75,192,192,1)',
          borderCapStyle: 'butt',
          borderDash: [],
          borderDashOffset: 0.0,
          borderJoinStyle: 'miter',
          pointBorderColor: 'rgba(75,192,192,1)',
          pointBackgroundColor: '#fff',
          pointBorderWidth: 1,
          pointHoverRadius: 5,
          pointHoverBackgroundColor: 'rgba(75,192,192,1)',
          pointHoverBorderColor: 'rgba(220,220,220,1)',
          pointHoverBorderWidth: 2,
          pointRadius: 1,
          pointHitRadius: 10,
          data: [0, 24, 145, 685, 1002, 1113, 502, 1174, 432, 839, 854, 766, 588],
          spanGaps: false,
        }]
    }
});

new Waypoint({
    element: document.getElementById('s4'),
    handler: function() {
      animate('img-workflow');
    },
    offset: '10%'
});

new Waypoint({
    element: document.getElementById('s6'),
    handler: function() {
      animate('greenhub-home');
    },
    offset: '10%'
});

new Waypoint({
    element: document.getElementById('s7'),
    handler: function() {
      animate('greenhub-device');
    },
    offset: '10%'
});

new Waypoint({
    element: document.getElementById('s8'),
    handler: function() {
      animate('greenhub-statistics');
    },
    offset: '10%'
});

new Waypoint({
    element: document.getElementById('s9'),
    handler: function() {
      animate('greenhub-settings');
    },
    offset: '10%'
});

new Waypoint({
    element: document.getElementById('s10'),
    handler: function() {
      animate('greenhub-more');
    },
    offset: '10%'
});
