# db-dumper-service

A Cloud Foundry service broker to dump and restore database on demand.

More details in the specifications at https://docs.google.com/document/d/1Y5vwWjvaUIwHI76XU63cAS8xEOJvN69-cNoCQRqLPqU/edit

## Requirement

- [Cloud Foundry](http://cloudfoundry.org/) with service broker api 2.6 at least
- A s3 service to store dump (e.g: [p-riakcs](http://docs.pivotal.io/p-riakcs/), [s3-cf-service-broker](https://github.com/cloudfoundry-community/s3-cf-service-broker) or aws s3 given with cups)
- A database service to store model (e.g: [p-mysql](http://docs.pivotal.io/p-mysql/), [cleardb](http://docs.pivotal.io/p-mysql/))


## Installation in 3 Minutes

1. Download latest release.zip in [releases](/releases) (current: https://github.com/Orange-OpenSource/db-dumper-service/releases/download/v1.0.0/db-dumper-service.zip )
2. Unzip the latest downloaded file
3. Create a s3 service instance in your cloud foundry instance (e.g for [p-riakcs](http://docs.pivotal.io/p-riakcs/): `cf cs p-riakcs developer riak-db-dumper-service`)
4. Create a database service instance in your cloud foundry instance (e.g for [p-mysql](http://docs.pivotal.io/p-mysql/): `cf cs p-mysql 100mb mysql-db-dumper-service`)
5. Update the manifest (`manifest.yml`) file in the unzipped folder (**Note**: If you don't want to use uaa to login into dashboard, remove *uaa* profile in `spring_profiles_active`
6. Push to your Cloud Foundry (in the manifest.yml folder: `cf push`)
7. Enable service broker by doing:
```
$ cf create-service-broker db-dumper-service broker-user-from-manifest broker-password-from-manifest https://db-dumper-service.your.domain
$ cf enable-service-access db-dumper-service
```
8. You're done

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
cf update-service test -c '{"action": "restore", "src_url":"mysql://user:password@nameorip.of.your.db:port/database-name", "target_url": "mysql://user:password@nameorip.of.your.second.db:port/database-second-name"}'
```

### Update a dump

If you want to update a dump you can use this command but it will replace your actual dump:
```
cf update-service test -c '{"src_url":"mysql://user:password@nameorip.of.your.db:port/database-name", "action": "dump"}'
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
