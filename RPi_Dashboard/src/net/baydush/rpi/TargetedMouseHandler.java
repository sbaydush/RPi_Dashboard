/**
 * 
 */
package net.baydush.rpi;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import info.monitorenter.gui.chart.Chart2D;

/**
 * @author BaydushLC
 *
 */
public class TargetedMouseHandler implements AWTEventListener {

    private Component parent;
    private Chart2D chart;
    private boolean hasExited = true;

    public TargetedMouseHandler( Component p, Chart2D p2 ) {
        parent = p;
        chart = p2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.AWTEventListener#eventDispatched(java.awt.AWTEvent)
     */
    @Override
    public void eventDispatched( AWTEvent event ) {
        if( event instanceof MouseEvent ) {
            if( SwingUtilities.isDescendingFrom( (Component)event.getSource(), parent ) ) {
                MouseEvent m = (MouseEvent)event;
                if( m.getID() == MouseEvent.MOUSE_ENTERED ) {
                    if( hasExited ) {
                        chart.enablePointHighlighting(hasExited); 
                        hasExited = false;
                    }
                } else if( m.getID() == MouseEvent.MOUSE_EXITED ) {
                    Point p = SwingUtilities.convertPoint( (Component)event.getSource(), m.getPoint(),
                            chart );
                    if( !chart.getBounds().contains( p ) ) {
                        chart.enablePointHighlighting(hasExited); 
                        hasExited = true;
                    }
                }
            }
        }
    }

}
