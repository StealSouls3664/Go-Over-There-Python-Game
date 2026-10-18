//! Go Over There - Rust port of the pygame move_towards demo.
//!
//! Mouse: click to set a new target position.
//! Keys:  R = restart, ESC = quit.

use macroquad::prelude::*;
use std::io::{self, Write};

const MIN_SPEED: f32 = 0.25;
const MAX_SPEED: f32 = 5.0;
const SCREEN_WIDTH: i32 = 1000;
const SCREEN_HEIGHT: i32 = 600;
const CIRCLE_RADIUS: f32 = 5.0;

struct Ball {
    position: Vec2,
    speed: f32,
}

impl Ball {
    /// Equivalent of pygame's `Vector2.move_towards_ip`: steps toward the
    /// target by at most `max_distance` and never overshoots.
    fn move_towards(&mut self, target: Vec2, max_distance: f32) {
        let delta = target - self.position;
        let dist = delta.length();
        if dist == 0.0 || dist <= max_distance {
            self.position = target;
        } else {
            self.position += delta / dist * max_distance;
        }
    }
}

fn ask_ball_count() -> usize {
    // Allow `go_over_there 500` to skip the prompt.
    if let Some(arg) = std::env::args().nth(1) {
        if let Ok(n) = arg.trim().parse::<usize>() {
            return n;
        }
    }
    loop {
        print!("Enter the number of balls to add to the pit: ");
        io::stdout().flush().ok();
        let mut line = String::new();
        if io::stdin().read_line(&mut line).is_err() {
            continue;
        }
        match line.trim().parse::<usize>() {
            Ok(n) => return n,
            Err(_) => println!("Please enter a non-negative whole number."),
        }
    }
}

fn reset(max_balls: usize) -> Vec<Ball> {
    (0..max_balls)
        .map(|_| Ball {
            position: vec2(
                rand::gen_range(0, SCREEN_WIDTH + 1) as f32,
                rand::gen_range(0, SCREEN_HEIGHT + 1) as f32,
            ),
            speed: rand::gen_range(MIN_SPEED, MAX_SPEED),
        })
        .collect()
}

fn window_conf() -> Conf {
    Conf {
        window_title: "Go Over There".to_owned(),
        window_width: SCREEN_WIDTH,
        window_height: SCREEN_HEIGHT,
        window_resizable: false,
        ..Default::default()
    }
}

#[macroquad::main(window_conf)]
async fn main() {
    let max_balls = ask_ball_count();
    rand::srand(miniquad::date::now() as u64);

    let mut balls = reset(max_balls);
    let mut target: Option<Vec2> = None;

    let bg = Color::from_rgba(31, 143, 65, 255);
    let ball_color = Color::from_rgba(118, 207, 145, 255);

    let mut delta_time_ms = 0.0_f32; // milliseconds, like pygame's clock.tick()

    loop {
        if is_key_released(KeyCode::Escape) {
            break;
        }
        if is_key_released(KeyCode::R) {
            target = None;
            balls = reset(max_balls);
        }
        if is_mouse_button_released(MouseButton::Left) {
            let (x, y) = mouse_position();
            target = Some(vec2(x, y));
        }

        clear_background(bg);

        for b in balls.iter_mut() {
            if let Some(t) = target {
                b.move_towards(t, b.speed * delta_time_ms);
            }
            draw_circle(b.position.x, b.position.y, CIRCLE_RADIUS, ball_color);
        }

        draw_text(
            &format!("fps: {}, ball count: {}", get_fps(), balls.len()),
            10.0,
            22.0,
            24.0,
            WHITE,
        );

        next_frame().await;
        delta_time_ms = get_frame_time() * 1000.0;
    }
}
