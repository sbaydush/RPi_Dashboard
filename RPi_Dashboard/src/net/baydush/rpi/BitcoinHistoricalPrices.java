/**
 * 
 */
package net.baydush.rpi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author BaydushLC
 *
 */
public class BitcoinHistoricalPrices {
    /**
     * LOGGER object
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BitcoinHistoricalPrices.class);
    private static String strURL = "https://api.coinbase.com/v2/prices/%s-USD/historic?period=day";
    private static SimpleDateFormat dateparser;
    
    public class Quote {
        public Date date;
        public double price;

        public Quote(String strDate, double price) {
            try {
				this.date = dateparser.parse(strDate);
			} catch (ParseException e) {
				LOGGER.error("",e);
				this.date = new Date();
			}
            this.price = price;
        }
    }
    
    public ArrayList<Quote> quotes;
    public String symbol;
    
    public BitcoinHistoricalPrices(String symbol) {
        this.symbol = symbol;
        if( dateparser == null ) {
            dateparser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            dateparser.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
        }
        refresh();
    }

    public void refresh() {
        try {
            this.quotes = new ArrayList<>();
			JSONObject json = new JSONObject(readUrl());
            
            JSONArray rawUsd = json.getJSONObject( "data" ).getJSONArray( "prices" );
            rawUsd.forEach( item -> {
                JSONObject quote = (JSONObject)item;
                quotes.add( new Quote( quote.getString("time"), quote.getDouble( "price" ) ) );
            });
            quotes.sort( (o1, o2) -> o1.date.compareTo( o2.date ));
        }
        catch( IOException e ) {
            LOGGER.error( "", e );
        }
    
    }
    
    /**
     * Read the JSON prices from the REST API
     * @param urlString URL to get the prices form
     * @return JSON string
     * @throws IOException is the URL is bad
     */
    private String readUrl() throws IOException {
        URL url = new URL( String.format( this.strURL, this.symbol ) );
        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        conn.addRequestProperty("accept", "application/json");
		conn.addRequestProperty("accept-language","en");
		conn.addRequestProperty("user-agent","Mozilla/5.0 (X11; Fedora; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
        
        conn.connect();
        try (BufferedReader reader = new BufferedReader( new InputStreamReader( conn.getInputStream() ) ) ) {
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while( ( read = reader.read( chars ) ) != -1 ) {
                buffer.append( chars, 0, read );
            }
            return buffer.toString();
        }
    }

}
