package eu.hansolo.steelseries.tools;

/**
 *
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public enum DisabledImageFactory 
{
    INSTANCE;

    private final eu.hansolo.steelseries.tools.Util UTIL = eu.hansolo.steelseries.tools.Util.INSTANCE;
    private int radWidth = 0;    
    private java.awt.image.BufferedImage radDisabledImage = UTIL.createImage(1, 1, java.awt.Transparency.TRANSLUCENT);
    private int linWidth = 0;
    private int linHeight = 0;    
    private java.awt.image.BufferedImage linDisabledImage = UTIL.createImage(1, 1, java.awt.Transparency.TRANSLUCENT);
    private final java.awt.Color DISABLED_COLOR = new java.awt.Color(102, 102, 102, 178);

    /**
     * Creates the image that will be displayed if the radial component is disabled.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @return a buffered image that contains the disabled image of a radial gauge
     */
    public java.awt.image.BufferedImage createRadialDisabled(final int WIDTH)
    {

        if (WIDTH <= 0)
        {
            return UTIL.createImage(1, 1, java.awt.Transparency.TRANSLUCENT);
        }

        // Take image from cache instead of creating a new one if parameters are the same as last time
        if (radWidth == WIDTH)
        {
            return radDisabledImage;
        }

        radDisabledImage.flush();
        radDisabledImage = UTIL.createImage(WIDTH, WIDTH, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = radDisabledImage.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = radDisabledImage.getWidth();
        final int IMAGE_HEIGHT = radDisabledImage.getHeight();
        
        final java.awt.geom.Ellipse2D BACKGROUND = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.08411215245723724, IMAGE_HEIGHT * 0.08411215245723724, IMAGE_WIDTH * 0.8317756652832031, IMAGE_HEIGHT * 0.8317756652832031);
        G2.setColor(DISABLED_COLOR);
        G2.fill(BACKGROUND);
        
        G2.dispose();

        // Cache current values
        radWidth = WIDTH;        

        return radDisabledImage;
    }

    /**
     * Creates the image that will be displayed if the linear gauge is disabled.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @param HEIGHT
     * @return a buffered image that contains the disabled image of a linear gauge
     */
    public java.awt.image.BufferedImage createLinearDisabled(final int WIDTH, final int HEIGHT)
    {
        if (WIDTH <= 0 || HEIGHT <= 0)
        {
            return UTIL.createImage(1, 1, java.awt.Transparency.TRANSLUCENT);
        }

        // Take image from cache instead of creating a new one if parameters are the same as last time
        if (linWidth == WIDTH && linHeight == HEIGHT)
        {
            return linDisabledImage;
        }

        linDisabledImage.flush();
        linDisabledImage = UTIL.createImage(WIDTH, HEIGHT, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = linDisabledImage.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        
        final int IMAGE_WIDTH = linDisabledImage.getWidth();
        final int IMAGE_HEIGHT = linDisabledImage.getHeight();

        final double OUTER_FRAME_CORNER_RADIUS;
        if (IMAGE_WIDTH >= IMAGE_HEIGHT)
        {
            OUTER_FRAME_CORNER_RADIUS = IMAGE_HEIGHT * 0.05;
        }
        else
        {
            OUTER_FRAME_CORNER_RADIUS = IMAGE_WIDTH * 0.05;
        }
        final java.awt.geom.RoundRectangle2D OUTER_FRAME = new java.awt.geom.RoundRectangle2D.Double(0.0, 0.0, IMAGE_WIDTH, IMAGE_HEIGHT, OUTER_FRAME_CORNER_RADIUS, OUTER_FRAME_CORNER_RADIUS);
        final double FRAME_MAIN_CORNER_RADIUS;
        if (IMAGE_WIDTH >= IMAGE_HEIGHT)
        {
            FRAME_MAIN_CORNER_RADIUS = OUTER_FRAME_CORNER_RADIUS - ((OUTER_FRAME.getHeight() - IMAGE_HEIGHT - 2) / 2.0);
        }
        else
        {
            FRAME_MAIN_CORNER_RADIUS = OUTER_FRAME_CORNER_RADIUS - ((OUTER_FRAME.getWidth() - IMAGE_WIDTH - 2) / 2.0);
        }
        final java.awt.geom.RoundRectangle2D FRAME_MAIN = new java.awt.geom.RoundRectangle2D.Double(1.0, 1.0, IMAGE_WIDTH - 2, IMAGE_HEIGHT - 2, FRAME_MAIN_CORNER_RADIUS, FRAME_MAIN_CORNER_RADIUS);

        final double INNER_FRAME_CORNER_RADIUS;
        if (IMAGE_WIDTH >= IMAGE_HEIGHT)
        {
            INNER_FRAME_CORNER_RADIUS = IMAGE_HEIGHT * 0.02857143;
        }
        else
        {
            INNER_FRAME_CORNER_RADIUS = IMAGE_WIDTH * 0.02857143;
        }

        final java.awt.geom.RoundRectangle2D INNER_FRAME = new java.awt.geom.RoundRectangle2D.Double(FRAME_MAIN.getX() + 16, FRAME_MAIN.getY() + 16, FRAME_MAIN.getWidth() - 32, FRAME_MAIN.getHeight() - 32, INNER_FRAME_CORNER_RADIUS, INNER_FRAME_CORNER_RADIUS);

        final double BACKGROUND_CORNER_RADIUS = INNER_FRAME_CORNER_RADIUS - 1;

        final java.awt.geom.RoundRectangle2D BACKGROUND = new java.awt.geom.RoundRectangle2D.Double(INNER_FRAME.getX() + 1, INNER_FRAME.getY() + 1, INNER_FRAME.getWidth() - 2, INNER_FRAME.getHeight() - 2, BACKGROUND_CORNER_RADIUS, BACKGROUND_CORNER_RADIUS);
                                
        G2.setColor(DISABLED_COLOR);
        G2.fill(BACKGROUND);
        
        G2.dispose();

        // Cache current values
        linWidth = WIDTH;
        linHeight = HEIGHT;

        return linDisabledImage;
    }

    @Override
    public String toString()
    {
        return "DisabledImageFactory";
    }
}
