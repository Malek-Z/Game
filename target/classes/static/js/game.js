// ── CONFIG ──────────────────────────────────────────────────────────
// INIT_BOARD, INIT_SCORE, INIT_SIZE, INIT_TARGET, PLAYER_BEST
// are injected by Thymeleaf in game.html before this script runs.

const LEVEL_NAMES = {
	"3-512": "🟢 Easy  3×3 · 512",
	"4-2048": "🟡 Medium 4×4 · 2048",
	"5-4096": "🔴 Hard  5×5 · 4096",
};

const EMOJI_MAP = {
	2: "🌱",
	4: "🌿",
	8: "🌳",
	16: "🦋",
	32: "🌸",
	64: "🔥",
	128: "⭐",
	256: "💎",
	512: "🌙",
	1024: "🏆",
	2048: "👑",
	4096: "🌌",
};

const CELL_SIZE = { 3: 104, 4: 90, 5: 72 };
const EMOJI_SIZE = { 3: "1.7rem", 4: "1.5rem", 5: "1.15rem" };
const NUM_SIZE = { 3: ".95rem", 4: ".85rem", 5: ".72rem" };

// ── STATE (seeded from server via Thymeleaf) ─────────────────────────
let board = parseBoard(INIT_BOARD, INIT_SIZE);
let score = INIT_SCORE;
let best = PLAYER_BEST;
let size = INIT_SIZE;
let goal = INIT_TARGET;
let dead = false;
let cont = false;
let busy = false; // prevent double-sends during fetch

// ── PARSE board string → 2D array ────────────────────────────────────
function parseBoard(state, sz) {
	const vals = state.split(",").map(Number);
	const grid = [];
	for (let r = 0; r < sz; r++) {
		grid.push(vals.slice(r * sz, (r + 1) * sz));
	}
	return grid;
}

// ── MOVE (calls backend) ──────────────────────────────────────────────
async function handleMove(dir) {
	if (busy || dead) return;
	busy = true;

	try {
		const resp = await fetch("/move", {
			method: "POST",
			headers: { "Content-Type": "application/x-www-form-urlencoded" },
			body: "direction=" + encodeURIComponent(dir),
		});

		if (!resp.ok) {
			busy = false;
			return;
		}

		const data = await resp.json();

		board = parseBoard(data.boardState, size);
		score = data.score;
		if (score > best) best = score;

		updateUI();
		render();

		if (data.win && !cont) {
			showOv(true);
		} else if (data.gameOver) {
			dead = true;
			showOv(false);
		}
	} catch (err) {
		notify("⚠ Connection error", "#e94560");
	}

	busy = false;
}

// ── SAVE & REDIRECT to menu ───────────────────────────────────────────
async function saveAndMenu() {
	notify("💾 Saving…", "#43e97b");
	try {
		const resp = await fetch("/save", { method: "POST" });
		if (resp.redirected || resp.ok) {
			window.location.href = "/menu";
		}
	} catch {
		window.location.href = "/menu";
	}
}

// ── NEW GAME (redirect to menu so player can pick level) ──────────────
function confirmNew() {
	window.location.href = "/menu";
}

// ── CONTINUE after win ────────────────────────────────────────────────
function continueGame() {
	cont = true;
	hideOv();
}

// ── OVERLAY ───────────────────────────────────────────────────────────
function showOv(win) {
	document.getElementById("ovEm").textContent = win ? "🏆" : "💀";
	document.getElementById("ovTitle").textContent = win ? "You Win!" : "Game Over";
	document.getElementById("ovMsg").textContent = win ? `You reached ${goal}! Score: ${score}` : `Final Score: ${score}`;
	document.getElementById("ov").classList.add("visible");
}
function hideOv() {
	document.getElementById("ov").classList.remove("visible");
}

// ── RENDER ────────────────────────────────────────────────────────────
function tileClass(v) {
	if (!v) return "";
	return v <= 4096 ? "t" + v : "thigh";
}

function render() {
	const g = document.getElementById("grid");
	const cs = CELL_SIZE[size] || 80;

	// Set a consistent gap based on size
	const gap = size === 3 ? 14 : size === 4 ? 10 : 8;

	g.style.gridTemplateColumns = `repeat(${size}, ${cs}px)`;
	g.style.gridTemplateRows = `repeat(${size}, ${cs}px)`;
	g.style.gap = `${gap}px`;
	g.innerHTML = "";

	for (let r = 0; r < size; r++) {
		for (let c = 0; c < size; c++) {
			const cell = document.createElement("div");
			cell.className = "cell";
			cell.style.width = cs + "px";
			cell.style.height = cs + "px";

			const v = board[r][c];
			if (v) {
				const tile = document.createElement("div");
				// The 'merged' class is already in your CSS, we'll use popIn by default
				tile.className = "tile " + tileClass(v);

				const em = document.createElement("div");
				em.className = "tile-emoji"; // Added class for CSS control
				em.style.fontSize = EMOJI_SIZE[size];
				em.style.filter = "drop-shadow(0 2px 4px rgba(0,0,0,0.5))";
				em.textContent = EMOJI_MAP[v] || "✨";

				const nm = document.createElement("div");
				nm.className = "tile-number"; // Added class for CSS control
				nm.style.fontSize = NUM_SIZE[size];
				nm.style.opacity = "0.9";
				nm.textContent = v;

				tile.appendChild(em);
				tile.appendChild(nm);
				cell.appendChild(tile);
			}
			g.appendChild(cell);
		}
	}
}

function updateUI() {
	document.getElementById("sc").textContent = score;
	document.getElementById("bs").textContent = best;
}

// ── LEVEL BADGE ───────────────────────────────────────────────────────
function setLevelBadge() {
	const key = size + "-" + goal;
	const el = document.getElementById("lvBadge");
	el.textContent = LEVEL_NAMES[key] || "🎮 " + size + "×" + size + " · " + goal;
}

// ── NOTIFY ────────────────────────────────────────────────────────────
function notify(msg, col) {
	const n = document.getElementById("notif");
	n.textContent = msg;
	n.style.background = col || "#43e97b";
	n.classList.add("show");
	setTimeout(() => n.classList.remove("show"), 2200);
}

// ── KEYBOARD ─────────────────────────────────────────────────────────
const KM = {
	ArrowLeft: "left",
	ArrowRight: "right",
	ArrowUp: "up",
	ArrowDown: "down",
	a: "left",
	d: "right",
	w: "up",
	s: "down",
};
document.addEventListener("keydown", (e) => {
	const d = KM[e.key];
	if (d) {
		e.preventDefault();
		handleMove(d);
	}
});

// ── SWIPE ─────────────────────────────────────────────────────────────
let tx = 0,
	ty = 0;
const gw = document.getElementById("gw");
gw.addEventListener(
	"touchstart",
	(e) => {
		tx = e.touches[0].clientX;
		ty = e.touches[0].clientY;
	},
	{ passive: true },
);
gw.addEventListener(
	"touchend",
	(e) => {
		const dx = e.changedTouches[0].clientX - tx;
		const dy = e.changedTouches[0].clientY - ty;
		if (Math.abs(dx) < 18 && Math.abs(dy) < 18) return;
		handleMove(Math.abs(dx) > Math.abs(dy) ? (dx > 0 ? "right" : "left") : dy > 0 ? "down" : "up");
	},
	{ passive: true },
);

// ── BOOT ─────────────────────────────────────────────────────────────
setLevelBadge();
updateUI();
render();
