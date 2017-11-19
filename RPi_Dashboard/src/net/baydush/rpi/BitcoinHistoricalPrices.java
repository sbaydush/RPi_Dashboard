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
    private static String strURL = "http://api.coindesk.com/charts/data?data=close&startdate=%s&enddate=%s&exchanges=bpi&dev=1&index=USD";
    
    public class Quote {
        public Date date;
        public double price;

        public Quote(long ms, double price) {
            this.date = new Date(ms);
            this.price = price;
        }
    }
    
    public ArrayList<Quote> quotes;
    public ArrayList<Quote> quotesByMinute;
    
    public BitcoinHistoricalPrices() {
        refresh();
    }

    public void refresh() {
        try {
            this.quotes = new ArrayList<>();
            Date today = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String strToday = dateFormat.format(today);
        
            JSONObject json = new JSONObject(
                    readUrl( String.format( strURL, strToday, strToday ) ) );
            
            JSONArray rawUsd = json.getJSONArray( "bpi" );
            rawUsd.forEach( item -> {
                JSONArray quote = (JSONArray)item;
                quotes.add( new Quote( quote.getLong( 0 ), quote.getDouble( 1 ) ) );
            });
            fillQuotesByMinute();
        }
        catch( IOException e ) {
            LOGGER.error( "", e );
        }
    
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
        conn.connect();
        try (BufferedReader reader = new BufferedReader( new InputStreamReader( conn.getInputStream() ) ) ) {
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while( ( read = reader.read( chars ) ) != -1 ) {
                buffer.append( chars, 0, read );
            }
            // strip leading "cb(" and trailing ");"
            return buffer.substring( 3, buffer.length()-2 );
        }
    }

}
