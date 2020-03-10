///////////////////////////////////////////////////////////////////////////////
//FILE:          AberrationCorrection.java
//PROJECT:       REALM
//-----------------------------------------------------------------------------
//
// DISCRIPTON:	 This class contains the adaptive optics algorithm.
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


import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Plot;
import java.util.Date; 
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.JOptionPane; 

public class AberrationCorrection {

	public double[][][] Mstore;
	public double[] M;
	public double[] Aest;
	public double[][] AestStore;
	public double[] Aerr;
	public double[][] AerrStore;
	public double EstWrmserr;
	public double EstWrms;
	public boolean succes;
	public Params paramstore;
	volatile boolean AOstopflag;		
	private Acquisition acqui;

	public void start(Params params) throws Exception {

		// update parameters		
		params.update();
		if (params.demomode) {
			params.height = 400;
			params.width = 400;
		}

		int Nrounds = params.Nrounds;
		int Nbiases = params.Nbiases;
		int Nzernikes = params.Nzernikes;
		double[] biases = params.biases;	

		this.Mstore = new double[Nrounds][Nzernikes][Nbiases];
		this.M = new double[Nbiases];
		this.Aest = new double[Nzernikes];
		this.AestStore = new double[Nrounds][Nzernikes];
		this.Aerr = new double[Nzernikes];
		this.AerrStore = new double[Nrounds][Nzernikes];		
		this.acqui = new Acquisition();
		this.acqui.getfreqsup(params); 
		this.succes = false;

		float[][] imgtmp2D = new float[params.width][params.height]; 
		short[] imgtmp1D = new short[params.width*params.height]; 

		ImagePlus Acquisitions = new ImagePlus();
		ImageStack imstack = new ImageStack(params.width,params.height);
		ImageStack fftlogstack = new ImageStack(this.acqui.n,this.acqui.n);

		// Check if all Zernike modes are zero
		boolean startok = true;
		if (!params.demomode) {
			for (int jzern = 0; jzern < Nzernikes; jzern ++) {			
				startok = startok && !(Math.abs( Double.valueOf(REALMframe.core_.getProperty(params.DMname, params.Zernikes[jzern])) ) > 0.0001);	
			}
		}
		if (!startok) {
			JOptionPane.showMessageDialog(null,"(Some) Zernike coefficients are non-zero.\nPlease set all coefficients to zero or load a (new) wavefront.", "Warning", 
					JOptionPane.PLAIN_MESSAGE);
			REALMframe.abcorstop_.setEnabled(false);
			REALMframe.abcorstart_.setEnabled(true);
			return;
		}

		///////////////// Start correction algorithm /////////////////////

		for (int jround = 0; jround < Nrounds; jround++) {
			for (int jzern = 0; jzern < Nzernikes; jzern ++) {
				for (int jbias = 0; jbias < Nbiases; jbias++) {

					// Check if stop button is pressed	
					if (this.AOstopflag) {

						this.succes = false;
						REALMframe.abcorstop_.setEnabled(false);
						REALMframe.abcorstart_.setEnabled(true);

						return;

					} else {

						// Apply bias
						if (!params.demomode) {
							REALMframe.core_.setProperty(params.DMname, params.Zernikes[jzern], (this.Aest[jzern] + biases[jbias]) / 1000);
							REALMframe.core_.setProperty(params.DMname,"ApplyZernikes",1);
						}	

					}

					if (!params.demomode) {
						// get image
						REALMframe.core_.snapImage();
						if (REALMframe.core_.getBytesPerPixel() == 1) {
							// 8-bit grayscale pixels
							byte[] imgtmp8 = (byte[])REALMframe.core_.getImage();
							for (int index = 0; index < imgtmp8.length; index ++)
								imgtmp1D[index] = (short) imgtmp8[index];

						} else if (REALMframe.core_.getBytesPerPixel() == 2){
							// 16-bit grayscale pixels
							imgtmp1D = (short[]) REALMframe.core_.getImage();  					            
						} else {
							System.out.println("Dont' know how to handle images with " +
									REALMframe.core_.getBytesPerPixel() + " byte pixels.");             
						}

						for (int i = 0; i < params.width; i++) {
							for (int j = 0; j < params.height; j++) {											
								int ind = i + j*params.width;
								imgtmp2D[i][j] = (float) imgtmp1D[ind];				
							}
						}								

					} else {
						// In DEMO mode: load dummy images
						String filename = null;

						if (params.Nbiases == 5) {
							int[] imind = {3, 5, 7, 9, 11};
							filename = "SMimage_Z" + (jzern+1) + "_" + imind[jbias] + ".tif";
						}
						if (params.Nbiases == 7) {
							int[] imind = {1, 3, 5, 7, 9, 11, 13};
							filename = "SMimage_Z" + (jzern+1) + "_" + imind[jbias] + ".tif";
						}
						if (params.Nbiases == 9) {
							int[] imind = {1, 3, 5, 6, 7, 8, 9, 11, 13};
							filename = "SMimage_Z" + (jzern+1) + "_" + imind[jbias] + ".tif";
						}
						if (params.Nbiases == 11) {
							int[] imind = {1,3,4,5,6,7,8,9,10,11,13};
							filename = "SMimage_Z" + (jzern+1) + "_" + imind[jbias] + ".tif";
						}
						if (params.Nbiases == 13) {
							int[] imind = {1,2,3,4,5,6,7,8,9,10,11,12,13};
							filename = "SMimage_Z" + (jzern+1) + "_" + imind[jbias] + ".tif";
						}	

						String dir = "REALM\\DemoAcquisitions\\";				

						ImagePlus dumim = new ImagePlus(dir + filename);
						ImageProcessor ip = dumim.getProcessor();
						imgtmp2D = ip.getFloatArray();					

					}
					
					this.acqui.setimage(imgtmp2D);
					imstack.addSlice(this.acqui.getip());
					Acquisitions.setStack("Acquisitions",imstack);
					Acquisitions.setSlice(imstack.getSize());

					// Show acquisitions
					Acquisitions.show();

					// pad with mode value of image and do FFT
					this.acqui.transform();

					if (params.show) {
						fftlogstack.addSlice(this.acqui.getfftlogip());	
					}

					// compute metric
					this.M[jbias] = this.acqui.getMetric();
					this.Mstore[jround][jzern][jbias] = this.M[jbias];

				}

				// Fit metric values
				// get initial parameter estimations
				int maxind = getMaxInd(this.M);
				int minind = getMinInd(this.M);
				double[] initialParams = {this.M[minind], this.M[maxind], 0.5*biases[maxind], 0.75 / (2 * Math.PI) * params.wavelength};

				// upper and lower boundaries of fit parameters
				double[] lowerbound = {0, 0.9 * getMax(this.M), - 0.5 / (2 * Math.PI) * params.wavelength, .5 / (2 * Math.PI) * params.wavelength};
				double[] upperbound = {getMin(this.M), 1.2 * getMax(this.M), 0.5 / (2 * Math.PI) * params.wavelength, 1 / (2 * Math.PI) * params.wavelength};	

				// Peform fit and compute standard error of fit parameters
				double[] theta = LMfitter.fitGauss(params.biases, this.M, initialParams, lowerbound, upperbound);
				double[] thetase = LMfitter.getparamSE(params.biases, this.M, theta);

				// Store results
				this.Aest[jzern] = theta[2] + this.Aest[jzern];
				this.AestStore[jround][jzern] =  theta[2];
				this.Aerr[jzern] = thetase[2];
				this.AerrStore[jround][jzern] = thetase[2];
				
				// Applycorrection
				if (!params.demomode) {
					REALMframe.core_.setProperty(params.DMname, params.Zernikes[jzern], this.Aest[jzern] / 1000);
					REALMframe.core_.setProperty(params.DMname,"ApplyZernikes",1);
				}

				// Show metric values and fit
				if (params.show) {
					Plot plot = new Plot(("Round "+ (jround + 1) + ", Zernike mode " + params.Zernikes[jzern]),"Bias [nm]","Metric value [au]");

					double[] x = params.linspace(-1.1*params.maxbiasnm, 1.1*params.maxbiasnm, 100);
					double[] f = LMfitter.getGauss(x, theta);

					plot.setLimits(-1.3 * params.maxbiasnm, 1.3 * params.maxbiasnm, 1.3 * getMin(this.M) - 0.3 * getMax(this.M) , 1.3 * getMax(this.M) - 0.3 * getMin(this.M));
					plot.addPoints(params.biases, this.M, 0);
					plot.addPoints(x, f, 2);
					plot.addLabel(0, 0, ("Estimated coefficient  = " + String.format("%.2f",this.AestStore[jround][jzern] ) + " nm"));
					plot.addLabel(0.5, 0, ("standard error = " + String.format("%.2f",this.AerrStore[jround][jzern]) + " nm"));
					plot.show();
				}	
			}						
		}	

		// Aberration correction finished. Store and show results	
		this.paramstore = params;
		this.EstWrms = getWrms(this.Aest);
		this.EstWrmserr = getWrmserr(this.Aest,this.Aerr);
		
		// Enable start button and disable stop button
		this.succes  = true;
		REALMframe.abcorstop_.setEnabled(false);
		REALMframe.abcorstart_.setEnabled(true);
		
		// Show Fourier transforms
		if (params.show) {
			ImagePlus FTlogAcquis = new ImagePlus("FFTacquisitions",fftlogstack);
			FTlogAcquis.show();		
		}

		// Make plot
		Plot plotzern = new Plot("ZernikeCoefficients","Zernike mode","A [rad]");

		plotzern.setLimits(-.5, (double) params.Nzernikes - .5, -1, 1);
		double[] xzern = params.linspace(0, params.Nzernikes - 1, params.Nzernikes);
		double[] Azern = new double[params.Nzernikes];
		double[] Azernerr = new double[params.Nzernikes];
		double ylim = 0;
		for (int i = 0; i < params.Nzernikes; i++) {
			Azern[i] = this.Aest[i] / params.wavelength * 2 * Math.PI;
			Azernerr[i] = this.Aerr[i] / params.wavelength * 2 * Math.PI;
			ylim = Math.max(ylim, Math.abs(Azern[i]) + Math.abs(Azernerr[i]));
		}
		plotzern.setLimits(-.5, (double) params.Nzernikes - .5, - 1.2 * ylim, 1.2*ylim);	
		plotzern.addPoints(xzern,Azern,Azernerr, 0);
		plotzern.addLabel(0, 0, ("Corrected wavefront  = " + String.format("%.2f",this.EstWrms / params.wavelength * 2 * Math.PI) + " rad"));
		plotzern.addLabel(0.5, 0, ("Estimated correction error = " + String.format("%.2f",this.EstWrmserr / params.wavelength * 2 * Math.PI) + " rad"));
		plotzern.show();

		// compute aberration image
		ZernikeModes zernikemodes = new ZernikeModes();
		zernikemodes.getbase();

		float[][] W = zernikemodes.getaberration(params.zernindn, params.zernindm, Azern) ;
		FloatProcessor Wip = new FloatProcessor(512,512);
		Wip.setFloatArray(W);
		ImagePlus Wipl = new ImagePlus(("Aberration, Wrms = "+ String.format("%.2f",this.EstWrms / params.wavelength * 2 * Math.PI) + " rad"),Wip);		
		Wipl.show();	

	}

