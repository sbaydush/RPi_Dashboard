import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.json.JSONException;
import org.json.JSONObject;
import java.awt.BorderLayout;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import java.awt.Font;


public class Frame1 {

	private JFrame frmRpiDashboard;
	private JTable tblCrypto;
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

	    	    
	    	    ImageIcon downarrow = new ImageIcon("resources/down_red_arrow.png");
	    	    ImageIcon uparrow = new ImageIcon("resources/up_green_arrow.png");
	    	    
	    	    //Get Current Prices
	    	    Double BTC_Price = json.getJSONObject("RAW").getJSONObject("BTC").getJSONObject("USD").getDouble("PRICE");
	    	    Double ETH_Price = json.getJSONObject("RAW").getJSONObject("ETH").getJSONObject("USD").getDouble("PRICE");
	    	    Double LTC_Price = json.getJSONObject("RAW").getJSONObject("LTC").getJSONObject("USD").getDouble("PRICE");

	    	    //Get 24Hour High Prices
	    	    Double BTC_High = json.getJSONObject("RAW").getJSONObject("BTC").getJSONObject("USD").getDouble("HIGH24HOUR");
	    	    Double ETH_High = json.getJSONObject("RAW").getJSONObject("ETH").getJSONObject("USD").getDouble("HIGH24HOUR");
	    	    Double LTC_High = json.getJSONObject("RAW").getJSONObject("LTC").getJSONObject("USD").getDouble("HIGH24HOUR");
	    	    
	    	    //Get 24Hour Low Prices
	    	    Double BTC_Low = json.getJSONObject("RAW").getJSONObject("BTC").getJSONObject("USD").getDouble("LOW24HOUR");
	    	    Double ETH_Low = json.getJSONObject("RAW").getJSONObject("ETH").getJSONObject("USD").getDouble("LOW24HOUR");
	    	    Double LTC_Low = json.getJSONObject("RAW").getJSONObject("LTC").getJSONObject("USD").getDouble("LOW24HOUR");
	    	    
	    	    
	    	    //Populate Current Prices
	    	    tblCrypto.setValueAt("$"+BTC_Price, 0,2);
	    	    tblCrypto.setValueAt("$"+LTC_Price, 1,2);
	    	    tblCrypto.setValueAt("$"+ETH_Price, 2,2);
	    	    
	    	    //Add increase/decrease Arrows
	    	    tblCrypto.setValueAt(downarrow, 0,1);
	    	    tblCrypto.setValueAt(uparrow, 1,1);
	    	    tblCrypto.setValueAt("a", 2,1);
	    	    
	    	    //Populate High Prices
	    	    tblCrypto.setValueAt("$"+BTC_High, 0,3);
	    	    tblCrypto.setValueAt("$"+LTC_High, 1,3);
	    	    tblCrypto.setValueAt("$"+ETH_High, 2,3);
	    	    
	    	    //Populate Low Prices
	    	    tblCrypto.setValueAt("$"+BTC_Low, 0,4);
	    	    tblCrypto.setValueAt("$"+LTC_Low, 1,4);
	    	    tblCrypto.setValueAt("$"+ETH_Low, 2,4);
	    	    
	    	    
	    	    
	    	    
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
		timer.schedule(new GetCryptoPrices(), 0, 15000);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmRpiDashboard = new JFrame();
		frmRpiDashboard.setAlwaysOnTop(true);
		frmRpiDashboard.setTitle("RPi Dashboard");
		frmRpiDashboard.setBounds(100, 100, 450, 300);
		frmRpiDashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JScrollPane scrollPane = new JScrollPane();
		frmRpiDashboard.getContentPane().add(scrollPane, BorderLayout.EAST);
		
		tblCrypto = new JTable();
		tblCrypto.setFont(new Font("Dialog", Font.PLAIN, 18));
		scrollPane.setViewportView(tblCrypto);
		tblCrypto.setShowHorizontalLines(false);
		tblCrypto.setShowVerticalLines(false);
		tblCrypto.setShowGrid(false);
		tblCrypto.setRowSelectionAllowed(false);
		tblCrypto.setModel(new DefaultTableModel(
			new Object[][] {
				{"BTC", null, null, null, null},
				{"LTC", null, null, null, null},
				{"ETH", null, null, null, null},
			},
			new String[] {
				"Coin", "", "Price", "High", "Low"
			}
		));
		
		tblCrypto.getColumnModel().getColumn(0).setPreferredWidth(51);
		tblCrypto.getColumnModel().getColumn(1).setPreferredWidth(25);
		tblCrypto.getColumnModel().getColumn(2).setPreferredWidth(116);
		tblCrypto.getColumnModel().getColumn(3).setPreferredWidth(113);
		tblCrypto.getColumnModel().getColumn(4).setPreferredWidth(134);
		tblCrypto.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		
		
		//frame.getContentPane().add(tblCrypto, BorderLayout.SOUTH);
	}

}

