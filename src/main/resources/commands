
## How to run jar file. Set cp to current directory, ctakes lib, ctakes desc, and ctakes resources
java -cp "./*;C:/root/vdt/icapa/nlp/apache-ctakes-4.0.0/lib/*;C:/root/vdt/icapa/nlp/apache-ctakes-4.0.0/desc;C:/root/vdt/icapa/nlp/apache-ctakes-4.0.0/resources" icapa.Main


## How to run jar from command line w/ class path
https://stackoverflow.com/questions/18413014/run-a-jar-file-from-the-command-line-and-specify-classpath


bin/spark-submit --class icapa.SparkMain --master local[2] .\custom-components-1.0-SNAPSHOT-jar-with-dependencies.jar

## Fix for temp dir delete issue: https://stackoverflow.com/questions/41825871/exception-while-deleting-spark-temp-dir-in-windows-7-64-bit

## Setup classpath with runPiperFile.bat dependencies
* Right-click -> open module settings -> + -> Add Jars or dependecies



$ ./usr/bin/spark-submit \
--class org.poc.ctakes.spark.CtakesSparkMain \
--master yarn --deploy-mode cluster \
--conf spark.executor.extraClassPath=/tmp/ctakesdependencies/  \
--conf spark.driver.extraClassPath=/tmp/ctakesdependencies/ \
--conf spark.driver.memory=5g --executor-memory=10g \
spark-ctakes-0.1-shaded.jar

### Teradata jdbc args
java -cp ./* -Djavax.security.auth.useSubjectCredsOnly=false -Djava.security.auth.login.config=C:\ProgramData\Teradata\jaas.conf -Djava.security.krb5.conf=C:\ProgramData\Teradata\krb5.conf icapa.Playground
### Teradata ini file: C:\Program Files\Teradata\Client\17.00\Teradata Studio nt-x8664