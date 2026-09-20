/**
 * Fruit Sort 3D - Web Edition
 * Full recreated browser implementation of the Android Fruit Sort 3D Game.
 */

// ==========================================
// 1. SOUND & AUDIO SYNTHESIS (Web Audio API)
// ==========================================
class SoundSynth {
  constructor() {
    this.ctx = null;
    this.soundEnabled = true;
    this.musicEnabled = true;
  }

  init() {
    if (!this.ctx) {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (AudioCtx) {
        this.ctx = new AudioCtx();
      }
    }
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  }

  playTone(freq, type, duration, gainVal = 0.2) {
    if (!this.soundEnabled) return;
    this.init();
    if (!this.ctx) return;
    try {
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = type;
      osc.frequency.setValueAtTime(freq, this.ctx.currentTime);
      gain.gain.setValueAtTime(gainVal, this.ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + duration);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start();
      osc.stop(this.ctx.currentTime + duration);
    } catch (e) {
      console.warn(e);
    }
  }

  playClick() {
    this.playTone(600, 'sine', 0.06, 0.15);
  }

  playPick() {
    this.playTone(520, 'triangle', 0.1, 0.25);
  }

  playDrop() {
    this.playTone(380, 'sine', 0.12, 0.3);
  }

  playCorkPop() {
    if (!this.soundEnabled) return;
    this.init();
    if (!this.ctx) return;
    // Frequency sweep for pop sound
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(400, this.ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(880, this.ctx.currentTime + 0.15);
    gain.gain.setValueAtTime(0.35, this.ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.2);
    osc.connect(gain);
    gain.connect(this.ctx.destination);
    osc.start();
    osc.stop(this.ctx.currentTime + 0.2);
  }

  playCoin() {
    this.playTone(987.77, 'triangle', 0.08, 0.2);
    setTimeout(() => this.playTone(1318.51, 'triangle', 0.15, 0.2), 60);
  }

  playWinFanfare() {
    if (!this.soundEnabled) return;
    const notes = [523.25, 659.25, 783.99, 1046.50];
    notes.forEach((freq, idx) => {
      setTimeout(() => this.playTone(freq, 'triangle', 0.25, 0.3), idx * 120);
    });
  }

  playError() {
    this.playTone(200, 'sawtooth', 0.15, 0.25);
  }
}

const sound = new SoundSynth();

// ==========================================
// 2. DATA MODELS & CONSTANTS
// ==========================================
const FRUITS_DEF = {
  WATERMELON: { id: 'WATERMELON', name: 'Watermelon', emoji: '🍉', color: '#e91e63' },
  ORANGE:     { id: 'ORANGE',     name: 'Orange',     emoji: '🍊', color: '#ff9800' },
  APPLE:      { id: 'APPLE',      name: 'Apple',      emoji: '🍎', color: '#f44336' },
  LEMON:      { id: 'LEMON',      name: 'Lemon',      emoji: '🍋', color: '#fbc02d' },
  GRAPE:      { id: 'GRAPE',      name: 'Grape',      emoji: '🍇', color: '#9c27b0' },
  KIWI:       { id: 'KIWI',       name: 'Kiwi',       emoji: '🥝', color: '#8bc34a' },
  STRAWBERRY: { id: 'STRAWBERRY', name: 'Strawberry', emoji: '🍓', color: '#e53935' },
  BLUEBERRY:  { id: 'BLUEBERRY',  name: 'Blueberry',  emoji: '🫐', color: '#3f51b5' }
};

const ALL_FRUIT_KEYS = Object.keys(FRUITS_DEF);

const CONTAINER_STYLES = [
  { id: 'GLASS_JAR',      name: 'Classic Glass Jar',    emoji: '🏺', costCoins: 0,    desc: 'Crystal clear laboratory glass' },
  { id: 'GOLD_FLUTE',     name: 'Gold Crystal Flute',   emoji: '🏆', costCoins: 400,  desc: 'Polished royal gold trim' },
  { id: 'FRUIT_BASKET',   name: 'Woven Fruit Basket',   emoji: '🧺', costCoins: 750,  desc: 'Handmade rustic wicker' },
  { id: 'FRUIT_BOWL',     name: 'Ceramic Fruit Bowl',   emoji: '🥣', costCoins: 1200, desc: 'Smooth artisan porcelain' },
  { id: 'WOODEN_CRATE',   name: 'Wooden Orchard Crate', emoji: '📦', costCoins: 1600, desc: 'Rustic pine fruit crate' },
  { id: 'ICE_BOX',        name: 'Frosted Ice Box',      emoji: '🧊', costCoins: 2200, desc: 'Glacial chill sub-zero container' },
  { id: 'NEON_TUBE',      name: 'Neon Cyber Tube',      emoji: '⚡', costCoins: 3000, desc: 'Electrified sci-fi glow' },
  { id: 'BAMBOO_JAR',     name: 'Bamboo Zen Jar',       emoji: '🎋', costCoins: 3600, desc: 'Natural zen bamboo stalk' },
  { id: 'POTION_FLASK',   name: 'Arcane Potion Flask',  emoji: '🧪', costCoins: 4500, desc: 'Mystic bubbling magic flask' },
  { id: 'DIAMOND_VASE',   name: 'Diamond Royal Vase',   emoji: '💎', costCoins: 6000, desc: 'Faceted sparkling gemstone' }
];

const THEMES = [
  { id: 'neon-night',       name: 'Deep Cosmic',    emoji: '🌌', costCoins: 0,    desc: 'Midnight cosmic indigo gradient' },
  { id: 'sunset-bliss',     name: 'Sunset Orchard',  emoji: '🌅', costCoins: 500,  desc: 'Warm fiery twilight sunset' },
  { id: 'emerald-garden',   name: 'Emerald Jungle',  emoji: '🌿', costCoins: 800,  desc: 'Lush exotic rainforest canopy' },
  { id: 'cyber-grid',       name: 'Cyberpunk Neon',  emoji: '🏙️', costCoins: 1500, desc: 'Futuristic high-tech teal' },
  { id: 'golden-luxury',    name: 'Golden Mirage',   emoji: '👑', costCoins: 2500, desc: 'Opulent amber treasure chamber' },
  { id: 'ocean-breeze',     name: 'Oceanic Depths',  emoji: '🌊', costCoins: 3500, desc: 'Submerged deep sapphire sea' },
  { id: 'twilight-orchard', name: 'Mystic Twilight', emoji: '🔮', costCoins: 4200, desc: 'Enchanted amethyst glow' },
  { id: 'volcano-glow',     name: 'Volcanic Flare',  emoji: '🌋', costCoins: 5500, desc: 'Molten magma subterranean heat' }
];

const FRUIT_PACKS = [
  { id: 'CLASSIC_FRUITS', name: 'Juicy Classic',       emojis: '🍉 🍊 🍎 🍋 🍇 🥝 🍓 🫐', costCoins: 0,    desc: 'The original crisp orchard harvest' },
  { id: 'TROPICAL_ISLAND',name: 'Tropical Island',     emojis: '🥥 🍍 🥭 🍌 🥑 🍈 🍉 🍋', costCoins: 800,  desc: 'Exotic island palm delicacies' },
  { id: 'CITRUS_BERRY',   name: 'Exotic Citrus & Berry',emojis: '🍋 🍊 🍓 🫐 🍇 🍒 🍑 🍎', costCoins: 1400, desc: 'Tart berry burst & citrus slices' },
  { id: 'CANDY_GLAZE',    name: 'Candy Sweet Glaze',   emojis: '🍭 🍬 🍫 🧁 🍩 🍪 🍦 🍯', costCoins: 2400, desc: 'Decadent candy store delights' },
  { id: 'GOLDEN_HARVEST', name: 'Royal Golden Harvest',emojis: '🌟 👑 💎 💰 🏆 🪙 🏅 💍', costCoins: 4000, desc: 'Precious royal treasures' }
];

// ==========================================
// 3. PERSISTENT STORAGE
// ==========================================
const STORAGE_KEY = 'fruit_sort_3d_web_v1';

function getDefaultUserData() {
  return {
    coins: 250,
    gems: 10,
    highestLevelUnlocked: 1,
    levelStars: {},
    selectedStyle: 'GLASS_JAR',
    unlockedStyles: ['GLASS_JAR'],
    selectedTheme: 'neon-night',
    unlockedThemes: ['neon-night'],
    selectedPack: 'CLASSIC_FRUITS',
    unlockedPacks: ['CLASSIC_FRUITS'],
    powerUps: {
      undo: 3,
      hammer: 2,
      tube: 2,
      shuffle: 1,
      frost: 1
    },
    soundEnabled: true,
    musicEnabled: true,
    hapticEnabled: true,
    lastSpinDay: 0,
    streakDay: 1,
    claimedMilestones: []
  };
}

let userData = loadUserData();

function loadUserData() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      return Object.assign(getDefaultUserData(), JSON.parse(raw));
    }
  } catch (e) {
    console.error('Failed to read userData from localStorage', e);
  }
  return getDefaultUserData();
}

