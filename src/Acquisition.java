import java.util.Arrays;

import ij.ImagePlus;
import ij.measure.Measurements;
import ij.process.FHT;
import ij.process.FloatProcessor;
import ij.process.ImageStatistics;

public class Acquisition {
	
	private FloatProcessor ip;
	private float[][] image;
	private FloatProcessor paddedimage;
	private FloatProcessor fftlogip;
	private FHT fht;
	private float[] fftpxl;
	private float[][] fftabs;
	private float[][] fftabslog;
	
	private float[][] fx;
	private float[][] fy;
	private float[][] fr;
	private float[][] OTF;
	public int n;
	private int[][] circmask1NA;
	private int[][] circmask2NA;
	private float[][] metricweight;
	private boolean transformed = false;
	
	public void getfreqsup(Params params) {
		  
		this.n = next2power(Math.max(params.width, params.height));
		this.fx = new float[n][n];
		this.fy = new float[n][n];		   
		this.fr = new float[n][n];
		this.circmask1NA = new int[n][n];
		this.circmask2NA = new int[n][n];
		this.OTF = new float[n][n];			   
		this.metricweight = new float[n][n];
		float maxweight = 0;		   
		for (int i = 0; i < this.n; i++) {
			for (int j = 0; j < this.n; j++) {
				float fx = ((float) (i - this.n /2) / this.n ) / params.pixelsize;
				float fy = ((float) (j - this.n /2) / this.n ) / params.pixelsize;
				float fr = (float) Math.abs(Math.sqrt(fy * fy + fx * fx));
				   
				this.fx[i][j] = fx;
				this.fy[i][j] = fy;
				this.fr[i][j] = fr;
				   
				if (fr <= params.diflim/2) { 
					this.circmask1NA[i][j] = 1;
				}
				   
				if (fr <= params.diflim) { 
					this.circmask2NA[i][j] = 1;
					this.OTF[i][j] = (float) ((2 / Math.PI)* ( Math.acos( fr / params.diflim )  -  fr / params.diflim  * ( Math.sqrt(1 - fr * fr / params.diflim / params.diflim ) ) ));					   			   
				}
				   
				this.metricweight[i][j] = (float) Math.pow( (1 - this.OTF[i][j]), params.alpha) * this.OTF[i][j] * this.circmask1NA[i][j];	  
				if (this.metricweight[i][j] > maxweight)
					maxweight = this.metricweight[i][j];
			   }
		   }
		   
		// Normalize metric weight
		for (int i = 0; i < this.n; i++) {
			for (int j = 0; j < this.n; j++) {
				this.metricweight[i][j] =  this.metricweight[i][j] / maxweight;
			}
		}
	}
	
	public void setimage(float[][] image) {
		
		this.image = image;
		this.ip = new FloatProcessor(image);
		this.transformed = false;		

	}
		
	public float[][] getimage() {
		
		return this.image ;
		
	}
	
	public FloatProcessor getip() {
		
		return this.ip;
		
	}	
	
	public FloatProcessor getfftlogip() {
		if (!this.transformed)
			this.transform();
		
		return this.fftlogip;
		
	}	
	
	public void transform() {
		if (this.transformed)
			return;
		
		int n = this.n;
		this.pad();
		
		this.fftabs = new float[n][n];
		this.fftabslog = new float[n][n];
		
		this.fht = new FHT(this.paddedimage);
		this.fht.setShowProgress(false);
		this.fht.transform();
		this.fftpxl = (float[]) this.fht.getPixels();
		
		for (int i = 0; i < n; i++) {
		      	int base = i*n;
		        int l;
		        for (int c=0; c<n; c++) {
		            l = ((n-i)%n) *n + (n-c)%n;
		            this.fftabs[i][c] = (float) Math.sqrt(((sqr(this.fftpxl[base+c]) + sqr(this.fftpxl[l]) ))/ n);
		            this.fftabslog[i][c] = (float) Math.log((sqr(this.fftpxl[base+c]) + sqr(this.fftpxl[l]) ) / n);
		        }
		}

		this.fftabs = swapquadrants(this.fftabs);
		this.fftabslog = swapquadrants(this.fftabslog);		
		this.fftlogip = new FloatProcessor(this.fftabslog);
		
	}
	
