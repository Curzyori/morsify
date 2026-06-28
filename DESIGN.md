# Morsify Design Spec

**Aesthetic Goal:** Notion-Clean Editorial
**Palette:**
- Primary: #5645D4 (Notion Purple)
- Canvas: #FFFFFF (Clean white)
- Surface: #F6F5F4 (Warm off-white)
- Surface Dark: #181715 (Editorial navy/black)
- Ink: #1A1A1A (Deep ink)
- Muted: #5D5B54 (Slate)
- Hairline: #E5E3DF

**Typography:**
- Display: Serif (system fallback) untuk app title
- UI: Inter / system sans-serif
- Morse text: Monospace (essential for clarity and proportion)
- Hierarchy: 48/36/28/22/18/16/14/13/12 px

**UI Components:**
- Single dashboard, no tabs
- Top bar: app title + settings icon (top-right) — NO flag toggle
- Hero card (Input + Live morse preview)
  - TextField dengan headline style
  - Clear button (X icon, top-right of input label row)
  - Character count (bottom-right of TextField)
  - Big monospace morse preview with character highlight during transmit
  - Pulse dot indicator (right side, lit when actively transmitting)
- Speed card with slider (0-100 mapped to 240-60ms)
- Output mode card (3 chips: Flash / Sound / Both)
- Big primary button: "Kirim Sinyal" / "Transmit" (turns into "Berhenti" / "Stop" with error red)
- Progress card (visible only during transmit): linear progress + char counter + last symbol
- Learn Morse section (replaces About footer)

**Settings Dialog (top-right icon):**
- AlertDialog modal
- Title: "Dukung Morsify" / "Support Morsify"
- Language toggle row (ID ↔ EN)
- GitHub repo link
- Donate section: EVM + BTC addresses with copy button

**Learn Morse Section:**
- Title + subtitle + pattern description
- "· = dit (pendek) · − = dat (panjang)"
- Tips: "Coba: SOS"
- Reference: ITU-R M.1677-1

**Interactions:**
- Tap Settings icon → opens Settings dialog
- Tap "Kirim Sinyal" → flash/beep starts immediately, button toggles to "Berhenti"
- Tap "Berhenti" → cancels coroutine, releases torch + sound
- Tap speed slider → updates TimingProfile, next transmission uses new speed
- Tap output mode chip → updates OutputMode, disables Flash/Both if no flash
- Tap clear (X) → clears input text

**i18n:**
- Default: Bahasa Indonesia (values/strings.xml)
- English override: values-en/strings.xml
- In-app switch via Settings dialog (not system locale)

**Logo:**
- Adaptive icon PNG with 3 concentric rings + center dot + "MO" morse pattern
- Background: #5645D4 indigo purple
- Foreground: white
- Mipmap: mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi
