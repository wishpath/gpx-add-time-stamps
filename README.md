# gpx-add-time-stamps

## What it does
- Adds time stamps to GPX file (with an even pace throughout)
- Generates output file with `_TIMED.gpx` suffix.

## Input
- provided GPX file (with no timestamps)
- `start` and `duration` variables.

## Timestamps
- The first point gets the start time.
- The last point gets the start time plus the duration.
- Points in between are spaced proportionally to the distance covered (even pace).

## Usage
1. Put your `.gpx` file in `src/main/resources/`.
2. Configure `Main.java`:
   - `start`: target start time and time zone
   - `duration`: total duration
   - `inputPath`: input file path (default `src/main/resources/barcelona-hyrox.gpx`)
3. Run `Main`, or from the terminal
4. Find output file in `src/main/resources` directory