	public float getMetric() {
		if (!this.transformed)
			this.transform();
		
		float Mnum = 0;
		float Mden = 0;
		float M = 0;
		
		for (int i = 0; i < this.n; i++) {
			for (int j = 0; j < this.n; j++) {
				Mnum += this.fftabs[i][j] * this.metricweight[i][j];	
				Mden += this.fftabs[i][j] * this.circmask1NA[i][j];
			}
		}			
		M = Mnum / Mden;		
		return M;
		
	}
	
	private static int next2power(int in) {
		int n = 2;
		while (n < in) {
			n *=  2;
		}
		return n;
	}	
	
	private void  pad() {
		
		int width = this.ip.getWidth();
		int height = this.ip.getHeight();
		int n = next2power(Math.max(width,height));
		float modeval = getMode(this.ip);
		
		float[][] paddedimage = new float[n][n];
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i < width && j < height) {
					paddedimage[i][j] = this.image[i][j];
				} else {
					paddedimage[i][j] = modeval;
				}
			}
		}
		
		this.paddedimage = new FloatProcessor(paddedimage);

	}
	
	/** square float */
	private static float sqr(float in){
			float out = in * in;
			return out;
	}

	private static float[][] swapquadrants (float[][] fftin){
		int n = fftin.length;
		int quadsize = n / 2;
		int newi;
		int newj;
		float[][] fftout = new float[n][n];
		for (int i = 0; i <n; i++) {
			for (int j = 0; j < n; j++) {
				if (i < quadsize && j < quadsize) {
					newi = i + quadsize;
					newj = j + quadsize;
				} else if (i >= quadsize && j < quadsize) {
					newi = i - quadsize;
					newj = j + quadsize;						
				} else if (i < quadsize && j >= quadsize) {
					newi = i + quadsize;
					newj = j - quadsize;
				} else {
					newi = i - quadsize;
					newj = j - quadsize;	
				}
				fftout[newi][newj] = fftin[i][j];
			}
		}
		return fftout;
	}
	
	private float getMode(FloatProcessor ip) {
		float mode;
		int Nbin = getBinOptimalNumber(ip);
		
		ip.setHistogramSize(Nbin);
		ImageStatistics imgstat = ImageStatistics.getStatistics(ip, Measurements.MODE, null);

        mode = (float) Math.round(imgstat.dmode);
		
		return mode;
	}
	
    private int getBinOptimalNumber(FloatProcessor ip)
    {
		int width = ip.getWidth();
		int height = ip.getHeight();		
		int pixelCount = width*height;

        float[] pixels2 = new float[pixelCount];
        for (int i = 0; i <width; i++) {
			for (int j = 0; j < height; j++) {
				int ind = i * height + j;
				pixels2[ind] = this.image[i][j];			
			}
        }

        Arrays.sort(pixels2);

        int qi25 = Math.round(pixelCount*0.25f);
        int qi75 = Math.round(pixelCount*0.75f);

        float IQR = pixels2[qi75]-pixels2[qi25];
        double h= 2*IQR*Math.pow((double)pixelCount, -1.0/3.0);

        return (int)Math.round((pixels2[pixelCount-1]-pixels2[0])/h);

    }
    
	public static void showMetric(Params params) {
		
		params.update();
		
		Acquisition acqui = new Acquisition();
		acqui.getfreqsup(params);
		FloatProcessor metricnum = new FloatProcessor(acqui.metricweight);
		FloatProcessor metricden = new FloatProcessor(acqui.circmask1NA); 
		   
		ImagePlus metricnumip = new ImagePlus("Metric numerator", metricnum);
		ImagePlus metricdenip = new ImagePlus("Metric denumerator", metricden);
		   
		metricnumip.show();
		metricdenip.show();
		
		return;
	}

}



