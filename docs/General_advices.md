# General Advices

## Class and instance variable annotations

| Annotation        | Description   | 
| -------------     |:-------------:| 
| *Local*           | The local variable value is not available in the cloud. |
| *ReadOnly*        | The variable value is sent as read-only copy if the serverless function is started. |
| *ByReference*     | The serverless function has access to the local variable value an can read and write.  |

## Custom exceptions

| Exception                           | Description   | 
| -------------                       |:-------------:| 
| *CloudRuntimeException*             | An error is occured in the serverless function. The exception contains the stack trace from the cloud. |
| *IllegalDefinitionException*        | This exception is currently only thrown if the package name of a class is too long. |
| *InvalidCredentialsException*       | The credential from AWS is invalid. |
| *MavenBuildException*               | The maven build process has failed. Check the maven log. |
| *MissingStartUpException*           | Your application start point need a StartUp annotation. |
| *RuntimeReferenceVariableException* | An error occurs with the message queue. |

