package net.baydush.rpi;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.IAxisScalePolicy;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.axis.scalepolicy.AxisScalePolicyManualTicks;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterDate;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterNumber;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.util.Range;
import net.baydush.rpi.BitcoinHistoricalPrices.Quote;

public class Dashboard {
    /**
     * LOGGER object
     */
    private static final Logger LOGGER = LoggerFactory.getLogger( Dashboard.class );
    /**
     * Configuration object representing Dashboard.config
     */
    private static PropertiesConfiguration CONFIG = null;
    /**
     * Set number format for String for currency (e.x. $7,230.35)
     */
    // static NumberFormat FORMATTER = NumberFormat.getCurrencyInstance();
    static NumberFormat FORMATTER = new DecimalFormat( "$#,##0.00;-$#,##0.00" );

    private JFrame frmRpiDashboard;
    
    private JLabel[] lblNames = new JLabel[3];
    private JLabel[] lblPrices = new JLabel[3];
    private String[] strSymbols;
    private double[] dblQuantities;
    private JLabel lblChangePrice;
    private JLabel lblProfitValue;
    private JLabel lbl_addBatch;
    private Chart2D graphPanel;
    private CryptoPrices cryptoPrices;
    private BitcoinHistoricalPrices bitcoinHistoricalPrices;
    private JLabel lblSinceYesterday;
    
    /**
     * Launch the application.
     * @param args passed in arguments
     */
    public static void main( String[] args ) {
        EventQueue.invokeLater( new Runnable() {

            public void run() {
                try {
                    Dashboard window = new Dashboard();
                    window.frmRpiDashboard.setVisible( true );
                }
                catch( Exception e ) {
                    LOGGER.error( "", e );
                }
            }
        } );
    }

    public static String readRSSFeed() {
        try {
            URL rssUrl = new URL( CONFIG.getString( "RSS.Feed" ) );
            HttpURLConnection conn = (HttpURLConnection)rssUrl.openConnection();
            for( String property : CONFIG.getStringArray( "RSS.Property" ) ) {
                String[] parts = property.split( "=" );
                if( parts.length != 2 ) {
                    throw new ConfigurationException( "RSS.Property must be a key = value pair!" );
                }
                conn.addRequestProperty( parts[0].trim(), parts[1].trim() );
            }
            conn.connect();

            StringBuilder sourceCode = new StringBuilder();
            try( BufferedReader in = new BufferedReader(
                    new InputStreamReader( conn.getInputStream() ) ) ) {

                String regex = "<title>(.*?)</title>";
                // Create a Pattern object
                Pattern pattern = Pattern.compile( regex );
                String line;
                while( ( line = in.readLine() ) != null ) {
                    Matcher matcher = pattern.matcher( line );
                    while( matcher.find() ) {
                        sourceCode.append( matcher.group( 1 ) );
                        sourceCode.append( " -- " );
                    }
                }

            }

            // Decode and Removes non-ascii characters from the string
            return StringEscapeUtils.unescapeHtml4( sourceCode.toString() )
                    .replaceAll( "[^\\x00-\\x7F]", "" );
        }
        catch( MalformedURLException ue ) {
            LOGGER.error( "Malformed URL", ue );
        }
        catch( IOException ioe ) {
            LOGGER.error( "Something went wrong reading the contents", ioe );
        }
        catch( ConfigurationException e ) {
            LOGGER.error( "", e );
        }
        return null;
    }

    class RefreshCryptoPrices extends TimerTask {
        public void run() {
            try {

                double dblSpent = CONFIG.getDouble( "Coins.TotalCost" );

                cryptoPrices.update();

                double dblProfitValue = 0.0;
                for( int ix = 0; ix < strSymbols.length; ix ++ ) {
                    //Gets current prices
                    CryptoPrice cp = cryptoPrices.get( strSymbols[ix] );
                    String strPrice = FORMATTER.format( cp.getPrice() );
                    lblPrices[ix].setText( strPrice );
                    // Set Profit value
                    dblProfitValue += dblQuantities[ix] * cp.getPrice();
                    if( ix == 0 ) {
                        // Populate 24 Hour Change in Price
                        double change24hour = cp.getChange24hour();
                        lblChangePrice.setText( FORMATTER.format( change24hour ) );
                        // Set Change color and size based on positive or negative value
                        if( change24hour < 0 ) {
                            lblChangePrice.setFont( new Font( "Cantarell", Font.BOLD, 24 ) );
                            lblChangePrice.setForeground( new Color( 204, 0, 0 ) );
                        } else {
                            lblChangePrice.setFont( new Font( "Cantarell", Font.BOLD, 19 ) );
                            lblChangePrice.setForeground( new Color( 0, 255, 0 ) );
                        }
                    }
                }
                dblProfitValue = dblProfitValue - dblSpent;
                // Sets the color of the text depending on whether negative
                // profit or not
                lblProfitValue.setText( FORMATTER.format( dblProfitValue ) );
                if( dblProfitValue < 0 ) {
                    lblProfitValue.setForeground( new Color( 204, 0, 0 ) );
                } else {
                    lblProfitValue.setForeground( new Color( 0, 255, 0 ) );
                }

            }
            catch( Exception e ) {
                LOGGER.error( "", e );
            }

        }
    }

