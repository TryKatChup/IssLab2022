package eu.hansolo.steelseries.extras;


/**
 *
 * @author hansolo
 */
public final class Altimeter extends eu.hansolo.steelseries.gauges.AbstractRadial
{
    private double value100 = 0;
    private double oldValue = 0;
    private double value1000 = 0;
    private double value10000 = 0;
    private double angleStep100ft;
    private double angleStep1000ft;
    private double angleStep10000ft;
    private final static double TICKMARK_OFFSET = Math.PI;    
    private float tickLabelPeriod = 1f; // Draw value at every 10th tickmark
    private final java.awt.geom.Point2D CENTER = new java.awt.geom.Point2D.Double();    
    // Images used to combine layers for background and foreground
    private java.awt.image.BufferedImage bImage;
    private java.awt.image.BufferedImage fImage;
        
    private java.awt.image.BufferedImage pointer10000FtImage;
    private java.awt.image.BufferedImage pointer1000FtImage;
    private java.awt.image.BufferedImage pointer100FtImage;    
    private java.awt.image.BufferedImage disabledImage;
    private final java.awt.geom.Rectangle2D LCD = new java.awt.geom.Rectangle2D.Double();
    private org.pushingpixels.trident.Timeline timeline = new org.pushingpixels.trident.Timeline(this);
    private final java.awt.font.FontRenderContext RENDER_CONTEXT = new java.awt.font.FontRenderContext(null, true, true);
    private java.awt.font.TextLayout unitLayout;
    private final java.awt.geom.Rectangle2D UNIT_BOUNDARY = new java.awt.geom.Rectangle2D.Double();
    private double unitStringWidth;
    private java.awt.font.TextLayout valueLayout;
    private final java.awt.geom.Rectangle2D VALUE_BOUNDARY = new java.awt.geom.Rectangle2D.Double();
    private java.awt.font.TextLayout infoLayout;
    private final java.awt.geom.Rectangle2D INFO_BOUNDARY = new java.awt.geom.Rectangle2D.Double();

        
    public Altimeter()
    {
        super();                    
        init(getInnerBounds().width, getInnerBounds().height);
        setMinValue(0);
        setMaxValue(10);
        calcAngleStep();
        setLcdColor(eu.hansolo.steelseries.tools.LcdColor.BLACK_LCD);
        setLcdVisible(true);
        setTitle("ALT");        
        setUnitString("ft");
    }
   
