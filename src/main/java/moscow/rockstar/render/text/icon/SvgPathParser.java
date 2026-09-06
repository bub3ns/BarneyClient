package moscow.rockstar.render.text.icon;

import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import moscow.rockstar.render.text.glyph.GlyphOutline;

/** Parses the SVG primitive/path subset used by the original icon resources. */
public final class SvgPathParser {
    private static final Pattern ELEMENT_PATTERN = Pattern.compile(
        "<(path|rect|circle|ellipse|polygon|line)\\b([^>]*)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(
        "([a-zA-Z-]+)\\s*=\\s*\"([^\"]*)\"");
    private static final Pattern NUMBER_PATTERN = Pattern.compile(
        "[-+]?(?:\\d*\\.\\d+|\\d+)(?:[eE][-+]?\\d+)?");
    private static final Pattern PATH_COMMAND_PATTERN = Pattern.compile(
        "([MmLlHhVvCcSsQqTtAaZz])([^MmLlHhVvCcSsQqTtAaZz]*)");
    private static final Pattern NON_RENDERING_ELEMENT_PATTERN = Pattern.compile(
        "<(defs|clipPath|mask|pattern|symbol|filter)\\b[\\s\\S]*?</\\1\\s*>",
        Pattern.CASE_INSENSITIVE);
    private static final double CUBIC_FLATNESS_TOLERANCE = 0.890625;

    private SvgPathParser() {
    }

    public static GlyphOutline parse(String svg) {
        Path2D.Double path = new Path2D.Double(Path2D.WIND_NON_ZERO);
        String content = NON_RENDERING_ELEMENT_PATTERN.matcher(svg).replaceAll("");
        Matcher elements = ELEMENT_PATTERN.matcher(content);
        while (elements.find()) {
            appendElement(path, elements.group(1).toLowerCase(), parseAttributes(elements.group(2)));
        }

        Area area = new Area(path);
        Rectangle2D bounds = area.getBounds2D();
        if (bounds.isEmpty()) {
            return GlyphOutline.fromShape(area, 1.0f);
        }

        double size = Math.max(bounds.getWidth(), bounds.getHeight());
        double scale = CUBIC_FLATNESS_TOLERANCE / size;
        AffineTransform transform = new AffineTransform();
        transform.translate(0.0, -2048.0);
        transform.translate(1024.0, 1024.0);
        transform.scale(2048.0 * scale, 2048.0 * scale);
        transform.translate(-bounds.getCenterX(), -bounds.getCenterY());
        return GlyphOutline.fromShape(transform.createTransformedShape(area), 1.0f);
    }

    private static Map<String, String> parseAttributes(String source) {
        Map<String, String> attributes = new HashMap<>();
        Matcher matcher = ATTRIBUTE_PATTERN.matcher(source);
        while (matcher.find()) {
            attributes.put(matcher.group(1).toLowerCase(), matcher.group(2));
        }
        return attributes;
    }

    private static void appendElement(Path2D.Double target, String type, Map<String, String> attributes) {
        if ("none".equalsIgnoreCase(attributes.getOrDefault("fill", ""))) {
            return;
        }

        Path2D.Double shape = new Path2D.Double(Path2D.WIND_NON_ZERO);
        switch (type) {
            case "path" -> appendPath(shape, attributes.getOrDefault("d", ""));
            case "rect" -> {
                double x = getAttribute(attributes, "x", 0.0);
                double y = getAttribute(attributes, "y", 0.0);
                double width = getAttribute(attributes, "width", 0.0);
                double height = getAttribute(attributes, "height", 0.0);
                double rx = getAttribute(attributes, "rx", 0.0);
                double ry = getAttribute(attributes, "ry", rx);
                if (rx > 0.0 || ry > 0.0) {
                    double radius = Math.max(rx, ry) * 2.0;
                    shape.append(new RoundRectangle2D.Double(x, y, width, height, radius, radius), false);
                } else {
                    shape.append(new Rectangle2D.Double(x, y, width, height), false);
                }
            }
            case "circle" -> {
                double radius = getAttribute(attributes, "r", 0.0);
                double cx = getAttribute(attributes, "cx", 0.0);
                double cy = getAttribute(attributes, "cy", 0.0);
                shape.append(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2.0, radius * 2.0), false);
            }
            case "ellipse" -> {
                double rx = getAttribute(attributes, "rx", 0.0);
                double ry = getAttribute(attributes, "ry", 0.0);
                double cx = getAttribute(attributes, "cx", 0.0);
                double cy = getAttribute(attributes, "cy", 0.0);
                shape.append(new Ellipse2D.Double(cx - rx, cy - ry, rx * 2.0, ry * 2.0), false);
            }
            case "polygon" -> {
                List<Double> points = parseNumbers(attributes.getOrDefault("points", ""));
                for (int index = 0; index + 1 < points.size(); index += 2) {
                    if (index == 0) {
                        shape.moveTo(points.get(index), points.get(index + 1));
                    } else {
                        shape.lineTo(points.get(index), points.get(index + 1));
                    }
                }
                shape.closePath();
            }
            case "line" -> {
                shape.moveTo(getAttribute(attributes, "x1", 0.0), getAttribute(attributes, "y1", 0.0));
                shape.lineTo(getAttribute(attributes, "x2", 0.0), getAttribute(attributes, "y2", 0.0));
            }
            default -> {
                return;
            }
        }

