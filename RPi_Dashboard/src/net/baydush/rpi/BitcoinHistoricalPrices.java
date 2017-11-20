/**
 * 
 */
package net.baydush.rpi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    //private static String strURL = "http://api.coindesk.com/charts/data?data=close&startdate=%s&enddate=%s&exchanges=bpi&dev=1&index=USD";
    private static String strURL = "http://api.coinbase.com/v2/prices/BTC-USD/historic?period=hour";
    
    private static SimpleDateFormat dateparser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    
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
    public ArrayList<Quote> quotesByMinute;
    public ArrayList<Quote> quotesByFiveMinutes;
    
    public BitcoinHistoricalPrices() {
        refresh();
    }

    public void refresh() {
        try {
            this.quotes = new ArrayList<>();
            //Date today = new Date();
            //DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            //String strToday = dateFormat.format(today);
			JSONObject json = new JSONObject(readUrl(strURL));
            
            JSONArray rawUsd = json.getJSONObject( "data" ).getJSONArray( "prices" );
            rawUsd.forEach( item -> {
                JSONObject quote = (JSONObject)item;
                quotes.add( new Quote( quote.getString("time"), quote.getDouble( "price" ) ) );
            });
            //fillQuotesByMinute();
            //fillQuotesByFiveMinutes();
        }
        catch( IOException e ) {
            LOGGER.error( "", e );
        }
    
    }
    
    private void fillQuotesByFiveMinutes() {
    	final long maxInterval = 65000;
        int fromIndex;
        int toIndex;
        long lastSample;
        
        this.quotesByMinute = new ArrayList<>();
        toIndex = fromIndex = this.quotes.size()-1;
        lastSample = quotes.get( fromIndex ).date.getTime();
        while( fromIndex > 0 && ( lastSample - quotes.get( fromIndex-1 ).date.getTime() ) < maxInterval ) {
            fromIndex--;
            lastSample = quotes.get( fromIndex ).date.getTime();
        }
        
        this.quotesByMinute.addAll( this.quotes.subList( fromIndex, toIndex ) );
	}

	/**
     * 
     */
    private void fillQuotesByMinute() {
        final long maxInterval = 65000;
        int fromIndex;
        int toIndex;
        long lastSample;
        
        this.quotesByMinute = new ArrayList<>();
        toIndex = fromIndex = this.quotes.size()-1;
        lastSample = quotes.get( fromIndex ).date.getTime();
        while( fromIndex > 0 && ( lastSample - quotes.get( fromIndex-1 ).date.getTime() ) < maxInterval ) {
            fromIndex--;
            lastSample = quotes.get( fromIndex ).date.getTime();
        }
        
        this.quotesByMinute.addAll( this.quotes.subList( fromIndex, toIndex ) );
    }

    /**
     * Read the JSON prices from the REST API
     * @param urlString URL to get the prices form
     * @return JSON string
     * @throws IOException is the URL is bad
     */
    private static String readUrl( String urlString ) throws IOException {
        URL url = new URL( urlString );
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.addRequestProperty("accept", "application/json");
        
        
		conn.addRequestProperty("accept-encoding","gzip, deflate, br");
		conn.addRequestProperty("accept-language","en");
		conn.addRequestProperty("cb-client","CoinbaseWeb");
		conn.addRequestProperty("cb-fp","65f6d7732e5e33db28e245138f6c94b7");
		conn.addRequestProperty("cb-version","2017-08-07");
		conn.addRequestProperty("dnt","1");
		conn.addRequestProperty("origin","https://www.coinbase.com");
		conn.addRequestProperty("referer","https://www.coinbase.com/");
		conn.addRequestProperty("user-agent","Mozilla/5.0 (X11; Fedora; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
        
        conn.connect();
        try (BufferedReader reader = new BufferedReader( new InputStreamReader( conn.getInputStream() ) ) ) {
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while( ( read = reader.read( chars ) ) != -1 ) {
                buffer.append( chars, 0, read );
            }
            // strip leading "cb(" and trailing ");"
            //return buffer.substring( 3, buffer.length()-2 );
            return buffer.toString();
        }
    }

}