    @Override
    public eu.hansolo.steelseries.gauges.AbstractGauge init(final int WIDTH, final int HEIGHT)
    {
        if (WIDTH <= 1 || HEIGHT <= 1)
        {
            return this;
        }
        
        if (isDigitalFont())
        {
            setLcdValueFont(getModel().getDigitalBaseFont().deriveFont(0.7f * WIDTH * 0.10f));            
        }
        else
        {
            setLcdValueFont(getModel().getStandardBaseFont().deriveFont(0.625f * WIDTH * 0.10f));       
        }

        if (isCustomLcdUnitFontEnabled())
        {
            setLcdUnitFont(getCustomLcdUnitFont().deriveFont(0.25f * WIDTH * 0.10f));
        }
        else
        {
            setLcdUnitFont(getModel().getStandardBaseFont().deriveFont(0.25f * WIDTH * 0.10f));
        }
        
        setLcdInfoFont(getModel().getStandardInfoFont().deriveFont(0.15f * WIDTH * 0.10f));
        
        // Create Background Image
        if (bImage != null)
        {
            bImage.flush();
        }
        bImage = UTIL.createImage(WIDTH, WIDTH, java.awt.Transparency.TRANSLUCENT);
        
        // Create Foreground Image
        if (fImage != null)
        {
            fImage.flush();
        }
        fImage = UTIL.createImage(WIDTH, WIDTH, java.awt.Transparency.TRANSLUCENT);
        
        if (isFrameVisible())
        {
            switch (getFrameType())
            {
                case ROUND:
                    FRAME_FACTORY.createRadialFrame(WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameEffect(), bImage);   
                    break;
                case SQUARE:
                    FRAME_FACTORY.createLinearFrame(WIDTH, WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameEffect(), bImage);
                    break;
                default:
                    FRAME_FACTORY.createRadialFrame(WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameEffect(), bImage);
                    break;
            }
        }        
                
        if (isBackgroundVisible())
        {
            create_BACKGROUND_Image(WIDTH, "", "", bImage);
        } 
                
        create_TICKMARKS_Image(WIDTH, 0, TICKMARK_OFFSET, 0, 10, angleStep100ft, (int)tickLabelPeriod, 0, true, true, null, bImage);        
                
        if (isLcdVisible())
        {
            create_LCD_Image(new java.awt.geom.Rectangle2D.Double(((getGaugeBounds().width - WIDTH * 0.4) / 2.0), (getGaugeBounds().height * 0.55), (WIDTH * 0.4), (WIDTH * 0.1)), getLcdColor(), getCustomLcdBackground(), bImage);
            LCD.setRect(((getGaugeBounds().width - WIDTH * 0.4) / 2.0), (getGaugeBounds().height * 0.55), WIDTH * 0.4, WIDTH * 0.1);
        } 
        
        if (pointer100FtImage != null)
        {
            pointer100FtImage.flush();
        }
        pointer100FtImage = create_100FT_POINTER_Image(WIDTH);
        
        if (pointer1000FtImage != null)
        {
            pointer1000FtImage.flush();
        }
        pointer1000FtImage = create_1000FT_POINTER_Image(WIDTH);
        
        if (pointer10000FtImage != null)
        {
            pointer10000FtImage.flush();
        }
        pointer10000FtImage = create_10000FT_POINTER_Image(WIDTH);
        

        create_POSTS_Image(WIDTH, fImage, eu.hansolo.steelseries.tools.PostPosition.CENTER);
        
        if (isForegroundVisible())
        {            
            switch(getFrameType())
            {
                case SQUARE:
                    FOREGROUND_FACTORY.createLinearForeground(WIDTH, WIDTH, false, bImage);
                    break;
                    
                case ROUND:
                    
                default:
                    FOREGROUND_FACTORY.createRadialForeground(WIDTH, false, getForegroundType(), fImage);
                    break;
            }
        }
        
        if (disabledImage != null)
        {
            disabledImage.flush();
        }
        disabledImage = create_DISABLED_Image(WIDTH);

        return this;
    }

