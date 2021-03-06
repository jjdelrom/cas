package org.apereo.cas.adaptors.ldap.services;

import com.google.common.base.Throwables;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.util.services.RegisteredServiceJsonSerializer;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Default implementation of {@link LdapRegisteredServiceMapper} that is able
 * to map ldap entries to {@link RegisteredService} instances based on
 * certain attributes names. This implementation also respects the object class
 * attribute of LDAP entries via {@link LdapUtils#OBJECTCLASS_ATTRIBUTE}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class DefaultLdapRegisteredServiceMapper implements LdapRegisteredServiceMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLdapRegisteredServiceMapper.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    private StringSerializer<RegisteredService> jsonSerializer = new RegisteredServiceJsonSerializer();

    @Override
    public LdapEntry mapFromRegisteredService(final String dn, final RegisteredService svc) {
        try {
            if (svc.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
                ((AbstractRegisteredService) svc).setId(System.nanoTime());
            }
            final String newDn = getDnForRegisteredService(dn, svc);
            LOGGER.debug("Creating entry {}", newDn);

            
            final Collection<LdapAttribute> attrs = new ArrayList<>();
            attrs.add(new LdapAttribute(casProperties.getServiceRegistry().getLdap().getIdAttribute(),
                    String.valueOf(svc.getId())));

            final StringWriter writer = new StringWriter();
            this.jsonSerializer.to(writer, svc);
            attrs.add(new LdapAttribute(casProperties.getServiceRegistry()
                    .getLdap().getServiceDefinitionAttribute(), writer.toString()));
            attrs.add(new LdapAttribute(LdapUtils.OBJECTCLASS_ATTRIBUTE, "top", 
                    casProperties.getServiceRegistry().getLdap().getObjectClass()));

            return new LdapEntry(newDn, attrs);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public RegisteredService mapToRegisteredService(final LdapEntry entry) {
        try {
            final String value = LdapUtils.getString(entry, 
                    casProperties.getServiceRegistry().getLdap().getServiceDefinitionAttribute());
            if (StringUtils.hasText(value)) {
                final RegisteredService service = this.jsonSerializer.from(value);
                return service;
            }

            return null;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public String getObjectClass() {
        return casProperties.getServiceRegistry().getLdap().getObjectClass();
    }

    @Override
    public String getIdAttribute() {
        return casProperties.getServiceRegistry().getLdap().getIdAttribute();
    }


    public void setJsonSerializer(final StringSerializer<RegisteredService> jsonSerializer) {
        this.jsonSerializer = jsonSerializer;
    }

    @Override
    public String getDnForRegisteredService(final String parentDn, final RegisteredService svc) {
        return String.format("%s=%s,%s", 
                casProperties.getServiceRegistry().getLdap().getIdAttribute(), 
                svc.getId(), parentDn);
    }

}
