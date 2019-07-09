public class Fitter {
	
	public static double[] fitGauss(double[] xvalue, double[] yvalue, double[] thetainit, double[] lowerbound, double[] upperbound) {
		
		int iiter = 0;
		int Nitermax = 100;
		double tollim = 0.0001;
		double monitor = 1;
		double hescoef = 1;
		double lambda = 1;
		double lambdafac = 1.2;
		double[] theta = thetainit;
		double[] residuals = getresiduals(xvalue, yvalue,theta);
		double chi2 = getChi2(residuals);
		double[][] hessian = getHessian(xvalue, residuals, theta);
		double[][] jacobian = getJacobian(xvalue,theta);
		double[] gradient = getGradient(residuals, jacobian);
		double chi2prev = chi2;
		double[] thetatry = new double[theta.length];		
		
		while ((iiter <=Nitermax) && (monitor > tollim)) {
			// Levenberg - Marquardt
			thetatry = thetaupdate(theta, lowerbound, upperbound, gradient, hessian, hescoef, lambda);
			residuals = getresiduals(xvalue, yvalue,thetatry);
			
			double chi2try = getChi2(residuals);
			double dchi2 = chi2try - chi2;
			
			if (dchi2 > 0)
				lambda = lambdafac * lambda;
			else {
				lambda = lambda / lambdafac;
				theta = thetatry;
				chi2 = chi2try;
				jacobian = getJacobian(xvalue,theta);
				gradient = getGradient(residuals, jacobian);
				dchi2 = chi2 - chi2prev;		
				monitor = Math.abs(dchi2/chi2);
				chi2prev = chi2;
			}
			
			iiter ++;
	        	
		}
		System.out.println(iiter);
		return theta;
	}
		
	public static double[] getparamSE(double[] x, double[] residuals, double[] theta) {
		
		double[] se = new double[theta.length];
		double[][] Hessian = getHessian(x, residuals, theta);
		double[] Covardiag = diag(inverse(Hessian));
		double Chi2 = getChi2(residuals);
		double numparams = theta.length;
		double numx = x.length;
		
		for (int i = 0; i < numparams; i ++) {
			double var = Chi2 / (numx - numparams) * Covardiag[i];
			se[i] = Math.sqrt(Math.abs(var));			
		}
		
		return se;
	}
	
	public static double[] thetaupdate(double[] theta, double[] lowerbound, double[] upperbound, double[] gradient, double[][] hessian, double hescoef, double alambda) {
		double[][] hessiandiag = new double[theta.length][theta.length];
		double[] thetatry = new double[theta.length];
		double[] dtheta = new double[theta.length];
		double[][] Bmat = new double[theta.length][theta.length];
		
		for (int i = 0; i < theta.length; i ++)
			hessiandiag[i][i] = hessian[i][i];
		
		for (int i = 0; i < theta.length; i ++) {
			for (int j = 0; j < theta.length; j ++) {
				Bmat[i][j] = hescoef * hessian[i][j] + alambda * hessiandiag[i][j];
			}	
		}
		
		dtheta = multiply(inverse(Bmat), gradient);
		
		for (int i = 0; i < theta.length; i ++) 
			if ((theta[i] - dtheta[i] > upperbound[i])) {
				thetatry[i] = upperbound[i];
			} else if ((theta[i] - dtheta[i]) < lowerbound[i]){
				thetatry[i] = lowerbound[i];
			} else {
				thetatry[i] = theta[i] - dtheta[i];
			}
		
		return thetatry;
	}
	
	
	public static double[] getresiduals(double[] xvalue, double[] yvalue, double[] theta) {
		double[] residuals = new double[xvalue.length];
		double[] ymodel = new double[xvalue.length];
//		Gauss modeled as G = a + (b - a) * exp( - (x - c)^2 / (2 * d^2))
		
		for (int i = 0; i < xvalue.length; i ++) {
			ymodel[i] = theta[0] + (theta[1] - theta[0]) * Math.exp(- (xvalue[i] - theta[2]) * (xvalue[i] - theta[2]) / (2 * theta[3] * theta[3]));
			residuals[i] = yvalue[i] - ymodel[i];
		}
		return residuals;
	}
	
	public static double[] getGauss(double[] xvalue, double[] theta) {
		double[] ymodel = new double[xvalue.length];
//		Gauss modeled as G = a + (b - a) * exp( - (x - c)^2 / (2 * d^2))
		for (int i = 0; i < xvalue.length; i ++) {
			ymodel[i] = theta[0] + (theta[1] - theta[0]) * Math.exp(- (xvalue[i] - theta[2]) * (xvalue[i] - theta[2]) / (2 * theta[3] * theta[3]));
		}
		return ymodel;
	}
	
	public static double getGauss(double xvalue, double[] theta) {
//		Gauss modeled as G = a + (b - a) * exp( - (x - c)^2 / (2 * d^2))
		
		double	ymodel = theta[0] + (theta[1] - theta[0]) * Math.exp(- (xvalue - theta[2]) * (xvalue - theta[2]) / (2 * theta[3] * theta[3]));
		
		return ymodel;
	}
	
	public static double getChi2(double[] residuals) {
		double Chi2 = 0;
		for (int i = 0; i < residuals.length; i ++)
			Chi2 += residuals[i] * residuals[i];
		return Chi2;
	}
	
	public static double[][] getHessian(double[] x, double[] residuals,  double[] theta){
		double[][] hessian = new double[theta.length][theta.length];
		double[][] jac = getJacobian(x, theta);
		double[][] jac2 = multiply(transpose(jac), jac);
		
		for (int k = 0; k < theta.length; k ++) {
			for (int l = 0; l < theta.length; l ++) {
				
				double hes2 = 0;
				for(int i =0 ; i < x.length; i++) {
					double[][] derder = getGausssecondderivatives(x[i], theta);
					hes2 += residuals[i] * derder[k][l];
				}
				
				hessian[k][l] = jac2[k][l] + hes2;
			}
		}
		
		return hessian;
	}
	
	public static double[] getGaussderivatives(double x, double[] theta) {
		// Gauss modelled as G = a + (b - a) * exp( - (x - c)^2 / (2 * d^2))
		double[] der = new double[4];
		double exp = Math.exp( - Math.pow(x - theta[2],2) / (2 * Math.pow(theta[3],2)) );
		der[0] =  1 - exp ;
		der[1] =  exp;
		der[2] = (theta[1] - theta[0]) * (x - theta[2]) / Math.pow(theta[3],2) * exp;
		der[3] = (theta[1] - theta[0]) * Math.pow(x - theta[2],2) / Math.pow(theta[3],3) * exp;
		return der;
	}
	
	public static double[][] getGausssecondderivatives(double x, double[] theta) {
		// Gauss modelled as G = a + (b-a) exp( - (x - c)^2 / (2 * d^2))
		double[][] derder = new double[4][4];
		double exp = Math.exp( - Math.pow(theta[3],2) / (2 * Math.pow(theta[3],2)));

		derder[0][0] = 0;
		derder[1][1] = 0;
		derder[2][2] = (theta[1] - theta[0]) * exp * ( Math.pow(x - theta[2],2) / Math.pow(theta[3],4) - 1 / Math.pow(theta[3],2) ); 
		derder[3][3] = (theta[1] - theta[0]) * exp *  Math.pow(x - theta[2],2) * ( Math.pow(x - theta[2],2) / Math.pow(theta[3],6) - 3 * 1 / Math.pow(theta[3],4) );
		
		derder[0][1] = 0;
		derder[1][0] = 0;
		
		derder[0][2] = - (x - theta[2]) / Math.pow(theta[3],2) * exp;
		derder[2][0] = - (x - theta[2]) / Math.pow(theta[3],2) * exp;
		
		derder[0][3] = - Math.pow(x - theta[2],2) / Math.pow(theta[3],3) * exp;
		derder[3][0] = - Math.pow(x - theta[2],2) / Math.pow(theta[3],3) * exp;		
		
		derder[1][2] = (x - theta[2]) / Math.pow(theta[3],2) * exp;
		derder[2][1] = (x - theta[2]) / Math.pow(theta[3],2) * exp;
		
		derder[1][3] = Math.pow(x - theta[2],2) / Math.pow(theta[3],3) * exp;
		derder[3][1] = Math.pow(x - theta[2],2)  / Math.pow(theta[3],3) * exp;		
		
		derder[2][3] = (theta[1] - theta[0]) * exp * (x - theta[2]) * ( Math.pow(x - theta[2],2)  / Math.pow(theta[3],5) - 2 / Math.pow(theta[3],3) );
		derder[3][2] = (theta[1] - theta[0]) * exp * (x - theta[2]) * ( Math.pow(x - theta[2],2)  / Math.pow(theta[3],5) - 2 / Math.pow(theta[3],3) );		

		return derder;
	}
	
	public static double[][] getJacobian(double[] x, double[] theta){
		double[][] jacobian = new double[x.length][theta.length];
		double[] der;
		
		for (int i = 0; i < x.length; i ++) {
			der = getGaussderivatives(x[i],theta);
			for (int j = 0; j < theta.length; j ++) 
				jacobian[i][j] = - der[j];
			
		}
		
		return jacobian;
	}
	
	public static double[] getGradient(double[] residuals, double[][] jac){

		double[] grad = multiply(transpose(jac),residuals);
	
		return grad;
	}	
	
	private static double[][] transpose(double[][] matrix) {
		double[][] transpose = new double[matrix[0].length][matrix.length];

		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; j < matrix[i].length; j++)
				transpose[j][i] = matrix[i][j];
		return transpose;
	}
	
	private static double[][] multiply(double[][] a, double[][] b) {
		// matrix matrix multiplication, returns matrix
		if (a[0].length != b.length)
			throw new IllegalStateException("invalid dimensions");

		double[][] matrix = new double[a.length][b[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < b[0].length; j++) {
				double sum = 0;
				for (int k = 0; k < a[i].length; k++)
					sum += a[i][k] * b[k][j];
				matrix[i][j] = sum;
			}
		}

		return matrix;
	}
	
	private static double[] multiply(double[][] a, double[] b) {
		// matrix vector multiplication, returns vector
		
		if (a[0].length != b.length)
			throw new IllegalStateException("invalid dimensions");
		
		double[] vector = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			double sum = 0;
			for (int k = 0; k < a[i].length; k++)
				sum += a[i][k] * b[k];
			vector[i] = sum;
		}
		
		return vector;
		
	}
		
	private static double[] diag(double[][] matrix) {
		if (matrix.length != matrix[0].length)
			throw new IllegalStateException("invalid dimensions");
		
		int n = matrix.length;
		double[] diag = new double[n];	
		
		for (int i = 0; i < n; i++)
			diag[i] = matrix[i][i];		
		return diag;
	}
		
	private static double determinant(double[][] matrix) {
		if (matrix.length != matrix[0].length)
			throw new IllegalStateException("invalid dimensions");

		if (matrix.length == 2)
			return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];

		double det = 0;
		for (int i = 0; i < matrix[0].length; i++)
			det += Math.pow(-1, i) * matrix[0][i] * determinant(minor(matrix, 0, i));
		return det;
	}

	private static double[][] inverse(double[][] matrix) {
		double[][] inverse = new double[matrix.length][matrix.length];

		// minors and cofactors
		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; j < matrix[i].length; j++)
				inverse[i][j] = Math.pow(-1, i + j) * determinant(minor(matrix, i, j));

		// adjugate and determinant
		double det = 1.0 / determinant(matrix);
		for (int i = 0; i < inverse.length; i++) {
			for (int j = 0; j <= i; j++) {
				double temp = inverse[i][j];
				inverse[i][j] = inverse[j][i] * det;
				inverse[j][i] = temp * det;
			}
		}

		return inverse;
	}
	
	private static double[][] minor(double[][] matrix, int row, int column) {
		double[][] minor = new double[matrix.length - 1][matrix.length - 1];

		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; i != row && j < matrix[i].length; j++)
				if (j != column)
					minor[i < row ? i : i - 1][j < column ? j : j - 1] = matrix[i][j];
		return minor;
	}
}
