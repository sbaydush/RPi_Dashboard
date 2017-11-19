package net.baydush.rpi;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.Timer;

class Marquee implements ActionListener {

    private static final int RATE = 12;
    private final Timer timer = new Timer(1000 / RATE, this);
    private final JLabel label;
    private final String allText;
    private final int marqueeWidth;
    private int index;

    public Marquee(JLabel label, String s, int marqueeWidth) {
        if (s == null || marqueeWidth < 1) {
            throw new IllegalArgumentException("Null string or marqueeWidth < 1");
        }
        StringBuilder sb = new StringBuilder(marqueeWidth);
        for (int i = 0; i < marqueeWidth; i++) {
            sb.append(' ');
        }
        this.label = label;
        this.allText = sb + s + sb;
        this.marqueeWidth = marqueeWidth;
        label.setFont(new Font("Serif", Font.ITALIC, 32));
        label.setText(sb.toString());
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
        label.setText(allText.substring(index, index + marqueeWidth));
    }
}