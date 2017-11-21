/**
 * 
 */
package net.baydush.rpi;


/**
 * @author BaydushLC
 *
 */
public class Coin {
    private String name;
    private String symbol;
    private double quantity;
    private double cost;
    
    public Coin( String name, String symbol, double quantity, double cost ) {
        this.name = name;
        this.symbol = symbol;
        this.quantity = quantity;
        this.cost = cost;
    }
    
    public String getName() {
        return name;
    }
    public void setName( String name ) {
        this.name = name;
    }
    public String getSymbol() {
        return symbol;
    }
    public void setSymbol( String symbol ) {
        this.symbol = symbol;
    }
    public double getQuantity() {
        return quantity;
    }
    public void setQuantity( double quantity ) {
        this.quantity = quantity;
    }
    public double getCost() {
        return cost;
    }
    public void setCost( double cost ) {
        this.cost = cost;
    }
}
