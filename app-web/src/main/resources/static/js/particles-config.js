window.onload = function() {
  Particles.init({
    selector: '.background',
    maxParticles: 80,
    sizeVariations: 3,
    speed: 0.5,
    color: '#ffffff',
    minDistance: 120,
    connectParticles: true,
    responsive: [
      {
        breakpoint: 768,
        options: {
          maxParticles: 50
        }
      }, {
        breakpoint: 425,
        options: {
          maxParticles: 20
        }
      }, {
        breakpoint: 320,
        options: {
          maxParticles: 10
        }
      }
    ]
  });
};
