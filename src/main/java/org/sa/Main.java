package org.sa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.regex.Pattern;

public class Main {
  private static final Pattern TRKPT = Pattern.compile("<trkpt lat=\"([^\"]+)\" lon=\"([^\"]+)\">\\s*</trkpt>");

  public static void main(String[] args) throws IOException {
    Instant start = ZonedDateTime.of(2026, 11, 11, 8, 40, 0, 0, ZoneId.of("Europe/Madrid")).toInstant();
    Duration duration = Duration.ofMinutes(70).plusSeconds(33);
    Path inputPath = Path.of("src/main/resources/barcelona-hyrox.gpx");
    String input = Files.readString(inputPath);

    if (input.contains("<time>")) throw new IllegalStateException("File already contains <time> tags");


    // 1. collect points
    List<double[]> points = TRKPT.matcher(input).results()
        .map(r -> new double[] {Double.parseDouble(r.group(1)), Double.parseDouble(r.group(2))})
        .toList();
    if (points.size() < 2) throw new IllegalStateException("Expected at least 2 <trkpt> elements, found " + points.size());


    // 2. accumulate distance (metres) at each point
    double[] distanceAtPoint = new double[points.size()];
    for (int i = 1; i < points.size(); i++) {
      distanceAtPoint[i] = distanceAtPoint[i - 1] + haversine(points.get(i - 1), points.get(i));
    }
    double totalMeters = distanceAtPoint[distanceAtPoint.length - 1];
    System.out.printf("Points: %d, distance: %.2f km, pace: %.2f min/km%n", points.size(), totalMeters / 1000, duration.toSeconds() / 60.0 / (totalMeters / 1000));


    // 3. write a <time> into every point, proportional to distance covered
    int[] index = {0};
    String output = TRKPT.matcher(input).replaceAll(m -> {
      double fraction = distanceAtPoint[index[0]++] / totalMeters;
      Instant time = start.plusMillis(Math.round(duration.toMillis() * fraction));
      return "<trkpt lat=\"" + m.group(1) + "\" lon=\"" + m.group(2) + "\">" + "<time>" + time + "</time></trkpt>";
    });

    String outName = inputPath.getFileName().toString().replaceAll("\\.gpx$", "_TIMED.gpx");
    Files.writeString(inputPath.resolveSibling(outName), output);
  }

  /** Great-circle distance in metres between two {lat, lon} points. */
  private static double haversine(double[] a, double[] b) {
    final double R = 6_371_000;
    double dLat = Math.toRadians(b[0] - a[0]);
    double dLon = Math.toRadians(b[1] - a[1]);
    double h = Math.pow(Math.sin(dLat / 2), 2)
        + Math.cos(Math.toRadians(a[0])) * Math.cos(Math.toRadians(b[0])) * Math.pow(Math.sin(dLon / 2), 2);
    return 2 * R * Math.asin(Math.sqrt(h));
  }
}