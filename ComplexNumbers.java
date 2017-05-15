package assignment;

//A class that deals with the complex numbers that will be utilized for the determination of the Ybus Matrix in the Assignment
//Basically, the objects that are created from this class can handle complex numbers as indivivdual numbers given as input in the object
//through the constructor and can be either zero initially or a non-zero value. The functions of this class vary from simple mathematical
//calculations such as addition, substraction, multiplication and division to the calculation of the conjugate of a given number,
//its reciprocal value that is utilized in the admittance matrix as well as its absolute value. The class can be further extended
//to include the absolute value and phase that can be used for further calculations e.g power flow calculations.

public class ComplexNumbers {
	
	private double real;
	private double imag;
	
	public ComplexNumbers(){
		this.real = 0.0;
		this.imag = 0.0;
	}
	
	//Creates an complex number object based on the given values of the real and imaginary parts
	public ComplexNumbers(double re,double im){
		this.real = re;
		this.imag = im;
	}
	
	//Used to add complex numbers - Returns an object of their sum
	public ComplexNumbers add(ComplexNumbers obj){
		double real = this.real + obj.real;
		double imag = this.imag + obj.imag;
		return new ComplexNumbers(real,imag);
	}
	
	//Used to substract two complex numbers - Returns the result of their substraction
	public ComplexNumbers substract(ComplexNumbers obj){
		double real = this.real - obj.real;
		double imag = this.imag - obj.imag;
		return new ComplexNumbers(real,imag);
	}
	
	//Used to multiply complex numbers - Returns the reult of the multiplication
	public ComplexNumbers multiply(ComplexNumbers obj){
		double real = this.real*obj.real - this.imag*obj.imag;
		double imag = this.real*obj.imag + this.imag*obj.real;
		return new ComplexNumbers(real,imag);
	}
	
	//Divides two complex numbers and returns the result
	public ComplexNumbers divide(ComplexNumbers obj){
		ComplexNumbers num = this;
		return num.multiply(obj.reciprocal());
	}
	
	//Returns the conjugate of a given complex number
	public ComplexNumbers conjugate(){
		return new ComplexNumbers(real,-imag);
	}
	
	//Returns the reciprocal of a complex number
	public ComplexNumbers reciprocal(){
		double abs = real*real + imag*imag;
		return new ComplexNumbers(real/abs, -imag/abs);
	}
	
	//Returns the real part of a complex number
	public double getReal(){
		return real;
	}
	
	//Returns the imaginary part of a complex numbers
	public double getImag(){
		return imag;
	}
	
	//Returns the absolute value of a complex number
	public double absolute(){
		return Math.hypot(real,imag);
	}

	//Method used to print the results of a complex number object
    public String toString() {
        if (imag == 0) return real + "";
        if (real == 0) return imag + "i";
        if (imag <  0) return real + " - " + (-imag) + "i";
        return real + " + " + imag + "i";
    }
    
    //Method that returns the phase of a given complex number
    public double Determinephase() {
        return Math.atan2(imag, real);
    }
    
    //Method that multiplies a complex number with a number and returns the result
    public ComplexNumbers multiNumber(double num) {
        return new ComplexNumbers(num * real, num * imag);
    }
}