function saveUserData() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(userData));
    updateTopBarUI();
  } catch (e) {
    console.error('Failed to save userData to localStorage', e);
  }
}

// ==========================================
// 4. PROCEDURAL 1000 LEVEL GENERATOR
// ==========================================
function generateLevel(levelNum) {
  const isBoss = (levelNum % 25 === 0);
  
  // Scaling fruit count (2 up to 7 fruits)
  let fruitCount = 2;
  if (levelNum > 3) fruitCount = 3;
  if (levelNum > 15) fruitCount = 4;
  if (levelNum > 45) fruitCount = 5;
  if (levelNum > 120) fruitCount = 6;
  if (levelNum > 350) fruitCount = 7;

  // Empty tubes
  let emptyTubes = 2;
  if (levelNum > 50 && levelNum % 3 === 0) emptyTubes = 1;
  if (levelNum > 200 && levelNum % 2 === 0) emptyTubes = 1;

  const capacity = 4;
  const chosenFruits = ALL_FRUIT_KEYS.slice(0, fruitCount);
  
  // Solvable reverse shuffle algorithm
  const tubes = [];
  // Initially, each fruit has its own full tube
  chosenFruits.forEach((fKey) => {
    tubes.push({
      items: [fKey, fKey, fKey, fKey],
      isLocked: false,
      completed: false
    });
  });

  // Add empty tubes
  for (let i = 0; i < emptyTubes; i++) {
    tubes.push({
      items: [],
      isLocked: false,
      completed: false
    });
  }

  // Shuffle moves
  const shuffleMoves = Math.min(180, Math.max(12, 10 + Math.floor(levelNum * 0.4)));
  for (let m = 0; m < shuffleMoves; m++) {
    const fromIdx = Math.floor(Math.random() * tubes.length);
    const toIdx = Math.floor(Math.random() * tubes.length);
    if (fromIdx !== toIdx) {
      const fromTube = tubes[fromIdx];
      const toTube = tubes[toIdx];
      if (fromTube.items.length > 0 && toTube.items.length < capacity) {
        toTube.items.push(fromTube.items.pop());
      }
    }
  }

  // Inject mechanics
  if (levelNum >= 10 && Math.random() < 0.3) {
    // Golden fruit
    for (let t of tubes) {
      if (t.items.length > 0) {
        t.items[0] = { key: t.items[0], golden: true };
        break;
      }
    }
  }

  if (levelNum >= 20 && levelNum % 2 === 0) {
    // Frozen fruit
    for (let t of tubes) {
      if (t.items.length > 1) {
        const item = t.items[t.items.length - 2];
        const key = typeof item === 'object' ? item.key : item;
        t.items[t.items.length - 2] = { key: key, frozen: true };
        break;
      }
    }
  }

  // Dynamic time limit: 80s down to 35s
  let timeLimit = 60;
  if (levelNum <= 5) timeLimit = 80;
  else if (levelNum <= 15) timeLimit = 70;
  else if (levelNum <= 40) timeLimit = 60;
  else if (levelNum <= 100) timeLimit = 55;
  else if (levelNum <= 250) timeLimit = 50;
  else if (levelNum <= 500) timeLimit = 45;
  else if (levelNum <= 750) timeLimit = 40;
  else timeLimit = 35;

  const moveLimit = (isBoss || (levelNum >= 60 && levelNum % 4 === 0))
    ? Math.floor(fruitCount * capacity * 2.2) + Math.max(4, Math.floor((1000 - levelNum) / 60))
    : null;

  return {
    levelNumber: levelNum,
    tubes: tubes,
    capacity: capacity,
    timeLimit: timeLimit,
    moveLimit: moveLimit,
    isBoss: isBoss,
    coinReward: 40 + Math.min(600, Math.floor(levelNum * 0.6)),
    gemReward: isBoss ? 10 : (levelNum % 5 === 0 ? 3 : 1)
  };
}

