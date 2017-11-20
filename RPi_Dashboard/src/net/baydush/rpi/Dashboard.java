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
import javax.swing.JSeparator;

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
    private JLabel lblPriceMain;
    private JLabel lblChangePrice;
    private JLabel lblCoinPrice1;
    private JLabel lblCoinPrice2;
    private JLabel lblProfitValue;
    private JLabel lbl_addBatch;
    private Chart2D graphPanel;
    private CryptoPrices cryptoPrices;
    private BitcoinHistoricalPrices bitcoinHistoricalPrices;
    private JSeparator separator_1;
    private JLabel lblPriceMainLabel;
    private JLabel lblSinceYesterday;
    private JSeparator separator_2;
    private JLabel lblCoinName1;
    private JLabel lblCoinName2;
    
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
                //Gets the coin placement from config file
                String strMainCoin = CONFIG.getString( "MainCoin.Name" ).toUpperCase();
                String strCoin1 = CONFIG.getString( "Coin1.Name" ).toUpperCase();
                String strCoin2 = CONFIG.getString( "Coin2.Name" ).toUpperCase();
                
                //Sets up variables for coins
                CryptoPrice eth = cryptoPrices.get( "ETH" );
                CryptoPrice ltc = cryptoPrices.get( "LTC" );
                CryptoPrice btc = cryptoPrices.get( "BTC" );
                
                //Gets current prices
                String strBTCPrice = FORMATTER.format( btc.getPrice() );
                String strETHPrice = FORMATTER.format( eth.getPrice() );
                String strLTCPrice = FORMATTER.format( ltc.getPrice() );
                

                // Switch based on Main coin name set in config
                // to populate current prices
                
                switch (strMainCoin) {
                case "BTC" : {
                	lblPriceMain.setText( strBTCPrice );
                	lblChangePrice.setText( FORMATTER.format( btc.getChange24hour() ) );
                	lblPriceMainLabel.setText( "BITCOIN PRICE" );
                	break;
                }
                case "ETH" : {
                	lblPriceMain.setText( strETHPrice );
                	lblChangePrice.setText( FORMATTER.format( eth.getChange24hour() ) );
                	lblPriceMainLabel.setText( "ETHEREUM PRICE" );
                	break;
                }
                case "LTC" : {
                	lblPriceMain.setText( strLTCPrice );
                	lblChangePrice.setText( FORMATTER.format( ltc.getChange24hour() ) );
                	lblPriceMainLabel.setText( "LITECOIN PRICE" );
                	break;
                }
                default: {
                	LOGGER.error("Value for MainCoin {} is invalid, defaulting to BTC",strMainCoin);
                	lblPriceMain.setText( strBTCPrice );
                	lblChangePrice.setText( FORMATTER.format( btc.getChange24hour() ) );
                	break;
                }
                }
                
                
                // Switch based on Coin1 name set in config 
                // to populate current prices
                
                switch (strCoin1) {
                case "BTC": {
                	lblCoinName1.setText("Bitcoin •");
                	lblCoinPrice1.setText( strBTCPrice );
                	break;
                }
                case "ETH": {
                	lblCoinName1.setText( "Ethereum •" );
                	lblCoinPrice1.setText( strETHPrice );
                	break;
                }
                case "LTC": {
                	lblCoinName1.setText( "Litecoin •" );
                	lblCoinPrice1.setText( strLTCPrice );
                	break;
                }
                default: {
                	LOGGER.error("Value for Coin1 {} is invalid, defaulting to BTC",strCoin1);
                	lblCoinName1.setText("Bitcoin •");
                	lblCoinPrice1.setText( strBTCPrice );
                	break;
                }
                }
                
                
                // Switch based on Coin2 name set in config
                // to populate current prices
                
                switch (strCoin2) {
                case "BTC" : {
                	lblCoinName2.setText("Bitcoin •");
                	lblCoinPrice2.setText( strBTCPrice );
                	break;
                }
                case "ETH" : {
                	lblCoinName2.setText( "Ethereum •" );
                	lblCoinPrice2.setText( strETHPrice );
                	break;
                }
                case "LTC" : {
                	lblCoinName2.setText( "Litecoin •" );
                	lblCoinPrice2.setText( strLTCPrice );
                	break;
                }
                default: {
                	LOGGER.error("Value for Coin2 {} is invalid, defaulting to BTC",strCoin2);
                	lblCoinName2.setText("Bitcoin •");
                	lblCoinPrice2.setText( strBTCPrice );
                	break;
                }
                }
                

                
                // Populate 24 Hour Change in Price
                
                // Set Change color and size based on positive or negative
                // value
                
                if( cryptoPrices.get( strMainCoin ).getChange24hour() < 0 ) {
                    lblChangePrice.setFont( new Font( "Cantarell", Font.BOLD, 24 ) );
                    lblChangePrice.setForeground( new Color( 204, 0, 0 ) );
                } else {
                    lblChangePrice.setFont( new Font( "Cantarell", Font.BOLD, 19 ) );
                    lblChangePrice.setForeground( new Color( 0, 255, 0 ) );
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

        lblPriceMain = new JLabel( "----" );
        lblPriceMain.setHorizontalAlignment( SwingConstants.CENTER );
        lblPriceMain.setFont( new Font( "Dialog", Font.BOLD, 25 ) );
        lblPriceMain.setBounds( 0, 61, 221, 23 );
        frmRpiDashboard.getContentPane().add( lblPriceMain );

        lblChangePrice = new JLabel( "----" );
        lblChangePrice.setHorizontalAlignment( SwingConstants.CENTER );
        lblChangePrice.setFont( new Font( "Dialog", Font.BOLD, 25 ) );
        lblChangePrice.setBounds( 221, 61, 259, 23 );
        frmRpiDashboard.getContentPane().add( lblChangePrice );

        lblCoinPrice1 = new JLabel( "----" );
        lblCoinPrice1.setHorizontalAlignment(SwingConstants.LEFT);
        lblCoinPrice1.setFont( new Font( "Dialog", Font.BOLD, 12 ) );
        lblCoinPrice1.setBounds( 82, 10, 66, 15 );
        frmRpiDashboard.getContentPane().add( lblCoinPrice1 );

        lblCoinPrice2 = new JLabel( "----" );
        lblCoinPrice2.setHorizontalAlignment(SwingConstants.LEFT);
        lblCoinPrice2.setFont( new Font( "Dialog", Font.BOLD, 12 ) );
        lblCoinPrice2.setBounds( 203, 10, 66, 15 );
        frmRpiDashboard.getContentPane().add( lblCoinPrice2 );

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
        
        separator_1 = new JSeparator();
        separator_1.setOrientation(SwingConstants.VERTICAL);
        separator_1.setBounds(229, 46, 2, 64);
        frmRpiDashboard.getContentPane().add(separator_1);
        
        lblCoinName1 = new JLabel("Ethereum •");
        lblCoinName1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblCoinName1.setForeground(Color.BLUE);
        lblCoinName1.setFont(new Font("Dialog", Font.BOLD, 10));
        lblCoinName1.setBounds(12, 11, 66, 15);
        frmRpiDashboard.getContentPane().add(lblCoinName1);
        
        lblCoinName2 = new JLabel("Litecoin •");
        lblCoinName2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblCoinName2.setForeground(Color.BLUE);
        lblCoinName2.setFont(new Font("Dialog", Font.BOLD, 10));
        lblCoinName2.setBounds(130, 11, 66, 15);
        frmRpiDashboard.getContentPane().add(lblCoinName2);
        
        lblPriceMainLabel = new JLabel("BITCOIN PRICE");
        lblPriceMainLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lblPriceMainLabel.setFont(new Font("Dialog", Font.BOLD, 10));
        lblPriceMainLabel.setBounds(61, 92, 113, 14);
        frmRpiDashboard.getContentPane().add(lblPriceMainLabel);
        
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
        
        separator_2 = new JSeparator();
        separator_2.setBounds(0, 122, 480, 2);
        frmRpiDashboard.getContentPane().add(separator_2);

    }
}
