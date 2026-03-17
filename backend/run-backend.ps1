$env:JAVA_HOME = 'G:\Data\Java17'
$env:Path = "${env:JAVA_HOME}\bin;" + $env:Path
mvn spring-boot:run
