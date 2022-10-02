# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [Unreleased]
### Added
- Created `ksd` documentation. #19
- Added a `SheetParserFactory.Feature.DISABLE_VULNERABILITY_PROTECTIONS` to disable vulnerability detection
  such as Zip bombs. #33
- Added a new option to `ksd` to disable disable vulnerability detection. #33

### Changed
- Improved error message in `Header`.
- Renamed `KeyedTableDiff.Synthesis.Action.UNCHANGED` to `SAME`. #32
- Updated dependencies:
    - org.apache.log4j-2.19.0
    - org.apache.poi-5.2.3
    - org.junit-5.9.1

### Fixed
- Fixed the `--no-added-or-removed-marks`option in `ksd`. #31


## [0.24.0] - 2022-08-24
### Added
- Added `WorkbookWriter.addEmptyRows(count)`.

### Changed
- Updated dependencies:
    - cdc-io-0.23.0
    - cdc-kernel-0.20.6
    - cdc-tuples-1.2.0
    - cdc-util-0.28.0
    - org.junit-5.9.0

### Fixed
- Improved computation of the size of cell comments. #29
- Do not generate warning message for non supported content validation in `CsvWorkbookWriter`.
- Added a `FULL_CHECK` option to `MultiplyShiftHashSearcher`.


## [0.23.1] - 2022-07-07
### Changed
- Updated dependencies:
    - cdc-io-0.22.0
    - cdc-kernel-0.20.5
    - cdc-util-0.27.0
    - com.j2html-1.6.0
    - org.apache.log4j-2.18.0
    - org.junit-5.9.0-RC1

### Fixed
- Automatically limited size of error/help messages for data content validation. #26
- Better computation of the size of the comment box. Things can still be improved. #27


## [0.23.0] - 2022-06-18
### Added
- Added Data validation. At the moment it is only implement for Excel formats. #21
- Added `WorkbookWriterFeatures.Fature.CONTENT_VALIDATION`. #21

### Changed
- Improved error message when a key is missing in `KeyedSheetDiff`. #23
- Paths are now relative to args file location in `KeyedSheetDiff`. #22
- Updated dependencies:
    - cdc-io-0.21.3
    - cdc-kernel-0.20.4
    - cdc-util-0.26.0

### Fixed
- Excluded the invalid odf dependency in pom.xml. This exclusion should be remove in the future. cdc-java/cdc-deps#17
- Removed automatically created sheet when using odftoolkit. #24


## [0.22.0] - 2022-05-21
### Added    
- Numerical comparison of integer tail of strings in `KeyedTableDiffExporter`. #17
- Created `Header.Builder`. #16
- Added  `VERBOSE` option to `KeyedSheetDiff`. #18
- Added `SAVE_SYNTHESIS` option to `KeyedSheetDiff`. #20

### Changed
- Updated maven plugins
- Updated dependencies:
    - cdc-io-0.21.2
    - cdc-kernel-0.20.3
    - cdc-tuples-1.1.0
    - cdc-util-0.25.0
    - org.apache.poi-5.2.2

### Deprecated
- Deprecate `Header` constructors. #16


## [0.21.1] - 2022-03-11
### Changed
- Updated dependencies:
    - cdc-io-0.21.1
    - cdc-kernel-0.20.2
    - cdc-util-0.23.0
    - org.apache.log4j-2.17.2
    - org.apache.poi-5.2.1
- `Config` data is now retrieved from Manifest.


## [0.21.0] - 2022-02-13
### Added
- Added `CsvWriter` constructors using `OutputStream`.  #15
- Added creation of `WorkbookWriter` using an OutputStream. #15

### Changed
- Resources (`Writer`, `OutputStream`, `PrintStream`) that are passed to CsvWriter are not closed.  
  Some `CsvWriter` constructors have been deprecated. #15
- Updated dependencies
    - cdc-io-0.21.0
    - cdc-kernel-0.20.1
    - cdc-util-0.20.0

### Fixed
- Added support of '"' in `MultiplyShiftHashSearcher`.


## [0.20.0] - 2022-02-05
### Changed
- Upgraded to Java 11
- Updated dependencies
    - cdc-io-0.20.0
    - cdc-kernel-0.20.0
    - odftoolkit-0.10.0. Support of ODS is far from perfect. It was not before. #14
    - htmlflow-3.9


## [0.15.0] - 2022-01-28
### Added
- Created `VerboseTablesHandler`.
- Created `Issue12Test` to analyze issue #11. No change to CDC code was identified. #11

### Removed
- Removed deprecated methods `Row.getColumnsCount()`, `TableHandler.processBegin()`
  and `TableHandler.processEnd()`.

### Changed
- Changed API of HeaderMapper. Now it can handler mandatory and optional names.  
  The constructor is now private and a Builder has been added.  #13
- Updated maven plugins

### Fixed
- Fixed `PoiStreamSheetParser` and `PoiSaxSheetPrser` so that POI does not generate
  any warning message when a READ OPCPackage is closed.
  The following message:  
  *WARN org.apache.poi.openxml4j.opc.OPCPackage - The close() method is intended to SAVE a package. This package is open in READ ONLY mode, use the revert() method instead!*  
  should not happen anymore. #12


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
