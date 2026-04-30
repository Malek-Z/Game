// ── CONFIG ──────────────────────────────────────────────────────────
const LEVELS = {
	1: { size: 3, goal: 1024, name: "Easy" },
	2: { size: 4, goal: 2048, name: "Medium" },
	3: { size: 5, goal: 4096, name: "Hard" },
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
const FONT_SIZE = { 3: "1.5rem", 4: "1.3rem", 5: "1.05rem" };
const EMOJI_SIZE = { 3: "1.7rem", 4: "1.5rem", 5: "1.15rem" };
const NUM_SIZE = { 3: ".95rem", 4: ".85rem", 5: ".72rem" };

// ── STATE ────────────────────────────────────────────────────────────
let level = 2,
	size = 4,
	goal = 2048;
let board = [],
	score = 0,
	best = 0,
	moves = 0;
let dead = false,
	won = false,
	cont = false;
let lb = JSON.parse(localStorage.getItem("lb48") || "[]");
let saves = JSON.parse(localStorage.getItem("sv48") || "{}");

// ── LEVEL ────────────────────────────────────────────────────────────
function setLevel(lv) {
	level = lv;
	size = LEVELS[lv].size;
	goal = LEVELS[lv].goal;
	document.querySelectorAll(".lvl").forEach((b) => b.classList.remove("active"));
	document.getElementById("lv" + lv).classList.add("active");
	newGame();
}

// ── NEW GAME ─────────────────────────────────────────────────────────
function newGame() {
	board = Array.from({ length: size }, () => Array(size).fill(0));
	score = 0;
	moves = 0;
	dead = false;
	won = false;
	cont = false;
	hideOv();
	spawnTile();
	spawnTile();
	updateUI();
	render();
}

// ── SPAWN ─────────────────────────────────────────────────────────────
function spawnTile() {
	const e = [];
	for (let r = 0; r < size; r++) for (let c = 0; c < size; c++) if (!board[r][c]) e.push([r, c]);
	if (!e.length) return;
	const [r, c] = e[Math.floor(Math.random() * e.length)];
	board[r][c] = Math.random() < 0.88 ? 2 : 4;
}

// ── SLIDE ─────────────────────────────────────────────────────────────
function slideRow(row) {
	let a = row.filter((x) => x),
		add = 0;
	for (let i = 0; i < a.length - 1; i++)
		if (a[i] === a[i + 1]) {
			a[i] *= 2;
			add += a[i];
			a.splice(i + 1, 1);
		}
	while (a.length < size) a.push(0);
	return { a, add };
}

// ── MOVE ──────────────────────────────────────────────────────────────
function handleMove(dir) {
	if (dead) return;
	let moved = false,
		gained = 0;
	const nb = board.map((r) => [...r]);

	if (dir === "left" || dir === "right") {
		for (let r = 0; r < size; r++) {
			const row = dir === "right" ? [...nb[r]].reverse() : nb[r];
			const { a, add } = slideRow(row);
			const res = dir === "right" ? a.reverse() : a;
			if (!res.every((v, i) => v === nb[r][i])) moved = true;
			nb[r] = res;
			gained += add;
		}
	} else {
		for (let c = 0; c < size; c++) {
			let col = nb.map((r) => r[c]);
			if (dir === "down") col.reverse();
			const { a, add } = slideRow(col);
			if (dir === "down") a.reverse();
			if (!a.every((v, i) => v === nb[i][c])) moved = true;
			for (let r = 0; r < size; r++) nb[r][c] = a[r];
			gained += add;
		}
	}

	if (!moved) {
		checkLoss();
		return;
	}
	board = nb;
	score += gained;
	if (score > best) best = score;
	moves++;
	spawnTile();
	updateUI();
	render();
	if (!cont) checkWin();
	else checkLoss();
}

// ── WIN / LOSS ────────────────────────────────────────────────────────
function checkWin() {
	for (let r = 0; r < size; r++)
		for (let c = 0; c < size; c++)
			if (board[r][c] >= goal) {
				showOv(true);
				return;
			}
	checkLoss();
}
function checkLoss() {
	for (let r = 0; r < size; r++) for (let c = 0; c < size; c++) if (!board[r][c]) return;
	for (let r = 0; r < size; r++)
		for (let c = 0; c < size; c++) {
			if (c < size - 1 && board[r][c] === board[r][c + 1]) return;
			if (r < size - 1 && board[r][c] === board[r + 1][c]) return;
		}
	dead = true;
	showOv(false);
}
function showOv(win) {
	won = win;
	addLB();
	document.getElementById("ovEm").textContent = win ? "🏆" : "💀";
	document.getElementById("ovTitle").textContent = win ? "You Win!" : "Game Over";
	document.getElementById("ovMsg").textContent = win ? `You reached ${goal}! Score: ${score}` : `Score: ${score}`;
	document.getElementById("ov").classList.add("visible");
}
function hideOv() {
	document.getElementById("ov").classList.remove("visible");
}
function continueGame() {
	cont = true;
	hideOv();
}

// ── RENDER ────────────────────────────────────────────────────────────
function tileClass(v) {
	if (!v) return "";
	return v <= 4096 ? "t" + v : "thigh";
}

function render() {
	const g = document.getElementById("grid");
	const cs = CELL_SIZE[size] || 80;
	g.style.gridTemplateColumns = `repeat(${size},${cs}px)`;
	g.style.gridTemplateRows = `repeat(${size},${cs}px)`;
	g.innerHTML = "";
	for (let r = 0; r < size; r++)
		for (let c = 0; c < size; c++) {
			const cell = document.createElement("div");
			cell.className = "cell";
			cell.style.width = cs + "px";
			cell.style.height = cs + "px";
			const v = board[r][c];
			if (v) {
				const t = document.createElement("div");
				t.className = "tile " + tileClass(v);

				const em = document.createElement("div");
				em.style.fontSize = EMOJI_SIZE[size];
				em.textContent = EMOJI_MAP[v] || "✨";

				const nm = document.createElement("div");
				nm.style.fontSize = NUM_SIZE[size];
				nm.style.fontWeight = "900";
				nm.textContent = v;

				t.appendChild(em);
				t.appendChild(nm);
				cell.appendChild(t);
			}
			g.appendChild(cell);
		}
}

function updateUI() {
	document.getElementById("sc").textContent = score;
	document.getElementById("bs").textContent = best;
	document.getElementById("mv").textContent = moves;
}

// ── SAVE / LOAD ───────────────────────────────────────────────────────
function saveGame() {
	const n = pname();
	saves[n] = { board: JSON.parse(JSON.stringify(board)), score, best, moves, level, size, goal, at: new Date().toLocaleTimeString() };
	localStorage.setItem("sv48", JSON.stringify(saves));
	notify("✓ Game Saved!", "#43e97b");
}
function loadGame() {
	const n = pname();
	const s = saves[n];
	if (!s) {
		notify("⚠ No save found!", "#e94560");
		return;
	}
	({ board: board, score: score, best: best, moves: moves, level: level, size: size, goal: goal } = s);
	dead = false;
	won = false;
	cont = false;
	document.querySelectorAll(".lvl").forEach((b) => b.classList.remove("active"));
	document.getElementById("lv" + level).classList.add("active");
	hideOv();
	updateUI();
	render();
	notify("📂 Loaded: " + s.at, "#43e97b");
}
function pname() {
	return document.getElementById("pname").value.trim() || "Player1";
}

// ── LEADERBOARD ───────────────────────────────────────────────────────
function addLB() {
	const n = pname();
	const e = lb.find((x) => x.n === n);
	if (e) {
		if (score > e.s) e.s = score;
	} else lb.push({ n, s: score, lv: LEVELS[level].name });
	lb.sort((a, b) => b.s - a.s);
	lb = lb.slice(0, 8);
	localStorage.setItem("lb48", JSON.stringify(lb));
	renderLB();
}
function renderLB() {
	const el = document.getElementById("lb");
	if (!lb.length) {
		el.innerHTML = '<div style="color:#444;font-size:.8rem">No scores yet.</div>';
		return;
	}
	const medals = ["🥇", "🥈", "🥉"];
	el.innerHTML = lb
		.map(
			(e, i) =>
				`<div class="lb-row"><span class="rk">${medals[i] || i + 1}</span><span class="nm">${e.n}</span><span class="lv">[${e.lv || ""}]</span><span class="sc">${e.s}</span></div>`,
		)
		.join("");
}

// ── NOTIFY ───────────────────────────────────────────────────────────
function notify(msg, col = "#43e97b") {
	const n = document.getElementById("notif");
	n.textContent = msg;
	n.style.background = col;
	n.classList.add("show");
	setTimeout(() => n.classList.remove("show"), 2200);
}

// ── KEYBOARD ─────────────────────────────────────────────────────────
const KM = { ArrowLeft: "left", ArrowRight: "right", ArrowUp: "up", ArrowDown: "down", a: "left", d: "right", w: "up", s: "down" };
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
renderLB();
newGame();
