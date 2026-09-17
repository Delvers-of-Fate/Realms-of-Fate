# Safe JSON Core Rebuild

## Fixed

- Replaced unsafe LibGDX `JsonValue.addChild/remove` mutations with `JsonTree`.
- Replaced recursive hand-built deep copies with parse-based lossless copies.
- Existing item objects are now replaced/moved as complete JSON objects instead of whitelisting known Sword/Bow fields.
- Unknown item, monster, spell, class, nested object, and array fields survive edits.
- Fixed Wand `chargeSpeed` output to Delver's `chargespeed` field.
- Added structural link repair and validation before every `.json` / `.dat` save.
- Added temp-file verification, `.bak` backup, and atomic replacement when supported.
- Removed the toolkit's build-time dependency on the entire `:Dungeoneer` project; it now depends directly on LibGDX 1.9.9 and FlatLaf.

## Regression coverage

`JsonPersistenceRegressionTest` reproduces the original sibling-loss bug and verifies safe round trips, unknown-field preservation, whole-object item replacement, and backup creation.
