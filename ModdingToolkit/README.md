# Realms of Fate Modding Toolkit

A desktop editor for Realms of Fate / Delver JSON data.

## Current design

The UI remains intentionally friendly and specialized, but the JSON document is now the source of truth.

The persistence stack is:

```
UI editors
    -> data views (items, monsters, spells, classes)
        -> JsonTree safe mutations
            -> JsonDocumentService
                -> .json / .dat
```

The data layer does **not** rebuild items from a schema or whitelist when saving. Existing objects are copied in full, edited in place, and written back with unknown fields preserved.

## Save guarantees

`JsonDocumentService` now:

- repairs LibGDX `JsonValue` parent / sibling / size links before serialization;
- validates the generated JSON before replacing the original file;
- writes to a temporary file first;
- parses the exact temporary bytes back for verification;
- creates `<filename>.bak` before replacing an existing file;
- uses an atomic move when the filesystem supports it.

`JsonTree` is the only place that should add, remove, replace, or move `JsonValue` children. This is important because Delver's LibGDX 1.9.9 `JsonValue.addChild` does not maintain all sibling bookkeeping used by `remove`.

## Regression test

`JsonPersistenceRegressionTest` covers the original data-loss bug and verifies:

- removing a middle field from a copied object does not delete neighboring fields;
- a no-edit deep-copy round trip preserves the document data;
- editing one property preserves unknown nested fields;
- saving an item replaces the complete object and creates a backup.

Run it with Gradle:

```bash
gradle regressionTest
```

## Build

```bash
gradle dist
```

The runnable JAR is written to `build/libs/Realms-of-Fate-Modding-Toolkit.jar`.
