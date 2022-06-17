Pipeline restFul API

## API

该插件提供一些访问流水线任务的 API。

### 获取 Pipeline

`curl http://localhost/jenkins/job/test/restFul/`

### 更新 Pipeline

`curl http://localhost/jenkins/job/test/restFul/update -X POST`

## 构建 Pipeline

`curl http://localhost/jenkins/job/test/restFul/build?delay=0&timeout=10 -X POST`

response 示例：
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

## 参数

增加参数： `curl http://localhost:8080/jenkins/job/test/restFul/addParameter --data 'params=[{"name":"name","value":"rick","desc":"this is a name"}]'`

删除参数： `curl -X POST "http://localhost:8080/jenkins/job/test/restFul/removeParameter?params=name,age"`

