# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [Unreleased]
### Added
- Created TablesHandler.

### Changed
- Updated dependencies.
- Changed processBegin to ProcessBeginTable and poropcessEnd to ProcessEndTablek in TableHandler.
- Replaced String by Charset in several places.
- Added systemId parameter and used TablesHandler in SheetParser.

### Fixed


## [0.10.1] - 2021-06-05
### Fixed
- Interrupt office loading when asked by application. #1
- Do not force line wrap in ExcelWorkbookWriter.


## [0.10.0] - 2021-05-03
### Added
- First release, extracted from [cdc-utils](https://gitlab.com/cdc-java/cdc-util). cdc-java/cdc-util#41
