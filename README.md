# REKOLT Planters' Cooperative Produce Tracker

A console application in Java that records deliveries from REKOLT's smallholder
planters, applies the cooperative's payment rules consistently, answers the
committee's end-of-season questions, and generates one Word document season
report.

## Build and run

Requires JDK 17+ and Maven.

```
mvn compile exec:java
```

Or, to build a runnable jar:

```
mvn package
java -cp target/rekolt-tracker-1.0.0.jar mu.rekolt.app.Main
```

## Project structure

```
src/main/java/mu/rekolt/
├── app/      entry point and console interaction
├── model/    Produce, CerealProduce, PerishableProduce, CashCropProduce, Member, Delivery, Grade
├── service/  grading, payment, reporting, document output
└── util/     validation and formatting
docs/         design documents, setup evidence, git history, rationale
output/       run-log.txt and the generated season-report.docx
```

## Objective 1 — type choices

- **`double` for mass, prices, multipliers and money.** Mass is a physical
  quantity that can be fractional (e.g. 236.5 kg), and every value derived
  from it (base value, commission, transport levy, net payable) inherits that
  precision. Money is kept unrounded through all five calculation steps and
  is only rounded to two decimals at the point of display, per the spec —
  rounding earlier would compound error across the five multiplications and
  subtractions.
- **`int` for the quality score.** The spec defines it as a whole number
  from 0 to 100; there is no meaningful fractional quality score, so `int`
  makes invalid values (e.g. `91.5`) impossible to represent rather than
  just invalid to enter.
- **Explicit cast:** `(int) (massKg / 50.0)` narrows a `double` to an `int`
  to count whole 50 kg sacks for an informational figure. This is the one
  place a fractional value is deliberately truncated, and it is kept
  separate from the money calculation so it never affects the net payable.

## Objective 6 note

The season's deliveries live only in the Java collections built in
Objective 3, for as long as the program runs. No delivery data is read from
or written to a file. The one exception is the season report itself, written
to `output/season-report.docx`, and a timestamped line appended to
`output/run-log.txt` each time it is generated.
