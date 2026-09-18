# ModdingToolkit

A clean, lossless-first JSON / Delver `.dat` editor for Realms of Fate.

## Requirements
- Java 8+
- Compatible with older Gradle versions used by the Delver project

## Run from source
```bash
gradle run
```

## Build distribution
```bash
gradle dist
```
Output: `build/distributions/ModdingToolkit.zip`

## Design principles
- The JSON tree is the source of truth.
- Unknown fields are preserved and editable.
- No rigid item/monster DTOs are used for serialization.
- Files are parsed before saving and written through a temporary file.
- A `.bak` backup is created before replacing an existing file.

## Current features
- Open `.json` and `.dat` files.
- Three-pane data-file / entry / editor layout.
- Automatic entry discovery for root arrays and common object-wrapped arrays.
- Recursive form editor for objects, arrays, strings, numbers, booleans and null.
- Add, rename and delete object properties.
- Add, duplicate and delete array values / entries.
- Raw JSON mode with syntax validation and Apply action.
- Search/filter entries.
- Undo / redo snapshots.
- Dirty-state tracking and save prompts.
- Safe save with backup + temp-file replacement.
- Dark Realms of Fate-oriented desktop UI.

This first version intentionally keeps Delver-specific metadata optional. The editor remains usable when the engine gains fields the toolkit has never seen before.

## UX pass
- Full dark Swing theme, including menus, dialogs, file chooser, tooltips, text controls, lists, and editor surfaces.
- Friendly labels and inline help for common Delver properties while retaining the exact JSON property name as a tooltip.
- Unknown/custom properties remain visible and editable and are explicitly described as preserved engine properties.
- Beginner-friendly value type names and Yes/No boolean creation.

## Asset picker pass
- Numeric `tex` fields now include a **Pick Sprite...** button.
- The sprite picker can open PNG/JPG sprite sheets, split them into a configurable tile grid, preview every tile, and write the selected tile index back to `tex`.
- Tile width/height can be adjusted for different atlases.
- The chosen sprite sheet is intentionally preview-only: the picker does not invent or overwrite atlas/path fields in the JSON.
- Advanced/raw JSON editing remains available at all times.

## Simple mod-folder workflow
- **Open Mod Folder** is now the primary workflow.
- The toolkit scans JSON/DAT content and presents beginner-facing **Items**, **Monsters**, **Spells**, **Levels**, and **Other Data** categories.
- Individual data-file opening remains available as an Advanced fallback.
- `+ New` creates a named starter object for Items, Monsters, and Spells instead of asking beginners to choose a JSON value type.
- The existing lossless form/raw editor and sprite picker remain available underneath the simplified UI.
