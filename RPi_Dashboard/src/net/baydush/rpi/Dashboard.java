package net.baydush.rpi;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
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
import javax.swing.JInternalFrame;
import javax.swing.border.MatteBorder;

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
    private JLabel lblPriceBTC;
    private JLabel lblChangeBTC;
    private JLabel lblPriceETH;
    private JLabel lblPriceLTC;
    private JLabel lblCryptoBG;
    private JLabel lblProfitValue;
    private JLabel lbl_addBatch;
    private Chart2D graphPanel;
    private CryptoPrices cryptoPrices;
    private BitcoinHistoricalPrices bitcoinHistoricalPrices;

    /**
     * Launch the application.
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

                cryptoPrices.update();

                // Populate Current Prices
                String strETHPrice = FORMATTER.format( cryptoPrices.get( "ETH" ).getPrice() );
                lblPriceETH.setText( strETHPrice );

                String strLTCPrice = FORMATTER.format( cryptoPrices.get( "LTC" ).getPrice() );
                lblPriceLTC.setText( strLTCPrice );

                CryptoPrice eth = cryptoPrices.get( "ETH" );
                CryptoPrice ltc = cryptoPrices.get( "LTC" );
                CryptoPrice btc = cryptoPrices.get( "BTC" );
                String strBTCPrice = FORMATTER.format( btc.getPrice() );
                lblPriceBTC.setText( strBTCPrice );
                // Populate 24 Hour Change in Price
                lblChangeBTC.setText( FORMATTER.format( btc.getChange24hour() ) );
                // Set Change color and size based on positive or negative
                // value
                if( btc.getChange24hour() < 0 ) {
                    lblChangeBTC.setFont( new Font( "Cantarell", Font.BOLD, 24 ) );
                    lblChangeBTC.setForeground( new Color( 204, 0, 0 ) );
                } else {
                    lblChangeBTC.setFont( new Font( "Cantarell", Font.BOLD, 19 ) );
                    lblChangeBTC.setForeground( new Color( 0, 255, 0 ) );
                }

                // Set Profit value
                double dblBitcoinOwned = CONFIG.getDouble( "Bitcoin.Qty" );
                double dblEthereumOwned = CONFIG.getDouble( "Ethereum.Qty" );
                double dblLitecoinOwned = CONFIG.getDouble( "Litecoin.Qty" );
                double dblSpent = CONFIG.getDouble( "Coins.TotalCost" );
                double dblProfitValue = (( dblBitcoinOwned * btc.getPrice() ) + (dblEthereumOwned * eth.getPrice()) + (dblLitecoinOwned * ltc.getPrice())) - dblSpent;

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
        cryptoPrices = new CryptoPrices();
        cryptoPrices.addSymbol( "BTC" );
        cryptoPrices.addSymbol( "LTC" );
        cryptoPrices.addSymbol( "ETH" );
        
        bitcoinHistoricalPrices = new BitcoinHistoricalPrices();
        
        initialize();
        fillGraph();

        Timer timer = new Timer();
        timer.schedule( new RefreshCryptoPrices(), 0, 10000 );
        timer.schedule( new RefreshMarquee(), 0, 600000 );
    }

    /**
     * 
     */
    private void fillGraph() {
        graphPanel.getAxisX().getAxisTitle().setTitle( "" );
        graphPanel.getAxisY().getAxisTitle().setTitle( "" );
        // Create an ITrace:
        ITrace2D trace = new Trace2DSimple("");
        // Add the trace to the chart. This has to be done before adding points
        // (deadlock prevention):
        graphPanel.addTrace( trace );

        for( Quote quote : bitcoinHistoricalPrices.quotesByMinute) {
            trace.addPoint( quote.date.getTime(), quote.price );
        }
        IAxis<IAxisScalePolicy> yAxis = (IAxis<IAxisScalePolicy>)this.graphPanel.getAxisY();
        //yAxis.setRangePolicy(new RangePolicyFixedViewport(new Range(0, 5)));
        //yAxis.setAxisScalePolicy(new AxisScalePolicyManualTicks());
        //yAxis.setMinorTickSpacing(0.5);
        yAxis.setFormatter(new LabelFormatterNumber(new DecimalFormat("$#,##0")));

        long minSample = bitcoinHistoricalPrices.quotesByMinute.get( 0 ).date.getTime();
        long maxSample = bitcoinHistoricalPrices.quotesByMinute
                .get( bitcoinHistoricalPrices.quotesByMinute.size() - 1 ).date.getTime();
        
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
        frmRpiDashboard.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ); // Already
                                                                          // there
        // frmRpiDashboard.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frmRpiDashboard.setUndecorated( true );
        frmRpiDashboard.setAlwaysOnTop( true );
        frmRpiDashboard.setTitle( "RPi Dashboard" );
        frmRpiDashboard.setBounds( 0, 0, 480, 320 );
        frmRpiDashboard.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frmRpiDashboard.getContentPane().setLayout( null );

        lblPriceBTC = new JLabel( "----" );
        lblPriceBTC.setHorizontalAlignment( SwingConstants.CENTER );
        lblPriceBTC.setFont( new Font( "Dialog", Font.BOLD, 25 ) );
        lblPriceBTC.setBounds( 0, 61, 221, 23 );
        frmRpiDashboard.getContentPane().add( lblPriceBTC );

        lblChangeBTC = new JLabel( "----" );
        lblChangeBTC.setHorizontalAlignment( SwingConstants.CENTER );
        lblChangeBTC.setFont( new Font( "Dialog", Font.BOLD, 25 ) );
        lblChangeBTC.setBounds( 221, 61, 259, 23 );
        frmRpiDashboard.getContentPane().add( lblChangeBTC );

        lblPriceETH = new JLabel( "----" );
        lblPriceETH.setFont( new Font( "Dialog", Font.BOLD, 12 ) );
        lblPriceETH.setBounds( 82, 10, 66, 15 );
        frmRpiDashboard.getContentPane().add( lblPriceETH );

        lblPriceLTC = new JLabel( "----" );
        lblPriceLTC.setFont( new Font( "Dialog", Font.BOLD, 12 ) );
        lblPriceLTC.setBounds( 203, 10, 66, 15 );
        frmRpiDashboard.getContentPane().add( lblPriceLTC );

        JLabel lblProfit = new JLabel( "Profit:" );
        lblProfit.setFont( new Font( "Dialog", Font.BOLD, 10 ) );
        lblProfit.setBounds( 270, 11, 35, 15 );
        frmRpiDashboard.getContentPane().add( lblProfit );

        lblProfitValue = new JLabel( "----" );
        lblProfitValue.setFont( new Font( "Dialog", Font.BOLD, 16 ) );
        lblProfitValue.setBounds( 317, 0, 113, 33 );
        frmRpiDashboard.getContentPane().add( lblProfitValue );

        lblCryptoBG = new JLabel();
        lblCryptoBG.setVerticalAlignment( SwingConstants.TOP );
        lblCryptoBG.setBounds( 0, 0, 480, 151 );
        lblCryptoBG.setIcon(
                new ImageIcon( Dashboard.class.getResource( "/resources/Background.png" ) ) );
        frmRpiDashboard.getContentPane().add( lblCryptoBG );

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

    }
}
