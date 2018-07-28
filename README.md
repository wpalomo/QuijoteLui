# QuijoteLui

Documentos Electrónicos SRI del Ecuador

Software REST API escrito en Kotlin y Java, Framework SpringBoot

## Bases de datos soportadas
* PostgreSQL
* MySql o MariaDB
* SQL Server
* Oracle
* DB2

## Dependencias:
Estás dependencias se deben añadir en el repositorio local de Maven

* QuijoteLui Firmador:

https://gitlab.com/allku/QuijoteLuiFirmador

* QuijoteLui Client

https://gitlab.com/allku/QuijoteLuiClient

* QuijoteLui Printer

https://gitlab.com/allku/QuijoteLuiPrinter

* QuijoteLui Key Store

https://gitlab.com/allku/QuijoteLuiKS

* Oracle JDBC (Para bases de datos oracle 11g)
Se debe descargar manualmente el archivo jar y añádirlo al repositorio local Maven
```
$ mvn install:install-file -Dfile=ojdbc6.jar -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0 -Dpackaging=jar
```