        AffineTransform transform = parseTransform(attributes.get("transform"));
        target.append(transform == null ? shape : transform.createTransformedShape(shape), false);
    }

    private static AffineTransform parseTransform(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        AffineTransform transform = new AffineTransform();
        Matcher matcher = Pattern.compile("([a-zA-Z]+)\\s*\\(([^)]*)\\)").matcher(source);
        while (matcher.find()) {
            List<Double> values = parseNumbers(matcher.group(2));
            switch (matcher.group(1).toLowerCase()) {
                case "translate" -> {
                    transform.translate(values.get(0), values.size() > 1 ? values.get(1) : 0.0);
                }
                case "scale" -> {
                    transform.scale(values.get(0), values.size() > 1 ? values.get(1) : values.get(0));
                }
                case "rotate" -> {
                    if (values.size() >= 3) {
                        transform.rotate(Math.toRadians(values.get(0)), values.get(1), values.get(2));
                    } else {
                        transform.rotate(Math.toRadians(values.get(0)));
                    }
                }
                case "matrix" -> {
                    if (values.size() >= 6) {
                        transform.concatenate(new AffineTransform(
                            values.get(0), values.get(1), values.get(2), values.get(3), values.get(4), values.get(5)));
                    }
                }
                default -> {
                }
            }
        }
        return transform;
    }

    private static double getAttribute(Map<String, String> attributes, String name, double fallback) {
        String value = attributes.get(name);
        if (value == null) {
            return fallback;
        }
        List<Double> numbers = parseNumbers(value);
        return numbers.isEmpty() ? fallback : numbers.get(0);
    }

    private static List<Double> parseNumbers(String source) {
        List<Double> values = new ArrayList<>();
        Matcher matcher = NUMBER_PATTERN.matcher(source);
        while (matcher.find()) {
            values.add(Double.parseDouble(matcher.group()));
        }
        return values;
    }

    private static void appendPath(Path2D.Double path, String source) {
        double currentX = 0.0;
        double currentY = 0.0;
        double subpathX = 0.0;
        double subpathY = 0.0;
        double previousCubicX = 0.0;
        double previousCubicY = 0.0;
        double previousQuadraticX = 0.0;
        double previousQuadraticY = 0.0;
        int previousCommand = 32;

        Matcher commands = PATH_COMMAND_PATTERN.matcher(source);
        while (commands.find()) {
            char command = commands.group(1).charAt(0);
            List<Double> values = parseNumbers(commands.group(2));
            boolean relative = Character.isLowerCase(command);
            int commandType = Character.toUpperCase(command);
            int valueIndex = 0;
            do {
                switch (commandType) {
                    case 'M' -> {
                        if (values.size() < valueIndex + 2) {
                            break;
                        }
                        currentX = relative ? currentX + values.get(valueIndex) : values.get(valueIndex);
                        currentY = relative ? currentY + values.get(valueIndex + 1) : values.get(valueIndex + 1);
                        if (valueIndex == 0) {
                            path.moveTo(currentX, currentY);
                            subpathX = currentX;
                            subpathY = currentY;
                        } else {
                            path.lineTo(currentX, currentY);
                        }
                        valueIndex += 2;
                    }
                    case 'L' -> {
                        if (values.size() < valueIndex + 2) {
                            break;
                        }
                        currentX = relative ? currentX + values.get(valueIndex) : values.get(valueIndex);
                        currentY = relative ? currentY + values.get(valueIndex + 1) : values.get(valueIndex + 1);
                        path.lineTo(currentX, currentY);
                        valueIndex += 2;
                    }
                    case 'H' -> {
                        if (values.size() < valueIndex + 1) {
                            break;
                        }
                        currentX = relative ? currentX + values.get(valueIndex) : values.get(valueIndex);
                        path.lineTo(currentX, currentY);
                        valueIndex++;
                    }
                    case 'V' -> {
                        if (values.size() < valueIndex + 1) {
                            break;
                        }
                        currentY = relative ? currentY + values.get(valueIndex) : values.get(valueIndex);
                        path.lineTo(currentX, currentY);
                        valueIndex++;
                    }
                    case 'C' -> {
                        if (values.size() < valueIndex + 6) {
                            break;
                        }
                        double control1X = relative ? currentX + values.get(valueIndex) : values.get(valueIndex);
                        double control1Y = relative ? currentY + values.get(valueIndex + 1) : values.get(valueIndex + 1);
                        double control2X = relative ? currentX + values.get(valueIndex + 2) : values.get(valueIndex + 2);
                        double control2Y = relative ? currentY + values.get(valueIndex + 3) : values.get(valueIndex + 3);
                        currentX = relative ? currentX + values.get(valueIndex + 4) : values.get(valueIndex + 4);
                        currentY = relative ? currentY + values.get(valueIndex + 5) : values.get(valueIndex + 5);
                        path.curveTo(control1X, control1Y, control2X, control2Y, currentX, currentY);
                        previousCubicX = control2X;
                        previousCubicY = control2Y;
                        valueIndex += 6;
                    }
                    case 'S' -> {
                        if (values.size() < valueIndex + 4) {
                            break;
                        }
                        boolean reflect = previousCommand == 'C' || previousCommand == 'S';
                        double control1X = reflect ? 2.0 * currentX - previousCubicX : currentX;
                        double control1Y = reflect ? 2.0 * currentY - previousCubicY : currentY;
                        double control2X = relative ? currentX + values.get(valueIndex) : values.get(valueIndex);
                        double control2Y = relative ? currentY + values.get(valueIndex + 1) : values.get(valueIndex + 1);
                        currentX = relative ? currentX + values.get(valueIndex + 2) : values.get(valueIndex + 2);
                        currentY = relative ? currentY + values.get(valueIndex + 3) : values.get(valueIndex + 3);
                        path.curveTo(control1X, control1Y, control2X, control2Y, currentX, currentY);
                        previousCubicX = control2X;
                        previousCubicY = control2Y;
                        valueIndex += 4;
                    }
                    case 'Q' -> {
                        if (values.size() < valueIndex + 4) {
                            break;
                        }
                        double controlX = relative ? currentX + values.get(valueIndex) : values.get(valueIndex);
                        double controlY = relative ? currentY + values.get(valueIndex + 1) : values.get(valueIndex + 1);
                        currentX = relative ? currentX + values.get(valueIndex + 2) : values.get(valueIndex + 2);
                        currentY = relative ? currentY + values.get(valueIndex + 3) : values.get(valueIndex + 3);
                        path.quadTo(controlX, controlY, currentX, currentY);
                        previousQuadraticX = controlX;
                        previousQuadraticY = controlY;
                        valueIndex += 4;
                    }
                    case 'T' -> {
                        if (values.size() < valueIndex + 2) {
                            break;
                        }
                        boolean reflect = previousCommand == 'Q' || previousCommand == 'T';
                        double controlX = reflect ? 2.0 * currentX - previousQuadraticX : currentX;
                        double controlY = reflect ? 2.0 * currentY - previousQuadraticY : currentY;
                        currentX = relative ? currentX + values.get(valueIndex) : values.get(valueIndex);
                        currentY = relative ? currentY + values.get(valueIndex + 1) : values.get(valueIndex + 1);
                        path.quadTo(controlX, controlY, currentX, currentY);
                        previousQuadraticX = controlX;
                        previousQuadraticY = controlY;
                        valueIndex += 2;
                    }
                    case 'A' -> {
                        if (values.size() < valueIndex + 7) {
                            break;
                        }
                        double radiusX = values.get(valueIndex);
                        double radiusY = values.get(valueIndex + 1);
                        double rotation = values.get(valueIndex + 2);
                        boolean largeArc = values.get(valueIndex + 3) != 0.0;
                        boolean sweep = values.get(valueIndex + 4) != 0.0;
                        double endX = relative ? currentX + values.get(valueIndex + 5) : values.get(valueIndex + 5);
                        double endY = relative ? currentY + values.get(valueIndex + 6) : values.get(valueIndex + 6);
                        appendArc(path, currentX, currentY, radiusX, radiusY, rotation, largeArc, sweep, endX, endY);
                        currentX = endX;
                        currentY = endY;
                        valueIndex += 7;
                    }
                    case 'Z' -> {
                        path.closePath();
                        currentX = subpathX;
                        currentY = subpathY;
                        valueIndex = values.size();
                    }
                    default -> valueIndex = values.size();
                }

                previousCommand = commandType;
                if (commandType == 'M') {
                    commandType = 'L';
                }
            } while (valueIndex < values.size() && commandType != 'Z');
        }
    }

    private static void appendArc(Path2D.Double path, double startX, double startY,
                                  double radiusX, double radiusY, double rotation,
                                  boolean largeArc, boolean sweep, double endX, double endY) {
        if (radiusX == 0.0 || radiusY == 0.0) {
            path.lineTo(endX, endY);
            return;
        }

        double angle = Math.toRadians(rotation);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double dx = (startX - endX) * 0.5;
        double dy = (startY - endY) * 0.5;
        double rotatedX = cos * dx + sin * dy;
        double rotatedY = -sin * dx + cos * dy;
        radiusX = Math.abs(radiusX);
        radiusY = Math.abs(radiusY);
        double radiiCorrection = rotatedX * rotatedX / (radiusX * radiusX)
            + rotatedY * rotatedY / (radiusY * radiusY);
        if (radiiCorrection > 1.0) {
            double correction = Math.sqrt(radiiCorrection);
            radiusX *= correction;
            radiusY *= correction;
        }

        double sign = largeArc == sweep ? -1.0 : 1.0;
        double numerator = radiusX * radiusX * radiusY * radiusY
            - radiusX * radiusX * rotatedY * rotatedY
            - radiusY * radiusY * rotatedX * rotatedX;
        double denominator = radiusX * radiusX * rotatedY * rotatedY
            + radiusY * radiusY * rotatedX * rotatedX;
        double factor = sign * Math.sqrt(Math.max(numerator / denominator, 0.0));
        double centerXRotated = factor * radiusX * rotatedY / radiusY;
        double centerYRotated = -factor * radiusY * rotatedX / radiusX;
        double centerX = cos * centerXRotated - sin * centerYRotated + (startX + endX) * 0.5;
        double centerY = sin * centerXRotated + cos * centerYRotated + (startY + endY) * 0.5;
        double startAngle = Math.atan2((rotatedY - centerYRotated) / radiusY,
            (rotatedX - centerXRotated) / radiusX);
        double endAngle = Math.atan2((-rotatedY - centerYRotated) / radiusY,
            (-rotatedX - centerXRotated) / radiusX);
        double sweepAngle = endAngle - startAngle;
        if (!sweep && sweepAngle > 0.0) {
            sweepAngle -= Math.PI * 2.0;
        }
        if (sweep && sweepAngle < 0.0) {
            sweepAngle += Math.PI * 2.0;
        }

        Arc2D.Double arc = new Arc2D.Double(centerX - radiusX, centerY - radiusY,
            radiusX * 2.0, radiusY * 2.0, -Math.toDegrees(startAngle),
            -Math.toDegrees(sweepAngle), Arc2D.OPEN);
        AffineTransform transform = AffineTransform.getRotateInstance(angle, centerX, centerY);
        path.append(transform.createTransformedShape(arc), true);
    }
}
