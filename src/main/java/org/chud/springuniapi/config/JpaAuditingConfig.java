package org.chud.springuniapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

//enables listener for createdAt and updatedAt instant
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
