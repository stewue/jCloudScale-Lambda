package ch.uzh.ifi.seal.jcs_lambda.utility;

import ch.uzh.ifi.seal.jcs_lambda.annotations.ByReference;
import ch.uzh.ifi.seal.jcs_lambda.annotations.ReadOnly;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ByReferenceUtil {
    private static Map<Class,Boolean> classHasAReferenceVariable = new HashMap<>();

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

    public static String getUUID( Object context ){
        try {
            Field uuidField = context.getClass().getDeclaredField("_uuid_" );
            uuidField.setAccessible(true);
            String uuid = (String) uuidField.get(context);

            return uuid;
        }
        catch ( Exception e ){

        }

        return null;
    }
}
