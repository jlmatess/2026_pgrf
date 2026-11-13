import objectdata.ConvexPolygon;
import objectdata.LineSegment;
import objectdata.Point;
import objectdata.Polygon;
import objectops.CohenSutherland;
import objectops.SutherlandHodgman;
import rasterdata.RasterBI;
import rasterops.LineRasterizer;
import rasterops.LineRasterizerTrivial;
import rasterops.PolygonRasterizer;
import rasterops.ScanLine;
import rasterops.SeedFill;
import rasterops.SeedFill4;
import rasterops.SeedFill8;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Canvas {

    private final JFrame frame;
    private final JPanel panel;
    private final RasterBI image;

    private final LineRasterizer lineRasterizer = new LineRasterizerTrivial();
    private final PolygonRasterizer polygonRasterizer = new PolygonRasterizer();
    private final ScanLine scanLine = new ScanLine();
    private final SeedFill seedFill4 = new SeedFill4();
    private final SeedFill seedFill8 = new SeedFill8();
    private final SutherlandHodgman sutherlandHodgman = new SutherlandHodgman();
    private final CohenSutherland cohenSutherland = new CohenSutherland(150, 100, 650, 500);
    private final ConvexPolygon clippingPolygon = ConvexPolygon.of(new Polygon().withPoints(List.of(
            new Point(200, 120),
            new Point(600, 120),
            new Point(670, 320),
            new Point(400, 520),
            new Point(130, 320)
    ))).orElseThrow();

    private final List<LineSegment> lineSegmentList = new ArrayList<>();
    private Polygon polygon = new Polygon();

    private Point startPoint = null;
    private Point currentPoint = null;

    // 0 = POLYGON, 1 = USECKY, 2 = SEEDFILL
    private int mode = 0;
    private boolean scanLineEnabled = false;
    private boolean clippingEnabled = false;
    private boolean seedFillBorderMode = false;
    private boolean seedFill8Mode = false;
    private boolean patternFillEnabled = false;
    private Point seedPoint = null;

    private int draggedPointIndex = -1;
    private int fillColor = 0x3366FF;
    private int outlineColor = 0xFFFF00;
    private AbstractButton btnPattern;

    public Canvas(final int width, final int height) {
        frame = new JFrame();
        frame.setTitle("PGRF1 - Uloha 2 | Matěj Šír");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        image = new RasterBI(width, height);

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                image.present(g);
            }
        };

        panel.setPreferredSize(new Dimension(width, height));
        panel.setFocusable(true);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    draggedPointIndex = polygon.getNearestPointIndex(e.getX(), e.getY());
                } else if (mode == 2) {
                    seedPoint = new Point(e.getX(), e.getY());
                    redraw();
                } else {
                    startPoint = new Point(e.getX(), e.getY());
                    currentPoint = startPoint;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && draggedPointIndex != -1) {
                    polygon = polygon.withUpdatedPoint(draggedPointIndex, new Point(e.getX(), e.getY()));
                } else if (startPoint != null) {
                    currentPoint = (mode == 1 && e.isShiftDown())
                            ? snapPoint(startPoint, e.getX(), e.getY())
                            : new Point(e.getX(), e.getY());
                }
                redraw();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    draggedPointIndex = -1;
                } else if (mode != 2) {
                    if (startPoint != null && currentPoint != null) {
                        Point endPoint = (mode == 1 && e.isShiftDown())
                                ? snapPoint(startPoint, e.getX(), e.getY())
                                : new Point(e.getX(), e.getY());
                        if (mode == 1) {
                            lineSegmentList.add(new LineSegment(startPoint, endPoint));
                        } else {
                            if (polygon.getPoints().isEmpty()) {
                                polygon = polygon.withPoint(startPoint);
                            }
                            polygon = polygon.withPoint(endPoint);
                        }
                    }
                    startPoint = null;
                    currentPoint = null;
                }
                redraw();
            }
        };

        panel.addMouseListener(mouseAdapter);
        panel.addMouseMotionListener(mouseAdapter);

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_C -> clearAll();
                    case KeyEvent.VK_M -> {
                        mode = (mode == 1) ? 0 : 1;
                        System.out.println("Aktualni rezim: " + (mode == 1 ? "USECKY" : "POLYGON"));
                    }
                    case KeyEvent.VK_F -> {
                        mode = (mode == 2) ? 0 : 2;
                        System.out.println("Aktualni rezim: " + (mode == 2 ? "SEEDFILL" : "POLYGON"));
                    }
                    case KeyEvent.VK_B -> {
                        seedFillBorderMode = !seedFillBorderMode;
                        System.out.println("SeedFill omezeni: " + (seedFillBorderMode ? "BARVA HRANICE" : "BARVA POZADI"));
                        redraw();
                    }
                    case KeyEvent.VK_8 -> {
                        seedFill8Mode = !seedFill8Mode;
                        System.out.println("SeedFill sousednost: " + (seedFill8Mode ? "8-SOUSEDNOST" : "4-SOUSEDNOST"));
                        redraw();
                    }
                    case KeyEvent.VK_P -> {
                        patternFillEnabled = !patternFillEnabled;
                        btnPattern.setSelected(patternFillEnabled);
                        System.out.println("Vyplneni vzorem: " + (patternFillEnabled ? "ZAPNUTO" : "VYPNUTO"));
                        redraw();
                    }
                    case KeyEvent.VK_S -> {
                        scanLineEnabled = !scanLineEnabled;
                        System.out.println("ScanLine: " + (scanLineEnabled ? "ZAPNUTO" : "VYPNUTO"));
                        redraw();
                    }
                    case KeyEvent.VK_O -> {
                        clippingEnabled = !clippingEnabled;
                        System.out.println("Orezani: " + (clippingEnabled ? "ZAPNUTO" : "VYPNUTO"));
                        redraw();
                    }
                }
            }
        });

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));

        JButton btnFillColor = new JButton("Barva výplně");
        btnFillColor.setBackground(new Color(fillColor));
        btnFillColor.setForeground(Color.WHITE);
        btnFillColor.setFocusPainted(false);

        JButton btnOutlineColor = new JButton("Barva okraje");
        btnOutlineColor.setBackground(new Color(outlineColor));
        btnOutlineColor.setForeground(Color.BLACK);
        btnOutlineColor.setFocusPainted(false);

        btnPattern = new JToggleButton("Vzor (P)");
        btnPattern.setFocusPainted(false);

        btnFillColor.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(frame, "Vyber barvu výplně", new Color(fillColor));
            if (chosen != null) {
                fillColor = chosen.getRGB() & 0xFFFFFF;
                btnFillColor.setBackground(chosen);
                redraw();
            }
            panel.requestFocusInWindow();
        });

        btnOutlineColor.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(frame, "Vyber barvu okraje", new Color(outlineColor));
            if (chosen != null) {
                outlineColor = chosen.getRGB() & 0xFFFFFF;
                btnOutlineColor.setBackground(chosen);
                redraw();
            }
            panel.requestFocusInWindow();
        });

        btnPattern.addActionListener(e -> {
            patternFillEnabled = btnPattern.isSelected();
            redraw();
            panel.requestFocusInWindow();
        });

        buttonsPanel.add(btnFillColor);
        buttonsPanel.add(btnOutlineColor);
        buttonsPanel.add(btnPattern);

        JLabel helpLabel = new JLabel("[M] Režim | [F] SeedFill | [B] Pozadí/Hranice | [8] 4/8-soused | [S] ScanLine | [O] Ořezání | [C] Smazat");
        helpLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        helpLabel.setForeground(Color.DARK_GRAY);

        helpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        helpLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        topBar.add(buttonsPanel, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.CENTER);
        frame.add(topBar, BorderLayout.NORTH);
        frame.add(helpLabel, BorderLayout.SOUTH);

        frame.pack();
        frame.add(panel, BorderLayout.CENTER);
        frame.add(topBar, BorderLayout.NORTH);

        frame.pack();
        frame.setVisible(true);
        panel.requestFocusInWindow();

        redraw();
    }

    private Point snapPoint(Point start, double x, double y) {
        double dx = x - start.x();
        double dy = y - start.y();
        double absDx = Math.abs(dx);
        double absDy = Math.abs(dy);

        if (absDx > 2 * absDy) {
            return new Point(x, start.y()); // Vodorovna
        } else if (absDy > 2 * absDx) {
            return new Point(start.x(), y); // Svisla
        } else {
            double len = Math.max(absDx, absDy); // Uhlopricna 45°
            return new Point(start.x() + Math.signum(dx) * len, start.y() + Math.signum(dy) * len);
        }
    }

    private RasterBI createPattern() {
        RasterBI pattern = new RasterBI(8, 8);
        pattern.clear(0x004488);
        for (int i = 0; i < 8; i++) {
            pattern.setColor(i, i, 0x00FFFF);
            pattern.setColor(7 - i, i, 0x00FFFF);
        }
        return pattern;
    }

    private void redraw() {
        image.clear(0x000000);

        for (LineSegment segment : lineSegmentList) {
            lineRasterizer.draw(image, segment.p1().x(), segment.p1().y(), segment.p2().x(), segment.p2().y(), 0x00FF00);
        }

        if (scanLineEnabled && polygon.getPoints().size() >= 3) {
            if (patternFillEnabled) {
                scanLine.fill(image, polygon, createPattern(), outlineColor, polygonRasterizer, lineRasterizer);
            } else {
                scanLine.fill(image, polygon, fillColor, outlineColor, polygonRasterizer, lineRasterizer);
            }
        } else {
            polygonRasterizer.draw(image, polygon, lineRasterizer, outlineColor);
        }

        if (clippingEnabled) {
            // Orezavaci obdelnik pro Cohen-Sutherland (bily)
            lineRasterizer.draw(image, 150, 100, 650, 100, 0xFFFFFF);
            lineRasterizer.draw(image, 650, 100, 650, 500, 0xFFFFFF);
            lineRasterizer.draw(image, 650, 500, 150, 500, 0xFFFFFF);
            lineRasterizer.draw(image, 150, 500, 150, 100, 0xFFFFFF);

            // Orezane usecky (azurove)
            for (LineSegment segment : lineSegmentList) {
                Optional<LineSegment> clipped = cohenSutherland.clip(segment);
                clipped.ifPresent(s -> lineRasterizer.draw(image, s.p1().x(), s.p1().y(), s.p2().x(), s.p2().y(), 0x00FFFF));
            }

            // Orezavaci 5-uhelnik pro Sutherland-Hodgman (fialovy)
            polygonRasterizer.draw(image, clippingPolygon, lineRasterizer, 0xFF00FF);

            // Orezany polygon (oranzovy)
            if (polygon.getPoints().size() >= 3) {
                Polygon clippedPolygon = sutherlandHodgman.clip(polygon, clippingPolygon);
                if (clippedPolygon.getPoints().size() >= 3) {
                    scanLine.fill(image, clippedPolygon, 0xFF8800, 0xFFAA00, polygonRasterizer, lineRasterizer);
                }
            }
        }

        if (mode == 2 && seedPoint != null)  {
            int sx = (int) seedPoint.x();
            int sy = (int) seedPoint.y();
            image.getColor(sx, sy).ifPresent(clickedColor -> {
                SeedFill filler = seedFill8Mode ? seedFill8 : seedFill4;
                var predicate = seedFillBorderMode
                        ? SeedFill.checkBorder(0xFF000000 | outlineColor)
                        : SeedFill.checkBackground(clickedColor);
                if (patternFillEnabled) {
                    filler.fill(image, sx, sy, predicate, createPattern());
                } else {
                    filler.fill(image, sx, sy, predicate, fillColor);
                }
            });
        }

        if (startPoint != null && currentPoint != null) {
            if (mode == 1) {
                lineRasterizer.draw(image, startPoint.x(), startPoint.y(), currentPoint.x(), currentPoint.y(), 0xFF0000);
            } else if (mode == 0) {
                List<Point> points = polygon.getPoints();
                if (points.isEmpty()) {
                    lineRasterizer.draw(image, startPoint.x(), startPoint.y(), currentPoint.x(), currentPoint.y(), 0xFF0000);
                } else {
                    Point first = points.get(0);
                    Point last = points.get(points.size() - 1);
                    lineRasterizer.draw(image, last.x(), last.y(), currentPoint.x(), currentPoint.y(), 0xFF0000);
                    lineRasterizer.draw(image, currentPoint.x(), currentPoint.y(), first.x(), first.y(), 0xFF0000);
                }
            }
        }

        panel.repaint();
    }

    private void clearAll() {
        lineSegmentList.clear();
        polygon = new Polygon();
        startPoint = null;
        currentPoint = null;
        seedPoint = null;
        scanLineEnabled = false;
        clippingEnabled = false;
        redraw();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Canvas(800, 600));
    }
}