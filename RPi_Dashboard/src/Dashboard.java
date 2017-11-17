import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
import javax.swing.JPanel;

public class Dashboard {

	private JFrame frmRpiDashboard;
	private JLabel lblPrice;
	private JLabel lblSign;
	private JLabel lblChange;
	private JLabel lblPriceETH;
	private JLabel lblPriceLTC;
	private JLabel lblCryptoBG;
	private JLabel lblProfitValue;
	private JLabel lbl_addBatch;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Dashboard window = new Dashboard();
					window.frmRpiDashboard.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static String readRSSFeed(String urlAddress){
        try{
            URL rssUrl = new URL (urlAddress);
            HttpURLConnection conn = null;
            conn = (HttpURLConnection) rssUrl.openConnection();
            conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Win98; en-US; rv:1.7.2) Gecko/20040803");
            conn.connect();
            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            
            String sourceCode = "";
            String line;
            
            while ((line = in.readLine()) != null) {
                int titleEndIndex = 0;
                int titleStartIndex = 0;
                while (titleStartIndex >= 0) {
                    titleStartIndex = line.indexOf("<title>", titleEndIndex);
                    if (titleStartIndex >= 0) {
                        titleEndIndex = line.indexOf("</title>", titleStartIndex);
                        sourceCode += line.substring(titleStartIndex + "<title>".length(), titleEndIndex) + "\n";
                    }
                }
            }

            in.close();
            return sourceCode;
        } catch (MalformedURLException ue){
            System.out.println("Malformed URL");
        } catch (IOException ioe){
            System.out.println("Something went wrong reading the contents");
            System.out.println(ioe);
        }
        return null;
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

	    	    	    	    
	    	    //Get 24Hour Change in Price
	    	    Double BTC_Change = json.getJSONObject("RAW").getJSONObject("BTC").getJSONObject("USD").getDouble("CHANGE24HOUR");
	    	    Double ETH_Change = json.getJSONObject("RAW").getJSONObject("ETH").getJSONObject("USD").getDouble("CHANGE24HOUR");
	    	    Double LTC_Change = json.getJSONObject("RAW").getJSONObject("LTC").getJSONObject("USD").getDouble("CHANGE24HOUR");
	    	    
	    	        	    
	    	    //Populate Current Prices
	    	    String strBTCPrice = formatter.format(BTC_Price);
	    	    lblPrice.setText(strBTCPrice);
	    	    
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
	    	    
	    	    //Sets the color of the text depending on whether negative profit or not
	    	    if(strProfitValue.contains("("))
	    	    {
	    	    	lblProfitValue.setForeground(new Color(204, 0, 0));
	    	    }
	    	    else
	    	    {
	    	    	lblProfitValue.setForeground(new Color(0, 255, 0));
	    	    }
	    	    
	    	    lblProfitValue.setText(strProfitValue);
	    	    
	    	    
	    	} catch (JSONException e) {
	    	    e.printStackTrace();
	    	} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	 
	    }
	}

	class RefreshMarquee extends TimerTask {
		public void run() {
			//Sets the marquee string using Reddit news
			String strNewsStories = readRSSFeed("https://www.reddit.com/r/news.rss");
			
			//Replaces linebreaks with " -- "
			strNewsStories = strNewsStories.replaceAll("\n", " -- ");
			
			//Removes non-ascii characters from the string
			strNewsStories = strNewsStories.replaceAll("[^\\x00-\\x7F]", "");
			
			//System.out.print(strNewsStories);
			
			//Generates and starts the Marquee
    	    Marquee marquee = new Marquee(lbl_addBatch, strNewsStories, 64);
            marquee.start();
		}
	}
	
	/**
	 * Create the application.
	 */
	public Dashboard() {
		initialize();
		
		Timer timer = new Timer();
		timer.schedule(new GetCryptoPrices(), 0, 10000);
		timer.schedule(new RefreshMarquee(),  0, 600000);
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
		lblSign.setFont(new Font("Cantarell", Font.BOLD, 30));
		lblSign.setBounds(270, 61, 20, 23);
		frmRpiDashboard.getContentPane().add(lblSign);
		
		lblChange = new JLabel("0.00");
		lblChange.setHorizontalAlignment(SwingConstants.CENTER);
		lblChange.setFont(new Font("Dialog", Font.BOLD, 25));
		lblChange.setBounds(221, 61, 259, 23);
		frmRpiDashboard.getContentPane().add(lblChange);
		
		lblPriceETH = new JLabel("0.00");
		lblPriceETH.setFont(new Font("Dialog", Font.BOLD, 12));
		lblPriceETH.setBounds(82, 10, 66, 15);
		frmRpiDashboard.getContentPane().add(lblPriceETH);
		
		lblPriceLTC = new JLabel("0.00");
		lblPriceLTC.setFont(new Font("Dialog", Font.BOLD, 12));
		lblPriceLTC.setBounds(203, 10, 66, 15);
		frmRpiDashboard.getContentPane().add(lblPriceLTC);
		
		JLabel lblProfit = new JLabel("Profit:");
		lblProfit.setFont(new Font("Dialog", Font.BOLD, 10));
		lblProfit.setBounds(270, 11, 35, 15);
		frmRpiDashboard.getContentPane().add(lblProfit);
		
		lblProfitValue = new JLabel("");
		lblProfitValue.setFont(new Font("Dialog", Font.BOLD, 16));
		lblProfitValue.setBounds(317, 0, 84, 33);
		frmRpiDashboard.getContentPane().add(lblProfitValue);
		
		lblCryptoBG = new JLabel();
		lblCryptoBG.setVerticalAlignment(SwingConstants.TOP);
		lblCryptoBG.setBounds(0, 0, 480, 151);
		lblCryptoBG.setIcon(new ImageIcon(Dashboard.class.getResource("/resources/Background.png")));
		frmRpiDashboard.getContentPane().add(lblCryptoBG);
		
		JPanel newspanel = new JPanel();
		newspanel.setBounds(0, 272, 480, 48);
		frmRpiDashboard.getContentPane().add(newspanel);
		
		lbl_addBatch = new JLabel();
		lbl_addBatch.setHorizontalAlignment(SwingConstants.LEFT);
        newspanel.add(lbl_addBatch);

		
		
	}
}

