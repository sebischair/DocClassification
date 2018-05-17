# DocClassification
DocClassification is a seed project for document classification.

User interface support for
* Creating pipelines
* Creating new labels
* Training documents
* Classifying documents

Check out the <a href="https://documenter.getpostman.com/view/4318985/docclassification/RW86LAES" target="_blank">API documentation</a>.

[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/88f1b89dcd10a038bfcb)

## Runtime dependencies:
* JDK - `1.8.0`
* Activator - sbt launcher version `0.13.8` (https://www.lightbend.com/activator/download)
* MongoDB - `3.6.2` (https://www.mongodb.com/download-center#community)

## Core Plugin dependencies (check build.sbt)
* Play - `2.5.9`
* Spark (core, sql, mllib) - `2.0.1`
* Morphia - `1.2.1`
* Angular - `1.5.8`
* Bootstrap - `3.3.6`

## Configuration
1. Rename application.local.conf.back to application.local.conf
1. Fill in database credentials
1. `morphia.db.name` is the database that stores DocClassification related information
1. `morphia.amelie.db.name` is the database that sotres projects, issues, etc.

## Run the project
* From the command prompt execute `activator run` or use `run.bat`
* Application is available at `localhost:9000`

## Docker support
1. Change the database configurations according to the instructions in `application.local.conf`
2. Start the application stack using `docker-compose up`

## How to use DocClassification
To find out how to use DocClassification, please checkout out the <a href="https://github.com/sebischair/DocClassification/wiki" target="_blank">DocClassification wiki</a>.