	// Save data 
	public void save(String file_path) throws Exception  {

		FileWriter write = new FileWriter( file_path , false);
		PrintWriter print_line = new PrintWriter(write);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();

		print_line.printf( "%s" + "%n" ,"REALM aberration correction");
		print_line.printf( "%s" + "%n" ,"Date: " + dateFormat.format(date));
		print_line.printf("%s" + "%n", "" );
		print_line.printf("%s" + "%n", "Settings");
		print_line.printf( "%s" + "%n" ,"NA: " + Float.toString(this.paramstore.NA));
		print_line.printf( "%s" + "%n" ,"Wavelength [nm]: " + Float.toString(this.paramstore.wavelength));
		print_line.printf( "%s" + "%n" ,"Pixelsize[nm]: " + Float.toString(this.paramstore.pixelsize));
		print_line.printf( "%s" + "%n" ,"Correction rounds: " + Integer.toString(this.paramstore.Nrounds));
		print_line.printf( "%s" ,"Zernike modes: ");
		for (int i = 0; i < this.paramstore.Zernikes.length; i ++) 
			print_line.printf("%s, ",this.paramstore.Zernikes[i]);
		print_line.printf( "%n");	
		print_line.printf( "%s" ,"Applied biases [nm]: ");		
		for (int i = 0; i < this.paramstore.biases.length; i ++) 
			print_line.printf("%s, ",Double.toString(this.paramstore.biases[i]));
		print_line.printf( "%n");
		print_line.printf( "%n");
		print_line.printf("%s" + "%n", "Aberration correction results");
		print_line.printf( "%s" + "%n" ,"Wrms [rad]: " + Double.toString(this.EstWrms / this.paramstore.wavelength * 2 * Math.PI));
		print_line.printf( "%s" + "%n" ,"Wrms standard error [rad]: " + Double.toString(this.EstWrmserr / this.paramstore.wavelength * 2 * Math.PI));
		print_line.printf( "%s" + "%n","Zernike coefficients [rad]");
		for (int i = 0; i < this.AestStore.length; i ++) {
			print_line.printf( "%s" ,"Round " + (i +1) + ": ");
			for (int j = 0; j < this.AestStore[0].length; j ++) 
				print_line.printf("%s, ",this.AestStore[i][j] / this.paramstore.wavelength * 2 * Math.PI);
			print_line.printf( "%n");
		}
		print_line.printf( "%s" ,"Final: ");
		for (int j = 0; j < this.Aest.length; j ++) 
			print_line.printf("%s, ",this.Aest[j] / this.paramstore.wavelength * 2 * Math.PI);
		print_line.printf( "%n");
		print_line.printf( "%n");
		print_line.printf( "%s" + "%n","Standard Error of Zernike coefficients [rad]");
		for (int i = 0; i < this.AerrStore.length; i ++) {
			print_line.printf( "%s" ,"Round " + (i +1) + ": ");
			for (int j = 0; j < this.AerrStore[0].length; j ++) 
				print_line.printf("%s, ",this.AerrStore[i][j] / this.paramstore.wavelength * 2 * Math.PI);
			print_line.printf( "%n");
		}
		print_line.printf( "%n");
		print_line.printf( "%s" + "%n","Metric values");
		for (int i = 0; i < this.Mstore[0].length; i ++) {
			print_line.printf( "%s" + "%n" ,"Zernike mode " + this.paramstore.Zernikes[i]);
			for (int j = 0; j < this.Mstore.length; j ++) {
				print_line.printf( "%s" ,"Round " + (j +1) + ": ");
				for (int k = 0; k < this.Mstore[0][0].length; k ++) 
					print_line.printf("%s, ",this.Mstore[j][i][k]);
				print_line.printf( "%n");
			}
		}			

		print_line.close();

	}		

