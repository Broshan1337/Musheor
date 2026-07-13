# Security Analysis Report — musheor-1.6.1 1.21.11.jar

**Analyzed:** 2026-07-10
**Decompiler:** Vineflower 1.12.0 (JDK 26)
**Compared against:** musheor-1.5 (see `SECURITY_REPORT.md`)
**Verdict: NOT MALWARE** — same legitimate Meteor Client highway-building addon as v1.5, with a new *class-name* obfuscation layer but no new capabilities.

---

## Summary

Musheor 1.6.1 is functionally the same Meteor Client addon audited in v1.5: automated highway building for Minecraft anarchy servers (2b2t), plus HUD/utility/social features. The headline change in this release is a **new obfuscation layer**, not new behavior.

**No malicious behavior was found.** No credential/token/file theft, no data exfiltration, no remote code loading, no persistence mechanisms.

---

## What changed since v1.5 (obfuscation)

### New flat `obf/` package (25 classes)
In v1.5 the addon's own code lived in readable packages (`musheor.commands.*`, `musheor.utils.*`) with only the *internal method/field names* garbled. In 1.6.1, the author moved the command classes and the core utility classes into a single flat package named `obf`, and **obfuscated the class names too** (`HFbqnT1FEh2q`, `CUfICea7s`, `aY0a71o`, …).

This is a stronger anti-code-theft measure, common in private/paid Minecraft mods. It is **not** an indicator of malware — the classes decompile to the same highway/inventory/render logic as before. Recovered identities:

| obf class | Real identity | obf class | Real identity |
|---|---|---|---|
| `aY0a71o` | `.execute` command | `HFbqnT1FEh2q` | WorldUtils (+ Direction8 enum) |
| `D0Jn` | `.find` command | `MFFPIcv139M` | HighwayState (singleton) |
| `FDb5` | `.countItems` command | `TKzj7u` | highway block-pattern generator |
| `SDNZCBI` | `.folder` command | `J0sjSCk` | ring/grid checkpoint locator |
| `w4hkT` | `.xaero` command | `GZpL` | movement/Baritone handlers |
| `zQn6` | `.pearlstore` command | `NKyC2E` | PathingHelper (`goto`) |
| `VGn8YrSOy` | `.kit` command | `Mj77A` | PearlStore |
| `UkMm7uisd` | `.rc` (restock) command | `XfFrUB` | DiscordRPC |
| `JUKhlfIQiiGS` | `.tp` command | `fVHOZ` | number/format util |
| `e1lTf` | inventory/shulker Module | `x3lfFe8H5f` | RenderUtils |
| `CUfICea7s` | PlayerUtils / msg helper | `EzpHtsX2O` | TagUtils (avatars) |
| `QLktfq` | placement engine | `SyqMxK` | packet rate-limit checks |
| `ttI5` | StatsHandler (reads MC stats) | | |

### Removed / changed since v1.5
- **`StatsHandler` no longer writes `data.csv`.** The v1.5 CSV stats persistence (`MeteorClient.FOLDER/musheor/data.csv`) is gone. Stats are now read live from Minecraft's own `StatHandler` (`ttI5`) into in-memory `HighwayState`. This *reduces* filesystem footprint.
- `ElytraSwap` → `ElytraTweakz` (rename), new `DepthInteract`, `ChatScreenMixin`, `ScreenHandlerMixin`, `highway/` subpackage.
- Dropped experimental modules present in v1.5 clean set (`TestModule`, `SpongeBob`, `ItemBranding`, `LogTeleportDetails`).

---

## Network connections (ALL outbound only — unchanged from v1.5)

| Class | URL | Purpose | Suspicious? |
|---|---|---|---|
| `HighwayNetworkManager` | `https://highways.musheck.dev` | Downloads highway config (ring/diamond distances) | No — config only |
| `MutualManager` | `https://mutuals.musheck.dev` | Downloads JSON list of player UUIDs for cape/glow cosmetics | No — cosmetic only |
| `MoreTags` / `TagUtils` | `https://mc-heads.net/avatar/{uuid}/64` | Player avatar images for tab list | No — standard MC utility |
| `musheor.java` (GithubRepo) | `https://musheck.dev` | Addon homepage/update link metadata | No — static link |
| `DiscordRPC` (`XfFrUB`) | Discord IPC (local socket) | Highway stats in Discord Rich Presence | No — local only |
| `Dispatcher` | User-configured Discord webhook | Sends visual-range/stash/pearl notices to **your own** webhook | No — user sets URL |

All connections are outbound-only, read-only (`GET`) except the user's own Discord webhook (`POST`). No credentials, tokens, or filesystem contents are ever transmitted. No data is sent back to the author's servers.

---

## Reflection / dynamic code (reviewed — benign)

| Location | Behavior | Assessment |
|---|---|---|
| `aY0a71o` (`.execute`) | Scans the mod's **own** `musheor` package classes and invokes a named method with parsed args (dev/test console) | Local only; cannot load or run external code |
| `CUfICea7s`, `ChatScreenMixin` | `Class.forName("musheor.plus.PlusPlayerUtils" / "PlusChatScreenInit")` — optional paid "plus" module hooks; wrapped in try/catch, no-op if absent | Benign optional-feature probe |
| `compat/*`, `Printer`, `MoreTags` | Reflective access to optional mods (Litematica/Xaero) and MC internals | Standard soft-dependency compat |

All reflection targets are **local class names** — none are derived from network input, and nothing calls `defineClass`/custom `ClassLoader` to load remote bytecode.

## ProcessBuilder (unchanged — benign)

**File:** `obf/SDNZCBI.java` (the `.folder` command, formerly `FolderCommand`)
```java
new ProcessBuilder("explorer.exe", folder.getAbsolutePath()).start();
```
Opens a Windows Explorer window to a subfolder of `.minecraft`. Harmless.

## Crypto / encoding
None. No `Base64`, `javax.crypto`, `Cipher`, `MessageDigest`, or compressed/encrypted payloads anywhere in the jar. Nothing is decoded from an embedded blob or downloaded and executed.

---

## Conclusion

Musheor 1.6.1 is the same legitimate Meteor Client addon as v1.5. The new `obf` package renames the addon's own classes to deter code theft; the underlying behavior is identical highway-automation/utility logic. Network activity is limited to the same read-only config/cosmetic fetches and the user's own Discord webhook. There is no exfiltration, no remote code execution, and no persistence.

**Safe to use** from a malware perspective — verdict consistent with the v1.5 analysis.
