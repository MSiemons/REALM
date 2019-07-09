import org.micromanager.api.MMPlugin;
import org.micromanager.api.ScriptInterface;

public class REALM implements MMPlugin {

		   public static final String menuName = "REALM";
		   public static final String tooltipDescription =
		      "Adaptive Optics plugin with MIRAO52E.";

		   // Provides access to the Micro-Manager Java API (for GUI control and high-
		   // level functions).
		   private ScriptInterface gui_;
		   private REALMframe myFrame_;
		   // Provides access to the Micro-Manager Core API (for direct hardware
		   // control)
		  // public static CMMCore core_ = new CMMCore();

		   @Override
		   public void setApp(ScriptInterface app) {
			      gui_ = app;       
			      if (myFrame_ == null) {
			         myFrame_ = new REALMframe(gui_);
			         myFrame_.setBackground(gui_.getBackgroundColor());
			         gui_.addMMBackgroundListener(myFrame_);
			         gui_.addMMListener(myFrame_);
			      }
			      
			      myFrame_.initialize();
			      myFrame_.setSize(320, 340);
			      myFrame_.setResizable(false);
			      myFrame_.setVisible(true);
			   }


		   @Override
		   public void dispose() {
		      // We do nothing here as the only object we create, our dialog, should
		      // be dismissed by the user.
		   }

		   @Override
		   public void show() {
			   
		   }
		   
		   @Override
		   public String getInfo () {
		      return "Adaptive Optics plugin for Single molecule Localization Microscopy.";
		   }

		   @Override
		   public String getDescription() {
		      return tooltipDescription;
		   }
		   
		   @Override
		   public String getVersion() {
		      return "1.0";
		   }
		   
		   @Override
		   public String getCopyright() {
		      return "Utrecht University, 2019";
		   }      		   

}

