package com.karchitecture.global.config

import com.karchitecture.global.config.filter.CorsCustomFilter
import jakarta.servlet.Filter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig : WebMvcConfigurer {

    @Bean
    fun corsFilter(): FilterRegistrationBean<Filter> {
        val filterRegistrationBean = FilterRegistrationBean<Filter>()
        filterRegistrationBean.filter = CorsCustomFilter()
        filterRegistrationBean.order = 1
        filterRegistrationBean.addUrlPatterns("/*")
        return filterRegistrationBean
    }
}