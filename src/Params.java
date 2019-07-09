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
