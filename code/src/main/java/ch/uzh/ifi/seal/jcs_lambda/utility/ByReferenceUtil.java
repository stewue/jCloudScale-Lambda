package ch.uzh.ifi.seal.jcs_lambda.utility;

import ch.uzh.ifi.seal.jcs_lambda.annotations.ByReference;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ByReferenceUtil {
    private static Map<Class,Boolean> classHasAReferenceVariable = new HashMap<>();

    /**
     * check if a class has a reference variable
     * @param clazz checking class
     * @return boolean if it exists or not
     */
    public static boolean checkIfClassHasAReferenceVariable( Class clazz ){

        if( classHasAReferenceVariable.containsKey( clazz ) ){
            return classHasAReferenceVariable.get( clazz );
        }

        Field [] fields = clazz.getDeclaredFields();
        boolean foundAnnoationInClass = false;

        for( Field field : fields ){

            Annotation [] annotations = field.getAnnotations();

            for( Annotation annotation : annotations ) {
                if ( annotation.annotationType().equals(ByReference.class) ) {
                    foundAnnoationInClass = true;
                    break;
                }
            }
        }

        classHasAReferenceVariable.put( clazz, foundAnnoationInClass );
        return foundAnnoationInClass;
    }

    /**
     * get uuid of a object
     * @param context current object
     * @return uuid
     */
    public static String getUUID( Object context ){
        UniqueIdentifierManager uuidManager = UniqueIdentifierManager.getInstance();
        String uuid = uuidManager.getUuid( context );

        return uuid;
    }
}
