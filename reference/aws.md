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
### Copy directory
aws --endpoint-url=http://localhost:4566 s3 cp <C:/src> <s3://dest> --recursive
### List keys in bucket
aws --endpoint-url=http://localhost:4566 s3api list-objects --bucket <bucket-name>
### Copy s3 bucket to local
aws --endpoint-url=http://localhost:4566 s3 cp s3://<bucket> C:/<dest> --recursive
### Remove object
aws --endpoint-url=http://localhost:4566 s3 rm <s3://bucket/key>
### Remove everything in bucket
aws --endpoint-url=http://localhost:4566 s3 rm <s3://bucket> --recursive

