package eu.hansolo.steelseries.tools;

/**
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public enum Scaler
{
    INSTANCE;
    
    /**
     * Returns a double that represents the area of the given point array of a polygon
     * @param POLYGON
     * @param N
     * @return a double that represents the area of the given point array of a polygon
     */
    private double calcSignedPolygonArea(final java.awt.geom.Point2D[] POLYGON)
    {        
        final int N = POLYGON.length;
        int i;
        int j;
        double area = 0;

        for (i = 0; i < N; i++)
        {
            j = (i + 1) % N;
            area += POLYGON[i].getX() * POLYGON[j].getY();
            area -= POLYGON[i].getY() * POLYGON[j].getX();
        }
        area /= 2.0;

        return (area);
        //return(area < 0 ? -area : area); for unsigned
    }
    
    /**
     * Returns a Point2D object that represents the center of mass of the given point array which represents a
     * polygon.
     * @param POLYGON
     * @return a Point2D object that represents the center of mass of the given point array
     */
    public java.awt.geom.Point2D calcCenterOfMass(final java.awt.geom.Point2D[] POLYGON)
    {
        final int N = POLYGON.length; 
        double cx = 0;
        double cy = 0;        
        double area = calcSignedPolygonArea(POLYGON);
        final java.awt.geom.Point2D CENTROID = new java.awt.geom.Point2D.Double();
        int i; 
        int j;

        double factor = 0;
        for (i = 0; i < N; i++)
        {
            j = (i + 1) % N;
            factor = (POLYGON[i].getX() * POLYGON[j].getY() - POLYGON[j].getX() * POLYGON[i].getY());
            cx += (POLYGON[i].getX() + POLYGON[j].getX()) * factor;
            cy += (POLYGON[i].getY() + POLYGON[j].getY()) * factor;
        }
        area *= 6.0f;
        factor = 1 / area;
        cx *= factor;
        cy *= factor;

        CENTROID.setLocation(cx, cy);
        return CENTROID;
    }

    /**
     * Returns a Point2D object that represents the center of mass of the given shape.
     * @param SHAPE
     * @return a Point2D object that represents the center of mass of the given shape
     */
    public java.awt.geom.Point2D getCentroid(final java.awt.Shape SHAPE)
    {        
        java.util.ArrayList<java.awt.geom.Point2D> pointList = new java.util.ArrayList<java.awt.geom.Point2D>(32);
        final java.awt.geom.PathIterator PATH_ITERATOR = SHAPE.getPathIterator(null);
        int lastMoveToIndex = -1;
        while(!PATH_ITERATOR.isDone())
        {
            final double[] COORDINATES = new double[6];            
            switch (PATH_ITERATOR.currentSegment(COORDINATES))
            {               
                case java.awt.geom.PathIterator.SEG_MOVETO:
                    pointList.add(new java.awt.geom.Point2D.Double(COORDINATES[0], COORDINATES[1]));
                    lastMoveToIndex++;
                    break;
                case java.awt.geom.PathIterator.SEG_LINETO:
                    pointList.add(new java.awt.geom.Point2D.Double(COORDINATES[0], COORDINATES[1]));
                    break;
                case java.awt.geom.PathIterator.SEG_QUADTO:
                    pointList.add(new java.awt.geom.Point2D.Double(COORDINATES[0], COORDINATES[1]));
                    pointList.add(new java.awt.geom.Point2D.Double(COORDINATES[2], COORDINATES[3]));
                    break;    
                case java.awt.geom.PathIterator.SEG_CUBICTO:
                    pointList.add(new java.awt.geom.Point2D.Double(COORDINATES[0], COORDINATES[1]));
                    pointList.add(new java.awt.geom.Point2D.Double(COORDINATES[2], COORDINATES[3]));
                    pointList.add(new java.awt.geom.Point2D.Double(COORDINATES[4], COORDINATES[5]));
                    break;
                case java.awt.geom.PathIterator.SEG_CLOSE:
                    if (lastMoveToIndex >= 0)
                    {
                        pointList.add(pointList.get(lastMoveToIndex));
                    }
                    break;
            }                                   
            PATH_ITERATOR.next();
        }
        final java.awt.geom.Point2D[] POINT_ARRAY = new java.awt.geom.Point2D[pointList.size()];
        pointList.toArray(POINT_ARRAY);        
        return (calcCenterOfMass(POINT_ARRAY));
    }
                    
    /**
     * Returns a scaled version of the given convex shape, calculated by the given scale factor.
     * The scaling will be calculated around the centroid of the shape.
     * @param SHAPE
     * @param SCALE_FACTOR
     * @return a scaled version of the given convex shape, calculated around the centroid by the given scale factor.
     */
    public java.awt.Shape scale(final java.awt.Shape SHAPE, final double SCALE_FACTOR)
    {
        final java.awt.geom.Point2D CENTROID = getCentroid(SHAPE);
        final java.awt.geom.AffineTransform TRANSFORM = java.awt.geom.AffineTransform.getTranslateInstance((1.0 - SCALE_FACTOR) * CENTROID.getX(), (1.0 - SCALE_FACTOR) * CENTROID.getY());
        TRANSFORM.scale(SCALE_FACTOR, SCALE_FACTOR);
        return TRANSFORM.createTransformedShape(SHAPE);
    }
    
    /**
     * Returns a scaled version of the given convex shape, calculated by the given scale factor.
     * The scaling will be calculated around the given point.
     * @param SHAPE
     * @param SCALE_FACTOR
     * @param SCALE_CENTER
     * @return a scaled version of the given convex shape, calculated around the given point with the given scale factor.
     */
    public java.awt.Shape scale(final java.awt.Shape SHAPE, final double SCALE_FACTOR, final java.awt.geom.Point2D SCALE_CENTER)
    {        
        final java.awt.geom.AffineTransform TRANSFORM = java.awt.geom.AffineTransform.getTranslateInstance((1.0 - SCALE_FACTOR) * SCALE_CENTER.getX(), (1.0 - SCALE_FACTOR) * SCALE_CENTER.getY());
        TRANSFORM.scale(SCALE_FACTOR, SCALE_FACTOR);
        return TRANSFORM.createTransformedShape(SHAPE);
    }
}
