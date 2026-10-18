// Go Over There - Go (Ebitengine) port of the pygame move_towards demo.
//
// Mouse: click to set a new target position.
// Keys:  R = restart, ESC = quit.
package main

import (
	"bufio"
	"fmt"
	"image/color"
	"math"
	"math/rand"
	"os"
	"strconv"
	"strings"
	"time"

	"github.com/hajimehoshi/ebiten/v2"
	"github.com/hajimehoshi/ebiten/v2/inpututil"
	"github.com/hajimehoshi/ebiten/v2/vector"
)

const (
	minSpeed     = 0.25
	maxSpeed     = 5.0
	screenWidth  = 1000
	screenHeight = 600
	circleRadius = 5.0
)

var (
	bgColor   = color.RGBA{31, 143, 65, 255}
	ballColor = color.RGBA{118, 207, 145, 255}
)

type Ball struct {
	X, Y  float64
	Speed float64
}

// MoveTowards is the equivalent of pygame's Vector2.move_towards_ip:
// it steps toward the target by at most maxDist and never overshoots.
func (b *Ball) MoveTowards(tx, ty, maxDist float64) {
	dx, dy := tx-b.X, ty-b.Y
	dist := math.Hypot(dx, dy)
	if dist == 0 || dist <= maxDist {
		b.X, b.Y = tx, ty
		return
	}
	b.X += dx / dist * maxDist
	b.Y += dy / dist * maxDist
}

type Game struct {
	maxBalls  int
	balls     []Ball
	hasTarget bool
	targetX   float64
	targetY   float64
	last      time.Time
}

func (g *Game) reset() {
	g.hasTarget = false
	g.balls = make([]Ball, g.maxBalls)
	for i := range g.balls {
		g.balls[i] = Ball{
			X:     float64(rand.Intn(screenWidth + 1)),
			Y:     float64(rand.Intn(screenHeight + 1)),
			Speed: minSpeed + rand.Float64()*(maxSpeed-minSpeed),
		}
	}
}

func (g *Game) Update() error {
	// delta time in milliseconds, like pygame's clock.tick()
	now := time.Now()
	deltaTime := float64(now.Sub(g.last).Microseconds()) / 1000.0
	g.last = now

	if inpututil.IsKeyJustReleased(ebiten.KeyEscape) {
		return ebiten.Termination
	}
	if inpututil.IsKeyJustReleased(ebiten.KeyR) {
		g.reset()
	}
	if inpututil.IsMouseButtonJustReleased(ebiten.MouseButtonLeft) {
		x, y := ebiten.CursorPosition()
		g.targetX, g.targetY = float64(x), float64(y)
		g.hasTarget = true
	}

	if g.hasTarget {
		for i := range g.balls {
			b := &g.balls[i]
			b.MoveTowards(g.targetX, g.targetY, b.Speed*deltaTime)
		}
	}

	ebiten.SetWindowTitle(fmt.Sprintf("fps: %.2f, ball count: %d", ebiten.ActualFPS(), len(g.balls)))
	return nil
}

func (g *Game) Draw(screen *ebiten.Image) {
	screen.Fill(bgColor)
	for _, b := range g.balls {
		vector.DrawFilledCircle(screen, float32(b.X), float32(b.Y), circleRadius, ballColor, true)
	}
}

func (g *Game) Layout(outsideWidth, outsideHeight int) (int, int) {
	return screenWidth, screenHeight
}

func askBallCount() int {
	// `go_over_there.exe 500` skips the prompt.
	if len(os.Args) > 1 {
		if n, err := strconv.Atoi(strings.TrimSpace(os.Args[1])); err == nil && n >= 0 {
			return n
		}
	}
	reader := bufio.NewReader(os.Stdin)
	for {
		fmt.Print("Enter the number of balls to add to the pit: ")
		line, err := reader.ReadString('\n')
		if n, convErr := strconv.Atoi(strings.TrimSpace(line)); convErr == nil && n >= 0 {
			return n
		}
		if err != nil {
			os.Exit(0)
		}
		fmt.Println("Please enter a non-negative whole number.")
	}
}

func main() {
	g := &Game{maxBalls: askBallCount(), last: time.Now()}
	g.reset()

	ebiten.SetWindowSize(screenWidth, screenHeight)
	ebiten.SetWindowTitle("Go Over There")
	if err := ebiten.RunGame(g); err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
}
