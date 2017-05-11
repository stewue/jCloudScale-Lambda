package ch.uzh.ifi.seal.jcs_lambda_matrix_multiplication;

public class Calculator
{
    public static void main ( String [] args ){
        int m = 2;
        int n = 3;
        int o = 2;

        Matrix matrixA = new Matrix( m, n );
        matrixA.setRandomValues();
        matrixA.print();

        Matrix matrixB = new Matrix( n, o );
        matrixB.setRandomValues();
        matrixB.print();

        Matrix matrixResult = matrixMultiplication( matrixA, matrixB );
        matrixResult.print();
    }

    private static Matrix matrixMultiplication( Matrix a, Matrix b ){
        int aY = a.getY();
        int aX = a.getX();
        int bY = b.getY();
        int bX = b.getX();

        if( aX != bY ){
            throw new IllegalArgumentException();
        }

        Matrix result = new Matrix( aY, bX );

        Matrix subResult = particalMatrixMultiplication( a, b, 0, aY, 0, 1 );
        mergeTemporaryDataIntoOriginStracture( result, subResult, 0, aY, 0, 1 );

        Matrix subResult2 = particalMatrixMultiplication( a, b, 0, aY, 1, bX );
        mergeTemporaryDataIntoOriginStracture( result, subResult2, 0, aY, 1, bX );

        return result;
    }

    private static Matrix particalMatrixMultiplication( Matrix a, Matrix b, int startY, int endY, int startX, int endX ){
        int aY = a.getY();
        int aX = a.getX();
        int bY = b.getY();
        int bX = b.getX();
        Matrix subresult = new Matrix( aY, bX );

        for( int y = startY; y<endY; y++ ){
            for( int x = startX; x<endX; x++ ){
                int sum = 0;

                for( int c = 0; c<aX; c++ ){
                    sum += a.getElement(y, c) * b.getElement(c, x);
                }

                subresult.setElementValue( y, x, sum );
            }
        }

        return subresult;
    }

    private static Matrix mergeTemporaryDataIntoOriginStracture( Matrix origin, Matrix tmp, int startY, int endY, int startX, int endX ){
        for( int y = startY; y<endY; y++ ){
            for( int x = startX; x<endX; x++ ){
                int value = tmp.getElement( y, x );
                origin.setElementValue( y, x, value );
            }
        }

        return origin;
    }
}