// ==========================================
// 5. ACTIVE GAMEPLAY ENGINE
// ==========================================
let currentLevelData = null;
let currentTubes = [];
let selectedTubeIndex = -1;
let movesCount = 0;
let timeRemaining = 60;
let timerInterval = null;
let moveHistory = [];
let isGameOver = false;

function startLevel(levelNum) {
  stopTimer();
  currentLevelData = generateLevel(levelNum);
  // Deep clone tubes
  currentTubes = JSON.parse(JSON.stringify(currentLevelData.tubes));
  selectedTubeIndex = -1;
  movesCount = 0;
  moveHistory = [];
  isGameOver = false;
  timeRemaining = currentLevelData.timeLimit;

  // Switch Screen to Game
  showScreen('screen-game');

  // Update HUD
  document.getElementById('hud-level-text').textContent = currentLevelData.isBoss
    ? `⚡ Boss #${levelNum}`
    : `Level ${levelNum}`;

  document.getElementById('hud-timer-text').textContent = formatTime(timeRemaining);
  document.getElementById('hud-moves-text').textContent = currentLevelData.moveLimit != null
    ? `${movesCount}/${currentLevelData.moveLimit}`
    : `${movesCount}`;

  renderBoard();
  startTimer();
  updatePowerUpBadges();
}

function startTimer() {
  stopTimer();
  timerInterval = setInterval(() => {
    if (timeRemaining > 0) {
      timeRemaining--;
      document.getElementById('hud-timer-text').textContent = formatTime(timeRemaining);
      if (timeRemaining <= 10) {
        sound.playTone(800, 'sine', 0.05, 0.1);
      }
    } else {
      stopTimer();
      triggerGameOver('OUT OF TIME!');
    }
  }, 1000);
}

function stopTimer() {
  if (timerInterval) {
    clearInterval(timerInterval);
    timerInterval = null;
  }
}

function formatTime(sec) {
  const m = Math.floor(sec / 60);
  const s = sec % 60;
  return `${m < 10 ? '0' : ''}${m}:${s < 10 ? '0' : ''}${s}`;
}

function renderBoard() {
  const board = document.getElementById('tubes-board');
  board.innerHTML = '';

  currentTubes.forEach((tube, tubeIdx) => {
    const wrapper = document.createElement('div');
    wrapper.className = `tube-wrapper ${tubeIdx === selectedTubeIndex ? 'selected' : ''}`;
    wrapper.dataset.tubeIdx = tubeIdx;

    const container = document.createElement('div');
    container.className = `tube-container style-${userData.selectedStyle.toLowerCase().replace('_', '-')}`;

    // Render fruits inside container (bottom to top)
    tube.items.forEach((item, itemIdx) => {
      const fruitKey = typeof item === 'object' ? item.key : item;
      const def = FRUITS_DEF[fruitKey] || FRUITS_DEF.APPLE;

      const fruitEl = document.createElement('div');
      fruitEl.className = 'fruit-item';
      fruitEl.style.setProperty('--fruit-color', def.color);
      fruitEl.textContent = def.emoji;

      if (typeof item === 'object') {
        if (item.frozen) fruitEl.classList.add('frozen');
        if (item.golden) fruitEl.classList.add('golden');
      }

      // If top fruit of selected tube, animate float
      if (tubeIdx === selectedTubeIndex && itemIdx === tube.items.length - 1) {
        fruitEl.classList.add('floating-top');
      }

      container.appendChild(fruitEl);
    });

    // Check if tube is completed
    if (isTubeComplete(tube, currentLevelData.capacity)) {
      const cork = document.createElement('div');
      cork.className = 'tube-cork';
      cork.textContent = '✓ SORTED';
      wrapper.appendChild(cork);
    }

    // Locked container overlay
    if (tube.isLocked) {
      const lock = document.createElement('div');
      lock.className = 'tube-lock-overlay';
      lock.textContent = '🔒';
      container.appendChild(lock);
    }

    wrapper.appendChild(container);

    wrapper.addEventListener('click', () => onTubeClicked(tubeIdx));
    board.appendChild(wrapper);
  });
}

function getItemKey(item) {
  return typeof item === 'object' ? item.key : item;
}

function isTubeComplete(tube, capacity) {
  if (tube.items.length !== capacity) return false;
  const firstKey = getItemKey(tube.items[0]);
  return tube.items.every(it => getItemKey(it) === firstKey);
}

