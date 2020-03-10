///////////////////////////////////////////////////////////////////////////////
//FILE:          Params.java
//PROJECT:       REALM
//-----------------------------------------------------------------------------
//
// DISCRIPTON:	 This class manages all parameters.
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

public class Params {

	public float NA;
	public float wavelength;
	public float pixelsize;
	public String[] Zernikes;
	public int[] zernindn;
	public int[] zernindm;
	public int Zernlist;
	public int Nrounds;
	public int Nbiases;
	public int Nzernikes;
	public double maxbiasrad;
	public double maxbiasnm;
	public double[] biases;
	public int width;
	public int height;
	public float diflim;
	public float alpha;
	public boolean show;
	public boolean demomode;
	public String DMname;

	public void update() {

		this.diflim = 2*this.NA/this.wavelength;
		this.maxbiasnm = (float) (this.maxbiasrad / (2 * Math.PI) * this.wavelength);
		this.biases = linspace(-this.maxbiasnm, this.maxbiasnm, this.Nbiases);

		this.pixelsize = 65;//(float) (AOSMLMframe.core_.getPixelSizeUm() * 1000);

		if (Zernlist == 0){
			this.Zernikes = REALMframe.ZERNLIST1;
			this.zernindn = REALMframe.zernindn1;
			this.zernindm = REALMframe.zernindm1;
		} else if(Zernlist == 1) {
			this.Zernikes = REALMframe.ZERNLIST2;
			this.zernindn = REALMframe.zernindn2;
			this.zernindm = REALMframe.zernindm2;
		} else if(Zernlist == 2) {
			this.Zernikes = REALMframe.ZERNLIST3;
			this.zernindn = REALMframe.zernindn3;
			this.zernindm = REALMframe.zernindm3;
		} else if(Zernlist == 3) {
			this.Zernikes = REALMframe.ZERNLIST4;
			this.zernindn = REALMframe.zernindn4;
			this.zernindm = REALMframe.zernindm4;
		} else if(Zernlist == 4) {
			this.Zernikes = REALMframe.ZERNLIST5;
			this.zernindn = REALMframe.zernindn5;
			this.zernindm = REALMframe.zernindm5;
		}
		this.Nzernikes = this.Zernikes.length;

		long height = REALMframe.core_.getImageHeight();
		this.height = Long.valueOf(height).intValue();	

		long width = REALMframe.core_.getImageWidth();
		this.width = Long.valueOf(width).intValue();

	}

	public double[] linspace(double xmin, double xmax, int n) {
		double dx = (xmax - xmin) / (n - 1);
		double[] array = new double[n];

		for (int i = 0; i < n; i++) {
			array[i] = xmin + dx * i;
		}

		return array;
	}



}
