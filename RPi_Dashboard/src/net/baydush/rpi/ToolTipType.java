/**
 * 
 */
package net.baydush.rpi;

import java.awt.event.MouseEvent;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.IToolTipType;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePoint2D;

/**
 * @author BaydushLC
 *
 */
public class ToolTipType implements IToolTipType {

    /*
     * (non-Javadoc)
     * 
     * @see info.monitorenter.gui.chart.IToolTipType#getDescription()
     */
    @Override
    public String getDescription() {
        return "Stephen's custom - snap to nearest point";
    }

    /*
     * (non-Javadoc)
     * 
     * @see info.monitorenter.gui.chart.IToolTipType#getToolTipText(info.
     * monitorenter.gui.chart.Chart2D, java.awt.event.MouseEvent)
     */
    @Override
    public String getToolTipText( Chart2D chart, MouseEvent me ) {
        String result;
        ITracePoint2D point = chart.getPointFinder().getNearestPoint( me, chart );
        /*
         * We need the axes of the point for correct formatting (expensive...).
         */
        ITrace2D trace = point.getListener();
        IAxis<?> xAxis = chart.getAxisX( trace );
        IAxis<?> yAxis = chart.getAxisY( trace );

        chart.setRequestedRepaint( true );
        StringBuffer buffer = new StringBuffer();
        buffer.append( yAxis.getFormatter().format( point.getY() ) );
        buffer.append( " at " );
        buffer.append( xAxis.getFormatter().format( point.getX() ) ).append( " " );
        result = buffer.toString();
        return result;
    }

}
