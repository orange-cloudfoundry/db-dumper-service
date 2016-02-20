# db-dumper-service

A Cloud Foundry service broker to dump and restore database on demand.

Currently supported databases are:
- mysql
- postgresql
- mongodb
- redis

More details in the specifications at https://docs.google.com/document/d/1Y5vwWjvaUIwHI76XU63cAS8xEOJvN69-cNoCQRqLPqU/edit

See also the [backlog](https://www.pivotaltracker.com/n/projects/1441714) with label "service-db-dumper"

## Requirement

- [Cloud Foundry](http://cloudfoundry.org/) (>=192) with service broker api 2.6 at least
- A s3 service to store dump (e.g: [p-riakcs](http://docs.pivotal.io/p-riakcs/), [s3-cf-service-broker](https://github.com/cloudfoundry-community/s3-cf-service-broker) or aws s3 given with cups)
- A database service to store model (e.g: [p-mysql](http://docs.pivotal.io/p-mysql/), [cleardb](http://docs.pivotal.io/p-mysql/))


## Installation in 5 Minutes

1. Download latest release.zip in [releases](/releases) (current: https://github.com/Orange-OpenSource/db-dumper-service/releases/download/v1.0.1/db-dumper-service.zip )
2. Unzip the latest downloaded file
3. Create a s3 service instance in your cloud foundry instance (e.g for [p-riakcs](http://docs.pivotal.io/p-riakcs/): `cf cs p-riakcs developer riak-db-dumper-service`)
4. Create a database service instance in your cloud foundry instance (e.g for [p-mysql](http://docs.pivotal.io/p-mysql/): `cf cs p-mysql 100mb mysql-db-dumper-service`)
5. Update the manifest (`manifest.yml`) file in the unzipped folder (**Note**: If you don't want to use uaa to login into dashboard, remove *uaa* profile in `spring_profiles_active`
6. Add cloudfoundry user with admin role for db-dumper-service following `cf_admin_user` and `cf_admin_password` var in manifest (This required to find database by their service name)
7. Push to your Cloud Foundry (in the manifest.yml folder: `cf push`)
8. Enable service broker by doing:
```
$ cf create-service-broker db-dumper-service broker-user-from-manifest broker-password-from-manifest https://db-dumper-service.your.domain
$ cf enable-service-access db-dumper-service
```
9. You're done

## Configure your UAA (Optional if you remove uaa profile)

You need to create a new uaa client if you want to use UAA to authenticate user in the dashboard, here the steps you need to do:

1. use uaa-cli and run: `uaac client add db-dumper-service --name "db-dumper-service" --scope "openid,cloud_controller.read" --authorities "openid" -s "mysupersecretkey" --signup_redirect_url "http://your.db-dumper-service.url"` (**Note**: `--signup_redirect_url`is optional but highly recommended for security)
2. Update your `manifest.yml` (see: `CF_TARGET`, `security_oauth2_client_clientId` and `security_oauth2_client_clientSecret` keys)


## Running locally

**Note**: Default user to access to dashboard is user/password

### Linux 64 bits users

1. Clone this project
2. Run the script `bin/install-binaries` to download all required binaries
3. You need to activate the spring profile `local` to do this set an env var `spring_profiles_default=local`, you can either use with uaa profile to: `spring_profiles_default=local,uaa` (in this case you will need to set the env `CF_TARGET` with the url of your uaa)
4. Run the application

### Others

1. Clone this project
2. You will need to have binaries for driver you want to use
3. These env vars need to be set (set only driver you want to use): `mysql_dump_bin_path`(Path to mysqldump binary), `mysql_restore_bin_path`(Path to mysql binary), `postgres_dump_bin_path`(Path to pg_dump binary), `postgres_restore_bin_path`(Path to psql binary), `mongodb_dump_bin_path`(Path to mongodump binary), `mongodb_restore_bin_path`(Path to mongorestore binary), `redis_rutil_bin_path`(Path to [rutil](https://github.com/pampa/rutil) binary)
4. You need to activate the spring profile `local` to do this set an env var `spring_profiles_default=local`, you can either use with uaa profile to: `spring_profiles_default=local,uaa` (in this case you will need to set the env `CF_TARGET` with the url of your uaa)
5. Run the application



## How to use

**Note**: The service broker will run task asynchronously.

### Create a dump 

**NOTE**: For the moment you need to pass your database uri but in next release you could just pass a database service to db-dumper-service
This command will create a dump for you: 
```
cf cs db-dumper-service experimental service-name -c '{"src_url":"mysql://user:password@nameorip.of.your.db:port/database-name"}'
```

### Restore a dump

To restore a dump you will need to pass 2 databases, the one you want to retrieve the dump, the second one to restore the retrieved dump (**Note**: the second database can be the same):
```
cf update-service test -c '{"action": "restore", "target_url": "mysql://user:password@nameorip.of.your.second.db:port/database-second-name"}'
```

### Update a dump

If you want to update a dump you can use this command but it will replace your actual dump:
```
cf update-service test -c '{"action": "dump"}'
```

### Delete a dump

This is equivalent to delete the service, your dumps will be deleted after 5 days to prevent mistake (set by `dump_delete_expiration_days` in manifest):
```
cf ds db-dumper-service experimental service-name
```

## Access to dashboard


### User access

Users can see their dumps by using dashboard, you can found it to this address: https://db-dumper-service.my.domain/manage

Preview:
![Screenshot user](https://rawgit.com/Orange-OpenSource/db-dumper-service/master/src/main/resources/static/images/preview/user-page.png)

### Admin access

Admin can access to his own page by going to https://db-dumper-service.my.domain/manage/admin (user and password has been set in the manifest, see `admin_username` and `admin_password`)

Preview:
![Screenshot user](https://rawgit.com/Orange-OpenSource/db-dumper-service/master/src/main/resources/static/images/preview/admin-page.png)

### Manage jobs

Sometimes, async jobs need to be managed, you can access to it with this url: https://db-dumper-service.my.domain/admin/control/jobs

Preview:
![Screenshot user](https://rawgit.com/Orange-OpenSource/db-dumper-service/master/src/main/resources/static/images/preview/jobs.png)
