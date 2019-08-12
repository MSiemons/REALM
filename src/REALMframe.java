///////////////////////////////////////////////////////////////////////////////
//FILE:          REALMframe.java
//PROJECT:       REALM
//-----------------------------------------------------------------------------
//
// DISCRIPTON:	 The main frame class of the plugin. This handles the GUI.
//
// AUTHOR:       Marijn Siemons
//
// COPYRIGHT:    Utrecht University 2019
//
// LICENSE:      This file is distributed under the GNU GENERAL PUBLIC LICENSE license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

import java.awt.event.KeyEvent;
import java.io.File;
import mmcorej.CMMCore;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.prefs.Preferences;
import java.util.Arrays;
import java.util.concurrent.Executors;

import org.apache.commons.io.FilenameUtils;
import org.micromanager.api.MMListenerInterface;
import org.micromanager.api.ScriptInterface;

public class REALMframe extends javax.swing.JFrame 
implements MMListenerInterface {


	private final ScriptInterface gui_;
	public static CMMCore core_;     
	private Preferences prefs_;
	private Params params_;

	private LayoutManager frameLayout_;

	private static final long serialVersionUID = 1L;
	final static String TAB1 = "Settings";
	final static String TAB2 = "Aberration correction";
	final static String TAB3 = "DM control";
	final static String NA = "NA";
	final static String WAVELENGTH = "Wavelength [nm]";
	final static String NROUNDS = "Number of correction rounds";
	final static String NBIASES = "Number of biases";
	final static String MAXBIAS = "Maximum bias [rad]";
	final static String[] Nroundslist = {"1","2","3","4","5"};
	final static String[] Nbiaseslist = {"5","7","9","11","13"};
	final static String SHOWDATA = "Show metric values and fits";	 
	final static String SHOWDATAdescript = "Plot metric values of acquisitions and metric curve fits";
	final static String DEMOMODE = "DEMO mode";	
	final static String DEMOMODEdescript = "DEMO mode requires no deformable mirror and uses dummy acquisitions";	
	final static String WAVELENGTHdescript = "Peak emission wavelength in nm";
	final static String NAdescript = "(Effective) NA of objective";
	final static String MAXBIASdescript = "Maximum applied bias during correction procedure in radians";
	final static String NROUNDSdescript = "Number of correction rounds";
	final static String NBIASESdescript = "Number of biases applied per Zernike mode (minimum 5)";
	final static String ZERNIKES = "Zernike mode selection";
	final static String ZERNIKESdescript = "Choose the level of Zernike modes to be correction. Low: Z22, Z2-2, Z31, Z3-1, Z40. Medium: Low + Z33, Z3-3, Z42, Z4-2. High: Medium + Z44, Z4-4, Z60";
	final static String[] ZERNLIST1 = {"Z22","Z2-2","Z31","Z3-1","Z40"};
	final static int[]	zernindn1 = {2, 2, 3, 3, 4};
	final static int[]	zernindm1 = {2,-2, 1,-1, 0};
	final static String[] ZERNLIST2 = {"Z22","Z2-2","Z31","Z3-1","Z33","Z3-3","Z40","Z42","Z4-2"};
	final static int[]	zernindn2 = {2, 2, 3, 3, 3, 3, 4, 4, 4,};
	final static int[]	zernindm2 = {2,-2, 1,-1, 3,-3, 0, 2,-2,};
	final static String[] ZERNLIST3 = {"Z22","Z2-2","Z31","Z3-1","Z33","Z3-3","Z40","Z42","Z4-2","Z44","Z4-4","Z60"};
	final static int[]	zernindn3 = {2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 6};
	final static int[]	zernindm3 = {2,-2, 1,-1, 3,-3, 0, 2,-2, 4,-4, 0};
	final static String[] ZERNLIST = {"Low (5 modes)","Medium (9 modes)","High (12 modes)"}; 
	final static String demofile = "REALM/DemoAcquisitions/SMimage_Z";

	private JComboBox Zernikesfield_;
	private JComboBox Nbiasesfield_;
	private JComboBox Nroundsfield_;
	private JTextField NAfield_;
	private JTextField wavelengthfield_;
	private JTextField maxbiasfield_;
	public static JButton abcorstart_;
	public static JButton abcorstop_;
	private JButton abcorsave_;
	private JButton showmetricfield_;
	private JButton savewavefront_;
	private JButton loadwavefront_;
	private JButton applyastig_;
	private JTextField filename_;
	private JTextField dir_ ;
	private JCheckBox showdatafield_;
	private JCheckBox demomodefield_;
	private AberrationCorrection abcorresults_;


	public REALMframe(ScriptInterface gui) {

		gui_ = gui;
		core_ = gui_.getMMCore(); 
		prefs_ = Preferences.userNodeForPackage(this.getClass());
		params_ = new Params(); 
		
		params_.NA = (float) prefs_.getFloat("NA",(float) 1.35);
		params_.wavelength = (float) prefs_.getFloat("wavelength",(float) 690);
		params_.Nrounds = (int) prefs_.getInt("Nrounds",2);
		params_.Nbiases = (int) prefs_.getInt("Nbiases",13);
		params_.maxbiasrad = (double) prefs_.getDouble("maxbiasrad",(double) 1.0);
		params_.Zernlist = (int) prefs_.getInt("Zernlist",1);	
		params_.alpha = (float) 1.3;
		params_.update();   

		frameLayout_ = this.getContentPane().getLayout();

	}

	public final void initialize() {

		// start with a clean slate
		this.getContentPane().removeAll();

		// The no-drive display changes the layout. Switch back here.
		this.getContentPane().setLayout(frameLayout_);      

		this.setSize(800, 500);
		this.setLocation(200, 200);
		setTitle("REALM");
		initComponents();

	}

	private void initComponents() {
		// Parameters 
		NAfield_ = new JTextField(String.valueOf(params_.NA),5);      	
		wavelengthfield_ = new JTextField(String.valueOf(params_.wavelength),5);
		Zernikesfield_ = new JComboBox(ZERNLIST); 
		Zernikesfield_.setSelectedIndex(params_.Zernlist);
		Nroundsfield_ = new JComboBox(Nroundslist); 
		Nroundsfield_.setSelectedItem(String.valueOf(params_.Nrounds));
		Nbiasesfield_ = new JComboBox(Nbiaseslist); 
		Nbiasesfield_.setSelectedItem(String.valueOf(params_.Nbiases));
		maxbiasfield_ = new JTextField(String.valueOf(params_.maxbiasrad),5);
		abcorstart_ = new JButton("START");
		abcorstart_.setFont(new Font("Arial", Font.PLAIN, 40));
		abcorstop_ = new JButton("STOP");
		abcorstop_.setEnabled(false);
		abcorsave_ = new JButton("Save correction data");
		savewavefront_ = new JButton("Save wavefront");
		loadwavefront_ = new JButton("Load wavefront");
		applyastig_= new JButton("Apply 60nm astigmatisme");
		showdatafield_ = new JCheckBox();
		showdatafield_.setMnemonic(KeyEvent.VK_C); 
		showdatafield_.setSelected(prefs_.getBoolean("showdata", false));
		demomodefield_ = new JCheckBox();
		demomodefield_.setMnemonic(KeyEvent.VK_C); 
		demomodefield_.setSelected(false);
		showmetricfield_ = new JButton("Show metric");
		filename_ = new JTextField(); 
		dir_ = new JTextField()  ;
		abcorresults_ = new AberrationCorrection();

		JTabbedPane tabbedPane = new JTabbedPane();

		//Create the "cards".
		JPanel card1 = new JPanel();

		TitledBorder SystemParamPanelbordertitle;
		TitledBorder AbcorParamPanelbordertitle;


		JPanel SystemParamPanel = new JPanel();
		SystemParamPanel.setLayout(new BoxLayout(SystemParamPanel, BoxLayout.Y_AXIS));
		SystemParamPanelbordertitle = BorderFactory.createTitledBorder("System parameters");   

		adddblParameterField(NA,NAdescript, NAfield_ ,SystemParamPanel);
		SystemParamPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		adddblParameterField(WAVELENGTH,WAVELENGTHdescript, wavelengthfield_ , SystemParamPanel);
		SystemParamPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		SystemParamPanel.setBorder(SystemParamPanelbordertitle);			

		JPanel AbCorParamPanel = new JPanel();
		AbCorParamPanel.setLayout(new BoxLayout(AbCorParamPanel, BoxLayout.Y_AXIS));
		AbcorParamPanelbordertitle = BorderFactory.createTitledBorder("Adaptive Optics parameters");

		addcmbParameterField(ZERNIKES,ZERNIKESdescript,Zernikesfield_, AbCorParamPanel);
		AbCorParamPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		addcmbParameterField(NROUNDS,NROUNDSdescript,Nroundsfield_, AbCorParamPanel);
		AbCorParamPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		addcmbParameterField( NBIASES,NBIASESdescript,Nbiasesfield_, AbCorParamPanel);
		AbCorParamPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		adddblParameterField(MAXBIAS,MAXBIASdescript, maxbiasfield_, AbCorParamPanel);
		AbCorParamPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		addcheckboxField(SHOWDATA,SHOWDATAdescript,showdatafield_, AbCorParamPanel);   
		AbCorParamPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		addcheckboxField(DEMOMODE,DEMOMODEdescript,demomodefield_, AbCorParamPanel);   
		AbCorParamPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		AbCorParamPanel.setBorder(AbcorParamPanelbordertitle);       

		card1.setLayout(new BoxLayout(card1, BoxLayout.Y_AXIS));
		card1.add(SystemParamPanel);
		card1.add(Box.createRigidArea(new Dimension(0, 10)));
		card1.add(AbCorParamPanel);	

		GridLayout gridlayout1 = new GridLayout(1,1); 
		GridLayout gridlayout2 = new GridLayout(2,2); 
		gridlayout1.setHgap(10);
		gridlayout2.setHgap(10);
		gridlayout2.setVgap(10);

		JPanel card2 = new JPanel(new GridLayout(2,1));
		card2.add(abcorstart_);

		JPanel card2sub = new JPanel();
		card2sub.setLayout( new GridLayout(0,2));
		card2sub.add(abcorstop_);
		card2sub.add(abcorsave_);
		card2sub.add(applyastig_);
		card2sub.add(showmetricfield_);
		card2sub.add(savewavefront_);     
		card2sub.add(loadwavefront_);    
		card2.add(card2sub);    

		tabbedPane.addTab(TAB1, card1);
		tabbedPane.addTab(TAB2, card2);


		this.add(tabbedPane, BorderLayout.CENTER);

		//////////////////// Action listeners settings tab ////////////////
		
		NAfield_.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				params_.NA = Float.valueOf(NAfield_.getText());
				params_.update();
				prefs_.putFloat("NA", (float) params_.NA);
			}
		});

		NAfield_.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent evt) {
				params_.NA = Float.valueOf(NAfield_.getText());
				params_.update();
				prefs_.putFloat("NA", (float) params_.NA);
			}
		});

		wavelengthfield_.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				params_.wavelength = Float.valueOf(wavelengthfield_.getText());
				params_.update();
				prefs_.putFloat("wavelength", (float) params_.wavelength);
			}
		});

		wavelengthfield_.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent evt) {
				params_.wavelength = Float.valueOf(wavelengthfield_.getText());
				params_.update();
				prefs_.putFloat("wavelength", (float) params_.wavelength);
			}
		});

		Zernikesfield_.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				params_.Zernlist = Zernikesfield_.getSelectedIndex();
				params_.update();
				prefs_.putInt("Zernlist", (int) params_.Zernlist);
			}
		});

		Zernikesfield_.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent evt) {
				params_.Zernlist = Zernikesfield_.getSelectedIndex();
				params_.update();
				prefs_.putInt("Zernlist", (int) params_.Zernlist);
			}
		});

		Nroundsfield_.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				params_.Nrounds = Integer.parseInt((String) Nroundsfield_.getItemAt(Nroundsfield_.getSelectedIndex()));
				params_.update();
				prefs_.putInt("Nrounds", (int) params_.Nrounds);
			}
		});

		Nroundsfield_.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent evt) {
				params_.Nrounds = Integer.parseInt((String) Nroundsfield_.getItemAt(Nroundsfield_.getSelectedIndex()));
				params_.update();
				prefs_.putInt("Nrounds", (int) params_.Nrounds);
			}
		});

		Nbiasesfield_.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				params_.Nbiases = Integer.parseInt((String) Nbiasesfield_.getItemAt(Nbiasesfield_.getSelectedIndex()));
				params_.update();
				prefs_.putInt("Nbiases", (int) params_.Nbiases);
			}
		});

		Nbiasesfield_.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent evt) {
				params_.Nbiases = Integer.parseInt((String) Nbiasesfield_.getItemAt(Nbiasesfield_.getSelectedIndex()));
				params_.update();
				prefs_.putInt("Nbiases", (int) params_.Nbiases);
			}
		});	

		maxbiasfield_.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				params_.maxbiasrad = Float.valueOf(maxbiasfield_.getText());
				params_.update();
				prefs_.putFloat("maxbiasrad", (float) params_.maxbiasrad);
			}
		});

		maxbiasfield_.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent evt) {
				params_.maxbiasrad = Float.valueOf(maxbiasfield_.getText());
				params_.update();
				prefs_.putFloat("maxbiasrad", (float) params_.maxbiasrad);
			}
		});
		
		showdatafield_.addItemListener(new java.awt.event.ItemListener() {    
			public void itemStateChanged(java.awt.event.ItemEvent e) {                 
				params_.show = (e.getStateChange()==1?true:false); 
				prefs_.putBoolean("showdata",params_.show);
			}    
		});  
		
		demomodefield_.addItemListener(new java.awt.event.ItemListener() {    
			public void itemStateChanged(java.awt.event.ItemEvent e) {  
				// check if demo files exist
				for (int jzern = 0; jzern < params_.Nzernikes; jzern ++) {
					for (int jbias = 0; jbias < params_.Nbiases; jbias++) {

						String file = demofile + (jzern+1) + "_" + (jbias + 1) + ".tif";
						File tempFile = new File(file);

						if (!tempFile.exists()){
							JOptionPane.showMessageDialog(null,"Cannot find demo files in REALM\\demofiles\\SMimages_ZX_X.tif", "Warning", 
									JOptionPane.PLAIN_MESSAGE);
							return;
						}
					}
				}	

				params_.demomode = (e.getStateChange()==1?true:false); 
				//               System.out.println(params_.demomode);
			}    
		}); 
		
		//////////////////// Action listeners Aberration correction tab ////////////////
		
		abcorstart_.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {

				// Stop current acquisition stream
				try {
					core_.stopSequenceAcquisition();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// Check if MIRAO52E device is loaded
				if (!params_.demomode) {
					String[] DeviceList = core_.getLoadedDevices().toArray();
					if (!Arrays.asList(DeviceList).contains("MIRAO52E")) {
						JOptionPane.showMessageDialog(null,"Cannot start aberration correction: MIRAO52E not loaded. \nConsider using DEMO mode.", "Warning", 
								JOptionPane.PLAIN_MESSAGE);
						return;
					}
				}				
				// (in)activate start and stop buttons
				abcorstart_.setEnabled(false);
				abcorstop_.setEnabled(true);
				abcorresults_.AOstopflag = false;
				
				// start aberration correction
				Executors.newSingleThreadExecutor().submit(new Runnable() {
					@Override 
					public void run() {	                     
						abcorstart_actionperformed();
					}
				});               	             
			}
		});

		abcorstop_.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				abcorstop_actionperformed();
				JOptionPane.showMessageDialog(null,"Aberration correction stopped. Zernike modes reset to zero.", "Warning", 
						JOptionPane.PLAIN_MESSAGE);			
			}
		});

		showmetricfield_.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				Acquisition.showMetric(params_);
			}
		});	    

		abcorsave_.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (!abcorresults_.succes) {
					JOptionPane.showMessageDialog(null,"Aberration correction not peformed: nothing to save.", "Error", 
							JOptionPane.PLAIN_MESSAGE);
					return;
				}
				abcorsave_actionperformed(evt);
			}
		});

		applyastig_.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				// Check if MIRAO52E device is loaded
				String[] DeviceList = core_.getLoadedDevices().toArray();
				if (!Arrays.asList(DeviceList).contains("MIRAO52E")) {
					JOptionPane.showMessageDialog(null,"Cannot apply astigmatism: MIRAO52E not loaded.", "Warning", 
							JOptionPane.PLAIN_MESSAGE);
					return;
				}
				
				applyastig_actionperformed(evt);
			}
		});

		loadwavefront_.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				
				// Check if MIRAO52E device is loaded
				String[] DeviceList = core_.getLoadedDevices().toArray();
				if (!Arrays.asList(DeviceList).contains("MIRAO52E")) {
					JOptionPane.showMessageDialog(null,"Cannot load wavefront: MIRAO52E not loaded.", "Error", 
							JOptionPane.PLAIN_MESSAGE);
					return;
				}
				
				loadwavefront_actionperformed(evt);
			}
		});

		savewavefront_.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				
				// Check if MIRAO52E device is loaded
				String[] DeviceList = core_.getLoadedDevices().toArray();
				if (!Arrays.asList(DeviceList).contains("MIRAO52E")) {
					JOptionPane.showMessageDialog(null,"Cannot save wavefront: MIRAO52E not loaded.", "Error", 
							JOptionPane.PLAIN_MESSAGE);
					return;
				}
				
				savewavefront_actionperformed(evt);
			}
		});

	}

	private void abcorstart_actionperformed() {

		try {
			abcorresults_.start(params_);
		} catch (Exception e) {
			abcorstop_actionperformed();
			JOptionPane.showMessageDialog(null,"Error in aberration correction. Zernike modes reset to zero.", "Warning", 
					JOptionPane.PLAIN_MESSAGE);			
		}	
		
	};

	private void abcorstop_actionperformed(){
		// (in)activate start and stop buttons
		abcorstart_.setEnabled(true);
		abcorstop_.setEnabled(false);
		abcorresults_.AOstopflag = true;
		
		try {
			for (int jzern = 0; jzern < params_.Nzernikes; jzern ++) {   			 
				if (!params_.demomode) {
					core_.setProperty("MIRAO52E", params_.Zernikes[jzern], 0);
					core_.setProperty("MIRAO52E","ApplyZernikes",1);
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,e.getMessage(), "Error", JOptionPane.PLAIN_MESSAGE);;
		}

	};  

	private void abcorsave_actionperformed(java.awt.event.ActionEvent evt) {

		JFileChooser c = new JFileChooser();

		if (prefs_.get("directory", null) != null)
			c.setSelectedFile(new File(prefs_.get("directory", "") + "/abcordata.txt"));
		else
			c.setSelectedFile(new File("abcordata.txt"));

		int rVal = c.showSaveDialog(REALMframe.this);
		if (rVal == JFileChooser.APPROVE_OPTION) {

			filename_.setText(FilenameUtils.getBaseName(c.getSelectedFile().getName()));
			dir_.setText(c.getCurrentDirectory().toString());	    	  
			prefs_.put("directory", dir_.getText());

			String savestring =  dir_.getText() + "\\" + filename_.getText() + ".txt";
			System.out.println(savestring);
			try {
				abcorresults_.save(savestring, params_);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

		}

		System.out.println("Aberration correction saved!");

	};  

	private void applyastig_actionperformed(java.awt.event.ActionEvent evt){
		System.out.println("Astigmatisme applied");		
		try {    		 
			double astigcurrent = Double.valueOf(core_.getProperty("MIRAO52E","Z22"));
			double astignew = astigcurrent + 0.060;
			System.out.println(astigcurrent);
			System.out.println(astignew);
			REALMframe.core_.setProperty("MIRAO52E", "Z22",astignew);
			REALMframe.core_.setProperty("MIRAO52E","ApplyZernikes",1);

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,e.getMessage(), "Error", JOptionPane.PLAIN_MESSAGE);;
		}	   	 
	};   
	
	private void loadwavefront_actionperformed(java.awt.event.ActionEvent evt){
		JFileChooser c = new JFileChooser();
		if (prefs_.get("directory", null) != null)
			c.setCurrentDirectory(new File(prefs_.get("directory", "")));

		int rVal = c.showOpenDialog(REALMframe.this);

		if (rVal == JFileChooser.APPROVE_OPTION) {

			filename_.setText(c.getSelectedFile().getName());
			dir_.setText(c.getCurrentDirectory().toString());
			prefs_.put("directory", dir_.getText());

			String loadstring =  dir_.getText() + "\\" + filename_.getText();
			System.out.println(loadstring);

			try { 
				REALMframe.core_.setProperty("MIRAO52E","Load wavefront correction", loadstring);  		 	  	    		  
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null,e.getMessage(), "Error", JOptionPane.PLAIN_MESSAGE);;
			}	
		}
	}
	
	private void savewavefront_actionperformed(java.awt.event.ActionEvent evt){
		JFileChooser c = new JFileChooser();		      
		if (prefs_.get("directory", null) != null)
			c.setSelectedFile(new File(prefs_.get("directory", "") + "/wfc.wlc"));
		else
			c.setSelectedFile(new File("wfc.wlc"));


		int rVal = c.showSaveDialog(REALMframe.this);

		if (rVal == JFileChooser.APPROVE_OPTION) {
			filename_.setText(FilenameUtils.getBaseName(c.getSelectedFile().getName()));
			dir_.setText(c.getCurrentDirectory().toString());
			prefs_.put("directory", dir_.getText());

			String savestring =  dir_.getText() + "\\" + filename_.getText()  + ".wcs";
			System.out.println(savestring);
			try { 
				REALMframe.core_.setProperty("MIRAO52E","Save current position [input filename]", savestring);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null,e.getMessage(), "Error", JOptionPane.PLAIN_MESSAGE);;
			}
		}
	}

	private static void addcheckboxField(String text, String hovertext, JCheckBox checkbox, Container container) {
		JPanel parampanel = new JPanel();
		parampanel.setLayout(new BorderLayout());

		JLabel paramlabel = new JLabel(text);
		checkbox.setMaximumSize(new Dimension (75, 25));
		parampanel.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
		parampanel.add(paramlabel, BorderLayout.WEST);
		//    	 parampanel.add(Box.createHorizontalGlue());
		parampanel.add(checkbox, BorderLayout.EAST); 
		parampanel.setToolTipText(hovertext);

		container.add(parampanel);
	}

	private static void adddblParameterField(String text, String hovertext, JTextField paramfield, Container container) {
		JPanel parampanel = new JPanel(new BorderLayout());
		//    	 parampanel.setLayout(new BoxLayout(parampanel, BoxLayout.X_AXIS));

		JLabel paramlabel = new JLabel(text);
		paramfield.setMaximumSize(new Dimension (75, 25));
		//    	 paramfield.setHorizontalAlignment(paramfield.RIGHT);
		parampanel.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
		parampanel.add(paramlabel, BorderLayout.WEST);
		parampanel.add(Box.createHorizontalGlue());
		parampanel.add(paramfield, BorderLayout.EAST); 
		parampanel.setToolTipText(hovertext);

		container.add(parampanel);
	}

	public  void addcmbParameterField(String text, String hovertext, JComboBox paramfield, Container container) {
		JPanel parampanel = new JPanel(new BorderLayout());

		JLabel paramlabel = new JLabel(text);
		paramfield.setEditable(false);
		paramfield.getEditor().getEditorComponent().setBackground(Color.WHITE);     
		paramfield.setMaximumSize(new Dimension (4, 25));

		parampanel.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
		parampanel.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
		parampanel.add(paramlabel, BorderLayout.WEST);
		parampanel.add(Box.createHorizontalGlue());
		parampanel.add(paramfield, BorderLayout.EAST); 
		parampanel.setToolTipText(hovertext);

		container.add(parampanel);
	}  

