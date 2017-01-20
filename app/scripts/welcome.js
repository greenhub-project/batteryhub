function animate(id, animation='fadeInUp') {
  document.getElementById(id).classList.add('animated', animation)
}

function animateMany(className, animation='fadeInUp') {
  const items = document.getElementsByClassName(className)
  Array.from(items).forEach(
    child => child.classList.add('animated', animation, 'delayed')
  );
}

document.getElementById('scroll-down').onclick = function() {
  document.querySelector('#features').scrollIntoView({ behavior: 'smooth' });
}

new Waypoint({
    element: document.getElementById('features'),
    handler: function() {
      animate('greenhub-home');
      animateMany('feature-item');
    },
    offset: '30%'
});

new Waypoint({
    element: document.getElementById('details'),
    handler: function() {
      animate('greenhub-details', 'fadeInLeft');
    },
    offset: '30%'
});
