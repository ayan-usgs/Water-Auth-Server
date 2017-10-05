package gov.usgs.wma.mlrauthserver.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

@Configuration
@EnableAuthorizationServer
public class AuthorizationConfig extends AuthorizationServerConfigurerAdapter {

	private int accessTokenValiditySeconds = 10000;
	private int refreshTokenValiditySeconds = 30000;

	@Value("${security.oauth2.resource.id}")
	private String resourceId;
	@Value("${keystoreLocation}")
	private String keystorePath;
	@Value("${keystoreDefaultKey}")
	private String keystoreDefaultKey;
	@Value("${keystoreTokenSigningKey}")
	private String keystoreTokenSigningKey;
	@Value("${keystorePassword}")
	private String keystorePassword;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Override
	public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
		oauthServer
		// we're allowing access to the token only for clients with 'ROLE_TRUSTED_CLIENT' authority
		.tokenKeyAccess("hasAuthority('ROLE_TRUSTED_CLIENT')")
		.checkTokenAccess("hasAuthority('ROLE_TRUSTED_CLIENT')");
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory()
		.withClient("trusted-app")
		.authorizedGrantTypes("client_credentials", "password", "refresh_token")
		.authorities("ROLE_TRUSTED_CLIENT")
		.scopes("read", "write")
		.resourceIds(resourceId)
		.accessTokenValiditySeconds(accessTokenValiditySeconds)
		.refreshTokenValiditySeconds(refreshTokenValiditySeconds)
		.secret("secret");
	}

	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		Resource storeFile;
		
		if(this.keystorePath.toLowerCase().startsWith("classpath:")){
			DefaultResourceLoader loader = new DefaultResourceLoader();
			String classpathLocation = this.keystorePath.replaceFirst("classpath:", "");
			storeFile = loader.getResource(classpathLocation);
		} else {
			FileSystemResourceLoader loader = new FileSystemResourceLoader();
			storeFile = loader.getResource(this.keystorePath);
		}
		
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		KeyStoreKeyFactory keyStoreKeyFactory =
				new KeyStoreKeyFactory(storeFile,this.keystorePassword.toCharArray());
		converter.setKeyPair(keyStoreKeyFactory.getKeyPair(this.keystoreTokenSigningKey));
		
		return converter;
	}

	@Bean
	@Primary
	public DefaultTokenServices tokenServices() {
		DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
		defaultTokenServices.setTokenStore(tokenStore());
		defaultTokenServices.setSupportRefreshToken(true);
		defaultTokenServices.setTokenEnhancer(accessTokenConverter());
		return defaultTokenServices;
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints
		.authenticationManager(this.authenticationManager)
		.tokenServices(tokenServices())
		.tokenStore(tokenStore())
		.accessTokenConverter(accessTokenConverter());
	}

}