    @Override
    protected void paintComponent(java.awt.Graphics g)
    {
        if (!isInitialized())
        {
            return;
        }
        
        final java.awt.Graphics2D G2 = (java.awt.Graphics2D) g.create();

        CENTER.setLocation(getGaugeBounds().getCenterX(), getGaugeBounds().getCenterX());

        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Translate the coordinate system related to the insets
        G2.translate(getInnerBounds().x, getInnerBounds().y);

        final java.awt.geom.AffineTransform OLD_TRANSFORM = G2.getTransform();

        // Draw combined background image
        G2.drawImage(bImage, 0, 0, null);

        // Draw LCD display
        if (isLcdVisible())
        {            
            if (getLcdColor() == eu.hansolo.steelseries.tools.LcdColor.CUSTOM)
            {
                G2.setColor(getCustomLcdForeground());
            }
            else
            {
                G2.setColor(getLcdColor().TEXT_COLOR);
            }
            G2.setFont(getLcdUnitFont());                        
            if (isLcdUnitStringVisible())
            {
                unitLayout = new java.awt.font.TextLayout(getLcdUnitString(), G2.getFont(), RENDER_CONTEXT);
                UNIT_BOUNDARY.setFrame(unitLayout.getBounds());
                G2.drawString(getLcdUnitString(), (int) (LCD.getX() + (LCD.getWidth() - UNIT_BOUNDARY.getWidth()) - LCD.getWidth() * 0.03), (int) (LCD.getY() + LCD.getHeight() * 0.76));
                unitStringWidth = UNIT_BOUNDARY.getWidth();
            }
            else
            {
                unitStringWidth = 0;
            }        
            G2.setFont(getLcdValueFont());            
            switch(getModel().getNumberSystem())
            {
                case HEX:
                    valueLayout = new java.awt.font.TextLayout(Integer.toHexString((int) getLcdValue()).toUpperCase(), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());        
                    G2.drawString(Integer.toHexString((int) getLcdValue()).toUpperCase(), (int) (LCD.getX() + (LCD.getWidth() - unitStringWidth - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09), (int) (LCD.getY() + LCD.getHeight() * 0.76));
                    break;
                    
                case OCT:
                    valueLayout = new java.awt.font.TextLayout(Integer.toOctalString((int) getLcdValue()), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());        
                    G2.drawString(Integer.toOctalString((int) getLcdValue()), (int) (LCD.getX() + (LCD.getWidth() - unitStringWidth - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09), (int) (LCD.getY() + LCD.getHeight() * 0.76));
                    break;
                    
                case DEC:
                    
                default:
                    valueLayout = new java.awt.font.TextLayout(formatLcdValue(getLcdValue()), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());        
                    G2.drawString(formatLcdValue(getLcdValue()), (int) (LCD.getX() + (LCD.getWidth() - unitStringWidth - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09), (int) (LCD.getY() + LCD.getHeight() * 0.76));
                    break;
            }
            // Draw lcd info string
            if (!getLcdInfoString().isEmpty())
            {
                G2.setFont(getLcdInfoFont());
                infoLayout = new java.awt.font.TextLayout(getLcdInfoString(), G2.getFont(), RENDER_CONTEXT);
                INFO_BOUNDARY.setFrame(infoLayout.getBounds());
                G2.drawString(getLcdInfoString(), LCD.getBounds().x + 5, LCD.getBounds().y + (int) INFO_BOUNDARY.getHeight() + 5);
            }
        }
        
        // Draw the 10000ft pointer
        //final double ANGLE10000FT = ((value10000 - getMinValue()) * angleStep10000ft);
        G2.rotate(((value10000 - getMinValue()) * angleStep10000ft), CENTER.getX(), CENTER.getY());
        G2.drawImage(pointer10000FtImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw the 1000ft pointer
        //final double ANGLE1000FT = ((value1000 - getMinValue()) * angleStep1000ft);
        G2.rotate(((value1000 - getMinValue()) * angleStep1000ft), CENTER.getX(), CENTER.getY());
        G2.drawImage(pointer1000FtImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw the 100ft pointer
        //final double ANGLE100FT = ((value100 - getMinValue()) * angleStep100ft);
        G2.rotate(((value100 - getMinValue()) * angleStep100ft), CENTER.getX(), CENTER.getY());
        G2.drawImage(pointer100FtImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw combined foreground image
        G2.drawImage(fImage, 0, 0, null);

        if (!isEnabled())
        {
            G2.drawImage(disabledImage, 0, 0, null);
        }
        
        // Translate the coordinate system back to original
        G2.translate(-getInnerBounds().x, -getInnerBounds().y);

        G2.dispose();
    }

    /**
     * Sets the current height in feet
     * @param VALUE 
     */
    @Override
    public void setValue(final double VALUE)
    {         
        if (isEnabled())
        {
            this.value100 = (VALUE % 1000) / 100;
            this.value1000 = (VALUE % 10000) / 100;
            this.value10000 = (VALUE % 100000) / 100;

            if (isValueCoupled())
            {
                setLcdValue(VALUE);
            }
            
            fireStateChanged();
            this.oldValue = VALUE;
            repaint();
        }
    }

    @Override
    public void setValueAnimated(final double VALUE)
    {
        if (isEnabled())
        {
            if (timeline.getState() == org.pushingpixels.trident.Timeline.TimelineState.PLAYING_FORWARD || timeline.getState() == org.pushingpixels.trident.Timeline.TimelineState.PLAYING_REVERSE)
            {
                timeline.abort();
            }
            timeline = new org.pushingpixels.trident.Timeline(this);
            timeline.addPropertyToInterpolate("value", this.oldValue, VALUE);
            timeline.setEase(new org.pushingpixels.trident.ease.Spline(0.5f));
            double range = Math.abs(this.value100 - VALUE);
            double fraction = range / 1000;

            timeline.setDuration((long) (1000 * fraction));
            timeline.play();
        }
    }

    private void calcAngleStep()
    {
        this.angleStep100ft = (2 * Math.PI) / (getMaxValue() -  getMinValue());
        this.angleStep1000ft = angleStep100ft / 10.0;
        this.angleStep10000ft = angleStep1000ft / 10.0;
    }

    @Override
    public java.awt.geom.Point2D getCenter()
    {
        return new java.awt.geom.Point2D.Double(bImage.getWidth() / 2.0 + getInnerBounds().x, bImage.getHeight() / 2.0 + getInnerBounds().y);
    }

    @Override
    public java.awt.geom.Rectangle2D getBounds2D()
    {
        return new java.awt.geom.Rectangle2D.Double(bImage.getMinX(), bImage.getMinY(), bImage.getWidth(), bImage.getHeight());
    }
    
    private java.awt.image.BufferedImage create_TICKMARKS_Image(final int WIDTH, final double FREE_AREA_ANGLE, final double OFFSET, final double MIN_VALUE, final double MAX_VALUE, final double ANGLE_STEP, final int TICK_LABEL_PERIOD, final int SCALE_DIVIDER_POWER, final boolean DRAW_TICKS, final boolean DRAW_TICK_LABELS, java.util.ArrayList<eu.hansolo.steelseries.tools.Section> tickmarkSections, java.awt.image.BufferedImage image)
    {
        if (WIDTH <= 0)
        {
            return null;
        }
        if (image == null)
        {
            image = UTIL.createImage(WIDTH, WIDTH, java.awt.Transparency.TRANSLUCENT);
        }
        final java.awt.Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        final java.awt.Font STD_FONT = new java.awt.Font("Verdana", 0, (int) (0.09 * WIDTH));
        final java.awt.BasicStroke MEDIUM_STROKE = new java.awt.BasicStroke(2.0f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_BEVEL);
        final java.awt.BasicStroke THIN_STROKE = new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_BEVEL);        
        final int TEXT_DISTANCE = (int) (0.17 * WIDTH);        
        final int MED_LENGTH = (int) (0.05 * WIDTH);
        final int MAX_LENGTH = (int) (0.07 * WIDTH);

        final java.awt.Color TEXT_COLOR = super.getBackgroundColor().LABEL_COLOR;
        final java.awt.Color TICK_COLOR = super.getBackgroundColor().LABEL_COLOR;

        // Create the ticks itself
        final float RADIUS = IMAGE_WIDTH * 0.4f;
        final java.awt.geom.Point2D ALTIMETER_CENTER = new java.awt.geom.Point2D.Double(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT / 2.0f);

        // Draw ticks
        java.awt.geom.Point2D innerPoint;
        java.awt.geom.Point2D outerPoint;
        java.awt.geom.Point2D textPoint = null;        
        java.awt.geom.Line2D tick;
        int counter = 0;
        int tickCounter = 0;

        G2.setFont(STD_FONT);

        double sinValue = 0;
        double cosValue = 0;

        double alpha; // angle for the tickmarks
        final double ALPHA_START = -OFFSET - (FREE_AREA_ANGLE / 2.0);
        float valueCounter; // value for the tickmarks

        for (alpha = ALPHA_START, valueCounter = 0 ; valueCounter <= 10 ; alpha -= ANGLE_STEP * 0.1, valueCounter += 0.1)
        {
            G2.setStroke(THIN_STROKE);
            sinValue = Math.sin(alpha);
            cosValue = Math.cos(alpha);                        

            // tickmark every 2 units
            if (counter % 2 == 0)
            {
                G2.setStroke(THIN_STROKE);
                innerPoint = new java.awt.geom.Point2D.Double(ALTIMETER_CENTER.getX() + (RADIUS - MED_LENGTH) * sinValue, ALTIMETER_CENTER.getY() + (RADIUS - MED_LENGTH) * cosValue);
                outerPoint = new java.awt.geom.Point2D.Double(ALTIMETER_CENTER.getX() + RADIUS * sinValue, ALTIMETER_CENTER.getY() + RADIUS * cosValue);
                // Draw ticks
                G2.setColor(TICK_COLOR);
                tick = new java.awt.geom.Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);
            }
            
            // Different tickmark every 10 units
            if (counter == 10 || counter == 0)
            {
                G2.setColor(TEXT_COLOR);
                G2.setStroke(MEDIUM_STROKE);
                innerPoint = new java.awt.geom.Point2D.Double(ALTIMETER_CENTER.getX() + (RADIUS - MAX_LENGTH) * sinValue, ALTIMETER_CENTER.getY() + (RADIUS - MAX_LENGTH) * cosValue);
                outerPoint = new java.awt.geom.Point2D.Double(ALTIMETER_CENTER.getX() + RADIUS * sinValue, ALTIMETER_CENTER.getY() + RADIUS * cosValue);
                textPoint = new java.awt.geom.Point2D.Double(ALTIMETER_CENTER.getX() + (RADIUS - TEXT_DISTANCE + STD_FONT.getSize() / 2f) * sinValue, ALTIMETER_CENTER.getY() + (RADIUS - TEXT_DISTANCE + STD_FONT.getSize() / 2f) * cosValue + TEXT_DISTANCE / 2.5f);

                // Draw text
                final java.awt.font.TextLayout TEXT_LAYOUT = new java.awt.font.TextLayout(String.valueOf(Math.round(valueCounter)), G2.getFont(), RENDER_CONTEXT);

                final java.awt.geom.Rectangle2D TEXT_BOUNDARY = TEXT_LAYOUT.getBounds();

                // if gauge is full circle, avoid painting maxValue over minValue
                if (FREE_AREA_ANGLE == 0)
                {
                    if (Float.compare(valueCounter, 10) != 0)
                    {
                        G2.drawString(String.valueOf(Math.round(valueCounter)), (int) (textPoint.getX() - TEXT_BOUNDARY.getWidth() / 2.0), (int) ((textPoint.getY() - TEXT_BOUNDARY.getHeight() / 2.0)));                        
                    }
                }
                else
                {
                    G2.drawString(String.valueOf(Math.round(valueCounter)), (int) (textPoint.getX() - TEXT_BOUNDARY.getWidth() / 2.0), (int) ((textPoint.getY() - TEXT_BOUNDARY.getHeight() / 2.0)));                    
                }
                counter = 0;
                tickCounter++;

                // Draw ticks
                G2.setColor(TICK_COLOR);
                tick = new java.awt.geom.Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);
            }

            counter++;
        }

        G2.dispose();

        return image;
    }

    private java.awt.image.BufferedImage create_100FT_POINTER_Image(final int WIDTH)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final java.awt.image.BufferedImage IMAGE = UTIL.createImage(WIDTH, (int) (1.0 * WIDTH), java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.GeneralPath POINTER100FT = new java.awt.geom.GeneralPath();
        POINTER100FT.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        POINTER100FT.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.4672897196261682);
        POINTER100FT.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.20093457943925233);
        POINTER100FT.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.16822429906542055);
        POINTER100FT.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.20093457943925233);
        POINTER100FT.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.4672897196261682);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5327102803738317);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5794392523364486, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5887850467289719);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.5934579439252337, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5981308411214953, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.6074766355140186);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.616822429906542, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.6261682242990654, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.6261682242990654);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.6261682242990654, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.616822429906542, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.6074766355140186);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5981308411214953, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.5934579439252337, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.5887850467289719);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.5794392523364486, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5327102803738317);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER100FT.closePath();
        final java.awt.geom.Point2D POINTER100FT_START = new java.awt.geom.Point2D.Double(0, POINTER100FT.getBounds2D().getMinY() );
        final java.awt.geom.Point2D POINTER100FT_STOP = new java.awt.geom.Point2D.Double(0, POINTER100FT.getBounds2D().getMaxY() );
        final float[] POINTER100FT_FRACTIONS =
        {
            0.0f,
            0.31f,
            0.3101f,
            0.32f,
            1.0f
        };
        final java.awt.Color[] POINTER100FT_COLORS =
        {
            new java.awt.Color(255, 255, 255, 255),
            new java.awt.Color(255, 255, 255, 255),
            new java.awt.Color(255, 255, 255, 255),
            new java.awt.Color(32, 32, 32, 255),
            new java.awt.Color(32, 32, 32, 255)
        };        
        final java.awt.LinearGradientPaint POINTER100FT_GRADIENT = new java.awt.LinearGradientPaint(POINTER100FT_START, POINTER100FT_STOP, POINTER100FT_FRACTIONS, POINTER100FT_COLORS);
        G2.setPaint(POINTER100FT_GRADIENT);
        G2.fill(POINTER100FT);

        G2.dispose();

        return IMAGE;
    }

    private java.awt.image.BufferedImage create_1000FT_POINTER_Image(final int WIDTH)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final java.awt.image.BufferedImage IMAGE = UTIL.createImage(WIDTH, (int) (1.0 * WIDTH), java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.GeneralPath POINTER1000FT = new java.awt.geom.GeneralPath();
        POINTER1000FT.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        POINTER1000FT.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.46261682242990654, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.40186915887850466, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.40186915887850466);
        POINTER1000FT.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3317757009345794);
        POINTER1000FT.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.40186915887850466);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.40186915887850466, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.46261682242990654, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5327102803738317);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.5747663551401869, IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.5934579439252337);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.616822429906542, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.6121495327102804, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.6121495327102804);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.6121495327102804, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.616822429906542, IMAGE_WIDTH * 0.5373831775700935, IMAGE_HEIGHT * 0.5934579439252337);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.5373831775700935, IMAGE_HEIGHT * 0.5747663551401869, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5327102803738317);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER1000FT.closePath();
        final java.awt.geom.Point2D POINTER1000FT_START = new java.awt.geom.Point2D.Double(0, POINTER1000FT.getBounds2D().getMinY() );
        final java.awt.geom.Point2D POINTER1000FT_STOP = new java.awt.geom.Point2D.Double(0, POINTER1000FT.getBounds2D().getMaxY() );
        final float[] POINTER1000FT_FRACTIONS =
        {
            0.0f,
            0.51f,
            0.52f,
            0.5201f,
            0.53f,
            1.0f
        };
        final java.awt.Color[] POINTER1000FT_COLORS =
        {
            new java.awt.Color(255, 255, 255, 255),
            new java.awt.Color(255, 255, 255, 255),
            new java.awt.Color(255, 255, 255, 255),
            new java.awt.Color(32, 32, 32, 255),
            new java.awt.Color(32, 32, 32, 255),
            new java.awt.Color(32, 32, 32, 255)
        };
        final java.awt.LinearGradientPaint POINTER1000FT_GRADIENT = new java.awt.LinearGradientPaint(POINTER1000FT_START, POINTER1000FT_STOP, POINTER1000FT_FRACTIONS, POINTER1000FT_COLORS);
        G2.setPaint(POINTER1000FT_GRADIENT);
        G2.fill(POINTER1000FT);

        G2.dispose();

        return IMAGE;
    }

    private java.awt.image.BufferedImage create_10000FT_POINTER_Image(final int WIDTH)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final java.awt.image.BufferedImage IMAGE = UTIL.createImage(WIDTH, (int) (1.0 * WIDTH), java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.GeneralPath POINTER10000FT = new java.awt.geom.GeneralPath();
        POINTER10000FT.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        POINTER10000FT.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER10000FT.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4672897196261682);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.3177570093457944);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.3037383177570093);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.1822429906542056);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.11682242990654206);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.11682242990654206);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.1822429906542056);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.29906542056074764);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.3177570093457944);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.4672897196261682);
        POINTER10000FT.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER10000FT.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
        POINTER10000FT.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
        POINTER10000FT.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
        POINTER10000FT.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER10000FT.closePath();        
        G2.setColor(java.awt.Color.WHITE);
        G2.fill(POINTER10000FT);

        G2.dispose();

        return IMAGE;
    }

    @Override
    public String toString()
    {
        return "Altimeter";
    }
}
