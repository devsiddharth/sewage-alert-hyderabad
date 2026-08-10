package com.sewagealert.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FieldOfficerPropertiesTest: Verifies the {@code app.field-officers.accounts} list defined
 * in application.yml binds correctly to {@link FieldOfficerProperties}. Guards against a
 * regression where the seed configuration would silently bind to zero officers.
 */
class FieldOfficerPropertiesTest {

    @Test
    void bindsThreeFieldOfficerAccountsFromApplicationYml() throws IOException {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        List<PropertySource<?>> sources = loader.load("application", new ClassPathResource("application.yml"));

        Binder binder = new Binder(ConfigurationPropertySources.from(sources));
        FieldOfficerProperties properties =
                binder.bind("app.field-officers", Bindable.of(FieldOfficerProperties.class)).get();

        assertThat(properties.getAccounts()).hasSize(3);
        assertThat(properties.getAccounts())
                .extracting(FieldOfficerProperties.Officer::getEmail)
                .containsExactly(
                        "ravi.officer@sewagealert.com",
                        "suresh.officer@sewagealert.com",
                        "anil.officer@sewagealert.com");
    }

    @Test
    void eachOfficerAccountHasNameEmailAndPassword() throws IOException {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        List<PropertySource<?>> sources = loader.load("application", new ClassPathResource("application.yml"));

        Binder binder = new Binder(ConfigurationPropertySources.from(sources));
        FieldOfficerProperties properties =
                binder.bind("app.field-officers", Bindable.of(FieldOfficerProperties.class)).get();

        for (FieldOfficerProperties.Officer officer : properties.getAccounts()) {
            assertThat(officer.getName()).isNotBlank();
            assertThat(officer.getEmail()).contains("@");
            assertThat(officer.getPassword()).isNotBlank();
        }
    }
}
