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

import java.util.prefs.BackingStoreException;
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
	final static String[] ZERNLIST4 = {"Z40","Z60"};
	final static int[]	zernindn4 = {4, 6,};
	final static int[]	zernindm4 = {0, 0,};
	final static String[] ZERNLIST5 = {"Z22","Z2-2","Z31","Z3-1","Z33","Z3-3","Z42","Z4-2"};
	final static int[]	zernindn5 = {2, 2, 3, 3, 3, 3, 4, 4,};
	final static int[]	zernindm5 = {2,-2, 1,-1, 3,-3, 2,-2,};
	
	
	final static String[] ZERNLIST = {"Low (5 modes)","Medium (9 modes)","High (12 modes)","Z40,Z60","Z22,Z31,Z33,Z42"}; 
	final static String demofile = "REALM/DemoAcquisitions/SMimage_Z";

	private JComboBox Zernikesfield_;
	private JComboBox Nbiasesfield_;
	private JComboBox Nroundsfield_;
	private JComboBox DMnamefield_;
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

//		try {
//			prefs_.clear();
//		} catch (BackingStoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	
		params_.NA = (float) prefs_.getFloat("NA",(float) 1.35);
		params_.wavelength = (float) prefs_.getFloat("wavelength",(float) 690);
		params_.Nrounds = (int) prefs_.getInt("Nrounds",2);
		params_.Nbiases = (int) prefs_.getInt("Nbiases",13);
		params_.maxbiasrad = (double) prefs_.getDouble("maxbiasrad",(double) 1.0);
		params_.Zernlist = (int) prefs_.getInt("Zernlist",1);
		params_.DMname = (String) prefs_.get("DMname", "Core");
		params_.show = prefs_.getBoolean("showdata", false);
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
		String[] DeviceList_ = core_.getLoadedDevices().toArray();
		
		// Parameters 
		NAfield_ = new JTextField(String.valueOf(params_.NA),5);      	
		wavelengthfield_ = new JTextField(String.valueOf(params_.wavelength),5);
		Zernikesfield_ = new JComboBox(ZERNLIST); 
		Zernikesfield_.setSelectedIndex(params_.Zernlist);
		Nroundsfield_ = new JComboBox(Nroundslist); 
		Nroundsfield_.setSelectedItem(String.valueOf(params_.Nrounds));
		Nbiasesfield_ = new JComboBox(Nbiaseslist); 
		Nbiasesfield_.setSelectedItem(String.valueOf(params_.Nbiases));
		DMnamefield_ = new JComboBox(DeviceList_); 
		DMnamefield_.setSelectedItem(String.valueOf(params_.DMname));
		
		maxbiasfield_ = new JTextField(String.valueOf(params_.maxbiasrad),5);
		abcorstart_ = new JButton("START");
		abcorstart_.setFont(new Font("Arial", Font.PLAIN, 40));
		abcorstop_ = new JButton("STOP");
		abcorstop_.setEnabled(false);
		abcorsave_ = new JButton("Save correction data");
		savewavefront_ = new JButton("Save wavefront");
		loadwavefront_ = new JButton("Load wavefront");
		applyastig_= new JButton("Apply 60nm astigmatism");
		showdatafield_ = new JCheckBox();
		showdatafield_.setMnemonic(KeyEvent.VK_C); 
		showdatafield_.setSelected(params_.show);
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
		addcmbParameterField("DM device","DM device name",DMnamefield_, AbCorParamPanel);
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
				System.out.println(params_.Nbiases);
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
		
		DMnamefield_.addActionListener(new java.awt.event.ActionListener() {   
			public void actionPerformed(java.awt.event.ActionEvent e) {                 
				String DMname =  (String) DMnamefield_.getItemAt(DMnamefield_.getSelectedIndex());
				String[] DevicePropertyList = null;
				try {
					DevicePropertyList = core_.getDevicePropertyNames(DMname).toArray();
				} catch (Exception e2) {
					e2.printStackTrace();
					System.out.print(e2);
				}
				
				System.out.println(Arrays.toString(DevicePropertyList));
				// Check if appropiate properties are available
				String MisProp = new String();
				for (int jzern = 0; jzern < params_.Nzernikes; jzern ++) {
					if (!Arrays.asList(DevicePropertyList).contains( params_.Zernikes[jzern] )) {
						MisProp = MisProp + params_.Zernikes[jzern] + "\n";
					}
				}
				
				if (!Arrays.asList(DevicePropertyList).contains("ApplyZernikes")) 
					MisProp = MisProp + "ApplyZernikes\n";
				if (!Arrays.asList(DevicePropertyList).contains("Load wavefront correction")) 
					MisProp = MisProp + "Load wavefront correction\n";
				if (!Arrays.asList(DevicePropertyList).contains("Save current position [input filename]")) 
					MisProp = MisProp + "Save current position [input filename]\n";
				
				if (MisProp.length() > 0) {
				JOptionPane.showMessageDialog(null,"REALM does not (fully) support this device. \nMissing properties are:\n" + MisProp, "Warning", 
						JOptionPane.PLAIN_MESSAGE);
				}
				System.out.println(MisProp);
				params_.DMname = DMname;
				prefs_.put("DMname",DMname);
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
							JOptionPane.showMessageDialog(null, demofile + "X_X.tif", "Warning", 
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
				
				// Check if supported device is loaded
				if (!params_.demomode) {
					String[] DevicePropertyList = null;
					try {
						DevicePropertyList = core_.getDevicePropertyNames(params_.DMname).toArray();
					} catch (Exception e2) {
						e2.printStackTrace();
						System.out.print(e2);
					}
					
					// Check if appropiate properties are available: loop over Zernike modes 
					for (int jzern = 0; jzern < params_.Nzernikes; jzern ++) {
						if (!Arrays.asList(DevicePropertyList).contains( params_.Zernikes[jzern] )) {
							JOptionPane.showMessageDialog(null,"Aberration correction cannot start: DM device is not supported.", "Warning", 
									JOptionPane.PLAIN_MESSAGE);
						return;
						}
					}
					
					if (!Arrays.asList(DevicePropertyList).contains("ApplyZernikes")) {
						JOptionPane.showMessageDialog(null,"Aberration correction cannot start: DM device is not supported.", "Warning", 
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
				// Check if proper device is loaded
				String[] DevicePropertyList = null;
				try {
					DevicePropertyList = core_.getDevicePropertyNames(params_.DMname).toArray();
				} catch (Exception e2) {
					e2.printStackTrace();
					System.out.print(e2);
				}
				
				System.out.println(Arrays.toString(DevicePropertyList));
				// Check if appropiate properties are available: loop over Zernike modes 
				if (!Arrays.asList(DevicePropertyList).contains("Z22")) {
						JOptionPane.showMessageDialog(null,"Astigmatism cannot be applied: DM device is not supported.", "Warning", 
								JOptionPane.PLAIN_MESSAGE);
					return;
				}
			
				if (!Arrays.asList(DevicePropertyList).contains("ApplyZernikes")) {
					JOptionPane.showMessageDialog(null,"Astigmatism cannot be applied: DM device is not supported.", "Warning", 
							JOptionPane.PLAIN_MESSAGE);
					return;
				}
				
				applyastig_actionperformed(evt);
			}
		});

		loadwavefront_.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				
				// Check if  device has proper property
				String[] DevicePropertyList = null;
				try {
					DevicePropertyList = core_.getDevicePropertyNames(params_.DMname).toArray();
				} catch (Exception e2) {
					e2.printStackTrace();
					System.out.print(e2);
				}
				
				System.out.println(Arrays.toString(DevicePropertyList));
				// Check if appropiate properties are available: loop over Zernike modes 
				if (!Arrays.asList(DevicePropertyList).contains("Load wavefront correction")) {
						JOptionPane.showMessageDialog(null,"Cannot load wavefront: DM device does not support property \"Load wavefront correction\".", "Warning", 
								JOptionPane.PLAIN_MESSAGE);
					return;
				}
				
				loadwavefront_actionperformed(evt);
			}
		});

		savewavefront_.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				
				// Check if  device has proper property
				String[] DevicePropertyList = null;
				try {
					DevicePropertyList = core_.getDevicePropertyNames(params_.DMname).toArray();
				} catch (Exception e2) {
					e2.printStackTrace();
					System.out.print(e2);
				}
				
				System.out.println(Arrays.toString(DevicePropertyList));
				// Check if appropiate properties are available: loop over Zernike modes 
				if (!Arrays.asList(DevicePropertyList).contains("Save current position [input filename]")) {
						JOptionPane.showMessageDialog(null,"Cannot save wavefront: DM device does not support property \"Save current position [input filename]\".", "Warning", 
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
					core_.setProperty(params_.DMname, params_.Zernikes[jzern], 0);
					core_.setProperty(params_.DMname,"ApplyZernikes",1);
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,e.getMessage(), "Error", JOptionPane.PLAIN_MESSAGE);;
		}

	};  

	private void abcorsave_actionperformed(java.awt.event.ActionEvent evt) {

		JFileChooser c = new JFileChooser();
		c.setSelectedFile(new File(prefs_.get("directory", null) + (File.separatorChar+ "abcordata.txt")));
	
//		JOptionPane.showMessageDialog(null, "save file", prefs_.get("directory", System.getProperty("user") + (File.separatorChar + "My Documents")) + (File.separatorChar+ "abcordata.txt"),
//				JOptionPane.PLAIN_MESSAGE);
		
		
		if (c.showSaveDialog(REALMframe.this) == JFileChooser.APPROVE_OPTION) {

//				JOptionPane.showMessageDialog(null, c.getCurrentDirectory().toString() + File.separatorChar + c.getSelectedFile().getName(),"Save file",	JOptionPane.PLAIN_MESSAGE);		
//				JOptionPane.showMessageDialog(null, (c.getCurrentDirectory().toString() + File.separatorChar + FilenameUtils.removeExtension(c.getSelectedFile().getName()) + ".txt"),"Save file",	JOptionPane.PLAIN_MESSAGE);	
//			filename_.setText(FilenameUtils.getBaseName(c.getSelectedFile().getName()));
//			dir_.setText(c.getCurrentDirectory().toString());	    	  
						
			String savestring =  new String(c.getCurrentDirectory().toString() + File.separatorChar + c.getSelectedFile().getName());
			if (!savestring.endsWith(".txt")) {
				savestring = savestring.concat(".txt");
			}
			prefs_.put("directory", c.getCurrentDirectory().toString());	

			try {	
				abcorresults_.save(savestring);
				JOptionPane.showMessageDialog(null, savestring,"File saved",	JOptionPane.PLAIN_MESSAGE);	
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null,e.getMessage(), "Error", JOptionPane.PLAIN_MESSAGE);
			}

		}

		System.out.println("Aberration correction saved!");

	};  

	private void applyastig_actionperformed(java.awt.event.ActionEvent evt){
		System.out.println("Astigmatism applied");		
		try {    		 
			double astigcurrent = Double.valueOf(core_.getProperty(params_.DMname,"Z22"));
			double astignew = astigcurrent + 0.060;
			System.out.println(astigcurrent);
			System.out.println(astignew);
			REALMframe.core_.setProperty(params_.DMname, "Z22",astignew);
			REALMframe.core_.setProperty(params_.DMname,"ApplyZernikes",1);

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,e.getMessage(), "Error", JOptionPane.PLAIN_MESSAGE);;
		}	   	 
	};   
	
	private void loadwavefront_actionperformed(java.awt.event.ActionEvent evt){
		JFileChooser c = new JFileChooser();

		c.setSelectedFile(new File(prefs_.get("directory", null) + (File.separatorChar+ ".wcs")));

		int rVal = c.showOpenDialog(REALMframe.this);

		if (rVal == JFileChooser.APPROVE_OPTION) {

			String loadstring =  new String(c.getCurrentDirectory().toString() + File.separatorChar + c.getSelectedFile().getName());
			prefs_.put("directory", c.getCurrentDirectory().toString());
			
			System.out.println(loadstring);

			try { 
				REALMframe.core_.setProperty(params_.DMname,"Load wavefront correction", loadstring);  		 	  	    		  
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null,e.getMessage(), "Error", JOptionPane.PLAIN_MESSAGE);;
			}	
		}
	}
	
	private void savewavefront_actionperformed(java.awt.event.ActionEvent evt){
		JFileChooser c = new JFileChooser();		      
		c.setSelectedFile(new File(prefs_.get("directory", null) + (File.separatorChar+ "wavefront.wcs")));

		int rVal = c.showSaveDialog(REALMframe.this);

		if (rVal == JFileChooser.APPROVE_OPTION) {

			String savestring =  c.getCurrentDirectory().toString() + File.separatorChar + c.getSelectedFile().getName();
			if (!savestring.endsWith(".wcs")) {
				savestring = savestring.concat(".wcs");
			}
			prefs_.put("directory", c.getCurrentDirectory().toString());
			
			try { 
				REALMframe.core_.setProperty(params_.DMname,"Save current position [input filename]", savestring);
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



