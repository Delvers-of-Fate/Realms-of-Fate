# Realms of Fate Modding Toolkit - Safe JSON Core

This project started as the lightweight UI foundation. The current milestone adds the rebuilt JSON persistence core while retaining the existing toolkit UI.

Core rule: **Delver's loaded JSON is the source of truth.** The toolkit preserves unknown fields and does not reconstruct existing items from a hand-maintained schema.

See `README.md` for architecture, save guarantees, build instructions, and regression tests.
