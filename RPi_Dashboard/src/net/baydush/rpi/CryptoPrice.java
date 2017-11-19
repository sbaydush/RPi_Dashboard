/**
 * 
 */
package net.baydush.rpi;

/**
 * @author BaydushLC
 *
 */
public class CryptoPrice {

    /**
     * name of the symbol represented by this object
     */
    private String symbol;
    /**
     * most recent price
     */
    private Double price;
    /**
     * price change in the last 24 hours
     */
    private double change24hour;

    /**
     * initialize a CryptoPrice object with no values
     * @param symbol
     *            ticker symbol
     */
    public CryptoPrice( String symbol ) {
        this.symbol = symbol;
        this.price = Double.NaN;
        this.change24hour = Double.NaN;
    }

    /**
     * initialize a CryptoPrice object with values
     * @param symbol
     *            ticker symbol
     * @param price
     *            most recent price
     * @param change24hour
     *            price change in the last 24 hours
     */
    public CryptoPrice( String symbol, double price, double change24hour ) {
        this.symbol = symbol;
        this.price = price;
        this.change24hour = change24hour;
    }

    /**
     * @return name of the symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * @return most recent price
     */
    public double getPrice() {
        return price.doubleValue();
    }
    /**
     * @param price
     */
    public void setPrice( double price ) {
        this.price = new Double(price);
    }

    /**
     * @return price change in the last 24 hours
     */
    public double getChange24hour() {
        return change24hour;
    }
    /**
     * @param change24hour
     */
    public void setChange24hour( double change24hour ) {
        this.change24hour = new Double(change24hour);
    }

    /**
     * Compare based only upon the symbol name
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj ) {
        if( obj == null ) {
            return false;
        }
        if( !CryptoPrice.class.isAssignableFrom( obj.getClass() ) ) {
            return false;
        }
        final CryptoPrice other = (CryptoPrice)obj;
        if( ( this.symbol == null ) ? ( other.symbol != null )
                : !this.symbol.equals( other.symbol ) ) {
            return false;
        }
        return true;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return( this.symbol != null ? this.symbol.hashCode() : 0 );
    }
}
