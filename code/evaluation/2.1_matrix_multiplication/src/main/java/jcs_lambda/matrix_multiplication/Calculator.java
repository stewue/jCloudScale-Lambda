package jcs_lambda.matrix_multiplication;

import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;

public class Calculator
{
    @StartUp( deployToCloud = false )
    public static void main ( String [] args ){
        int m = 1000;
        int n = 1000;
        int o = 1000;

        for(int i=0; i<5; i++) {
            Matrix matrixA = new Matrix(m, n);
            matrixA.setRandomValues();
            //matrixA.print();

            Matrix matrixB = new Matrix(n, o);
            matrixB.setRandomValues();
            //matrixB.print();

            long startTime = System.currentTimeMillis();
            Matrix matrixResult = matrixMultiplication(matrixA, matrixB);
            System.out.println( (System.currentTimeMillis() - startTime) );
            //matrixResult.print();
        }
    }

    @CloudMethod( memory = 1024, timeout = 45 )
    public static Matrix matrixMultiplication( Matrix a, Matrix b ){
        int aY = a.getY();
        int aX = a.getX();
        int bY = b.getY();
        int bX = b.getX();

        if( aX != bY ){
            throw new IllegalArgumentException();
        }

        Matrix result = new Matrix( aY, bX );

        for( int y=0; y<aY; y++ ){
            for( int x=0; x<bX; x++ ){
                int sum = 0;

                for( int c = 0; c<aX; c++ ){
                    sum += a.getElement(y, c) * b.getElement(c, x);
                }

                result.setElementValue( y, x, sum );
            }
        }

        return result;
    }
}
