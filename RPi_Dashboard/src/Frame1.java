import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.json.JSONException;
import org.json.JSONObject;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.SwingConstants;


public class Frame1 {

	private JFrame frmRpiDashboard;
	private JLabel lblPrice;
	private JLabel lblSign;
	private JLabel lblChange;
	private JLabel lblPriceBTC;
	private JLabel lblPriceETH;
	private JLabel lblPriceLTC;
	private JLabel lblCryptoBG;
	private JLabel lblProfitValue;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Frame1 window = new Frame1();
					window.frmRpiDashboard.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private static String readUrl(String urlString) throws Exception {
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	}
	
	class GetCryptoPrices extends TimerTask {
	    public void run() {
	    	try {
	    		
	    	    JSONObject json = new JSONObject(readUrl("https://min-api.cryptocompare.com/data/pricemultifull?fsyms=BTC,LTC,ETH&tsyms=USD&e=Coinbase&extraParams=your_app_name"));

	    	    //Set numberformat for String for currency (e.x. $7,230.35)
	    	    NumberFormat formatter = NumberFormat.getCurrencyInstance();
	    	    
	    	    
	    	    //Get Current Prices
	    	    Double BTC_Price = json.getJSONObject("RAW").getJSONObject("BTC").getJSONObject("USD").getDouble("PRICE");
	    	    Double ETH_Price = json.getJSONObject("RAW").getJSONObject("ETH").getJSONObject("USD").getDouble("PRICE");
	    	    Double LTC_Price = json.getJSONObject("RAW").getJSONObject("LTC").getJSONObject("USD").getDouble("PRICE");

	    	    //Get 24Hour High Prices
	    	    //Double BTC_High = json.getJSONObject("RAW").getJSONObject("BTC").getJSONObject("USD").getDouble("HIGH24HOUR");
	    	    //Double ETH_High = json.getJSONObject("RAW").getJSONObject("ETH").getJSONObject("USD").getDouble("HIGH24HOUR");
	    	    //Double LTC_High = json.getJSONObject("RAW").getJSONObject("LTC").getJSONObject("USD").getDouble("HIGH24HOUR");
	    	    
	    	    //Get 24Hour Low Prices
	    	    //Double BTC_Low = json.getJSONObject("RAW").getJSONObject("BTC").getJSONObject("USD").getDouble("LOW24HOUR");
	    	    //Double ETH_Low = json.getJSONObject("RAW").getJSONObject("ETH").getJSONObject("USD").getDouble("LOW24HOUR");
	    	    //Double LTC_Low = json.getJSONObject("RAW").getJSONObject("LTC").getJSONObject("USD").getDouble("LOW24HOUR");
	    	    
	    	    //Get 24Hour Change in Price
	    	    Double BTC_Change = json.getJSONObject("RAW").getJSONObject("BTC").getJSONObject("USD").getDouble("CHANGE24HOUR");
	    	    Double ETH_Change = json.getJSONObject("RAW").getJSONObject("ETH").getJSONObject("USD").getDouble("CHANGE24HOUR");
	    	    Double LTC_Change = json.getJSONObject("RAW").getJSONObject("LTC").getJSONObject("USD").getDouble("CHANGE24HOUR");
	    	    
	    	    //Round 24Hour Change
	    	    
	    	    BTC_Change = Math.round(BTC_Change * 100.0) / 100.0;
	    	    ETH_Change = Math.round(ETH_Change * 100.0) / 100.0;
	    	    LTC_Change = Math.round(LTC_Change * 100.0) / 100.0;
	    	    
	    	    
	    	    
	    	    
	    	    
	    	    //Populate Coin Labels
	    	    //tblCrypto.setValueAt("BTC", 0, 0);
	    	    //tblCrypto.setValueAt("LTC", 1, 0);
	    	    //tblCrypto.setValueAt("ETH", 2, 0);
	    	    
	    	    //Populate Current Prices
	    	    //String strBTCPrice = numberFormat.format(BTC_Price);
	    	    String strBTCPrice = formatter.format(BTC_Price);
	    	    lblPrice.setText(strBTCPrice);
	    	    lblPriceBTC.setText(strBTCPrice);
	    	    
	    	    String strETHPrice = formatter.format(ETH_Price);
	    	    lblPriceETH.setText(strETHPrice);
	    	    
	    	    String strLTCPrice = formatter.format(LTC_Price);
	    	    lblPriceLTC.setText(strLTCPrice);
	    	    
	    	    
	    	    //Populate 24 Hour Change in Price
	    	    String strBTCChange = (formatter.format(BTC_Change)).replaceAll("-", "");
	    	    lblChange.setText(strBTCChange);
	    	    
	    	    
	    	    
	    	    //Set Change Sign based on Positive or negative value
	    	    if(BTC_Change < 0)
	    	    {
	    	    	lblSign.setFont(new Font("Cantarell", Font.BOLD, 24));
	    	    	lblSign.setForeground(new Color(204, 0, 0));
	    	    	lblSign.setText("-");
	    	    }else
	    	    {
	    	    	lblSign.setFont(new Font("Cantarell", Font.BOLD, 19));
	    	    	lblSign.setForeground(new Color(0, 255, 0));
	    	    	lblSign.setText("+");
	    	    }
	    	    
	    	    
	    	    //Set Profit value
	    	    
	    	    
	    	    double dblBitcoinOwned = 1.12;
	    	    double dblSpent = 8400.00;
	    	    double dblProfitValue = (dblBitcoinOwned * BTC_Price) - dblSpent;
	    	    String strProfitValue = formatter.format(dblProfitValue);
	    	    lblProfitValue.setText(strProfitValue);
	    	    
	    	    
	    	    
	    	} catch (JSONException e) {
	    	    e.printStackTrace();
	    	} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	 
	    }
	}


