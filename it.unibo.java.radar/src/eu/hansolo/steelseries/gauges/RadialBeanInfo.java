package eu.hansolo.steelseries.gauges;

/**
 *
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public class RadialBeanInfo extends java.beans.SimpleBeanInfo
{
    @Override
    public java.awt.Image getIcon(final int ICON_TYPE)
    {
        switch(ICON_TYPE)
        {
            case ICON_COLOR_16x16:
                return loadImage("/eu/hansolo/steelseries/resources/Radial416.png");

            case ICON_COLOR_32x32:
                return loadImage("/eu/hansolo/steelseries/resources/Radial432.png");

            case ICON_MONO_16x16:
                return null;

            case ICON_MONO_32x32:
                return null;

            default:
                return null;
        }
    }
}