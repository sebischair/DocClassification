# DocClassification
Seed project for document classification with Java Play 2.5.9
User interface support for
    + Creating pipelines
    + Creating new labels
    + Training documents
    + Classifying documents

## Runtime dependencies:
JDK - 1.8.0
Activator - sbt launcher version 0.13.8 (https://www.lightbend.com/activator/download)
MongoDB - 3.2.10 (https://www.mongodb.com/download-center#community)

## Core Plugin dependencies
Play - 2.5.9
Spark (core, sql, mllib) - 2.0.1
Morphia - 1.2.1
Angular - 1.5.8
Bootstrap - 3.3.6

## Deploying the project
Ensure mongodb is running on port 27017; else configure settings in Global.java
From the command prompt execute
> activator run
or use run.bat

Application is available at localhost:9000

