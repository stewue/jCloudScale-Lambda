# Technical Restrictions of JCloudScale Lambda

- [Amazon Web Services Limits](http://docs.aws.amazon.com/general/latest/gr/aws_service_limits.html)
- JCloudScale Lambda is limited to 300 cloud functions. Because one api gateway from aws can only have a restricted number of endpoints.
- The package and the class name together (ex: com.packageName.className) shouldn't be longer than 100 characters, because the internal restrictions from Amazon Lambda is 128 and our application add some extra characters
- inner classes aren't supported
- The framework is based on Java 8, but it only supports the basic Java functionality (no lambda expressions, stream collection types is supported, etc.)
- The whole communication between cloud and local application are serialized object. So if something isn't serializable, it cannot be used in the cloud as parameter, reference or by-value variable
- Generics aren't supported yet, because [gson](https://github.com/google/gson) has problems with deserialize object with dynamic types (at runtime defined types)
- no file system support
- May be there are some other restrictions that we lost and it can cause exceptions or incorrect result.