/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.hlth.mohums.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Implement audit logging
 * @author greg.perkins
 */
@Configuration
public class AuditLoggingFilterConfig {

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
       return new AuditLoggingFilter();
    }
}