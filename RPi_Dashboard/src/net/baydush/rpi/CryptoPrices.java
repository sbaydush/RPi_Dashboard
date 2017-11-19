/**
 * 
 */
package net.baydush.rpi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author BaydushLC Object to get prices for stock symbols
 */
public class CryptoPrices {
    /**
     * container to hold all symbols and their most recently pulled values
     */
    private Hashtable<String, CryptoPrice> symbols;

    /**
     * default constructor
     */
    public CryptoPrices() {
        symbols = new Hashtable<>();
    }

    /**
     * @param symbol
     *            Stock symbol to add to the set if it does not exist already
     * @return true if this set did not already contain the specified element
     */
    public boolean addSymbol( String symbol ) {
        return( symbols.putIfAbsent( symbol, new CryptoPrice( symbol ) ) == null );
    }

    /**
     * Update the prices for each symbol
     * @throws JSONException if the result has a JSON error
     * @throws Exception is thown for any other error
     */
    public void update() throws JSONException, Exception {
        StringBuilder stringifiedSymbols = new StringBuilder();

        for( String symbol : symbols.keySet() ) {
            if( stringifiedSymbols.length() != 0 )
                stringifiedSymbols.append( ',' );
            stringifiedSymbols.append( symbol );
        }

        JSONObject json = new JSONObject(
                readUrl( "https://min-api.cryptocompare.com/data/pricemultifull?fsyms="
                        + stringifiedSymbols.toString()
                        + "&tsyms=USD&e=Coinbase&extraParams=your_app_name" ) );

        for( String symbol : symbols.keySet() ) {
            CryptoPrice cryptoPrice = symbols.get( symbol );
            JSONObject rawUsd = json.getJSONObject( "RAW" ).getJSONObject( symbol )
                    .getJSONObject( "USD" );
            // Get Current Prices
            cryptoPrice.setPrice( rawUsd.getDouble( "PRICE" ) );
            // Get 24Hour Change in Price
            cryptoPrice.setChange24hour( rawUsd.getDouble( "CHANGE24HOUR" ) );
        }
    }

    /**
     * Read the JSON prices from the REST API
     * @param urlString URL to get the prices form
     * @return JSON string
     * @throws IOException is the URL is bad
     */
    private static String readUrl( String urlString ) throws IOException {
        URL url = new URL( urlString );
        try (BufferedReader reader = new BufferedReader( new InputStreamReader( url.openStream() ) ) ) {
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while( ( read = reader.read( chars ) ) != -1 ) {
                buffer.append( chars, 0, read );
            }
            return buffer.toString();
        }
    }

    /**
     * @param symbol to get the prices for
     * @return CryptoPrice object representing the symbol
     */
    public CryptoPrice get( String symbol ) {

        CryptoPrice cryptoPrice = symbols.get( symbol );
        if( cryptoPrice == null ) {
            return new CryptoPrice(symbol);
        }
        
        return cryptoPrice;
    }

}
