package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.web.flow.OAuth20LogoutAction;
import org.apereo.cas.support.oauth.web.flow.OAuth20WebflowConfigurer;
import org.apereo.cas.support.oauth.web.flow.OAuth20RegisteredServiceUIAction;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.pac4j.core.config.Config;
import org.pac4j.springframework.web.ApplicationLogoutController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasOAuthWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casOAuthWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuthWebflowConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("logoutFlowRegistry")
    private FlowDefinitionRegistry logoutFlowDefinitionRegistry;

    @Autowired
    @Qualifier("oauthSecConfig")
    private Config oauthSecConfig;

    @Autowired
    @Qualifier("oauth20AuthenticationRequestServiceSelectionStrategy")
    private AuthenticationRequestServiceSelectionStrategy oauth20AuthenticationRequestServiceSelectionStrategy;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @ConditionalOnMissingBean(name = "oauth20LogoutWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer oauth20LogoutWebflowConfigurer() {
        final OAuth20WebflowConfigurer c = new OAuth20WebflowConfigurer();
        c.setFlowBuilderServices(this.flowBuilderServices);
        c.setLoginFlowDefinitionRegistry(this.loginFlowDefinitionRegistry);
        c.setLogoutFlowDefinitionRegistry(this.logoutFlowDefinitionRegistry);
        c.setOauth20LogoutAction(oauth20LogoutAction());
        c.setOauth20RegisteredServiceUIAction(oauth20RegisteredServiceUIAction());
        return c;
    }

    @Bean
    public Action oauth20LogoutAction() {
        final ApplicationLogoutController controller = new ApplicationLogoutController();
        controller.setConfig(oauthSecConfig);
        final OAuth20LogoutAction action = new OAuth20LogoutAction();
        action.setApplicationLogoutController(controller);
        return action;
    }

    @ConditionalOnMissingBean(name = "oauth20RegisteredServiceUIAction")
    @Bean
    public Action oauth20RegisteredServiceUIAction() {
        return new OAuth20RegisteredServiceUIAction(this.servicesManager, oauth20AuthenticationRequestServiceSelectionStrategy);
    }
}
