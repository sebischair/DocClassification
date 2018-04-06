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
1. Change the database configurations according to the instructions in `application.conf`
1. Start the complete application stack using `docker-compose up`

Notice: the current docker uses a mongoDB image without any data. This will be dealt with in the future work.

# Note
The documents are retrieved from server.sociocortex.com

## Creating a training pipeline
1. Click on Training tab
2. Add a new pipeline and save it
3. Click on the newly created pipeline
4. Click on Link to SocioCortex workspace button
5. Select Amelie Workspace from the dropdown list
6. Click on Type checkbox
7. Select Types: Check Task
8. Select attributes for mining: Check Summary and Description
9. Select an attribute for labeling: Check design decision
10. Select attribute values as labels: Check both 0 and 1
11. Click on update labels button

12. Once the labels are created, select the classifier (for e.g., LibSVM)
13. Click on Train documents button

## Classifying documents
1. Select the pipeline
2. Input the text in the text area
3. Click on Classify documents button

One can POST a request to localhost:9000/pipeline/predict
with a JSON body:
{
 "pipelineName": "Decision detection",
 "textToClassify": "input text"
}
Response:
{
 "status": "OK"
 "result": "label"
}