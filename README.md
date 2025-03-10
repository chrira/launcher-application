# Fabric8-Launcher Application

[![CircleCI](https://circleci.com/gh/fabric8-launcher/launcher-application.svg?style=svg)](https://circleci.com/gh/fabric8-launcher/launcher-application)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=io.fabric8.launcher%3Alauncher-parent&metric=alert_status)](https://sonarcloud.io/dashboard/index/io.fabric8.launcher:launcher-parent)
[![Dependabot Status](https://api.dependabot.com/badges/status?host=github&identifier=111528311)](https://dependabot.com)
[![Project Chat](https://img.shields.io/badge/zulip-join_chat-brightgreen.svg)](https://launcher.zulipchat.com/)

This is a [Quarkus](https://quarkus.io) application exposing a Web front-end and several JAX-RS endpoints to handle launching of application into Openshift. This application collects from an end user the information needed to generate a Zip file containing a Maven project populated for an Eclipse Vert.x, Spring Boot, Thorntail or Node.js container
(see https://github.com/fabric8-launcher/launcher-booster-catalog for the full list).

The OpenAPI 3.0 descriptor for these services is available at https://editor.swagger.io/?url=https://forge.api.openshift.io/openapi

Chat
-----
We're in ZulipChat! Join us now at https://launcher.zulipchat.com/

Contributions
-------------

Contributions are welcome!

Please read the [Contributing Guide](./CONTRIBUTING.md) to help you make a great contribution.

Setting up the environment
--------------------------

* You have to setup environment variables before you start the back-end. 

> You can source, or use as an example, the environment script template located there: [./launcher-env-template.sh](./launcher-env-template.sh)

```bash
$ source ./launcher-env-template.sh
```

> [KeyCloak](http://www.keycloak.org/) adds authentication to the Launcher and secures services with minimum fuss. No need to deal with storing users or authenticating users. It's all available out of the box.
> For development only, you can choose to work without KeyCloak by changing the option in the environment script.
> If you are not using KeyCloak, you can find information on how to [setup your git providers default credentials](README.md#setup-git-providers-default-credentials-no-keycloak-mode).

 
IDE Setup
---------

 * *Immutables* - Setup your IDE for dealing with generated immutable classes see the
   [online documentation](https://immutables.github.io/apt.html). You have to build the project at least
   once for the classes to be generated or you will still get errors in the IDE.

Build and Run the Application
-----------------------------

* First follow the environment setup [instructions](README.md#setting-up-the-environment). 

* Build this project:
```bash
$ mvn clean install
```

* Run:
```bash
$ java -jar web/target/launcher-runner.jar
```

Then follow the [front-end README](https://github.com/fabric8-launcher/launcher-frontend/blob/master/README.md) to run the front-end.

Running with Docker
-------------------

* First follow the environment setup [instructions](README.md#setting-up-the-environment). 

* Build and run:
```bash
$ ./docker.sh
```

This will build and run a Docker image that will work in the same way as if you were running it locally. So the frontend can connect to it in the same way.

Build and Run the Unit Tests
----------------------------

* First follow the environment setup [instructions](README.md#setting-up-the-environment). 

* Execute:
```bash
$ mvn clean install
```
        
Build and Run the Unit and Integration Tests
--------------------------------------------

* First follow the environment setup [instructions](README.md#setting-up-the-environment).

* To build the project and run the integration tests, allowing Maven to start the WildFly Swarm server:
```bash
$ mvn clean install -Pit
```

* To skip building and just run the integration tests, allowing Maven to start the WildFly Swarm server:
```bash
$ mvn integration-test -Pit
```

Reindex the booster catalog
---------------------------

Run the following command, replace TOKEN with the value defined in the environment variable `LAUNCHER_BACKEND_CATALOG_REINDEX_TOKEN`. Doesn't need to be specified if the environment variable doesn't exist in the running environment:

        $ curl -v -H "Content-Type: application/json" -d '{}' -X POST  https://localhost:8180/api/booster-catalog/reindex\?token\=TOKEN
        
        
Setup git providers default credentials (No KeyCloak mode)
----------------------------------------------------------


#### GitHub

* Log into GitHub and generate an access token for use here:
--  https://github.com/settings/tokens
    * Set scopes
        * `repo`
        * `admin:repo_hook`
        * `delete_repo`
* Run the following commands:
 ```bash
 git config --global github.user "<replace with your github username>"
 git config --global github.token "<replace with your github token>"
 ```

#### GitLab
 
* Log into GitLab and generate an access token for use here: 
--  https://gitlab.com/profile/personal_access_tokens
    * Set scopes
        * `api`
        * `read_user`
* Run the following commands:
 ```bash
 git config --global gitlab.user "<replace with your gitlab username>"
 git config --global gitlab.token "<replace with your gitlab token>"
 ```

#### BitBucket


* Log into Bitbucket and generate an application password for use here: 
--  https://bitbucket.org/account/admin/app-passwords
    * Activate permissions:
        * Account: Email, Read
        * Team: Read
        * Projects: Read
        * Repositories: Read, Write, Admin, Delete
        * Pull requests: Read
        * Issue: Read
        * Webhook: Read and write
* Run the following commands:
 ```bash
 git config --global bitbucket.user "<replace with your github username>"
 git config --global bitbucket.password "<replace with your bitbucket application password>"
 ```

#### Gitea

Launcher accesses Gitea using the [Sudo](https://docs.gitea.io/en-us/api-usage/) feature, so make sure that the user referenced in the `Authorization` HTTP Header also exists in the Gitea server 
  
* Logged as an admin user, generate an access token (eg. https://try.gitea.com/user/settings/applications).
* You need to provide 3 environment variables when running the backend:

|Environment |Description|
|------------|-----------|
|`LAUNCHER_GIT_PROVIDER`|The default Git provider to use, should be `Gitea`|
|`LAUNCHER_BACKEND_GITEA_URL`|The URL where the Gitea server is running|
|`LAUNCHER_BACKEND_GITEA_USERNAME`| The admin username|
|`LAUNCHER_BACKEND_GITEA_TOKEN`|The admin access token|

Filtering the booster catalog
-----------------------------

This feature can be activated by providing the `LAUNCHER_BOOSTER_CATALOG_FILTER` env param/system property
The script must evaluate to a boolean using the `booster` variable, that is an instance of [RhoarBooster](https://github.com/fabric8-launcher/launcher-booster-catalog-service/blob/master/src/main/java/io/fabric8/launcher/booster/catalog/rhoar/RhoarBooster.java).

        $ export LAUNCHER_BOOSTER_CATALOG_FILTER=booster.mission.id === 'rest-http'

Examples:

- `booster.mission.id == 'rest-http'`: will return only HTTP Rest mission boosters
- `booster.runtime.id == 'spring-boot'`: returns only the Spring Boot boosters
- `booster.mission.id == 'rest-http' && booster.runtime.id` == 'spring-boot': returns only HTTP Rest Spring Boot boosters
- `booster.metadata.istio`: returns only boosters that contains the `istio: true` flag in the booster metadata
- `booster.mission.metadata.istio`: returns only boosters that contains the `istio: true` flag in the mission metadata assigned to the booster


Multi-tenant (User impersonation)
---------------------------------
In a single multi-tenant cluster, the Keycloak used to authenticate in Launcher may be the same as the one used in OpenShift.
In this case, Launcher shouldn't require Keycloak to be configured with OpenShift as an identity provider.
Launcher supports that using a ServiceAccount with user impersonation.

More info in https://kubernetes.io/docs/reference/access-authn-authz/authentication/#user-impersonation

Here are the steps to configure:

1) Create an impersonator cluster role as described below (it can be any name, as long as it contains the `impersonate` verb):

    ```
    apiVersion: rbac.authorization.k8s.io/v1
    kind: ClusterRole
    metadata:
      name: impersonator
    rules:
    - apiGroups: [""]
      resources: ["users", "groups", "serviceaccounts"]
      verbs: ["impersonate"]
    ```

2) Create a ServiceAccount in the cluster with the impersonator cluster role
3) Set the `LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN` env in the launcher-backend pointing to the token corresponding to the ServiceAccount created in step 2
4) Set the `LAUNCHER_MISSIONCONTROL_OPENSHIFT_IMPERSONATE_USER` env var to true meaning that launcher will impersonate the current logged user from the ServiceAccount
created in step 2 when connecting to OpenShift


Code of Conduct
-------------

Please adopt our [Code of Conduct](./CODE_OF_CONDUCT.md) to follow our community standards, signal a welcoming and inclusive project, and outline procedures for handling abuse.