function onTubeClicked(tubeIdx) {
  if (isGameOver) return;
  sound.init();

  const targetTube = currentTubes[tubeIdx];

  if (targetTube.isLocked) {
    sound.playError();
    showToast('🔒 This container is locked!');
    return;
  }

  // First tap: Select source tube
  if (selectedTubeIndex === -1) {
    if (targetTube.items.length === 0) {
      sound.playError();
      return;
    }
    const topItem = targetTube.items[targetTube.items.length - 1];
    if (typeof topItem === 'object' && topItem.frozen) {
      sound.playDrop();
      topItem.frozen = false;
      showToast('❄️ Fruit defrosted! Tap again to select.');
      renderBoard();
      return;
    }

    selectedTubeIndex = tubeIdx;
    sound.playPick();
    renderBoard();
  } else {
    // Second tap: If tapping same tube, unselect
    if (selectedTubeIndex === tubeIdx) {
      selectedTubeIndex = -1;
      sound.playDrop();
      renderBoard();
      return;
    }

    // Attempt fruit transfer
    const sourceTube = currentTubes[selectedTubeIndex];
    if (sourceTube.items.length === 0) {
      selectedTubeIndex = -1;
      renderBoard();
      return;
    }

    const sourceItem = sourceTube.items[sourceTube.items.length - 1];
    const sourceKey = getItemKey(sourceItem);

    // Can we move into target tube?
    if (targetTube.items.length < currentLevelData.capacity) {
      const isTargetEmpty = targetTube.items.length === 0;
      const targetTopKey = !isTargetEmpty ? getItemKey(targetTube.items[targetTube.items.length - 1]) : null;

      if (isTargetEmpty || targetTopKey === sourceKey) {
        // Valid move! Save history for undo
        saveStateForUndo();

        const transferred = sourceTube.items.pop();
        targetTube.items.push(transferred);
        selectedTubeIndex = -1;
        movesCount++;
        sound.playDrop();

        // Check if golden fruit
        if (typeof transferred === 'object' && transferred.golden) {
          userData.coins += 50;
          sound.playCoin();
          showToast('⭐ Golden Fruit Sorted! +50 Coins');
        }

        // Check cork celebration
        if (isTubeComplete(targetTube, currentLevelData.capacity)) {
          sound.playCorkPop();
        }

        // Update moves HUD
        document.getElementById('hud-moves-text').textContent = currentLevelData.moveLimit != null
          ? `${movesCount}/${currentLevelData.moveLimit}`
          : `${movesCount}`;

        renderBoard();

        // Check Win Condition
        if (checkWinCondition()) {
          triggerLevelWin();
          return;
        }

        // Check Move Limit Defeat
        if (currentLevelData.moveLimit != null && movesCount >= currentLevelData.moveLimit) {
          triggerGameOver('OUT OF MOVES!');
          return;
        }

        return;
      }
    }

    // Invalid move: change selection to the clicked tube if it has fruits
    sound.playError();
    if (targetTube.items.length > 0) {
      selectedTubeIndex = tubeIdx;
    } else {
      selectedTubeIndex = -1;
    }
    renderBoard();
  }
}

function checkWinCondition() {
  const cap = currentLevelData.capacity;
  for (let tube of currentTubes) {
    if (tube.items.length > 0) {
      if (!isTubeComplete(tube, cap)) return false;
    }
  }
  return true;
}

function triggerLevelWin() {
  stopTimer();
  isGameOver = true;
  sound.playWinFanfare();

  // Compute stars (1 to 3) based on time & moves
  let stars = 3;
  if (timeRemaining < currentLevelData.timeLimit * 0.3) stars = 2;
  if (timeRemaining < currentLevelData.timeLimit * 0.1) stars = 1;

  const currentLevelNum = currentLevelData.levelNumber;
  userData.levelStars[currentLevelNum] = Math.max(userData.levelStars[currentLevelNum] || 0, stars);

  if (currentLevelNum === userData.highestLevelUnlocked && userData.highestLevelUnlocked < 1000) {
    userData.highestLevelUnlocked = currentLevelNum + 1;
  }

  userData.coins += currentLevelData.coinReward;
  userData.gems += currentLevelData.gemReward;
  saveUserData();

  // Populate Win Modal
  document.getElementById('win-level-name').textContent = `Level ${currentLevelNum} Completed!`;
  document.getElementById('win-coins-amount').textContent = `+${currentLevelData.coinReward}`;
  document.getElementById('win-gems-amount').textContent = `+${currentLevelData.gemReward}`;

  const starIcons = document.querySelectorAll('#win-stars-row .star-icon');
  starIcons.forEach((el, idx) => {
    el.style.opacity = idx < stars ? '1' : '0.25';
  });

  openModal('modal-win');
}

function triggerGameOver(reasonText) {
  stopTimer();
  isGameOver = true;
  sound.playError();
  document.getElementById('lose-title-text').textContent = reasonText;
  openModal('modal-lose');
}

// ==========================================
// 6. POWER-UPS IMPLEMENTATION
// ==========================================
function saveStateForUndo() {
  moveHistory.push({
    tubes: JSON.parse(JSON.stringify(currentTubes)),
    moves: movesCount
  });
  if (moveHistory.length > 10) moveHistory.shift();
}

function useUndo() {
  if (userData.powerUps.undo <= 0) {
    showToast('Need Undo Power-up! Buy in shop.');
    return;
  }
  if (moveHistory.length === 0) {
    showToast('No moves to undo!');
    return;
  }
  const lastState = moveHistory.pop();
  currentTubes = lastState.tubes;
  movesCount = lastState.moves;
  selectedTubeIndex = -1;
  userData.powerUps.undo--;
  saveUserData();
  updatePowerUpBadges();
  sound.playTone(440, 'triangle', 0.1);
  renderBoard();
  showToast('↩️ Move Undone');
}

function useHammer() {
  if (userData.powerUps.hammer <= 0) {
    showToast('Need Hammer Power-up!');
    return;
  }
  if (selectedTubeIndex === -1) {
    showToast('Tap a tube to hammer its top fruit!');
    return;
  }
  const tube = currentTubes[selectedTubeIndex];
  if (tube.items.length === 0) return;

  saveStateForUndo();
  tube.items.pop();
  selectedTubeIndex = -1;
  userData.powerUps.hammer--;
  saveUserData();
  updatePowerUpBadges();
  sound.playTone(300, 'sawtooth', 0.2);
  renderBoard();
  showToast('🔨 Smashed Top Fruit!');
}

function useAddTube() {
  if (userData.powerUps.tube <= 0) {
    showToast('Need Extra Tube Power-up!');
    return;
  }
  saveStateForUndo();
  currentTubes.push({
    items: [],
    isLocked: false,
    completed: false
  });
  userData.powerUps.tube--;
  saveUserData();
  updatePowerUpBadges();
  sound.playTone(660, 'sine', 0.15);
  renderBoard();
  showToast('🧪 Extra Tube Added!');
}

function useShuffle() {
  if (userData.powerUps.shuffle <= 0) {
    showToast('Need Shuffle Power-up!');
    return;
  }
  saveStateForUndo();
  // Collect all uncompleted fruits
  let pool = [];
  currentTubes.forEach(t => {
    if (!isTubeComplete(t, currentLevelData.capacity)) {
      pool.push(...t.items);
      t.items = [];
    }
  });

  // Randomize pool
  pool.sort(() => Math.random() - 0.5);

  // Distribute back
  let pIdx = 0;
  currentTubes.forEach(t => {
    if (!isTubeComplete(t, currentLevelData.capacity)) {
      while (t.items.length < currentLevelData.capacity && pIdx < pool.length) {
        t.items.push(pool[pIdx++]);
      }
    }
  });

  userData.powerUps.shuffle--;
  saveUserData();
  updatePowerUpBadges();
  sound.playTone(720, 'triangle', 0.2);
  renderBoard();
  showToast('🔀 Board Shuffled!');
}

