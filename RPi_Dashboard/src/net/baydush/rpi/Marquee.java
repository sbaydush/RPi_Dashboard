package net.baydush.rpi;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.Timer;

class Marquee extends JLabel implements ActionListener {

    private static final int RATE = 12;
    private final Timer timer = new Timer(1000 / RATE, this);
    private String allText;
    private int marqueeWidth;
    private int index;

    /**
     * 
     */
    public Marquee() {
        super();
        // set some default
        marqueeWidth = 64;
        index = 0;
    }

    public Marquee(String s, int marqueeWidth) {
        super();
        if (marqueeWidth < 1) {
            throw new IllegalArgumentException("Null string or marqueeWidth < 1");
        }
        this.marqueeWidth = marqueeWidth;
        super.setText(s);
    }

    public int getMarqueeWidth() {
        return this.marqueeWidth;
    }
    
    public void setMarqueeWidth( int marqueeWidth ) {
        if (marqueeWidth < 1) {
            throw new IllegalArgumentException("Null string or marqueeWidth < 1");
        }
        this.marqueeWidth = marqueeWidth;
    }

    public void setMarqueeText( String s ) {
        if (s == null || s.length() == 0 ) {
            if( timer != null && timer.isRunning() ) {
                timer.stop();
            }
            super.setText( "" );
            return;
        }
        char[] chars = new char[marqueeWidth];
        Arrays.fill(chars, ' ');
        String sb = new String(chars);
        this.allText = sb + s + sb;
        super.setText(sb);
        if( !timer.isRunning() ) {
            timer.start();
        }
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        index++;
        if (index > allText.length() - marqueeWidth) {
            index = 0;
        }
        super.setText(allText.substring(index, index + marqueeWidth));
    }
}