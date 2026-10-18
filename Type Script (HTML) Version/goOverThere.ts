/**
 * Go Over There - TypeScript port of the pygame move_towards demo.
 *
 * Mouse: click to set a new target position.
 * Keys:  R = restart, ESC = stop the demo.
 */

const MIN_SPEED = 0.25;
const MAX_SPEED = 5;
const WIDTH = 1000;
const HEIGHT = 600;
const CIRCLE_RADIUS = 5;
const BG_COLOR = "rgb(31, 143, 65)";
const BALL_COLOR = "rgb(118, 207, 145)";

interface Vec2 {
  x: number;
  y: number;
}

class Ball {
  constructor(public position: Vec2, public speed: number) {}

  // Equivalent of pygame's Vector2.move_towards_ip
  moveTowards(target: Vec2, maxDistance: number): void {
    const dx = target.x - this.position.x;
    const dy = target.y - this.position.y;
    const dist = Math.hypot(dx, dy);
    if (dist === 0 || dist <= maxDistance) {
      this.position.x = target.x;
      this.position.y = target.y;
    } else {
      this.position.x += (dx / dist) * maxDistance;
      this.position.y += (dy / dist) * maxDistance;
    }
  }
}

function askBallCount(): number {
  const fromUrl = new URLSearchParams(window.location.search).get("balls");
  let raw: string | null = fromUrl;
  while (true) {
    if (raw === null) {
      raw = window.prompt("Enter the number of balls to add to the pit:", "100");
    }
    if (raw === null) return 0;
    const n = Number.parseInt(raw.trim(), 10);
    if (Number.isInteger(n) && n >= 0) return n;
    raw = null;
  }
}

function main(): void {
  const canvas = document.getElementById("screen") as HTMLCanvasElement;
  const ctx = canvas.getContext("2d") as CanvasRenderingContext2D;
  canvas.width = WIDTH;
  canvas.height = HEIGHT;

  const maxBalls = askBallCount();
  let balls: Ball[] = [];
  let target: Vec2 | null = null;
  let running = true;

  function reset(): void {
    target = null;
    balls = [];
    for (let i = 0; i < maxBalls; i++) {
      const pos: Vec2 = {
        x: Math.floor(Math.random() * (WIDTH + 1)),
        y: Math.floor(Math.random() * (HEIGHT + 1)),
      };
      const speed = MIN_SPEED + Math.random() * (MAX_SPEED - MIN_SPEED);
      balls.push(new Ball(pos, speed));
    }
  }

  canvas.addEventListener("mouseup", (e: MouseEvent) => {
    const rect = canvas.getBoundingClientRect();
    target = {
      x: (e.clientX - rect.left) * (WIDTH / rect.width),
      y: (e.clientY - rect.top) * (HEIGHT / rect.height),
    };
  });

  window.addEventListener("keyup", (e: KeyboardEvent) => {
    if (e.key === "Escape") running = false;
    if (e.key === "r" || e.key === "R") reset();
  });

  reset();

  let deltaTime = 0; // milliseconds, like pygame's clock.tick()
  let last = performance.now();
  const recent: number[] = [];

  function frame(now: number): void {
    if (!running) return;

    deltaTime = now - last;
    last = now;
    recent.push(deltaTime);
    if (recent.length > 10) recent.shift();

    if (target !== null) {
      for (const b of balls) b.moveTowards(target, b.speed * deltaTime);
    }

    ctx.fillStyle = BG_COLOR;
    ctx.fillRect(0, 0, WIDTH, HEIGHT);
    ctx.fillStyle = BALL_COLOR;
    for (const b of balls) {
      ctx.beginPath();
      ctx.arc(b.position.x, b.position.y, CIRCLE_RADIUS, 0, Math.PI * 2);
      ctx.fill();
    }

    const avg = recent.reduce((a, b) => a + b, 0) / recent.length;
    const fps = avg > 0 ? 1000 / avg : 0;
    document.title = `fps: ${fps.toFixed(2)}, ball count: ${balls.length}`;

    requestAnimationFrame(frame);
  }

  requestAnimationFrame(frame);
}

main();