//	private void onWindowClosing(java.awt.event.WindowEvent evt) {
//		System.out.println("Preferences saved");
//		prefs_.putFloat("NA", (float) params_.NA);
//		prefs_.putFloat("Wavelength", (float) params_.wavelength);
//		prefs_.putInt("Zernlist", (int) params_.Zernlist);
//		prefs_.putInt("Nrounds", (int) params_.Nrounds);
//		prefs_.putInt("Nbiases", (int) params_.Nbiases);
//		prefs_.putFloat("maxbiasrad", (float) params_.maxbiasrad);
//	}

	@Override
	public void propertiesChangedAlert() {
	}

	@Override
	public void propertyChangedAlert(String device, String property, String value) {
	}

	@Override
	public void configGroupChangedAlert(String groupName, String newConfig) {
	}

	@Override
	public void systemConfigurationLoaded() {
		initialize();
	}

	@Override
	public void pixelSizeChangedAlert(double newPixelSizeUm) {
	}

	@Override
	public void stagePositionChangedAlert(String deviceName, double pos) {
	}

	@Override
	public void xyStagePositionChanged(String deviceName, double xPos, double yPos) {
	}

	@Override
	public void exposureChanged(String cameraName, double newExposureTime) {
	}

	@Override
	public void slmExposureChanged(String cameraName, double newExposureTime) {
	}
}



