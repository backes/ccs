package att.grappa;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.Enumeration;

import javax.swing.JScrollBar;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * A class used for drawing the graph.
 *
 * @version 1.2, 10 Oct 2006; Copyright 1996 - 2006 by AT&T Corp.
 * @author <a href="mailto:john@research.att.com">John Mocenigo</a>, <a
 * href="http://www.research.att.com">Research @ AT&T Labs</a>
 */
public class GrappaPanel extends javax.swing.JPanel implements
        att.grappa.GrappaConstants, ComponentListener, AncestorListener,
        PopupMenuListener, MouseListener, MouseMotionListener, Printable,
        Scrollable {

    private static final long serialVersionUID = -5985995332514870244L;

    private final Graph graph;
    private final Subgraph subgraph;
    private final GrappaBacker backer;
    private boolean nodeLabels, edgeLabels, subgLabels;
    private AffineTransform transform = null;
    private AffineTransform oldTransform = null;
    private AffineTransform inverseTransform = null;

    private boolean scaleToFit = false;
    private GrappaSize scaleToSize = null;
    private GrappaListener grappaListener = null;

    private Element pressedElement = null;
    private GrappaPoint pressedPoint = null;
    private int pressedModifiers = 0;
    private GrappaStyle selectionStyle = null;
    private GrappaStyle deletionStyle = null;
    private double scaleFactor = 1;
    private GrappaBox outline = null;
    private GrappaBox savedOutline = null;
    private GrappaBox zoomBox = null;
    private boolean inMenu = false;

    private Point2D panelcpt = null;

    private Dimension sizeNeeded;

    /**
     * Constructs a new canvas associated with a particular subgraph. Keep in
     * mind that Graph is a sub-class of Subgraph so that usually a Graph object
     * is passed to the constructor.
     *
     * @param subgraph
     *            the subgraph to be rendered on the canvas
     */
    public GrappaPanel(Subgraph subgraph) {
        this(subgraph, null);
    }

    /**
     * Constructs a new canvas associated with a particular subgraph.
     *
     * @param subgraph
     *            the subgraph to be rendered on the canvas.
     * @param backer
     *            used to draw a background for the graph.
     */
    public GrappaPanel(Subgraph subgraph, GrappaBacker backer) {
        super();
        this.subgraph = subgraph;
        this.backer = backer;
        this.graph = subgraph.getGraph();

        addAncestorListener(this);
        addComponentListener(this);

        selectionStyle =
                (GrappaStyle) (graph
                    .getGrappaAttributeValue(GRAPPA_SELECTION_STYLE_ATTR));
        deletionStyle =
                (GrappaStyle) (graph
                    .getGrappaAttributeValue(GRAPPA_DELETION_STYLE_ATTR));
    }

    /**
     * Adds the specified listener to receive mouse events from this graph.
     *
     * @param listener
     *            the event listener.
     * @return the previous event listener.
     *
     * @see GrappaAdapter
     */
    public GrappaListener addGrappaListener(GrappaListener listener) {
        final GrappaListener oldGL = grappaListener;
        grappaListener = listener;
        if (grappaListener == null) {
            if (oldGL != null) {
                removeMouseListener(this);
                removeMouseMotionListener(this);
            }
            setToolTipText(null);
        } else {
            if (oldGL == null) {
                addMouseListener(this);
                addMouseMotionListener(this);
            }
            String tip = graph.getToolTipText();
            if (tip == null) {
                tip = Grappa.getToolTipText();
            }
            setToolTipText(tip);
        }
        return (oldGL);
    }

    /**
     * Removes the current listener from this graph. Equivalent to
     * <TT>addGrappaListener(null)</TT>.
     *
     * @return the event listener just removed.
     */
    public GrappaListener removeGrappaListener() {
        return (addGrappaListener(null));
    }

    public int print(Graphics g, PageFormat pf, int pi) {
        final GrappaSize prevToSize = scaleToSize;
        final boolean prevToFit = scaleToFit;

        if (pi >= 1) {
            return Printable.NO_SUCH_PAGE;
        }
        try {
            scaleToFit = false;
            scaleToSize =
                    new GrappaSize(pf.getImageableWidth(), pf
                        .getImageableHeight());
            ((Graphics2D) g).translate(pf.getImageableX(), pf.getImageableY());
            paintComponent(g);
        } finally {
            scaleToSize = prevToSize;
            scaleToFit = prevToFit;
        }
        return Printable.PAGE_EXISTS;
    }


    @Override
    public void paintComponent(Graphics g) {
        if (Grappa.synchronizePaint || graph.getSynchronizePaint()) {
            if (graph.setPaint(true)) {
                componentPaint(g);
                graph.setPaint(false);
            }
        } else {
            componentPaint(g);
        }
    }

    void setCPT(Point2D cpt) {
        panelcpt = cpt;
    }

    Point2D getCPT() {
        return panelcpt;
    }

    private void componentPaint(Graphics g) {
        if (subgraph == null || !subgraph.reserve())
            return;

        final Graphics2D g2d = (Graphics2D) g;

        final GrappaBox bbox = new GrappaBox(subgraph.getBoundingBox());

        final GrappaSize margins =
                (GrappaSize) (subgraph.getAttributeValue(MARGIN_ATTR));

        if (margins != null) {
            final double x_margin = PointsPerInch * margins.width;
            final double y_margin = PointsPerInch * margins.height;

            bbox.x -= x_margin;
            bbox.y -= y_margin;
            bbox.width += 2.0 * x_margin;
            bbox.height += 2.0 * y_margin;
        }

        subgLabels = subgraph.getShowSubgraphLabels();
        nodeLabels = subgraph.getShowNodeLabels();
        edgeLabels = subgraph.getShowEdgeLabels();
        if (Grappa.useAntiAliasing) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        }
        if (Grappa.antiAliasText) {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        } else {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }
        if (Grappa.useFractionalMetrics) {
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        }
        g2d.setStroke(GrappaStyle.defaultStroke);

        oldTransform = transform;
        transform = new AffineTransform();
        double scaleInfo = 1;
        if (scaleToFit || scaleToSize != null) {
            scaleFactor = 1;
            zoomBox = null;
            final double scaleToWidth = scaleToFit ? getWidth() : scaleToSize.width;
            final double scaleToHeight = scaleToFit ? getHeight() : scaleToSize.height;
            final double widthRatio = scaleToWidth / bbox.getWidth();
            final double heightRatio = scaleToHeight / bbox.getHeight();
            double xTranslate = 0;
            double yTranslate = 0;
            if (widthRatio < heightRatio) {
                xTranslate =
                        (scaleToWidth - widthRatio * bbox.getWidth())
                                / (2.0 * widthRatio);
                yTranslate =
                        (scaleToHeight - widthRatio * bbox.getHeight())
                                / (2.0 * widthRatio);
                transform.scale(widthRatio, widthRatio);
                scaleInfo = widthRatio;
            } else {
                xTranslate =
                        (scaleToWidth - heightRatio * bbox.getWidth())
                                / (2.0 * heightRatio);
                yTranslate =
                        (scaleToHeight - heightRatio * bbox.getHeight())
                                / (2.0 * heightRatio);
                transform.scale(heightRatio, heightRatio);
                scaleInfo = heightRatio;
            }
            transform.translate(xTranslate-bbox.getMinX(), yTranslate-bbox.getMinY());
            scaleFactor = scaleInfo;
        } else if (zoomBox != null) {
            scaleFactor = 1;
            if (zoomBox.width != 0 && zoomBox.height != 0
                    && oldTransform != null) {

                double scaleToWidth = getWidth();
                double scaleToHeight = getHeight();

                final double widthRatio = scaleToWidth / zoomBox.width;
                final double heightRatio = scaleToHeight / zoomBox.height;

                if (widthRatio < heightRatio) {
                    scaleFactor = widthRatio;
                } else {
                    scaleFactor = heightRatio;
                }
                transform.scale(scaleFactor, scaleFactor);
                transform.translate(-bbox.getMinX(), -bbox.getMinY());
                scaleInfo = scaleFactor;
                //transform.translate(xTranslate, yTranslate);
                scaleToWidth = bbox.getWidth() * scaleFactor;
                scaleToHeight = bbox.getHeight() * scaleFactor;
            }
            zoomBox = null;

            scaleFactor = scaleInfo;
            setSizeNeeded(null);
        } else if (scaleFactor != 1) {
            transform.scale(scaleFactor, scaleFactor);
            transform.translate(-bbox.getMinX(), -bbox.getMinY());
            scaleInfo = scaleFactor;
            setSizeNeeded(new Dimension((int)Math.ceil(bbox.getWidth()*scaleFactor),
                (int)Math.ceil(bbox.getHeight()*scaleFactor)));
        } else {
            scaleInfo = 1;
            transform.translate(-bbox.getMinX(), -bbox.getMinY());
            setSizeNeeded(new Dimension((int)Math.ceil(bbox.getWidth()),
                (int)Math.ceil(bbox.getHeight())));
        }

        if (scaleInfo < Grappa.nodeLabelsScaleCutoff) {
            nodeLabels = false;
        }

        if (scaleInfo < Grappa.edgeLabelsScaleCutoff) {
            edgeLabels = false;
        }

        if (scaleInfo < Grappa.subgLabelsScaleCutoff) {
            subgLabels = false;
        }

        try {
            inverseTransform = transform.createInverse();
        } catch (final NoninvertibleTransformException nite) {
            inverseTransform = null;
        }
        g2d.transform(transform);

        final Shape clip = g2d.getClip();
        if (clip == null)
            return;

        synchronized (graph) {

            final GrappaNexus grappaNexus = subgraph.grappaNexus;

            if (grappaNexus != null) {

                Color bkgdColor = null;

                // do fill now in case there is a Backer supplied
                g2d.setPaint(bkgdColor = (Color) (graph
                        .getGrappaAttributeValue(GRAPPA_BACKGROUND_COLOR_ATTR)));
                g2d.fill(clip);
                if (grappaNexus.style.filled || grappaNexus.image != null) {
                    if (grappaNexus.style.filled) {
                        if (grappaNexus.fillcolor != null) {
                            g2d.setPaint(bkgdColor = grappaNexus.fillcolor);
                            grappaNexus.fill(g2d);
                            if (grappaNexus.color != null)
                                g2d.setPaint(grappaNexus.color);
                            else
                                g2d.setPaint(grappaNexus.style.line_color);
                        } else {
                            g2d.setPaint(bkgdColor = grappaNexus.color);
                            grappaNexus.fill(g2d);
                            g2d.setPaint(grappaNexus.style.line_color);
                        }
                    }
                    grappaNexus.drawImage(g2d);
                    // for the main graph, only outline when filling/imaging
                    if (GrappaStyle.defaultStroke != grappaNexus.style.stroke) {
                        g2d.setStroke(grappaNexus.style.stroke);
                        grappaNexus.draw(g2d);
                        g2d.setStroke(GrappaStyle.defaultStroke);
                    } else {
                        grappaNexus.draw(g2d);
                    }
                }

                if (backer != null && Grappa.backgroundDrawing) {
                    backer.drawBackground(g2d, graph, bbox, clip);
                }

                paintSubgraph(g2d, subgraph, clip, bkgdColor);

            }

        }

        //g2d.setBackground(origBackground);
        //g2d.setComposite(origComposite);
        //g2d.setPaint(origPaint);
        //g2d.setRenderingHints(origRenderingHints);
        //g2d.setStroke(origStroke);
        //g2d.setTransform(origAffineTransform);
        //g2d.setFont(origFont);


        subgraph.release();
    }


    protected void setSizeNeeded(Dimension newSizeNeeded) {
        if (newSizeNeeded == null ? sizeNeeded != null : !newSizeNeeded.equals(sizeNeeded)) {
            sizeNeeded = newSizeNeeded;
            revalidate();
        }
    }

    /**
     * Get the AffineTransform that applies to this drawing.
     *
     * @return the AffineTransform that applies to this drawing.
     */
    public AffineTransform getTransform() {
        return (AffineTransform) (transform.clone());
    }

    /**
     * Get the inverse AffineTransform that applies to this drawing.
     *
     * @return the inverse AffineTransform that applies to this drawing.
     */
    public AffineTransform getInverseTransform() {
        return inverseTransform;
    }

    /**
     * Registers the default text to display in a tool tip. Setting the default
     * text to null turns off tool tips. The default text is displayed when the
     * mouse is outside the graph boundaries, but within the panel.
     *
     * @see Graph#setToolTipText(String)
     */
    @Override
    public void setToolTipText(String tip) {
        //System.err.println("tip set to: " + tip);
        super.setToolTipText(tip);
    }

    /**
     * Generate an appropriate tooltip based on the mouse location provided by
     * the given event.
     *
     * @return if a GrappaListener is available, the result of its
     *         <TT>grappaTip()</TT> method is returned, otherwise null.
     *
     * @see GrappaPanel#setToolTipText(String)
     */
    @Override
    public String getToolTipText(MouseEvent mev) {
        if (inverseTransform == null || grappaListener == null)
            return (null);
        //System.err.println("tip requested");

        final Point2D pt = inverseTransform.transform(mev.getPoint(), null);

        return (grappaListener.grappaTip(subgraph, findContainingElement(
            subgraph, pt), new GrappaPoint(pt.getX(), pt.getY()), mev
            .getModifiers(), this));
    }

    /**
     * Enable/disable scale-to-fit mode.
     *
     * @param setting
     *            if true, the graph drawing is scaled to fit the panel,
     *            otherwise the graph is drawn full-size.
     */
    public void setScaleToFit(boolean setting) {
        scaleToFit = setting;
        if (setting) {
            zoomBox = null;
            setSizeNeeded(null);
        }
        revalidate();
    }

    /**
     * Scale the graph drawing to a specific size.
     */
    public void setScaleToSize(Dimension2D scaleSize) {
        if (scaleSize == null) {
            scaleToSize = null;
        } else {
            zoomBox = null;
            setSizeNeeded(null);
            scaleToSize =
                    new GrappaSize(scaleSize.getWidth(), scaleSize.getHeight());
        }
        revalidate();
    }

    /**
     * Get the subgraph being drawn on this panel.
     *
     * @return the subgraph being drawn on this panel.
     */
    public Subgraph getSubgraph() {
        return (subgraph);
    }

    /**
     * Reset the scale factor to one.
     */
    public void resetZoom() {
        scaleFactor = 1;
        zoomBox = null;
        revalidate();
    }

    /**
     * Check if a swept outline is still available.
     *
     * @return true if there is an outline available.
     */
    public boolean hasOutline() {
        return (savedOutline != null);
    }

    /**
     * Clear swept outline, if any.
     */
    public void clearOutline() {
        savedOutline = null;
    }

    /**
     * Zoom the drawing to the outline just swept with the mouse, if any.
     *
     * @return the box corresponding to the swept outline, or null.
     */
    public GrappaBox zoomToOutline() {
        zoomBox = null;
        if (savedOutline != null) {
            scaleFactor = 1;
            zoomBox = new GrappaBox(savedOutline);
            savedOutline = null;
        }
        //System.err.println("zoomBox=" + zoomBox);
        return (zoomBox);
    }

    /**
     * Zoom the drawing to the outline just swept with the mouse, if any.
     *
     * @param outline
     *            the zoom bounds
     * @return the box corresponding to the swept outline, or null.
     */
    public GrappaBox zoomToOutline(GrappaBox outline) {
        zoomBox = null;
        if (outline != null) {
            scaleFactor = 1;
            zoomBox = new GrappaBox(outline);
        }
        return (zoomBox);
    }

    /**
     * Adjust the scale factor by the supplied multiplier.
     *
     * @param multiplier
     *            multiply the scale factor by this amount.
     * @return the value of the previous scale factor.
     */
    public double multiplyScaleFactor(double multiplier) {
        final double old = scaleFactor;
        zoomBox = null;
        scaleFactor *= multiplier;
        if (scaleFactor == 0)
            scaleFactor = old;
        revalidate();
        return old;
    }


    ////////////////////////////////////////////////////////////////////////
    //
    // Private methods
    //
    ////////////////////////////////////////////////////////////////////////

    private void paintSubgraph(Graphics2D g2d, Subgraph subg, Shape clipper,
            Color bkgdColor) {
        if (subg != subgraph && !subg.reserve())
            return;

        final Rectangle2D bbox = subg.getBoundingBox();
        GrappaNexus grappaNexus = subg.grappaNexus;

        if (bbox != null && grappaNexus != null && subg.visible
                && !grappaNexus.style.invis && clipper.intersects(bbox)) {

            if (subg != subgraph) {
                g2d.setPaint(grappaNexus.color);
                if (grappaNexus.style.filled) {
                    if (grappaNexus.fillcolor != null) {
                        bkgdColor = grappaNexus.fillcolor;
                        grappaNexus.fill(g2d);
                        if (grappaNexus.color != null)
                            g2d.setPaint(grappaNexus.color);
                        else
                            g2d.setPaint(grappaNexus.style.line_color);
                    } else {
                        bkgdColor = grappaNexus.color;
                        grappaNexus.fill(g2d);
                        g2d.setPaint(grappaNexus.style.line_color);
                    }
                } else if (grappaNexus.color == bkgdColor) { // using == is OK (caching)
                    g2d.setPaint(grappaNexus.style.line_color);
                }
                grappaNexus.drawImage(g2d);
                if (subg.isCluster() || Grappa.outlineSubgraphs) {
                    if (GrappaStyle.defaultStroke != grappaNexus.style.stroke) {
                        g2d.setStroke(grappaNexus.style.stroke);
                        grappaNexus.draw(g2d);
                        g2d.setStroke(GrappaStyle.defaultStroke);
                    } else {
                        grappaNexus.draw(g2d);
                    }
                }
            }

            if ((subg.highlight & DELETION_MASK) == DELETION_MASK) {
                g2d.setPaint(deletionStyle.line_color);
                if (GrappaStyle.defaultStroke != deletionStyle.stroke) {
                    g2d.setStroke(deletionStyle.stroke);
                    grappaNexus.draw(g2d);
                    g2d.setStroke(GrappaStyle.defaultStroke);
                } else {
                    grappaNexus.draw(g2d);
                }
            } else if ((subg.highlight & SELECTION_MASK) == SELECTION_MASK) {
                g2d.setPaint(selectionStyle.line_color);
                if (GrappaStyle.defaultStroke != selectionStyle.stroke) {
                    g2d.setStroke(selectionStyle.stroke);
                    grappaNexus.draw(g2d);
                    g2d.setStroke(GrappaStyle.defaultStroke);
                } else {
                    grappaNexus.draw(g2d);
                }
            }

            if (grappaNexus.lstr != null && subgLabels) {
                g2d.setFont(grappaNexus.font);
                g2d.setPaint(grappaNexus.font_color);
                for (int i = 0; i < grappaNexus.lstr.length; i++) {
                    g2d.drawString(grappaNexus.lstr[i],
                        (int) grappaNexus.lpos[i].x,
                        (int) grappaNexus.lpos[i].y);
                }
            }

            final Enumeration<Subgraph> enm1 = subg.subgraphElements();
            while (enm1.hasMoreElements()) {
                final Subgraph subsubg = enm1.nextElement();
                if (subsubg != null)
                    paintSubgraph(g2d, subsubg, clipper, bkgdColor);
            }

            final Enumeration<Node> enm2 = subg.nodeElements();
            while (enm2.hasMoreElements()) {
                final Node node = enm2.nextElement();
                if (node == null || !node.reserve())
                    continue;
                if ((grappaNexus = node.grappaNexus) != null && node.visible
                        && !grappaNexus.style.invis
                        && clipper.intersects(grappaNexus.rawBounds2D())) {
                    if (grappaNexus.style.filled) {
                        if (grappaNexus.fillcolor != null) {
                            g2d.setPaint(grappaNexus.fillcolor);
                            grappaNexus.fill(g2d);
                            if (grappaNexus.color != null)
                                g2d.setPaint(grappaNexus.color);
                            else
                                g2d.setPaint(grappaNexus.style.line_color);
                        } else {
                            g2d.setPaint(grappaNexus.color);
                            grappaNexus.fill(g2d);
                            g2d.setPaint(grappaNexus.style.line_color);
                        }
                    } else {
                        g2d.setPaint(grappaNexus.color);
                    }
                    grappaNexus.drawImage(g2d);
                    if ((node.highlight & DELETION_MASK) == DELETION_MASK) {
                        g2d.setPaint(deletionStyle.line_color);
                        if (GrappaStyle.defaultStroke != deletionStyle.stroke) {
                            g2d.setStroke(deletionStyle.stroke);
                            grappaNexus.draw(g2d);
                            g2d.setStroke(GrappaStyle.defaultStroke);
                        } else {
                            grappaNexus.draw(g2d);
                        }
                    } else if ((node.highlight & SELECTION_MASK) == SELECTION_MASK) {
                        g2d.setPaint(selectionStyle.line_color);
                        if (GrappaStyle.defaultStroke != selectionStyle.stroke) {
                            g2d.setStroke(selectionStyle.stroke);
                            grappaNexus.draw(g2d);
                            g2d.setStroke(GrappaStyle.defaultStroke);
                        } else {
                            grappaNexus.draw(g2d);
                        }
                    } else {
                        if (GrappaStyle.defaultStroke != grappaNexus.style.stroke) {
                            g2d.setStroke(grappaNexus.style.stroke);
                            grappaNexus.draw(g2d);
                            g2d.setStroke(GrappaStyle.defaultStroke);
                        } else {
                            grappaNexus.draw(g2d);
                        }
                    }
                    if (grappaNexus.lstr != null && nodeLabels) {
                        g2d.setFont(grappaNexus.font);
                        g2d.setPaint(grappaNexus.font_color);
                        for (int i = 0; i < grappaNexus.lstr.length; i++) {
                            g2d.drawString(grappaNexus.lstr[i],
                                (int) grappaNexus.lpos[i].x,
                                (int) grappaNexus.lpos[i].y);
                        }
                    }
                }
                node.release();
            }

            final Enumeration<Edge> enm3 = subg.edgeElements();
            while (enm3.hasMoreElements()) {
                final Edge edge = enm3.nextElement();
                if (edge == null || !edge.reserve())
                    continue;
                if ((grappaNexus = edge.grappaNexus) != null && edge.visible
                        && !grappaNexus.style.invis
                        && clipper.intersects(grappaNexus.rawBounds2D())) {
                    grappaNexus.drawImage(g2d);
                    if ((edge.highlight & DELETION_MASK) == DELETION_MASK) {
                        g2d.setPaint(deletionStyle.line_color);
                        grappaNexus.fill(g2d);
                        if (GrappaStyle.defaultStroke != deletionStyle.stroke) {
                            g2d.setStroke(deletionStyle.stroke);
                            grappaNexus.draw(g2d);
                            g2d.setStroke(GrappaStyle.defaultStroke);
                        } else {
                            grappaNexus.draw(g2d);
                        }
                    } else if ((edge.highlight & SELECTION_MASK) == SELECTION_MASK) {
                        g2d.setPaint(selectionStyle.line_color);
                        grappaNexus.fill(g2d);
                        if (GrappaStyle.defaultStroke != selectionStyle.stroke) {
                            g2d.setStroke(selectionStyle.stroke);
                            grappaNexus.draw(g2d);
                            g2d.setStroke(GrappaStyle.defaultStroke);
                        } else {
                            grappaNexus.draw(g2d);
                        }
                    } else {
                        g2d.setPaint(grappaNexus.color);
                        grappaNexus.fill(g2d);
                        if (GrappaStyle.defaultStroke != grappaNexus.style.stroke) {
                            g2d.setStroke(grappaNexus.style.stroke);
                            grappaNexus.draw(g2d);
                            g2d.setStroke(GrappaStyle.defaultStroke);
                        } else {
                            grappaNexus.draw(g2d);
                        }
                    }
                    if (grappaNexus.lstr != null && edgeLabels) {
                        g2d.setFont(grappaNexus.font);
                        g2d.setPaint(grappaNexus.font_color);
                        for (int i = 0; i < grappaNexus.lstr.length; i++) {
                            g2d.drawString(grappaNexus.lstr[i],
                                (int) grappaNexus.lpos[i].x,
                                (int) grappaNexus.lpos[i].y);
                        }
                    }
                }
                edge.release();
            }
        }
        subg.release();
    }

    private Element findContainingElement(Subgraph subg, Point2D pt) {
        return (findContainingElement(subg, pt, null));
    }

    private Element findContainingElement(Subgraph subg, Point2D pt,
            Element crnt) {
        Element elem;
        final Element[] stash = new Element[2];

        stash[0] = crnt;
        stash[1] = null;

        if ((elem = reallyFindContainingElement(subg, pt, stash)) == null)
            elem = stash[1];
        return (elem);
    }


    private Element reallyFindContainingElement(Subgraph subg, Point2D pt,
            Element[] stash) {

        Enumeration<?> enm;

        final Rectangle2D bb = subg.getBoundingBox();

        GrappaNexus grappaNexus = null;


        if (bb.contains(pt)) {

            if ((Grappa.elementSelection & EDGE) == EDGE) {
                enm = subg.edgeElements();
                Edge edge;
                while (enm.hasMoreElements()) {
                    edge = (Edge) enm.nextElement();
                    if ((grappaNexus = edge.grappaNexus) == null
                            || !edge.selectable)
                        continue;
                    if (grappaNexus.rawBounds2D().contains(pt)) {
                        if (grappaNexus.contains(pt.getX(), pt.getY())) {
                            if (stash[0] == null)
                                return (edge);
                            if (stash[1] == null)
                                stash[1] = edge;
                            if (stash[0] == edge)
                                stash[0] = null;
                        }
                    }
                }
            }

            if ((Grappa.elementSelection & NODE) == NODE) {
                enm = subg.nodeElements();
                Node node;
                while (enm.hasMoreElements()) {
                    node = (Node) enm.nextElement();
                    if ((grappaNexus = node.grappaNexus) == null
                            || !node.selectable)
                        continue;
                    if (grappaNexus.rawBounds2D().contains(pt)) {
                        if (grappaNexus.contains(pt)) {
                            if (stash[0] == null)
                                return (node);
                            if (stash[1] == null)
                                stash[1] = node;
                            if (stash[0] == node)
                                stash[0] = null;
                        }
                    }
                }
            }

            Element subelem = null;

            enm = subg.subgraphElements();
            while (enm.hasMoreElements()) {
                if ((subelem =
                        reallyFindContainingElement((Subgraph) (enm
                            .nextElement()), pt, stash)) != null
                        && subelem.selectable) {
                    if (stash[0] == null)
                        return (subelem);
                    if (stash[1] == null)
                        stash[1] = subelem;
                    if (stash[0] == subelem)
                        stash[0] = null;
                }
            }

            if ((Grappa.elementSelection & SUBGRAPH) == SUBGRAPH
                    && subg.selectable) {
                if (stash[0] == null)
                    return (subg);
                if (stash[1] == null)
                    stash[1] = subg;
                if (stash[0] == subg)
                    stash[0] = null;
            }
        }
        return (null);
    }

    ///////////////////////////////////////////////////////////////////
    //
    // AncestorListener Interface
    //
    ///////////////////////////////////////////////////////////////////

    public void ancestorMoved(AncestorEvent aev) {
        // don't care
    }

    public void ancestorAdded(AncestorEvent aev) {
        graph.addPanel(this);
    }

    public void ancestorRemoved(AncestorEvent aev) {
        graph.removePanel(this);
    }

    ///////////////////////////////////////////////////////////////////
    //
    // ComponentListener Interface
    //
    ///////////////////////////////////////////////////////////////////

    public void componentHidden(ComponentEvent cev) {
        // don't care
    }

    public void componentMoved(ComponentEvent cev) {
        // don't care
    }

    public void componentResized(ComponentEvent cev) {
        // Needed to reset JScrollPane scrollbars, for example
        revalidate();
    }

    public void componentShown(ComponentEvent cev) {
        // don't care
    }

    ///////////////////////////////////////////////////////////////////
    //
    // PopupMenuListener Interface
    //
    ///////////////////////////////////////////////////////////////////

    public void popupMenuCanceled(PopupMenuEvent pmev) {
        // don't care
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent pmev) {
        inMenu = false;
    }

    public void popupMenuWillBecomeVisible(PopupMenuEvent pmev) {
        inMenu = true;
    }

    ///////////////////////////////////////////////////////////////////
    //
    // MouseListener Interface
    //
    ///////////////////////////////////////////////////////////////////

    public void mouseClicked(MouseEvent mev) {
        if (inverseTransform == null || grappaListener == null || inMenu)
            return;

        final Point2D pt = inverseTransform.transform(mev.getPoint(), null);

        grappaListener
            .grappaClicked(
                subgraph,
                findContainingElement(
                    subgraph,
                    pt,
                    (subgraph.currentSelection != null
                            && subgraph.currentSelection instanceof Element
                                                                           ? ((Element) subgraph.currentSelection)
                                                                           : null)),
                new GrappaPoint(pt.getX(), pt.getY()), mev.getModifiers(), mev
                    .getClickCount(), this);
    }

    public void mousePressed(MouseEvent mev) {
        if (inverseTransform == null || grappaListener == null || inMenu)
            return;

        final Point2D pt = inverseTransform.transform(mev.getPoint(), null);

        outline = null;

        grappaListener.grappaPressed(subgraph, (pressedElement =
                findContainingElement(subgraph, pt)), (pressedPoint =
                new GrappaPoint(pt.getX(), pt.getY())), (pressedModifiers =
                mev.getModifiers()), this);
    }

    public void mouseReleased(MouseEvent mev) {
        if (inverseTransform == null || grappaListener == null || inMenu)
            return;

        final int modifiers = mev.getModifiers();

        final Point2D pt = inverseTransform.transform(mev.getPoint(), null);

        final GrappaPoint gpt = new GrappaPoint(pt.getX(), pt.getY());

        grappaListener.grappaReleased(subgraph, findContainingElement(subgraph,
            pt), gpt, modifiers, pressedElement, pressedPoint,
            pressedModifiers, outline, this);

        if ((modifiers & java.awt.event.InputEvent.BUTTON1_MASK) != 0
                && (modifiers & java.awt.event.InputEvent.BUTTON1_MASK) == modifiers) {
            if (outline != null) {
                //System.err.println("saving outline");
                savedOutline =
                        GrappaSupport.boxFromCorners(outline, pressedPoint.x,
                            pressedPoint.y, gpt.x, gpt.y);
                outline = null;
            } else {
                //System.err.println("clearing outline");
                savedOutline = null;
            }
        }

    }

    public void mouseEntered(MouseEvent mev) {
        // don't care
    }

    public void mouseExited(MouseEvent mev) {
        // don't care
    }

    ///////////////////////////////////////////////////////////////////
    //
    // MouseMotionListener Interface
    //
    ///////////////////////////////////////////////////////////////////

    public void mouseDragged(MouseEvent mev) {
        if (inverseTransform == null || grappaListener == null || inMenu)
            return;

        final int modifiers = mev.getModifiers();

        final Point2D pt = inverseTransform.transform(mev.getPoint(), null);

        final GrappaPoint gpt = new GrappaPoint(pt.getX(), pt.getY());

        grappaListener.grappaDragged(subgraph, gpt, modifiers, pressedElement,
            pressedPoint, pressedModifiers, outline, this);

        if ((modifiers & java.awt.event.InputEvent.BUTTON1_MASK) != 0
                && (modifiers & java.awt.event.InputEvent.BUTTON1_MASK) == modifiers) {
            outline =
                    GrappaSupport.boxFromCorners(outline, pressedPoint.x,
                        pressedPoint.y, gpt.x, gpt.y);
        }
    }

    public void mouseMoved(MouseEvent mev) {
        // don't care
    }

    // --- Scrollable interface ----------------------------------------

    @Override
    public Dimension getPreferredSize() {
        return sizeNeeded != null ? sizeNeeded : super.getPreferredSize();
    }

    /**
     * Returns the size of the bounding box of the graph augmented by the margin
     * attribute and any scaling.
     *
     * @return The preferredSize of a JViewport whose view is this Scrollable.
     * @see JViewport#getPreferredSize()
     */
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    /**
     * Always returns 1 since a GrappaPanel has not logical rows or columns.
     *
     * @param visibleRect
     *            The view area visible within the viewport
     * @param orientation
     *            Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
     * @param direction
     *            Less than zero to scroll up/left, greater than zero for
     *            down/right.
     * @return The "unit" increment for scrolling in the specified direction,
     *         which in the case of a GrappaPanel is always 1.
     * @see JScrollBar#setUnitIncrement
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect,
            int orientation, int direction) {
        return 1;
    }

    /**
     * Returns 90% of the view area dimension that is in the orientation of the
     * requested scroll.
     *
     * @param visibleRect
     *            The view area visible within the viewport
     * @param orientation
     *            Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
     * @param direction
     *            Less than zero to scroll up/left, greater than zero for
     *            down/right.
     * @return The "unit" increment for scrolling in the specified direction,
     *         which in the case of a GrappaPanel is 90% of the visible width
     *         for a horizontal increment or 90% of the visible height for a
     *         vertical increment.
     * @see JScrollBar#setBlockIncrement
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect,
            int orientation, int direction) {
        int block;

        if (orientation == javax.swing.SwingConstants.VERTICAL) {
            block = (int) (visibleRect.height * 0.9);
        } else {
            block = (int) (visibleRect.width * 0.9);
        }
        if (block < 1)
            block = 1;

        return block;
    }

    /**
     * Always returns false as the viewport should not force the width of this
     * GrappaPanel to match the width of the viewport.
     *
     * @return false
     */
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    /**
     * Always returns false as the viewport should not force the height of this
     * GrappaPanel to match the width of the viewport.
     *
     * @return false
     */
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean isScaleToFit() {
        return scaleToFit;
    }

    public double getCurrentScaleFactor() {
        if (subgraph == null)
            return 1.0;

        final GrappaBox bbox = new GrappaBox(subgraph.getBoundingBox());

        final GrappaSize margins =
                (GrappaSize) (subgraph.getAttributeValue(MARGIN_ATTR));

        if (margins != null) {
            final double x_margin = PointsPerInch * margins.width;
            final double y_margin = PointsPerInch * margins.height;

            bbox.x -= x_margin;
            bbox.y -= y_margin;
            bbox.width += 2.0 * x_margin;
            bbox.height += 2.0 * y_margin;
        }

        if (scaleToFit || scaleToSize != null) {
            final double scaleToWidth = scaleToFit ? getWidth() : scaleToSize.width;
            final double scaleToHeight = scaleToFit ? getHeight() : scaleToSize.height;
            final double widthRatio = scaleToWidth / bbox.getWidth();
            final double heightRatio = scaleToHeight / bbox.getHeight();
            return Math.min(widthRatio, heightRatio);
        } else if (zoomBox != null && zoomBox.width != 0 && zoomBox.height != 0) {

                final double scaleToWidth = getWidth();
                final double scaleToHeight = getHeight();

                final double widthRatio = scaleToWidth / zoomBox.width;
                final double heightRatio = scaleToHeight / zoomBox.height;
                return Math.min(widthRatio, heightRatio);
        } else if (scaleFactor != 1) {
            return scaleFactor;
        } else {
            return 1.0;
        }
    }

    public void setScaleFactor(double scaleFactor) {
        setScaleToFit(false);
        this.scaleFactor = scaleFactor;
        revalidate();
    }

}