    class RefreshMarquee extends TimerTask {
        public void run() {
            // Sets the marquee string using Reddit news
            String strNewsStories = readRSSFeed();
            if( strNewsStories == null ) {
                return;
            }

            // Generates and starts the Marquee
            Marquee marquee = new Marquee( lbl_addBatch, strNewsStories, 64 );
            marquee.start();
        }
    }

    /**
     * Create the application.
     * 
     * @throws ConfigurationException
     *             if there is an error
     */
    public Dashboard() throws ConfigurationException {
        initConfig();

        this.strSymbols = CONFIG.getStringArray( "Coin.Symbols" );
        String[] strNames = CONFIG.getStringArray( "Coin.Names" );
        String[] strQuantities = CONFIG.getStringArray( "Coin.Quantities" );
        if( strSymbols.length != strNames.length || strSymbols.length != strQuantities.length
                || strSymbols.length != 3 ) {
            final String msg = "Symbols and Names config must have 3 entries";
            LOGGER.error( msg );
            throw new ConfigurationException(msg);
        }
        dblQuantities = new double[strQuantities.length];
        for( int ix = 0; ix < strQuantities.length; ix++ ) {
            dblQuantities[ix] = Double.parseDouble( strQuantities[ix] );
        }

        cryptoPrices = new CryptoPrices();
        
        // Entry [0] is the main one
        bitcoinHistoricalPrices = new BitcoinHistoricalPrices(strSymbols[0]);
        
        initialize();
        for( int ix = 0; ix < strNames.length; ix++ ) {
            lblNames[ix].setText( strNames[ix] );
            cryptoPrices.addSymbol( strSymbols[ix] );
        }
        fillLabels(strNames);

        fillGraph();

        Timer timer = new Timer();
        timer.schedule( new RefreshCryptoPrices(), 0, 10000 );
        timer.schedule( new RefreshMarquee(), 0, 600000 );
    }

    /**
     * 
     */
    private void fillLabels( String[] strNames ) {
        for( int ix = 0; ix < strNames.length; ix ++ ) {
            this.lblNames[ix].setText( strNames[ix] );
        }
    }

    /**
     * 
     */
    private void fillGraph() {
        graphPanel.getAxisX().getAxisTitle().setTitle( "Timestamp" );
        graphPanel.getAxisY().getAxisTitle().setTitle( "Price" );
        // Create an ITrace:
        ITrace2D trace = new Trace2DSimple("");
		trace.setColor(new Color(0, 0, 255));
		trace.setStroke(new BasicStroke(1.5f));
		
        // Add the trace to the chart. This has to be done before adding points
        // (deadlock prevention):
        graphPanel.addTrace( trace );

        for( Quote quote : bitcoinHistoricalPrices.quotes) {
            trace.addPoint( quote.date.getTime(), quote.price );
        }
        IAxis<IAxisScalePolicy> yAxis = (IAxis<IAxisScalePolicy>)this.graphPanel.getAxisY();
        yAxis.setFormatter(new LabelFormatterNumber(new DecimalFormat("$#,##0")));

        long minSample = bitcoinHistoricalPrices.quotes.get( 0 ).date.getTime();
        long maxSample = bitcoinHistoricalPrices.quotes
                .get( bitcoinHistoricalPrices.quotes.size() - 1 ).date.getTime();
        
        IAxis<IAxisScalePolicy> xAxis = (IAxis<IAxisScalePolicy>)this.graphPanel.getAxisX();
        xAxis.setRangePolicy(new RangePolicyFixedViewport(new Range(minSample, maxSample)));
        xAxis.setAxisScalePolicy(new AxisScalePolicyManualTicks());
        xAxis.setMinorTickSpacing( ( maxSample - minSample ) / 5 );
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        xAxis.setFormatter(new LabelFormatterDate(dateFormat));
    }

    /**
     * Load configuration values from the file "Dashboard.properties"
     * 
     * @throws ConfigurationException
     *             if there is an error
     */
    private void initConfig() throws ConfigurationException {
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<PropertiesConfiguration>(
                PropertiesConfiguration.class )
                        .configure( new Parameters().properties().setFileName( "Dashboard.config" )
                                .setThrowExceptionOnMissing( true )
                                .setListDelimiterHandler( new DefaultListDelimiterHandler( ',' ) )
                                .setIncludesAllowed( false ) );
        CONFIG = builder.getConfiguration();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frmRpiDashboard = new JFrame();
        frmRpiDashboard.getContentPane().setBackground(Color.WHITE);
        frmRpiDashboard.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ); // Already
                                                                          // there
        // frmRpiDashboard.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frmRpiDashboard.setUndecorated( true );
        frmRpiDashboard.setAlwaysOnTop( true );
        frmRpiDashboard.setTitle( "RPi Dashboard" );
        frmRpiDashboard.setBounds( 0, 0, 480, 320 );
        frmRpiDashboard.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frmRpiDashboard.getContentPane().setLayout( null );

