-- Go Over There - LÖVE port of the pygame move_towards demo.
--
-- Mouse: click to set a new target position.
-- Keys:  R = restart, ESC = quit.

local MIN_SPEED = 0.25
local MAX_SPEED = 5
local SCREEN_W, SCREEN_H = 1000, 600
local CIRCLE_RADIUS = 5

local BG_COLOR = { 31 / 255, 143 / 255, 65 / 255 }
local BALL_COLOR = { 118 / 255, 207 / 255, 145 / 255 }

local state = "prompt"   -- "prompt" (asking for ball count) or "running"
local input_text = ""
local max_balls = 0
local balls = {}
local target = nil

-- Equivalent of pygame's Vector2.move_towards_ip: steps toward the target
-- by at most max_dist and never overshoots.
local function move_towards(b, tx, ty, max_dist)
    local dx, dy = tx - b.x, ty - b.y
    local dist = math.sqrt(dx * dx + dy * dy)
    if dist == 0 or dist <= max_dist then
        b.x, b.y = tx, ty
    else
        b.x = b.x + dx / dist * max_dist
        b.y = b.y + dy / dist * max_dist
    end
end

local function reset()
    target = nil
    balls = {}
    for _ = 1, max_balls do
        balls[#balls + 1] = {
            x = love.math.random(0, SCREEN_W),
            y = love.math.random(0, SCREEN_H),
            speed = MIN_SPEED + love.math.random() * (MAX_SPEED - MIN_SPEED),
        }
    end
end

local function start(n)
    max_balls = n
    state = "running"
    reset()
end

function love.load(args)
    -- `love . 500` (or `go_over_there.exe 500`) skips the prompt
    local n = tonumber(args and args[1])
    if n and n >= 0 then
        start(math.floor(n))
    end
end

function love.textinput(t)
    if state == "prompt" and t:match("^%d$") and #input_text < 7 then
        input_text = input_text .. t
    end
end

function love.keypressed(key)
    if state == "prompt" then
        if key == "backspace" then
            input_text = input_text:sub(1, -2)
        elseif (key == "return" or key == "kpenter") and #input_text > 0 then
            start(tonumber(input_text))
        end
    end
end

function love.keyreleased(key)
    if key == "escape" then
        love.event.quit()
    elseif key == "r" and state == "running" then
        reset()
    end
end

function love.mousereleased(x, y, button)
    if state == "running" and button == 1 then
        target = { x = x, y = y }
    end
end

function love.update(dt)
    if state ~= "running" then return end
    local delta_ms = dt * 1000  -- milliseconds, like pygame's clock.tick()
    if target then
        for _, b in ipairs(balls) do
            move_towards(b, target.x, target.y, b.speed * delta_ms)
        end
    end
    love.window.setTitle(string.format("fps: %.2f, ball count: %d", love.timer.getFPS(), #balls))
end

function love.draw()
    love.graphics.clear(BG_COLOR)
    if state == "prompt" then
        love.graphics.setColor(1, 1, 1)
        love.graphics.printf("Enter the number of balls to add to the pit:", 0, 240, SCREEN_W, "center")
        love.graphics.printf(input_text .. "_", 0, 280, SCREEN_W, "center")
        love.graphics.printf("Press Enter to start", 0, 320, SCREEN_W, "center")
        return
    end
    love.graphics.setColor(BALL_COLOR)
    for _, b in ipairs(balls) do
        love.graphics.circle("fill", b.x, b.y, CIRCLE_RADIUS)
    end
    love.graphics.setColor(1, 1, 1)
end
