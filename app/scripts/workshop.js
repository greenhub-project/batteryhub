let currentSlide = 1;

window.onkeydown = e => doScrolling(e);

// Blocking the Mouse Wheel
/* IE7, IE8 */
window.onmousewheel = () => stopWheel()
/* Chrome, Safari, Firefox */
if (document.addEventListener) {
    document.addEventListener('DOMMouseScroll', stopWheel, false);
}

function stopWheel(e) {
    if (!e) { e = window.event; } /* IE7, IE8, Chrome, Safari */
    if (e.preventDefault) { e.preventDefault(); } /* Chrome, Safari, Firefox */
    e.returnValue = false; /* IE7, IE8 */
}

function doScrolling(e) {
  if([32, 37, 38, 39, 40].indexOf(e.keyCode) > -1) {
    e.preventDefault();
  }
  if (e.keyCode === 40) {
    if (currentSlide === 10) return;
    currentSlide += 1;
  } else if (e.keyCode === 38) {
    if (currentSlide === 1) return;
    currentSlide -= 1;
  }
  document.querySelector('#s' + currentSlide).scrollIntoView({ behavior: 'smooth' });
}
