package jcs_lambda.matrix_multiplication;

import java.util.concurrent.ThreadLocalRandom;

public class Matrix {
    private static final int MIN_RANDOM = -10;
    private static final int MAX_RANDOM = 10;

    private int y;
    private int x;
    private int [][] data;

    public Matrix (int y, int x ){
        this.y = y;
        this.x = x;
        data = new int [y][x];
    }

    public void setRandomValues(){
        for(int y = 0; y<this.y; y++ ){
            for(int x = 0; x<this.x; x++ ){
                int value = ThreadLocalRandom.current().nextInt( MIN_RANDOM, MAX_RANDOM + 1);
                data[y][x] = value;
            }
        }
    }

    public void print(){
        System.out.println("");
        System.out.println("-----------------");
        for(int y = 0; y<this.y; y++ ){
            for(int x = 0; x<this.x; x++ ){

                if( data[y][x] > 0 ){
                    System.out.print( "+" + data[y][x] + "\t");
                }
                else if( data[y][x] == 0 ){
                    System.out.print( " " + data[y][x] + "\t");
                }
                else
                {
                    System.out.print( data[y][x] + "\t");
                }
            }
            System.out.println("");
        }
        System.out.println("-----------------");
    }

    public int getY(){
        return y;
    }

    public int getX(){
        return x;
    }

    public int getElement( int y, int x ){
        return data[y][x];
    }

    public void setElementValue( int y, int x, int value ){
        data[y][x] = value;
    }
}