function useFrostBomb() {
  if (userData.powerUps.frost <= 0) {
    showToast('Need Defrost Bomb!');
    return;
  }
  let defrostedCount = 0;
  currentTubes.forEach(t => {
    t.items.forEach(it => {
      if (typeof it === 'object' && it.frozen) {
        it.frozen = false;
        defrostedCount++;
      }
    });
  });
  userData.powerUps.frost--;
  saveUserData();
  updatePowerUpBadges();
  sound.playTone(880, 'sine', 0.2);
  renderBoard();
  showToast(`❄️ Defrosted ${defrostedCount} Fruits!`);
}

function updatePowerUpBadges() {
  document.getElementById('pu-count-undo').textContent = userData.powerUps.undo;
  document.getElementById('pu-count-hammer').textContent = userData.powerUps.hammer;
  document.getElementById('pu-count-tube').textContent = userData.powerUps.tube;
  document.getElementById('pu-count-shuffle').textContent = userData.powerUps.shuffle;
  document.getElementById('pu-count-frost').textContent = userData.powerUps.frost;
}

// ==========================================
// 7. 1000 SAGA LEVEL MAP MODAL
// ==========================================
let currentMapChapter = 0;

function renderChapterChips() {
  const container = document.getElementById('chapter-chips');
  container.innerHTML = '';
  for (let c = 0; c < 10; c++) {
    const start = c * 100 + 1;
    const end = (c + 1) * 100;
    const chip = document.createElement('button');
    chip.className = `chapter-chip ${c === currentMapChapter ? 'active' : ''}`;
    chip.textContent = `Ch ${c + 1} (${start}-${end})`;
    chip.addEventListener('click', () => {
      currentMapChapter = c;
      renderChapterChips();
      renderLevelsGrid();
    });
    container.appendChild(chip);
  }
}

function renderLevelsGrid() {
  const grid = document.getElementById('levels-grid');
  grid.innerHTML = '';

  const startLevel = currentMapChapter * 100 + 1;
  const endLevel = (currentMapChapter + 1) * 100;

  for (let lvl = startLevel; lvl <= endLevel; lvl++) {
    const isUnlocked = lvl <= userData.highestLevelUnlocked;
    const isCurrent = lvl === userData.highestLevelUnlocked;
    const isBoss = (lvl % 25 === 0);
    const stars = userData.levelStars[lvl] || 0;

    const tile = document.createElement('div');
    tile.className = `level-tile ${isUnlocked ? 'unlocked' : ''} ${isCurrent ? 'current' : ''} ${isBoss ? 'boss' : ''}`;

    const numSpan = document.createElement('span');
    numSpan.className = 'tile-num';
    numSpan.textContent = isUnlocked ? (isBoss ? `👑 ${lvl}` : `${lvl}`) : '🔒';

    tile.appendChild(numSpan);

    if (isUnlocked && stars > 0) {
      const starsSpan = document.createElement('span');
      starsSpan.className = 'tile-stars';
      starsSpan.textContent = '⭐'.repeat(stars);
      tile.appendChild(starsSpan);
    }

    tile.addEventListener('click', () => {
      if (isUnlocked) {
        closeAllModals();
        startLevel(lvl);
      } else {
        sound.playError();
        showToast(`Complete Level ${lvl - 1} first!`);
      }
    });

    grid.appendChild(tile);
  }
}

// ==========================================
// 8. SHOP MODAL
// ==========================================
function setupShop() {
  const tabs = document.querySelectorAll('.shop-tab');
  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      tabs.forEach(t => t.classList.remove('active'));
      document.querySelectorAll('.shop-tab-pane').forEach(p => p.classList.remove('active'));

      tab.classList.add('active');
      const paneId = `shop-tab-${tab.dataset.tab}`;
      document.getElementById(paneId).classList.add('active');
    });
  });

  renderShopTubes();
  renderShopThemes();
  renderShopFruits();
  renderShopBoosts();
}

function renderShopTubes() {
  const pane = document.getElementById('shop-tab-tubes');
  pane.innerHTML = '';

  CONTAINER_STYLES.forEach(style => {
    const isUnlocked = userData.unlockedStyles.includes(style.id);
    const isEquipped = userData.selectedStyle === style.id;

    const card = document.createElement('div');
    card.className = `shop-item-card ${isEquipped ? 'equipped' : ''}`;

    card.innerHTML = `
      <div class="shop-item-info">
        <span class="shop-item-icon">${style.emoji}</span>
        <div>
          <div class="shop-item-name">${style.name}</div>
          <div class="shop-item-desc">${style.desc}</div>
        </div>
      </div>
      <div class="shop-item-action">
        ${isEquipped
          ? '<span class="tag-active">ACTIVE</span>'
          : isUnlocked
            ? `<button class="btn-shop-action btn-equip" data-equip-tube="${style.id}">EQUIP</button>`
            : `<button class="btn-shop-action btn-buy" data-buy-tube="${style.id}">🪙 ${style.costCoins}</button>`
        }
      </div>
    `;

    pane.appendChild(card);
  });

  pane.querySelectorAll('[data-equip-tube]').forEach(b => {
    b.addEventListener('click', (e) => {
      userData.selectedStyle = e.target.dataset.equipTube;
      saveUserData();
      renderShopTubes();
      showToast('Equipped Tube Skin!');
    });
  });

  pane.querySelectorAll('[data-buy-tube]').forEach(b => {
    b.addEventListener('click', (e) => {
      const id = e.target.dataset.buyTube;
      const targetStyle = CONTAINER_STYLES.find(s => s.id === id);
      if (userData.coins >= targetStyle.costCoins) {
        userData.coins -= targetStyle.costCoins;
        userData.unlockedStyles.push(id);
        userData.selectedStyle = id;
        sound.playCoin();
        saveUserData();
        renderShopTubes();
        showToast(`Unlocked ${targetStyle.name}!`);
      } else {
        sound.playError();
        showToast('Not enough coins!');
      }
    });
  });
}

