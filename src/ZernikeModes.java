public class ZernikeModes {
	int size = 512;
	double[][] rho = new double[size][size];
	double[][] theta = new double[size][size];
	
	
	public double[][] getzernike(int n, int m){
		if (Math.abs(m) > n)
			throw new IllegalStateException("Zernike mode does not exists");
		if (Math.abs(m) % 2 != n % 2) 
			throw new IllegalStateException("Zernike mode does not exists");
				
		double[][] zernike = new double[size][size];
		double num;
		double den;
		int mabs = Math.abs(m);
		
		for (int k = 0; k <= (n - mabs) / 2; k ++) {
			if ( k % 2 > 0) {
				num = -1 *  factorial(n - k);
			} else {
				num = factorial(n - k);	
			}
			
			den = factorial(k) * factorial((n + mabs) /2 - k) * factorial((n - mabs) /2 - k);
			
			double normfac = 2 * (n + 1);
			if (mabs == 0)
					normfac = normfac / 2;
			normfac = Math.sqrt(normfac);

			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					if (Math.abs(rho[i][j]) <= 1)
						if ( m >= 0) {
							zernike[i][j] += normfac * num / den * Math.pow(rho[i][j], n - 2 * k ) * Math.cos(mabs * theta[i][j]) ;
						} else {
							zernike[i][j] += normfac * num / den * Math.pow(rho[i][j], n - 2 * k ) * Math.sin(mabs * theta[i][j]) ;
						}
				}
			}	
		}
		
		return zernike;
	}
	
	public float[][] getaberration(int[] n, int[] m, double[] coef){
		if ((n.length != m.length) && (n.length != coef.length))
			throw new IllegalStateException("Orders and coefficients are not equal in length");
			
		float[][] W = new float[size][size]; 
		int nzern = n.length;
		
		for(int k = 0; k < nzern; k++) {
			double[][] zernike = this.getzernike(n[k], m[k]);
			
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					W[i][j] += (float) coef[k] * zernike[i][j];
				}
			}	
			
		}
		
		return W;
	}
	
	public void getbase(){
		
		double[] rhox = linspace(-1,1,size);
		double[] rhoy = linspace(-1,1,size);
		
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				this.rho[i][j] = Math.sqrt(rhox[i] * rhox[i] + rhoy[j] * rhoy[j]);		
				this.theta[i][j] = Math.atan2(rhoy[j], rhox[i]);
			}		
		}
		
		return;
	}
	
	private static double[] linspace(double xmin, double xmax, int n) {
		double dx = (xmax - xmin) / (n - 1);
		double[] array = new double[n];
		   
		for (int i = 0; i < n; i++) {
			array[i] = xmin + dx * i;
		}
		   
		return array;
	}
	
	private static int factorial (int varin) {
		if (varin == 0) {
			return 1;
		}
			
		int varout = varin;
		int next = varin;
		
		for(int i = 0; i < varin - 1; i ++) {
			next = next - 1;
			varout = varout * next;						
		}		
		
		return varout;
	}

}
