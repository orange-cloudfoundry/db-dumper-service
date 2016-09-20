# db-dumper-service [![Build Status](https://travis-ci.org/orange-cloudfoundry/db-dumper-service.svg?branch=master)](https://travis-ci.org/orange-cloudfoundry/db-dumper-service) [![Codacy Badge](https://api.codacy.com/project/badge/grade/f94fec332f4645a099a99b9b743fc8f9)](https://www.codacy.com/app/arthur-halet/db-dumper-service) [![Codacy Badge](https://api.codacy.com/project/badge/coverage/f94fec332f4645a099a99b9b743fc8f9)](https://www.codacy.com/app/arthur-halet/db-dumper-service) [![Report badge](https://img.shields.io/badge/report-latest-blue.svg)](https://rawgit.com/orange-cloudfoundry/db-dumper-service/report/index.html)

A Cloud Foundry service broker to dump and restore database on demand.

Currently supported databases are:
- mysql
- postgresql
- mongodb
- redis

More details in the specifications at https://docs.google.com/document/d/1Y5vwWjvaUIwHI76XU63cAS8xEOJvN69-cNoCQRqLPqU/edit

See also the [backlog](https://www.pivotaltracker.com/n/projects/1441714) with label "service-db-dumper"

## Requirements

- [Cloud Foundry](http://cloudfoundry.org/) (>=192) with service broker api 2.6 at least
- A s3 service to store dumps (e.g: [p-riakcs](http://docs.pivotal.io/p-riakcs/), [s3-cf-service-broker](https://github.com/cloudfoundry-community/s3-cf-service-broker) or aws s3 given with cups)
- A database service to store models (e.g: [p-mysql](http://docs.pivotal.io/p-mysql/), [cleardb](http://docs.pivotal.io/p-mysql/))


## Installation in 5 Minutes

1. Download latest release.zip in [releases](/releases) 

```sh
LATEST_RELEASE_URL=$(curl -s https://api.github.com/repos/orange-cloudfoundry/db-dumper-service/releases/latest | grep browser_download_url | head -n 1 | cut -d '"' -f 4)
echo "Downloading $LATEST_RELEASE_URL"
curl -O -L $LATEST_RELEASE_URL
```

2. Unzip the latest downloaded file
3. Create a s3 service instance in your cloud foundry instance (e.g for [p-riakcs](http://docs.pivotal.io/p-riakcs/): `cf cs p-riakcs developer riak-db-dumper-service`)
4. Create a database service instance in your cloud foundry instance (e.g for [p-mysql](http://docs.pivotal.io/p-mysql/): `cf cs p-mysql 100mb mysql-db-dumper-service`)
5. Update the manifest (`manifest.yml`) file in the unzipped folder (**Note**: If you don't want to use uaa to protect access to dashboards, remove *uaa* profile from `spring_profiles_active`)
6. Add cloudfoundry API user following `cf_admin_user` and `cf_admin_password` var in manifest (This required for db-dumper to lookup databases by their service name using CC API. A  ``cloudcontroller.read`` permission is required on all spaces with db instances to dump. Alternatively, ``cloudcontroller.admin`` permission could be granted to this user to access any service instance in any space)
7. Push to your Cloud Foundry (in the manifest.yml folder: `cf push`)
8. Register and enable the service broker with:
```
$ cf create-service-broker db-dumper-service broker-user-from-manifest broker-password-from-manifest https://db-dumper-service.your.domain
$ cf enable-service-access db-dumper-service
```


## Configure your UAA (Optional if you remove uaa profile)

You need to create a new uaa client if you want to use UAA to authenticate user in the dashboard, here the steps you need to do:

1. use uaa-cli and run: `uaac client add db-dumper-service --name "db-dumper-service" --scope "openid,cloud_controller_service_permissions.read" --authorities "openid" -s "mysupersecretkey" --signup_redirect_url "http://your.db-dumper-service.url"` (**Note**: `--signup_redirect_url`is optional but highly recommended for security)
2. Update your `manifest.yml` (see: `uaa_url`, `security_oauth2_client_clientId` and `security_oauth2_client_clientSecret` keys)


## Running locally

**Note**:
- Default user to access to dashboard is user/password
- By default when you activate profile it use a filesystem filer, to use a S3 filer instead activate the profile `s3` (e.g. `spring_profiles_default=local,s3` ) and set the uri of your s3 in `config/spring-cloud.properties` file (change the value of `spring.cloud.mys3`).

### Linux 64 bits users

1. Clone this project
2. Run the script `bin/install-binaries` to download all required binaries
3. You need to activate the spring profile `local` to do this set an env var `spring_profiles_default=local`, you can either use with uaa profile to: `spring_profiles_default=local,uaa` (in this case you will need to set the env `uaa_url` with the url of your uaa)
4. Run the application

### Others

1. Clone this project
2. You will need to have binaries for driver you want to use
3. These env vars need to be set (set only driver you want to use): `mysql_dump_bin_path`(Path to mysqldump binary), `mysql_restore_bin_path`(Path to mysql binary), `postgres_dump_bin_path`(Path to pg_dump binary), `postgres_restore_bin_path`(Path to psql binary), `mongodb_dump_bin_path`(Path to mongodump binary), `mongodb_restore_bin_path`(Path to mongorestore binary), `redis_rutil_bin_path`(Path to [rutil](https://github.com/pampa/rutil) binary)
4. You need to activate the spring profile `local` to do this set an env var `spring_profiles_default=local`, you can either use with uaa profile to: `spring_profiles_default=local,uaa` (in this case you will need to set the env `uaa_url` with the url of your uaa)
5. Run the application



## How to use

**Note**:
- The service broker will run tasks asynchronously.
- The user token is needed when you want to dump or/and restore a database by its service name to check if your user is able to access to this service (We are waiting for [token delegation implementation](https://docs.google.com/document/d/1DoAbJa_YiGIJbOZ_zPzakh7sc4TB9Tmadq41cfSX0dw/edit?usp=sharing) to remove the need for a  mandatory user token)

### Use with a Cloud Foundry cli plugin

You may use the cli plugin associated to this broker:

1. Download the latest release of the [db-dumper-cli-plugin](https://github.com/Orange-OpenSource/db-dumper-cli-plugin) here: https://github.com/Orange-OpenSource/db-dumper-cli-plugin/releases (e.g. ``curl -O -L https://github.com/Orange-OpenSource/db-dumper-cli-plugin/releases/download/v1.1.2/db-dumper_linux_amd64``
2. ``chmod +x db-dumper_linux_amd64``
3. Install it to your cli with this command: `cf install-plugin "path/of/db-dumper-cli-plugin" -f`

**Note**: You can use this service without cli plugin, it was made only to have a more convenient way to use it than using broker arbitrary parameters.

### Create a dump by passing a database uri

This command will create a dump for you: 
```
cf cs db-dumper-service experimental service-name -c '{"db":"mysql://user:password@nameorip.of.your.db:port/database-name"}'
```

### Create a dump and tag it

Sometime, you would like to tag a particular dump, for this you can pass a list of tags during dumps creation.
For this you will need to pass this particular json:
```json
{
  "metadata":
    {
      "tags": [
        "mytag1",
        "mytag2"
        //...
      ]
    }
}
```

Example
```
cf cs db-dumper-service experimental service-name -c '{"db":"mysql://user:password@nameorip.of.your.db:port/database-name", "metadata": {"tags": ["v1"]}}'
```

### Create a dump by passing a service name

For example you have a `p-mysql` service instance named `my-mysql-db`, you can create a dump with these parameters:

```
cf cs db-dumper-service experimental service-name -c '{"db":"my-mysql-db", "cf_user_token": "token retrieve from cf oauth-token", "org": "org of the service", "space": "space of the service"}'
```

### Restore a dump by passing a database uri

```
cf update-service test -c '{"action": "restore", "db": "mysql://user:password@nameorip.of.your.second.db:port/database-second-name"}'
```

### Restore a dump by passing a service name

For example you have a `p-mysql` service instance named `my-mysql-restore-db`, you can restore a dump with these parameters:

```
cf update-service test -c '{"action": "restore", "db":"my-mysql-restore-db", "cf_user_token": "token retrieve from cf oauth-token", "org": "org of the service", "space": "space of the service"}'
```

### Binding

When you bind to your service instance you could find dumps created by your service instance as credentials, here what it's look like in credentials:

```json
{
  "dumps": [
    {
      "dump_id": "1",
      "download_url": "http://generated-user:generated-password@url.of.db.dumper/1",
      "show_url": "http://url.of.db.dumper/1",
      "filename": "file.sql.gzip",
      "created_at": "01-01-1970 00:00",
      "database_type": "mysql",
      "database_name": "uri or service name passed",
      "size": 20, // filesize in bytes
      "deleted": false,
      "tags": [
        "tag1"
      ]
    }
  ]
}
```

#### See all dumps during binding

By default, you can see only dumps corresponding to your service instance,
but if you would like to see all dumps created by any service instance linked to this database, you can pass a `see_all_dumps` parameter to `true` during binding.

Example:
```
cf bind-service myapp test -c '{"see_all_dumps": true}'
```

#### See dumps filtered by tags during binding

You can decide to only look at dumps created with a particular tags, to do so you will also need to pass a `find_by_tags` parameter during binding which contains one or multiple tags.

Example:
```
cf bind-service myapp test -c '{"find_by_tags": ["mytag"]}'
```

### Add a new dump

If you want to create a new dump you can use this command:

```
cf update-service test -c '{"action": "dump"}'
```

### Delete dumps

This is equivalent to delete the service, your dumps will be deleted after 5 days to prevent mistake (set by `dump_delete_expiration_days` in manifest):

```
cf ds db-dumper-service experimental service-name
```

## Access to dashboard


### User access

Users can see their dumps by using the dashboard URL accessible in each service instance, which has format:  https://db-dumper-service.my.domain/manage

Preview:
![Screenshot user](https://rawgit.com/Orange-OpenSource/db-dumper-service/master/src/main/resources/static/images/preview/user-page.png?refresh)

### Admin access

Admins have access to a backoffice UI available at https://db-dumper-service.my.domain/manage/admin (protected by basic auth with user and password set in the manifest, see `admin_username` and `admin_password`)

Preview:
![Screenshot user](https://rawgit.com/Orange-OpenSource/db-dumper-service/master/src/main/resources/static/images/preview/admin-page.png)

### Manage jobs

Sometimes, async jobs need to be managed, admins can use the job backoffice UI at https://db-dumper-service.my.domain/admin/control/jobs

Preview:
![Screenshot user](https://rawgit.com/Orange-OpenSource/db-dumper-service/master/src/main/resources/static/images/preview/jobs.png)

# Run tests

**In order to run all tests you need to clone this project**

## Unit test

1. Install [maven](https://maven.apache.org/download.cgi)
2. run `mvn clean test` inside the project

## Integration tests

### Prepare environment

To run integration tests on a non linux system you will have to install and run each database type you want to test:

- MySQL (or mariadb which is preferred one): https://downloads.mariadb.org/mariadb/repositories/#mirror=urbach
- PostgreSQL: http://www.postgresql.org/download/
- MongoDB:
 - To run database: https://www.mongodb.org/downloads#production
 - To dump and restore, prefer to install those binaries (Waiting the [PR related](https://github.com/mongodb/mongo-tools/pull/61) to be merged): https://github.com/ArthurHlt/mongo-tools
- Redis:
 - To run database: http://redis.io/download
 - To dump and restore: https://github.com/pampa/rutil


You will need to set java properties or env vars (replace '.' by '_' ) to specify ``dump`` and ``restore`` binaries path for each database type:

| Properties                | Example Location                           |
| ------------------------- |:------------------------------------------:|
| mysql.dump.bin.path       | /usr/local/bin/mysqldump                   |
| mysql.restore.bin.path    | /usr/local/Cellar/mariadb/10.1.8/bin/mysql |
| postgres.dump.bin.path    | /usr/local/bin/pg_dump                     |
| postgres.restore.bin.path | /usr/local/bin/psql                        |
| mongodb.dump.bin.path     | /usr/local/bin/mongodump                   |
| mongodb.restore.bin.path  | /usr/local/bin/mongorestore                |
| redis.rutil.bin.path      | /Users/arthurhalet/go/bin/rutil            |

**TIP:** If you are on linux 64 based OS you will do not need to do this, simply run the script `bin/install-binaries`

### Run test with a real Cloud Foundry

If you want to test on a real Cloud Foundry you will need to set these properties or env var (replace '.' by '_' ):

| Properties | Default value | Description |
| ---------- |:-------------:|:-----------:|
| cloud.controller.url | N/A | API url of your cloud foundry |
| cf.admin.user | N/A | Username to connect to cloud foundry |
| cf.admin.password | N/A | Password to connect to cloud foundry |
| int.cf.admin.org | N/A | Org to target to put services |
| int.cf.admin.space | N/A | Space to target to put services |
| int.cf.service.name.mysql | cleardb | Mysql service name from marketplace |
| int.cf.service.plan.mysql | spark (free plan) | Mysql service plan from marketplace |
| int.cf.service.name.postgresql | elephantsql | postgresql service name from marketplace |
| int.cf.service.plan.postgresql | turtle (free plan) | postgresql service plan from marketplace |
| int.cf.service.name.mongodb | mongolab | mongodb service name from marketplace |
| int.cf.service.plan.mongodb | sandbox (free plan) | mongodb service plan from marketplace |
| int.cf.service.name.redis | rediscloud | redis service name from marketplace |
| int.cf.service.plan.redis | 30mb (free plan) | redis service plan from marketplace |

### Run test with a real s3 bucket

Set two env var:
- `DYNO=true`
- `S3_URL=s3://accessKeyId:secretAccessKeyId@mys3.com/mybucket` (Change the value with your own s3 url)

### Other java properties

| Properties | Default value | Description |
| ---------- |:-------------:|:-----------:|
| int.mysql.server | mysql://root@localhost/dbdumpertestsource | URI of your mysql server, you can have to change dbdumpertestsource by a real database created on your mysql server |
| int.postgres.server | postgres://postgres@localhost/dbdumpertestsource | URI of your mysql server, you can have to change dbdumpertestsource by a real database created on your postgresql server |
| int.redis.server | redis://localhost | URI of your redis server |
| int.mongodb.server | mongodb://localhost/dbdumpertestsource | URI of your mongodb server |
| http.proxyHost | N/A | host of your http proxy (if you have one) |
| http.proxyPort | N/A | port of your http proxy (if you have one) |
| http.proxyUsername | N/A | username of your http proxy (if you have one) |
| http.proxyPassword | N/A | password of your http proxy (if you have one) |
| skip.ssl.verification | false | Set to true if you want to skip ssl verification when connecting to cloud foundry or s3 bucket |

### Run integration test

1. Install [maven](https://maven.apache.org/download.cgi)
2. run `mvn clean integration-test` inside the project
