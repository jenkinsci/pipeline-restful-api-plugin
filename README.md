Pipeline restFul API

## API

This plugin provides some APIs to access the Pipeline Job.

### Get Pipeline

`curl http://localhost/jenkins/job/test/restFul/`

### Update Pipeline

`curl http://localhost/jenkins/job/test/restFul/update -X POST`

## Build Pipeline

`curl http://localhost/jenkins/job/test/restFul/build?delay=0&timeout=10 -X POST`

Sample response:
```
{
	"_class": "io.jenkins.plugins.pipeline.restful.api.PipelineRestfulAPI$IdentityBuild",
	"build": {
		"number": 6,
		"url": "http://localhost:8080/jenkins/job/test/6/"
	},
	"cause": {
		"shortDescription": "Identity cause, message is null, uuid is 809b77d4-5a26-4743-b08f-7e801d6fb03b"
	}
}
```

## Run script on agent

`curl http://localhost/jenkins/instance/run?agent=&script=println%20"uname%20-a".execute().text -X POST`
