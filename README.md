# DocClassification
Seed project for document classification with Java Play 2.5.9
User interface support for
    + Creating pipelines
    + Creating new labels
    + Training documents
    + Classifying documents

API support for Classifying documents - <a href="https://documenter.getpostman.com/view/693941/collection/RW1aKg3Z" target="_blank">API Documentation</a>

[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/b608adb9766710441ec2)


## Runtime dependencies:
JDK - 1.8.0
Activator - sbt launcher version 0.13.8 (https://www.lightbend.com/activator/download)
MongoDB - 3.6.2 (https://www.mongodb.com/download-center#community)

## Core Plugin dependencies (check build.sbt)
Play - 2.5.9
Spark (core, sql, mllib) - 2.0.1
Morphia - 1.2.1
Angular - 1.5.8
Bootstrap - 3.3.6

## Deploying the project
Ensure mongodb is running on default port 27017; else configure settings in Global.java
From the command prompt execute
> activator run
or use run.bat

Application will be available at localhost:9000

## Docker support
1. Change the database configurations according to the instructions in `application.local.conf.back`
2. Start the application stack using `docker-compose up`

## How to use DocClassification
To find out how to use DocClassification, please checkout out the <a href="https://github.com/sebischair/DocClassification/wiki" target="_blank">DocClassification wiki</a>.
