# Overview over annotations and exceptions from jCloudScale Lambda 

## Class and instance variable annotations

| Annotation        | Description   | 
| -------------     |:-------------:| 
| *Local*           | The local variable value is not available in the cloud. |
| *ReadOnly*        | The variable value is sent as read-only copy if the serverless function is started. |
| *ByReference*     | The serverless function has access to the local variable value and has read and write access.  |

## Custom exceptions

| Exception                           | Description   | 
| -------------                       |:-------------:| 
| *CloudRuntimeException*             | An error has occurred in the serverless function. The exception contains the stack trace from the cloud. |
| *IllegalDefinitionException*        | This exception is currently only thrown if the package name of a class is too long. |
| *InvalidCredentialsException*       | The credential from AWS is invalid (or your Internet connection is down). |
| *MavenBuildException*               | The maven build process has failed. Check the maven log. |
| *MissingStartUpException*           | Your application start point needs a StartUp annotation. |
| *RuntimeReferenceVariableException* | An error occurs with the message queue. |