        JLabel lblPrice1 = new JLabel( "----" );
        lblPrice1.setHorizontalAlignment( SwingConstants.CENTER );
        lblPrice1.setFont( new Font( "Dialog", Font.BOLD, 25 ) );
        lblPrice1.setBounds( 0, 61, 221, 23 );
        frmRpiDashboard.getContentPane().add( lblPrice1 );

        lblChangePrice = new JLabel( "----" );
        lblChangePrice.setHorizontalAlignment( SwingConstants.CENTER );
        lblChangePrice.setFont( new Font( "Dialog", Font.BOLD, 25 ) );
        lblChangePrice.setBounds( 221, 61, 259, 23 );
        frmRpiDashboard.getContentPane().add( lblChangePrice );

        JLabel lblPrice2 = new JLabel( "----" );
        lblPrice2.setHorizontalAlignment(SwingConstants.LEFT);
        lblPrice2.setFont( new Font( "Dialog", Font.BOLD, 12 ) );
        lblPrice2.setBounds( 82, 10, 66, 15 );
        frmRpiDashboard.getContentPane().add( lblPrice2 );

        JLabel lblPrice3 = new JLabel( "----" );
        lblPrice3.setHorizontalAlignment(SwingConstants.LEFT);
        lblPrice3.setFont( new Font( "Dialog", Font.BOLD, 12 ) );
        lblPrice3.setBounds( 203, 10, 66, 15 );
        frmRpiDashboard.getContentPane().add( lblPrice3 );

        JLabel lblProfit = new JLabel( "Profit •" );
        lblProfit.setForeground(Color.BLUE);
        lblProfit.setFont( new Font("Dialog", Font.BOLD, 10) );
        lblProfit.setBounds( 266, 11, 44, 15 );
        frmRpiDashboard.getContentPane().add( lblProfit );

        lblProfitValue = new JLabel( "----" );
        lblProfitValue.setFont( new Font( "Dialog", Font.BOLD, 16 ) );
        lblProfitValue.setBounds( 317, 0, 113, 33 );
        frmRpiDashboard.getContentPane().add( lblProfitValue );
        
        JSeparator separator = new JSeparator();
        separator.setBounds(0, 34, 480, 2);
        frmRpiDashboard.getContentPane().add(separator);
        
        JSeparator separator_1 = new JSeparator();
        separator_1.setOrientation(SwingConstants.VERTICAL);
        separator_1.setBounds(229, 46, 2, 64);
        frmRpiDashboard.getContentPane().add(separator_1);
        
        JLabel lblName2 = new JLabel("Ethereum •");
        lblName2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblName2.setForeground(Color.BLUE);
        lblName2.setFont(new Font("Dialog", Font.BOLD, 10));
        lblName2.setBounds(12, 11, 66, 15);
        frmRpiDashboard.getContentPane().add(lblName2);
        
        JLabel lblName3 = new JLabel("Litecoin •");
        lblName3.setHorizontalAlignment(SwingConstants.RIGHT);
        lblName3.setForeground(Color.BLUE);
        lblName3.setFont(new Font("Dialog", Font.BOLD, 10));
        lblName3.setBounds(130, 11, 66, 15);
        frmRpiDashboard.getContentPane().add(lblName3);
        
        JLabel lblName1 = new JLabel("BITCOIN PRICE");
        lblName1.setHorizontalAlignment(SwingConstants.CENTER);
        lblName1.setFont(new Font("Dialog", Font.BOLD, 10));
        lblName1.setBounds(61, 92, 113, 14);
        frmRpiDashboard.getContentPane().add(lblName1);
        
        lblSinceYesterday = new JLabel("SINCE YESTERDAY (USD)");
        lblSinceYesterday.setFont(new Font("Dialog", Font.BOLD, 10));
        lblSinceYesterday.setBounds(282, 92, 146, 14);
        frmRpiDashboard.getContentPane().add(lblSinceYesterday);

        JPanel newspanel = new JPanel();
        newspanel.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0)));
        newspanel.setBounds( 0, 274, 480, 46 );
        frmRpiDashboard.getContentPane().add( newspanel );

        lbl_addBatch = new JLabel();
        lbl_addBatch.setHorizontalAlignment( SwingConstants.LEFT );
        newspanel.add( lbl_addBatch );
        
        graphPanel = new Chart2D();
        graphPanel.setBounds(0, 124, 480, 150);
        frmRpiDashboard.getContentPane().add(graphPanel);
        
        JSeparator separator_2 = new JSeparator();
        separator_2.setBounds(0, 122, 480, 2);
        frmRpiDashboard.getContentPane().add(separator_2);

        lblNames[0] = lblName1;
        lblPrices[0] = lblPrice1;
        lblNames[1] = lblName2;
        lblPrices[1] = lblPrice2;
        lblNames[2] = lblName3;
        lblPrices[2] = lblPrice3;
    }
}
