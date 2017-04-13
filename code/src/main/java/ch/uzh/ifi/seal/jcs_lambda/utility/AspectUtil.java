package ch.uzh.ifi.seal.jcs_lambda.utility;

import ch.uzh.ifi.seal.jcs_lambda.annotations.ReadOnly;
import ch.uzh.ifi.seal.jcs_lambda.management.CloudMethodEntity;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class AspectUtil {

    /**
     * Get all class variables as map
     * @param joinPoint invoke joint point, what we can get the current class
     * @return map with all variables
     */
    public static Map<String, Object> getClassVariablesValues( ProceedingJoinPoint joinPoint ){
        Map<String, Object> classVariableValues = new HashMap<>();

        Class clazz = joinPoint.getSignature().getDeclaringType();
        Field[] fields = clazz.getDeclaredFields();

        try {
            // iterate over all class variables
            for (Field field : fields) {
                Annotation[] annotations = field.getAnnotations();

                for (Annotation annotation : annotations) {
                    // check if its the read only annotation
                    if ( annotation.annotationType().equals(ReadOnly.class) ) {

                        Object value;
                        field.setAccessible(true);

                        // static variable
                        if (Modifier.isStatic(field.getModifiers())) {
                            value = field.get( null );
                        }
                        // non static / instance variable
                        else {
                            Object _this = joinPoint.getThis();
                            value = field.get( _this );
                        }

                        classVariableValues.put( field.getName(), value );
                    }
                }
            }
        }
        catch ( Exception e ){

        }

        return classVariableValues;
    }

    /**
     * convert jointPoint of the method into the full qualified name
     * @param joinPoint current point of execution
     * @return the full qualified name of the method
     */
    public static String getFullQualifiedName ( ProceedingJoinPoint joinPoint ){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String packageName = signature.getMethod().getDeclaringClass().getPackage().getName();
        String className = signature.getMethod().getDeclaringClass().getSimpleName();
        String methodName = signature.getMethod().getName();

        Class[] parameterTypes = signature.getParameterTypes();
        String [] parameterNames = signature.getParameterNames();
        HashMap<String, Class> parameters = new HashMap<>();

        for( int i=0; i<parameterTypes.length; i++ ){
            Class parameterType = parameterTypes[i];
            String parameterName = parameterNames[i];

            parameters.put( parameterName, parameterType );
        }

        return Util.getFullQualifiedName( packageName, className, methodName, parameters );
    }

    /**
     * get all parameters (value + name) of the method
     * @param joinPoint current point of execution
     * @return return a map with the values and parameter names of the injected method
     */
    public static HashMap<String, Object> getParametersWithValue( ProceedingJoinPoint joinPoint, CloudMethodEntity cloudMethodEntity ){
        HashMap<String, Object> parameters = new HashMap<>();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String [] parameterNames = signature.getParameterNames();
        Object [] arguments = joinPoint.getArgs();

        for( int i=0; i<arguments.length; i++ ){
            Object parameterValue = arguments[i];

            String parameterName;

            // real parameter name is present
            if( cloudMethodEntity.isParameterNamePresent() ){
                parameterName = parameterNames[i];
            }
            // only internal name is present
            else{
                parameterName = "arg" + i;
            }

            parameters.put( parameterName, parameterValue );
        }

        return parameters;
    }
}