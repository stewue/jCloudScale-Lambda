package ch.uzh.ifi.seal.jcs_lambda.utility;

import ch.uzh.ifi.seal.jcs_lambda.annotations.ByReference;
import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.ReadOnly;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ReflectionUtil {
    /**
     * Get all class variables as map, which has a ReadOnly annotation
     * @param method method object from that we need the variables
     * @return map with all variables
     */
    public static Map<String, Type> getClassVariablesReadOnly(Method method ){
        return getClassVariablesWithAnnotation( method, ReadOnly.class );
    }

    /**
     * Get all class variables as map, which has a ByReference annotation
     * @param method method object from that we need the variables
     * @return map with all variables
     */
    public static Map<String, Type> getClassVariablesByReference(Method method ){
        return getClassVariablesWithAnnotation( method, ByReference.class );
    }

    /**
     * Get all class variables as map of a specific annotation
     * @param method context as a method
     * @param variableAnnotation the getClass of the annotation
     * @return map with all variables
     */
    public static Map<String, Type> getClassVariablesWithAnnotation(Method method, Class variableAnnotation ){
        Map<String, Type> classVariables = new HashMap<>();

        Class clazz = method.getDeclaringClass();
        Field[] fields = clazz.getDeclaredFields();

        for( Field field : fields ){
            if( field.isAnnotationPresent( variableAnnotation ) ){
                String name = field.getName();
                Type type = field.getGenericType();

                classVariables.put( name, type );
            }
        }

        return classVariables;
    }

    /**
     * Get all parameters (name + type) of a method
     * @param method current method
     * @return hash-map with the parameter names and types
     */
    public static HashMap<String, Type> getMethodParametersWithGenerics( Method method ){
        HashMap<String, Type> parameters = new HashMap<>();

        Parameter [] methodParameters = method.getParameters();

        for( Parameter parameter : methodParameters ){
            String parameterName = parameter.getName();
            Type parameterType = parameter.getParameterizedType();

            parameters.put( parameterName, parameterType );
        }


        return parameters;
    }

    /**
     * Get all parameters (name + type) of a method
     * @param method current method
     * @return hash-map with the parameter names and types
     */
    public static HashMap<String, Class> getMethodParameters( Method method ){
        HashMap<String, Class> parameters = new HashMap<>();

        Parameter [] methodParameters = method.getParameters();

        for( Parameter parameter : methodParameters ){
            String parameterName = parameter.getName();
            Class parameterType = parameter.getType();

            parameters.put( parameterName, parameterType );
        }


        return parameters;
    }

    /**
     * check if method name is present or if we have only the internal names (arg0, arg1, ...)
     * @param method method object
     * @return result of the inspection
     */
    public static boolean isMethodParameterNamePresent( Method method ){
        Parameter [] methodParameters = method.getParameters();

        if( methodParameters.length == 0 ){
            return false;
        }
        else{
            return  methodParameters[0].isNamePresent();
        }
    }

    /**
     * convert a classname string into a class object
     * @param classname full classname as string
     * @return class object
     * @throws ClassNotFoundException return exceptions if an error occurred
     */
    public static Class getClassFromString ( String classname ) throws ClassNotFoundException {
        Class clazz;

        // first check if it is a primitive type
        if( classname.equals("byte") ){
            clazz = byte.class;
        }
        else if( classname.equals("short") ){
            clazz = short.class;
        }
        else if( classname.equals("int") ){
            clazz = int.class;
        }
        else if( classname.equals("long") ){
            clazz = long.class;
        }
        else if( classname.equals("float") ){
            clazz = float.class;
        }
        else if( classname.equals("double") ){
            clazz = double.class;
        }
        else if( classname.equals("boolean") ){
            clazz = boolean.class;
        }
        else if( classname.equals("char") ){
            clazz = char.class;
        }
        // default if it isn't a primitive type
        else{
            clazz = Class.forName( classname );
        }

        return clazz;
    }

    /**
     * Get all method with @CloudMethod annotation
     * @return a set with all methods, which has the annotation
     */
    public static Set<Method> getAllMethodWithCloudMethodAnnotation (){
        ConfigurationBuilder reflectionConfig = new ConfigurationBuilder()
                .setUrls( ClasspathHelper.forPackage("") )
                .setScanners( new MethodAnnotationsScanner());
        Reflections reflections = new Reflections( reflectionConfig );
        return reflections.getMethodsAnnotatedWith( CloudMethod.class );
    }
}