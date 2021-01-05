## Prereq
* Docker

## Start
localstack start
aws configure

## Useful Commands
### View file contents
aws --endpoint-url=http://localhost:4566 s3 cp s3://temp/temp-key.md -
aws --endpoint-url=http://localhost:4566 s3 cp s3://temp/temp-key.md <path>
### Create bucket
aws --endpoint-url=http://localhost:4566 s3 mb s3://temp
### List buckets
aws --endpoint-url=http://localhost:4566 s3 ls
### Copy file contents
aws --endpoint-url=http://localhost:4566 s3 cp <C:/src> <s3://dest>