	private double getWrms(double[] Aest) {
		double Wrms2 = 0;
		for (int jzern= 0; jzern< Aest.length; jzern++) {
			Wrms2 += Aest[jzern] * Aest[jzern];
		}
		return Math.sqrt(Wrms2);		
	}

	private double getWrmserr(double[] Aest, double[] Aerr) {
		double Wrms = getWrms(Aest);
		double Wrmserr2 = 0;
		for (int jzern= 0; jzern < Aerr.length; jzern++) {
			if (!Double.isNaN(Aerr[jzern]))
				Wrmserr2 += Aerr[jzern] * Aerr[jzern] * Aest[jzern] * Aest[jzern] / (Wrms * Wrms);
		}
		return Math.sqrt(Wrmserr2);		
	}

	private static double getMin(double[] array) {
		double min = array[0];
		for(int i = 0; i < array.length; i++) {
			if(min > array[i]) {
				min = array[i];
			}
		}
		return min;
	}

	private static double getMax(double[] array) {
		double max = array[0];
		for(int i = 0; i < array.length; i++) {
			if(max < array[i]) {
				max = array[i];
			}
		}
		return max;
	}

	private static int getMinInd(double[] array) {
		double min = array[0];
		int index = 0;
		for(int i = 0; i < array.length; i++) {
			if(min > array[i]) {
				min = array[i];
				index = i;
			}
		}
		return index;
	}

	private static int getMaxInd(double[] array) {
		double max = array[0];
		int index = 0;
		for(int i = 0; i < array.length; i++) {
			if(max < array[i]) {
				max = array[i];
				index = i;
			}
		}
		return index;
	}

}
