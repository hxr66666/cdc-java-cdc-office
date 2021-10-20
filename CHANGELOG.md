# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [0.12.3] - 2021-10-15
### Fixed
- Deployment issue with version 0.12.2


## [0.12.2] - 2021-10-14 [YANKED]
### Changed
- Renamed package `cdc.office.ss.tools` to `cdc.office.tools`. #5

### Fixed
- Infinite recursion in CsvParser.parse(). #6


## [0.12.1] - 2021-10-03
### Fixed
- Fixed regression in handling of colors in `KeyedTableDiffExporter`. #4


## [0.12.0] - 2021-10-02
### Added
- Created `KeyedTableDiffExporter`. cdc-java/cdc-applic#84
- Added `Row.LEXICOGRAPHIC_COMPARATOR`.
- Added `SheetParserFactory.Feature.EVALUATE_FORMULA`to force fresh evaluation
  of formula.  
  This is disabled by default. It can consume more CPU and memory.
  At the moment, it is only used in StandardPOI.  
  It should used with care.
  When not used, formatting of result may be incorrect. #3

### Changed
- `Rows.toExtract` handles negative numbers.

### Fixed
- When parsing sheets, returns value instead of formula when reading XLS
  or using STANDARD_POI. #3


## [0.11.0] - 2021-07-23
### Added
- Created `TablesHandler`.

### Changed
- Updated dependencies.
- Changed processBegin to processBeginTable and processEnd to processEndTable in TableHandler.
- Replaced String parameter with Charset in several places.
- Added systemId parameter and used TablesHandler in SheetParser.

### Fixed
- Now, when cell value is empty, returns def, in Row.getValue. #2


## [0.10.1] - 2021-06-05
### Fixed
- Interrupt office loading when asked by application. #1
- Do not force line wrap in ExcelWorkbookWriter.


## [0.10.0] - 2021-05-03
### Added
- First release, extracted from [cdc-utils](https://gitlab.com/cdc-java/cdc-util). cdc-java/cdc-util#41