function renderShopThemes() {
  const pane = document.getElementById('shop-tab-themes');
  pane.innerHTML = '';

  THEMES.forEach(theme => {
    const isUnlocked = userData.unlockedThemes.includes(theme.id);
    const isEquipped = userData.selectedTheme === theme.id;

    const card = document.createElement('div');
    card.className = `shop-item-card ${isEquipped ? 'equipped' : ''}`;

    card.innerHTML = `
      <div class="shop-item-info">
        <span class="shop-item-icon">${theme.emoji}</span>
        <div>
          <div class="shop-item-name">${theme.name}</div>
          <div class="shop-item-desc">${theme.desc}</div>
        </div>
      </div>
      <div class="shop-item-action">
        ${isEquipped
          ? '<span class="tag-active">ACTIVE</span>'
          : isUnlocked
            ? `<button class="btn-shop-action btn-equip" data-equip-theme="${theme.id}">APPLY</button>`
            : `<button class="btn-shop-action btn-buy" data-buy-theme="${theme.id}">🪙 ${theme.costCoins}</button>`
        }
      </div>
    `;

    pane.appendChild(card);
  });

  pane.querySelectorAll('[data-equip-theme]').forEach(b => {
    b.addEventListener('click', (e) => {
      userData.selectedTheme = e.target.dataset.equipTheme;
      applyAppTheme();
      saveUserData();
      renderShopThemes();
      showToast('Applied Theme!');
    });
  });

  pane.querySelectorAll('[data-buy-theme]').forEach(b => {
    b.addEventListener('click', (e) => {
      const id = e.target.dataset.buyTheme;
      const t = THEMES.find(th => th.id === id);
      if (userData.coins >= t.costCoins) {
        userData.coins -= t.costCoins;
        userData.unlockedThemes.push(id);
        userData.selectedTheme = id;
        applyAppTheme();
        sound.playCoin();
        saveUserData();
        renderShopThemes();
        showToast(`Unlocked ${t.name}!`);
      } else {
        sound.playError();
        showToast('Not enough coins!');
      }
    });
  });
}

function renderShopFruits() {
  const pane = document.getElementById('shop-tab-fruits');
  pane.innerHTML = '';

  FRUIT_PACKS.forEach(pack => {
    const isUnlocked = userData.unlockedPacks.includes(pack.id);
    const isEquipped = userData.selectedPack === pack.id;

    const card = document.createElement('div');
    card.className = `shop-item-card ${isEquipped ? 'equipped' : ''}`;

    card.innerHTML = `
      <div class="shop-item-info">
        <div>
          <div class="shop-item-name">${pack.name}</div>
          <div style="font-size: 13px; margin: 2px 0;">${pack.emojis}</div>
          <div class="shop-item-desc">${pack.desc}</div>
        </div>
      </div>
      <div class="shop-item-action">
        ${isEquipped
          ? '<span class="tag-active">EQUIPPED</span>'
          : isUnlocked
            ? `<button class="btn-shop-action btn-equip" data-equip-pack="${pack.id}">EQUIP</button>`
            : `<button class="btn-shop-action btn-buy" data-buy-pack="${pack.id}">🪙 ${pack.costCoins}</button>`
        }
      </div>
    `;

    pane.appendChild(card);
  });

  pane.querySelectorAll('[data-equip-pack]').forEach(b => {
    b.addEventListener('click', (e) => {
      userData.selectedPack = e.target.dataset.equipPack;
      saveUserData();
      renderShopFruits();
      showToast('Equipped Fruit Pack!');
    });
  });

  pane.querySelectorAll('[data-buy-pack]').forEach(b => {
    b.addEventListener('click', (e) => {
      const id = e.target.dataset.buyPack;
      const p = FRUIT_PACKS.find(pk => pk.id === id);
      if (userData.coins >= p.costCoins) {
        userData.coins -= p.costCoins;
        userData.unlockedPacks.push(id);
        userData.selectedPack = id;
        sound.playCoin();
        saveUserData();
        renderShopFruits();
        showToast(`Unlocked ${p.name}!`);
      } else {
        sound.playError();
        showToast('Not enough coins!');
      }
    });
  });
}

function renderShopBoosts() {
  const pane = document.getElementById('shop-tab-boosts');
  pane.innerHTML = '';

  const boosts = [
    { id: 'undo',    name: 'Undo Booster x3',    emoji: '↩️', cost: 150, amount: 3 },
    { id: 'hammer',  name: 'Hammer Booster x2',  emoji: '🔨', cost: 200, amount: 2 },
    { id: 'tube',    name: 'Extra Tube x2',      emoji: '🧪', cost: 300, amount: 2 },
    { id: 'shuffle', name: 'Shuffle Board x2',   emoji: '🔀', cost: 250, amount: 2 },
    { id: 'frost',   name: 'Defrost Bomb x2',    emoji: '❄️', cost: 200, amount: 2 }
  ];

  boosts.forEach(b => {
    const card = document.createElement('div');
    card.className = 'shop-item-card';
    card.innerHTML = `
      <div class="shop-item-info">
        <span class="shop-item-icon">${b.emoji}</span>
        <div>
          <div class="shop-item-name">${b.name}</div>
          <div class="shop-item-desc">Owned: ${userData.powerUps[b.id]}</div>
        </div>
      </div>
      <button class="btn-shop-action btn-buy" data-buy-boost="${b.id}" data-cost="${b.cost}" data-amt="${b.amount}">🪙 ${b.cost}</button>
    `;
    pane.appendChild(card);
  });

  pane.querySelectorAll('[data-buy-boost]').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const bId = btn.dataset.buyBoost;
      const cost = parseInt(btn.dataset.cost);
      const amt = parseInt(btn.dataset.amt);
      if (userData.coins >= cost) {
        userData.coins -= cost;
        userData.powerUps[bId] += amt;
        sound.playCoin();
        saveUserData();
        updatePowerUpBadges();
        renderShopBoosts();
        showToast(`Purchased ${amt}x Boosters!`);
      } else {
        sound.playError();
        showToast('Not enough coins!');
      }
    });
  });
}

