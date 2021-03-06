# Water-Auth-Server
## SAML Configuration Environment Variables
### SAML IDP Metadata
This service can consume the metadata from a SAML Idnetity Provider either from a local file or from an HTTP URL.

- **samlIdpMetadataLocation** - The local file path or http(s) URL location of the SAML IDP Server metadata xml. The file can be loaded from the classpath by prepending "classpath:" to the start of the file path.

### SAML IDP AuthNRequest Configuration
The authentication request sent from the application to the SAML IDP server can be customized using the properties below. entityID must always be specified, however providerName is optional.

- **samlAuthnRequestEntityId** - The entity ID to include in AuthNRequests. This value is is also used in the issuer field of the request.

- **samlAuthnRequestProviderName** - [Default: *empty*] The provider name to specify for the request. This value is optional.

### Keystore
A keystore is used by the SAML service to ensure secure communication with the IDP server. Your keystore should contain the certs used on the IDP server as well as any certs required to reach your metadata XML if it is being supplied via http. The same keystore is also used by the Oauth2 portion of the application in order to sign JWT tokens.

- **keystoreLocation** - The file path to the keystore to be used for saml. The file can be loaded from the classpath by prepending "classpath:" to the start of the file path.

- **keystoreDefaultKey** - The default key of the keystore.

- **keystorePassword** - The password used to access the keystore.

- **keystoreTokenSigningKey** - The key alias to use for singing Oauth2 tokens. Note that this key alias **must** be different from the default key alias when running this application in Docker using the provided Dockerfile.

### SAML Login/Logout URL Routing
The login success and error as well as logout success URLs can be customized. These are the URLs that the client will be redirected to upon logging in or out, based on the status of that action.

- **loginSuccessTargetUrl** - The URL to redirect the client to upon a successful login.

- **loginErrorTargetUrl** - The URL to redirect the client to upon a failed login.

- **logoutSuccessTargetUrl** - The URL to redirect the client to upon logging out.

### SAML Access URL Overrides
The URLs that the application maps to various SAML action endpoints can be customized if desired, though all of these variables have default values. All values in this section should being with a forward slash ( / ) and end **without** a trailing slash.

These URLs are primarily used internally by the Spring Authentication Manager, rather than by clients themselves.

- **samlBaseEndpoint** - [Default: /saml] The base endpoint to append to the front of all sub-endpoints. This is also used for the security configuration to allow anonymous connections within all sub-endpoints of this endpoint.

- **samlLoginEndpoint** - [Default: /login] The login sub-endpoint to use for logging users in.

- **samlLogoutEndpoint** - [Default: /logout] The logout sub-endpoint to use for logging users out.

- **samlSingleLogoutEndpoint** - [Default: /singlelogout] The single logout sub-endpoint to use for logging users out of the application but **not** out of the IDP server.

- **samlSSOEndpoint** - [Default: /sso] The SSO sub-endpoint to use for handling single-sign-on Spring Security Context creation. 

- **samlSSOHOKEndpoint** - [Default: /ssohok] The SSO HoK (Holder-of-Key) sub-endpoint to use for handling single-sign-on Spring Security Context creation when the user is a holder of key.

- **samlMetadataEndpoint** - [Default: /metadata] The metadata sub-endpoint to use for retrieving the metadata for the application which can be loaded into the IDP server or used to verify that the other SAML settings have been applied properly.

## Running Locally

The application.yml file in `src/main/resources` should be copied to the project root directory and then the values can be modified to fit your local configuration.

## Docker

This project is configured to work with docker. Each relevant environemnt variable should be converted from the `application.yml` file to a `--env` command on a new docker service. The built JAR will be fetched from the MLR Artifactory when the image is built. The version of the JAR to grab must be passed as a build argument with: `mlr_version`

### Keystore

The dockerfile does not include automatic keystore generation, rather a pre-built keystore should be passed in using a docker secret or docker config. The mounted location of this keystore should then be passed in through the `keystoreLocation` environment variable. During the container startup the keystore will be modified to include the public cert of the IDP server in order to allow metadata pulling. The IDP cert properties are listed below.

- **samlIdpHost** - The host of the SAML IDP server that you are connecting to

- **samlIdpPort** - The port of the SAML IDP server that you are connecting to. If the metadata is being served through abrowser from an https url then this is likely 443.

### DOI Root Cert

If you are using an http location for your SAML IDP metadata and you are working from within the USGS network you will also need to inject the DOI root cert. This cert can be added by setting the following environemnt variable to true:

- **use_doi_cert** - [Default: false] Whether or not to include the DOI Root Cert in the keystore for the application.