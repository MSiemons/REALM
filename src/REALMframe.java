import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.awt.event.ActionEvent;
import mmcorej.CMMCore;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.util.concurrent.Executors;


import org.micromanager.api.MMListenerInterface;
import org.micromanager.api.ScriptInterface;

public class REALMframe extends javax.swing.JFrame 
	implements MMListenerInterface {

	
private final ScriptInterface gui_;
     public static CMMCore core_;
     
//     private Preferences prefs_;

     private Params params_;
      
//     private NumberFormat nf_;
   
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
	 final static String MAXBIASdescript = "Maximum applied bias during correction in radians";
	 final static String NROUNDSdescript = "Number of correction rounds of correction procedure";
	 final static String NBIASESdescript = "Number of biases applied per Zernike mode (minimum 5)";
	 final static String ZERNIKES = "Zernike mode selection";
	 final static String ZERNIKESdescript = "Choose the level of Zernike modes to be correction. Low order: astigmatism, coma and spherical aberration. High order includes Zernikes up to the fourth Zenrike order";
	 final static String[] ZERNLIST1 = {"Z22","Z2-2","Z31","Z3-1","Z40"};
	 final static int[]	zernindn1 = {2,2,3,3,4};
	 final static int[]	zernindm1 = {2,-2,1,-1,0};
	 final static String[] ZERNLIST2 = {"Z22","Z2-2","Z31","Z3-1","Z33","Z3-3","Z40","Z42","Z4-2","Z44","Z4-4"};
	 final static int[]	zernindn2 = {2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4};
	 final static int[]	zernindm2 = {2,-2, 1,-1, 3,-3, 0, 2,-2, 4,-4};
	 final static String[] ZERNLIST3 = {"Z22","Z2-2","Z31","Z3-1","Z33","Z3-3","Z40","Z42","Z4-2","Z44","Z4-4","Z60"};
	 final static int[]	zernindn3 = {2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 6};
	 final static int[]	zernindm3 = {2,-2, 1,-1, 3,-3, 0, 2,-2, 4,-4, 0};
	 final static String[] ZERNLIST = {"Low order","Medium order","Medium order + Z60"}; 
	 final static String demofile = "REALM/demofiles/SMimage_Z";
	 
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
	 private JButton abcorsavewavefront_;
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
//    	nf_ = NumberFormat.getInstance();
 //   	prefs_ = Preferences.userNodeForPackage(this.getClass());
      
    	params_ = new Params();
    	params_.NA = (float) 1.49;
    	params_.wavelength = 690;
    	params_.Nrounds = 1;
    	params_.Nbiases = 5;	
    	params_.maxbiasrad = 1;
    	params_.Zernikes = ZERNLIST1;		
    	params_.alpha = (float) 1.3;
    	params_.update();     
            	    	
    	initComponents();
      
    	frameLayout_ = this.getContentPane().getLayout();

     }
   
     /**
      * Initialized GUI components based on current hardware configuration
      * Can be called at any time to adjust display (for instance after hardware
      * configuration change)
      */
     public final void initialize() {

    	 // start with a clean slate
    	 this.getContentPane().removeAll();
      
    	 // The no-drive display changes the layout. Switch back here.
    	 this.getContentPane().setLayout(frameLayout_);      
            
    	 this.setSize(800, 500);
    	 setTitle("REALM");
    	 initComponents();
//    	 initialized_ = true;
            
     }
   
     private void initComponents() {
    	// Parameters 
     	NAfield_ = new JTextField(String.valueOf(params_.NA),5);      	
     	wavelengthfield_ = new JTextField(String.valueOf(params_.wavelength),5);
     	Zernikesfield_ = new JComboBox(ZERNLIST); 
     	Nroundsfield_ = new JComboBox(Nroundslist); 
     	Nbiasesfield_ = new JComboBox(Nbiaseslist); 
     	maxbiasfield_ = new JTextField(String.valueOf(params_.maxbiasrad),5);
     	abcorstart_ = new JButton("START");
     	abcorstart_.setFont(new Font("Arial", Font.PLAIN, 40));
     	abcorstop_ = new JButton("STOP");
     	abcorstop_.setEnabled(false);
     	abcorsave_ = new JButton("Save correction data");
     	abcorsavewavefront_ = new JButton("Save wavefront");
     	savewavefront_ = new JButton("Save wavefront");
     	loadwavefront_ = new JButton("Load wavefront");
     	applyastig_= new JButton("Apply 60nm astigmatisme");
     	showdatafield_ = new JCheckBox();
	    showdatafield_.setMnemonic(KeyEvent.VK_C); 
	    showdatafield_.setSelected(false);
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
	    
	  // Update params when changed
	    NAfield_.addActionListener(new java.awt.event.ActionListener() {
	         public void actionPerformed(java.awt.event.ActionEvent evt) {
	        	 params_.NA = Float.valueOf(NAfield_.getText());
	        	 params_.update();
	        	 System.out.println(params_.NA);
	         }
	    });
	    
	    NAfield_.addFocusListener(new java.awt.event.FocusAdapter() {
	         public void focusLost(java.awt.event.FocusEvent evt) {
	        	 params_.NA = Float.valueOf(NAfield_.getText());
	        	 System.out.println(params_.NA);
	         }
	    });
	    
	    wavelengthfield_.addActionListener(new java.awt.event.ActionListener() {
	         public void actionPerformed(java.awt.event.ActionEvent evt) {
	        	 params_.wavelength = Float.valueOf(wavelengthfield_.getText());
	        	 params_.update();
	        	 System.out.println(params_.wavelength);
	         }
	    });
	    
	    wavelengthfield_.addFocusListener(new java.awt.event.FocusAdapter() {
	         public void focusLost(java.awt.event.FocusEvent evt) {
	        	 params_.wavelength = Float.valueOf(wavelengthfield_.getText());
	        	 System.out.println(params_.wavelength);
	         }
	    });
	    
	    Zernikesfield_.addActionListener(new java.awt.event.ActionListener() {
	         public void actionPerformed(java.awt.event.ActionEvent evt) {
	        	 params_.Zernlist = Zernikesfield_.getSelectedIndex();
	        	 params_.update();
	        	 System.out.println(params_.Zernlist);
	        	 System.out.println(java.util.Arrays.toString(params_.Zernikes));
	         }
	    });
	    
	    Zernikesfield_.addFocusListener(new java.awt.event.FocusAdapter() {
	         public void focusLost(java.awt.event.FocusEvent evt) {
	        	 params_.Zernlist = Zernikesfield_.getSelectedIndex();
	        	 params_.update();
	        	 System.out.println(params_.Zernlist);
	        	 System.out.println(java.util.Arrays.toString(params_.Zernikes));
	         }
	    });
	    
	    Nroundsfield_.addActionListener(new java.awt.event.ActionListener() {
	         public void actionPerformed(java.awt.event.ActionEvent evt) {
	        	 params_.Nrounds = Integer.parseInt((String) Nroundsfield_.getItemAt(Nroundsfield_.getSelectedIndex()));
	        	 params_.update();
	        	 System.out.println(params_.Nrounds);
	         }
	    });
	    
	    Nroundsfield_.addFocusListener(new java.awt.event.FocusAdapter() {
	         public void focusLost(java.awt.event.FocusEvent evt) {
	        	 params_.Nrounds = Integer.parseInt((String) Nroundsfield_.getItemAt(Nroundsfield_.getSelectedIndex()));
	        	 params_.update();
	        	 System.out.println(params_.Nrounds);
	         }
	    });
	    
	    Nbiasesfield_.addActionListener(new java.awt.event.ActionListener() {

	         public void actionPerformed(java.awt.event.ActionEvent evt) {
	        	 params_.Nbiases = Integer.parseInt((String) Nbiasesfield_.getItemAt(Nbiasesfield_.getSelectedIndex()));
	        	 params_.update();
	        	 System.out.println(params_.Nbiases);
	         }
	    });
	    
	    Nbiasesfield_.addFocusListener(new java.awt.event.FocusAdapter() {
	         public void focusLost(java.awt.event.FocusEvent evt) {
	        	 params_.Nbiases = Integer.parseInt((String) Nbiasesfield_.getItemAt(Nbiasesfield_.getSelectedIndex()));
	        	 params_.update();
	        	 System.out.println(params_.Nbiases);
	         }
	    });	
	    
	    maxbiasfield_.addActionListener(new java.awt.event.ActionListener() {
	         public void actionPerformed(java.awt.event.ActionEvent evt) {
	        	 params_.maxbiasrad = Float.valueOf(maxbiasfield_.getText());
	        	 params_.update();
	        	 System.out.println(params_.maxbiasrad);
	         }
	    });
	    
	    maxbiasfield_.addFocusListener(new java.awt.event.FocusAdapter() {
	         public void focusLost(java.awt.event.FocusEvent evt) {
	        	 params_.maxbiasrad = Float.valueOf(maxbiasfield_.getText());
	        	 params_.update();
	        	 System.out.println(params_.maxbiasrad);
	         }
	    });
	    
	    // Aberration correction tab
	    abcorstart_.addActionListener(new java.awt.event.ActionListener() {
	         public void actionPerformed(java.awt.event.ActionEvent evt) {
	        	 abcorstart_.setEnabled(false);
	        	 abcorstop_.setEnabled(true);
	        	 abcorresults_.AOstopflag = false;
	             System.out.println("Running: " + abcorresults_.AOstopflag);
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
//	        	 abcorstart_.setEnabled(true);
	        	 abcorstop_.setEnabled(false);
	        	 abcorstop_actionperformed();
	         }
	    });
	    
	    showmetricfield_.addActionListener(new java.awt.event.ActionListener() {
	         public void actionPerformed(java.awt.event.ActionEvent evt) {
	        	 Acquisition.showMetric(params_);
	         }
	    });	    
	    
	    showdatafield_.addItemListener(new java.awt.event.ItemListener() {    
             public void itemStateChanged(java.awt.event.ItemEvent e) {                 
                params_.show = (e.getStateChange()==1?true:false); 
                System.out.println(params_.show);
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
               System.out.println(params_.demomode);
            }    
         }); 
	    
	    abcorsave_.addActionListener(new java.awt.event.ActionListener() {
	         public void actionPerformed(java.awt.event.ActionEvent evt) {
	        	 abcorsave_actionperformed(evt);
	         }
	    });

	    abcorsavewavefront_.addActionListener(new savewavefront());
	    
	    // DM control tab
	    applyastig_.addActionListener(new java.awt.event.ActionListener() {
	         public void actionPerformed(java.awt.event.ActionEvent evt) {
	        	 applyastig_actionperformed(evt);
	         }
	    });
	    
 	    loadwavefront_.addActionListener(new loadwavefront());
 	    
 	    savewavefront_.addActionListener(new savewavefront());
 	    

 	    dir_.setEditable(false);
 	    filename_.setEditable(false);
	    
	    
     }
     
     private void abcorstart_actionperformed() {
    	 
    	 System.out.println("Aberration correction started!"); 
    	 
    	 try {
    		 abcorresults_.start(params_);
    	 } catch (Exception e) {
	         System.out.println(e.getMessage());
	     }	 
     };
     
     private void abcorstop_actionperformed(){
    	 abcorresults_.AOstopflag = true;
    	 try {
    		 for (int jzern = 0; jzern < params_.Nzernikes; jzern ++) {   			 
        		 if (!params_.demomode) {
        			 core_.setProperty("MIRAO52E", params_.Zernikes[jzern], 0);
        			 core_.setProperty("MIRAO52E","ApplyZernikes",1);
        		 }
    		 }
          } catch (Exception e) {
    	         System.out.println(e.getMessage());
    	 }
    	 JOptionPane.showMessageDialog(null,"Aberration correction stopped. Zernike modes reset to zero.", "Warning", 
		            JOptionPane.PLAIN_MESSAGE);
    	 System.out.println("Aberration correction stopped. Zernike modes reset to zero.");
    //	 AberrationCorrection result  = AberrationCorrection.start(params_);
    //	 return result;
     };  
     
     private void abcorsave_actionperformed(java.awt.event.ActionEvent evt) {
    	 if (!abcorresults_.succes) {
				JOptionPane.showMessageDialog(null,"Aberration correction not peformed: nothing to save.", "Error", 
			            JOptionPane.PLAIN_MESSAGE);
				return;
			}
    	 
	      JFileChooser c = new JFileChooser();
	      // Demonstrate "Open" dialog:
	      int rVal = c.showSaveDialog(REALMframe.this);
	      if (rVal == JFileChooser.APPROVE_OPTION) {
	    	  
	    	  filename_.setText(c.getSelectedFile().getName());
	    	  dir_.setText(c.getCurrentDirectory().toString());
	      
	    	  String savestring =  dir_.getText() + "\\" + filename_.getText() + ".txt";
	    	  System.out.println(savestring);
	    	  try {
	    		  abcorresults_.save(savestring, params_);
	    	  } catch (Exception e) {
	 	         System.out.println(e.getMessage());
	 	         System.exit(1);
	 	     }
	      }
	      if (rVal == JFileChooser.CANCEL_OPTION) {
//	        filename_.setText("You pressed cancel");
//	        dir_.setText("");
	      }
	    
    	 System.out.println("Aberration correction saved!");

     };  

     private void applyastig_actionperformed(java.awt.event.ActionEvent evt){
    	 System.out.println("Astigmatisme applied");
    	 try {    		 
    		 if (!params_.demomode) {
        		 double astigcurrent = Double.valueOf(core_.getProperty("MIRAO52E","Z22"));
        		 double astignew = astigcurrent + 0.060;
        		 System.out.println(astigcurrent);
        		 System.out.println(astignew);
    			 REALMframe.core_.setProperty("MIRAO52E", "Z22",astignew);
    			 REALMframe.core_.setProperty("MIRAO52E","ApplyZernikes",1);
    		 }
    	 } catch (Exception e) {
	         System.out.println(e.getMessage());
	         System.exit(1);
	     }	   	 
     };    
    
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
   
  
   class loadwavefront implements ActionListener {
	    public void actionPerformed(ActionEvent e) {
	      JFileChooser c = new JFileChooser();
	      // Demonstrate "Open" dialog:
	      int rVal = c.showOpenDialog(REALMframe.this);
	      if (rVal == JFileChooser.APPROVE_OPTION) {
	    	  
	    	  filename_.setText(c.getSelectedFile().getName());
	    	  dir_.setText(c.getCurrentDirectory().toString());
	      
	    	  String loadstring =  dir_.getText() + "\\" + filename_.getText();
	    	  System.out.println(loadstring);
	    	  
	    	  try { 
	     		 if (!params_.demomode) {
	     			REALMframe.core_.setProperty("MIRAO52E","Load wavefront correction", loadstring);  		 
	     		 } 	    		  	    		  
	    	  } catch (Exception ex) {
	    		  System.out.println(ex.getMessage());
	    		  System.exit(1);
		   	  }	
	      }
	      if (rVal == JFileChooser.CANCEL_OPTION) {
	      }
	    }
	  }

	  class savewavefront implements ActionListener {
	    public void actionPerformed(ActionEvent e) {
	      JFileChooser c = new JFileChooser();
	      // Demonstrate "Save" dialog:
	      int rVal = c.showSaveDialog(REALMframe.this);
	      if (rVal == JFileChooser.APPROVE_OPTION) {
	        filename_.setText(c.getSelectedFile().getName());
	        dir_.setText(c.getCurrentDirectory().toString());
	      
	        String savestring =  dir_.getText() + "\\" + filename_.getText()  + ".wcs";
	        System.out.println(savestring);
	        try { 
	        	if (!params_.demomode) {
	        		REALMframe.core_.setProperty("MIRAO52E","Save current position [input filename]", savestring);
	        	}
   	 		} catch (Exception ex) {
   	 			System.out.println(ex.getMessage());
   	 			System.exit(1);
   	 		}
	      }
	      if (rVal == JFileChooser.CANCEL_OPTION) {

	      }
	    }
	  }
        

    private void onWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_onWindowClosing
//       prefs_.putInt(FRAMEXPOS, (int) getLocation().getX());
//       prefs_.putInt(FRAMEYPOS, (int) getLocation().getY());
//       prefs_.putDouble(SMALLMOVEMENT, smallMovement_);
//       prefs_.putDouble(MEDIUMMOVEMENT, mediumMovement_);
//       prefs_.putDouble(LARGEMOVEMENT, largeMovement_);
    }//GEN-LAST:event_onWindowClosing
 
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