// ==========================================
// 9. LUCKY WHEEL (CANVAS 2D)
// ==========================================
const WHEEL_PRIZES = [
  { text: '150 Coins', coins: 150, gems: 0, color: '#fbc02d' },
  { text: '300 Coins', coins: 300, gems: 0, color: '#ff9800' },
  { text: '2 Gems',    coins: 0,   gems: 2, color: '#00e5ff' },
  { text: 'JACKPOT! 1000', coins: 1000, gems: 5, color: '#e040fb' },
  { text: '500 Coins', coins: 500, gems: 0, color: '#4caf50' },
  { text: '1 Gem',     coins: 0,   gems: 1, color: '#29b6f6' }
];

let wheelAngle = 0;
let isSpinning = false;

function drawWheel() {
  const canvas = document.getElementById('wheel-canvas');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  const cx = canvas.width / 2;
  const cy = canvas.height / 2;
  const radius = cx - 8;
  const arc = (2 * Math.PI) / WHEEL_PRIZES.length;

  ctx.clearRect(0, 0, canvas.width, canvas.height);

  WHEEL_PRIZES.forEach((prize, idx) => {
    const angle = wheelAngle + idx * arc;
    ctx.beginPath();
    ctx.fillStyle = prize.color;
    ctx.moveTo(cx, cy);
    ctx.arc(cx, cy, radius, angle, angle + arc);
    ctx.lineTo(cx, cy);
    ctx.fill();
    ctx.stroke();

    // Text
    ctx.save();
    ctx.translate(cx, cy);
    ctx.rotate(angle + arc / 2);
    ctx.textAlign = 'right';
    ctx.fillStyle = '#fff';
    ctx.font = 'bold 12px sans-serif';
    ctx.shadowColor = 'rgba(0,0,0,0.8)';
    ctx.shadowBlur = 4;
    ctx.fillText(prize.text, radius - 20, 4);
    ctx.restore();
  });

  // Center gold hub
  ctx.beginPath();
  ctx.arc(cx, cy, 22, 0, 2 * Math.PI);
  ctx.fillStyle = '#ffd54f';
  ctx.fill();
  ctx.stroke();
}

function spinWheel() {
  if (isSpinning) return;
  isSpinning = true;
  sound.playTone(500, 'triangle', 0.2);

  const extraSpins = 5 + Math.floor(Math.random() * 4);
  const targetPrizeIdx = Math.floor(Math.random() * WHEEL_PRIZES.length);
  const arc = (2 * Math.PI) / WHEEL_PRIZES.length;
  // Pointer is at the top (-PI/2)
  const targetAngle = (2 * Math.PI * extraSpins) - (targetPrizeIdx * arc) - (arc / 2) - (Math.PI / 2);

  const startAngle = wheelAngle;
  const duration = 3500;
  const startTime = performance.now();

  function animate(now) {
    const elapsed = now - startTime;
    const progress = Math.min(1, elapsed / duration);
    // Cubic ease out
    const ease = 1 - Math.pow(1 - progress, 3);
    wheelAngle = startAngle + (targetAngle - startAngle) * ease;
    drawWheel();

    if (progress < 1) {
      requestAnimationFrame(animate);
    } else {
      isSpinning = false;
      const prize = WHEEL_PRIZES[targetPrizeIdx];
      userData.coins += prize.coins;
      userData.gems += prize.gems;
      saveUserData();
      sound.playWinFanfare();
      showToast(`🎉 Lucky Win: ${prize.text}!`);
    }
  }

  requestAnimationFrame(animate);
}

// ==========================================
// 10. DAILY STREAK MODAL
// ==========================================
function renderDailyStreak() {
  const container = document.getElementById('daily-cards-grid');
  container.innerHTML = '';

  const streakRewards = [
    { day: 1, reward: '🪙 100' },
    { day: 2, reward: '🪙 150' },
    { day: 3, reward: '💎 2' },
    { day: 4, reward: '🪙 250' },
    { day: 5, reward: '🔨 1' },
    { day: 6, reward: '🪙 400' },
    { day: 7, reward: '👑 MEGA' }
  ];

  streakRewards.forEach(item => {
    const card = document.createElement('div');
    const isCurrent = item.day === userData.streakDay;
    card.style.cssText = `
      background: ${isCurrent ? 'rgba(255,213,79,0.3)' : 'rgba(255,255,255,0.06)'};
      border: 1px solid ${isCurrent ? '#ffd54f' : 'rgba(255,255,255,0.1)'};
      border-radius: 12px;
      padding: 10px;
      text-align: center;
    `;
    card.innerHTML = `
      <div style="font-size: 11px; font-weight: 700; color: #aaa;">Day ${item.day}</div>
      <div style="font-size: 14px; font-weight: 900; margin-top: 4px;">${item.reward}</div>
    `;
    container.appendChild(card);
  });
}

function claimDailyStreak() {
  userData.coins += 150;
  userData.gems += 2;
  userData.streakDay = (userData.streakDay % 7) + 1;
  saveUserData();
  sound.playCoin();
  closeAllModals();
  showToast('🎁 Claimed Daily Streak Reward! +150 Coins +2 Gems');
}

// ==========================================
// 11. AD SIMULATION (SPONSOR BONUS & REVIVE)
// ==========================================
let adTimerInterval = null;

function showAdVideo(onComplete) {
  const overlay = document.getElementById('ad-video-overlay');
  const timerSpan = document.getElementById('ad-timer');
  const closeBtn = document.getElementById('btn-close-ad');
  
  overlay.classList.add('active');
  let timeLeft = 5;
  timerSpan.textContent = `${timeLeft}s`;
  closeBtn.disabled = true;
  closeBtn.textContent = `Reward in ${timeLeft}s...`;

  if (adTimerInterval) clearInterval(adTimerInterval);

  adTimerInterval = setInterval(() => {
    timeLeft--;
    if (timeLeft > 0) {
      timerSpan.textContent = `${timeLeft}s`;
      closeBtn.textContent = `Reward in ${timeLeft}s...`;
    } else {
      clearInterval(adTimerInterval);
      timerSpan.textContent = 'DONE';
      closeBtn.disabled = false;
      closeBtn.textContent = 'CLAIM REWARD ✓';
    }
  }, 1000);

  closeBtn.onclick = () => {
    overlay.classList.remove('active');
    if (onComplete) onComplete();
  };
}

