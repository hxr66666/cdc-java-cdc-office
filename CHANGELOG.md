# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [Unreleased]
### Changed
- Changed API of HeaderMapper. Now it can handler mandatory and optional names.  
  The constructor is now private and a Builder has been added.  #13


## [0.14.2] - 2022-01-15
### Added
- Added `WorkbookWriter.addCellAndComment()`.

### Security
- Updated dependencies:
    - cdc-util-0.14.2
    - cdc-io-0.13.2
    - cdc-kernel-0.14.2
    - org.apache.log4j-2.17.1. #10
    - org.apache.poi-5.2.0 #10


## [0.14.1] - 2021-12-28
### Security
- Updated dependencies:
    - cdc-util-0.14.1
    - cdc-io-0.13.1
    - cdc-kernel-0.14.1
    - fastods-0.8.1
    - odftoolkit-0.9.0
    - org.apache.log4j-2.17.0. #10


## [0.14.0] - 2021-12-14
### Security
- Updated dependencies:
    - cdc-util-0.14.0
    - cdc-io-0.13.0
    - cdc-kernel-0.14.0
    - org.apache.log4j-2.16.0. #10


## [0.13.1] - 2021-10-03
### Changed
- Updated dependencies.

### Fixed
- Fixed warnings.


### Fixed
- invalid index and check in `KeyedTableDiff.getKey()`. #9


## [0.13.0] - 2021-10-01
### Added
- A new `NO_CELL_STYLES` feature was added to `WorkbookWriterFeatures`.  
  With POI, setting a style twice seems to be an issue.
  This new features may be used to let the user set styles. #7
- A new `SHOW_CHANGE_DETAILS` option was added to `KeyedSheetDiff`.  
  It is now possible to show both values on CHANGED cells.  #8

## Removed
- `DiffKind` has been removed and replaced by `CellDiffKind` and `RowDiffKind`. #7

### Fixed
- Colors were not working correctly in some cases with `KeyedSheetDiff`. #7 
- Computation of differences has been fixed. #7


## [0.12.3] - 2021-10-20
### Fixed
- Deployment issue with version 0.12.2.
  Replication to Maven Central took almost 20h.


## [0.12.2] - 2021-10-19
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