	/**
	 * Create the application.
	 */
	public Frame1() {
		initialize();
		
		Timer timer = new Timer();
		timer.schedule(new GetCryptoPrices(), 0, 10000);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmRpiDashboard = new JFrame();
		frmRpiDashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Already there
		//frmRpiDashboard.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frmRpiDashboard.setUndecorated(true);
		frmRpiDashboard.setAlwaysOnTop(true);
		frmRpiDashboard.setTitle("RPi Dashboard");
		frmRpiDashboard.setBounds(0, 0, 480, 320);
		frmRpiDashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmRpiDashboard.getContentPane().setLayout(null);
		
		lblPrice = new JLabel("0.00");
		lblPrice.setHorizontalAlignment(SwingConstants.CENTER);
		lblPrice.setFont(new Font("Dialog", Font.BOLD, 25));
		lblPrice.setBounds(0, 61, 221, 23);
		frmRpiDashboard.getContentPane().add(lblPrice);
		
		lblSign = new JLabel("");
		lblSign.setForeground(new Color(0, 255, 0));
		lblSign.setFont(new Font("Cantarell", Font.BOLD, 25));
		lblSign.setBounds(270, 61, 20, 23);
		frmRpiDashboard.getContentPane().add(lblSign);
		
		lblChange = new JLabel("0.00");
		lblChange.setHorizontalAlignment(SwingConstants.CENTER);
		lblChange.setFont(new Font("Dialog", Font.BOLD, 25));
		lblChange.setBounds(221, 61, 259, 23);
		frmRpiDashboard.getContentPane().add(lblChange);
		
		lblPriceBTC = new JLabel("0.00");
		lblPriceBTC.setFont(new Font("Dialog", Font.BOLD, 10));
		lblPriceBTC.setBounds(57, 10, 66, 15);
		frmRpiDashboard.getContentPane().add(lblPriceBTC);
		
		lblPriceETH = new JLabel("0.00");
		lblPriceETH.setFont(new Font("Dialog", Font.BOLD, 10));
		lblPriceETH.setBounds(180, 10, 66, 15);
		frmRpiDashboard.getContentPane().add(lblPriceETH);
		
		lblPriceLTC = new JLabel("0.00");
		lblPriceLTC.setFont(new Font("Dialog", Font.BOLD, 10));
		lblPriceLTC.setBounds(281, 10, 66, 15);
		frmRpiDashboard.getContentPane().add(lblPriceLTC);
		
		JLabel lblProfit = new JLabel("Profit:");
		lblProfit.setFont(new Font("Dialog", Font.BOLD, 10));
		lblProfit.setBounds(335, 10, 35, 15);
		frmRpiDashboard.getContentPane().add(lblProfit);
		
		lblProfitValue = new JLabel("");
		lblProfitValue.setFont(new Font("Dialog", Font.BOLD, 10));
		lblProfitValue.setBounds(376, 10, 66, 15);
		frmRpiDashboard.getContentPane().add(lblProfitValue);
		
		lblCryptoBG = new JLabel();
		lblCryptoBG.setVerticalAlignment(SwingConstants.TOP);
		lblCryptoBG.setBounds(0, 0, 480, 151);
		lblCryptoBG.setIcon(new ImageIcon(Frame1.class.getResource("/resources/Background.png")));
		frmRpiDashboard.getContentPane().add(lblCryptoBG);

		
		
	}
}