// ==========================================
// 12. UI NAVIGATION & GLOBAL EVENT HANDLERS
// ==========================================
function showScreen(screenId) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  document.getElementById(screenId).classList.add('active');
  updateTopBarUI();
}

function openModal(modalId) {
  document.getElementById(modalId).classList.add('active');
}

function closeAllModals() {
  document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
}

function showToast(msg) {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.classList.add('show');
  setTimeout(() => t.classList.remove('show'), 2200);
}

function applyAppTheme() {
  const app = document.getElementById('app-container');
  THEMES.forEach(th => app.classList.remove(`theme-${th.id}`));
  app.classList.add(`theme-${userData.selectedTheme}`);
}

function updateTopBarUI() {
  document.getElementById('txt-coins').textContent = userData.coins;
  document.getElementById('txt-gems').textContent = userData.gems;
  document.getElementById('home-current-level').textContent = `Level ${userData.highestLevelUnlocked}`;
  document.getElementById('btn-play-level-num').textContent = userData.highestLevelUnlocked;

  const totalStars = Object.values(userData.levelStars).reduce((a, b) => a + b, 0);
  document.getElementById('home-total-stars').textContent = totalStars;

  const pct = Math.min(100, Math.max(1, (userData.highestLevelUnlocked / 1000) * 100));
  document.getElementById('home-progress-fill').style.width = `${pct}%`;
}

// ==========================================
// INITIALIZATION ON PAGE LOAD
// ==========================================
window.addEventListener('DOMContentLoaded', () => {
  applyAppTheme();
  updateTopBarUI();
  setupShop();
  drawWheel();

  // Navigation Buttons
  document.getElementById('btn-home').addEventListener('click', () => {
    stopTimer();
    showScreen('screen-home');
  });

  document.getElementById('btn-play-level').addEventListener('click', () => {
    startLevel(userData.highestLevelUnlocked);
  });

  document.getElementById('btn-restart-level').addEventListener('click', () => {
    startLevel(currentLevelData.levelNumber);
  });

  // Modal Openers
  document.getElementById('btn-open-levels').addEventListener('click', () => {
    currentMapChapter = Math.min(9, Math.floor((userData.highestLevelUnlocked - 1) / 100));
    renderChapterChips();
    renderLevelsGrid();
    openModal('modal-levels');
  });

  document.getElementById('btn-open-shop').addEventListener('click', () => openModal('modal-shop'));
  document.getElementById('btn-shop-shortcut').addEventListener('click', () => openModal('modal-shop'));

  document.getElementById('btn-open-wheel').addEventListener('click', () => {
    drawWheel();
    openModal('modal-wheel');
  });
  document.getElementById('btn-spin-shortcut').addEventListener('click', () => {
    drawWheel();
    openModal('modal-wheel');
  });

  document.getElementById('btn-open-daily').addEventListener('click', () => {
    renderDailyStreak();
    openModal('modal-daily');
  });

  document.getElementById('btn-settings').addEventListener('click', () => openModal('modal-settings'));

  // Close Modal Buttons
  document.querySelectorAll('.btn-close-modal').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const targetId = e.target.dataset.target;
      document.getElementById(targetId).classList.remove('active');
    });
  });

  // Power-Up Click Bindings
  document.getElementById('pu-undo').addEventListener('click', useUndo);
  document.getElementById('pu-hammer').addEventListener('click', useHammer);
  document.getElementById('pu-tube').addEventListener('click', useAddTube);
  document.getElementById('pu-shuffle').addEventListener('click', useShuffle);
  document.getElementById('pu-frost').addEventListener('click', useFrostBomb);

  // Wheel Spin Button
  document.getElementById('btn-spin-now').addEventListener('click', spinWheel);

  // Daily Claim Button
  document.getElementById('btn-claim-daily').addEventListener('click', claimDailyStreak);

  // Win Modal Next Level
  document.getElementById('btn-win-next').addEventListener('click', () => {
    closeAllModals();
    startLevel(currentLevelData.levelNumber + 1);
  });

  document.getElementById('btn-win-replay').addEventListener('click', () => {
    closeAllModals();
    startLevel(currentLevelData.levelNumber);
  });

  // Lose Modal Revive with Ad
  document.getElementById('btn-revive-ad').addEventListener('click', () => {
    closeAllModals();
    showAdVideo(() => {
      timeRemaining += 30;
      isGameOver = false;
      startTimer();
      showToast('Revived with +30 Seconds! ⏱️');
    });
  });

  document.getElementById('btn-lose-restart').addEventListener('click', () => {
    closeAllModals();
    startLevel(currentLevelData.levelNumber);
  });

  // Free Sponsor Ad Button
  document.getElementById('btn-watch-ad').addEventListener('click', () => {
    showAdVideo(() => {
      userData.coins += 100;
      userData.gems += 3;
      saveUserData();
      sound.playCoin();
      showToast('📺 Sponsor Reward Claimed! +100 Coins +3 Gems');
    });
  });

  // Settings Toggles
  const chkSound = document.getElementById('chk-sound');
  const chkMusic = document.getElementById('chk-music');
  const chkHaptic = document.getElementById('chk-haptic');

  chkSound.checked = userData.soundEnabled;
  chkMusic.checked = userData.musicEnabled;
  chkHaptic.checked = userData.hapticEnabled;

  chkSound.addEventListener('change', () => {
    userData.soundEnabled = chkSound.checked;
    sound.soundEnabled = chkSound.checked;
    saveUserData();
  });

  chkMusic.addEventListener('change', () => {
    userData.musicEnabled = chkMusic.checked;
    sound.musicEnabled = chkMusic.checked;
    saveUserData();
  });

  chkHaptic.addEventListener('change', () => {
    userData.hapticEnabled = chkHaptic.checked;
    saveUserData();
  });

  document.getElementById('btn-reset-data').addEventListener('click', () => {
    if (confirm('Are you sure you want to reset all game progress?')) {
      localStorage.removeItem(STORAGE_KEY);
      userData = getDefaultUserData();
      saveUserData();
      applyAppTheme();
      closeAllModals();
      showScreen('screen-home');
      showToast('Game progress reset to default.');
    }
  });
});
