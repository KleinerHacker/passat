<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Passat Changelog

## [Unreleased]

### Added

- FPC SDK now reads compiled units with `ppudump`: an FPC home must include the `ppudump` tool
  (the SDK is rejected with guidance to install a fuller FPC distribution when only the compiler is
  present). On adding an SDK, every `.ppu` is decoded via `ppudump -Fjson` into Kotlin model objects
  and indexed/persisted, so `uses` completion offers each unit's real internal name (e.g. `System`)
  instead of its lower-cased file name, and a unit's interface symbols (types, classes, procedures,
  functions, consts, vars) are cached for later use.
