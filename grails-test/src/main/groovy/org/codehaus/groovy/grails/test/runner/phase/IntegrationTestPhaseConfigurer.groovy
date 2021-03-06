/*
 * Copyright 2013 SpringSource
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.test.runner.phase

import grails.util.Holders
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.cli.support.MetaClassRegistryCleaner
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.spring.GrailsWebApplicationContext
import org.codehaus.groovy.grails.compiler.GrailsProjectWatcher
import org.codehaus.groovy.grails.plugins.GrailsPluginManager
import org.codehaus.groovy.grails.project.loader.GrailsProjectLoader
import org.codehaus.groovy.grails.support.PersistenceContextInterceptorExecutor
import org.codehaus.groovy.grails.test.GrailsTestType
import org.codehaus.groovy.grails.test.runner.GrailsProjectTestCompiler
import org.codehaus.groovy.grails.web.context.GrailsConfigUtils

/**
 * Test phase configurer for the integration test phase
 *
 * @author Graeme Rocher
 * @since 2.3
 */
@CompileStatic
class IntegrationTestPhaseConfigurer extends DefaultTestPhaseConfigurer{

    static GrailsWebApplicationContext currentApplicationContext

    protected MetaClassRegistryCleaner registryCleaner
    protected GrailsProjectTestCompiler projectTestCompiler
    protected GrailsProjectLoader projectLoader

    protected GrailsWebApplicationContext appCtx
    protected GrailsProjectWatcher projectWatcher

    IntegrationTestPhaseConfigurer(GrailsProjectTestCompiler projectTestCompiler, GrailsProjectLoader projectLoader) {
        this.projectTestCompiler = projectTestCompiler
        this.projectLoader = projectLoader
    }

    @Override
    void prepare(Binding testExecutionContext, Map<String, Object> testOptions) {
        registryCleaner = org.codehaus.groovy.grails.cli.support.MetaClassRegistryCleaner.createAndRegister()
        GroovySystem.metaClassRegistry.addMetaClassRegistryChangeEventListener(registryCleaner)
        projectTestCompiler.packageTests()
        appCtx = (GrailsWebApplicationContext)projectLoader.configureApplication()
        currentApplicationContext = appCtx

        Holders.servletContext = appCtx.servletContext
        // Get the Grails application instance created by the bootstrap process.
        def app = appCtx.getBean(GrailsApplication.APPLICATION_ID, GrailsApplication)
        final pluginManager = appCtx.getBean(GrailsPluginManager)

        if (app.parentContext == null) {
            app.applicationContext = appCtx
        }

        if (projectWatcher) {
            projectWatcher.pluginManager = pluginManager
        }

        PersistenceContextInterceptorExecutor.initPersistenceContext(appCtx)


        GrailsConfigUtils.configureServletContextAttributes(appCtx.servletContext, app, pluginManager, appCtx)
        GrailsConfigUtils.executeGrailsBootstraps(app, appCtx, appCtx.servletContext)
    }

    @Override
    void cleanup(Binding testExecutionContext, Map<String, Object> testOptions) {
        currentApplicationContext = null
        PersistenceContextInterceptorExecutor.destroyPersistenceContext(appCtx)
        appCtx?.close()
        registryCleaner.clean()
        GroovySystem.metaClassRegistry.removeMetaClassRegistryChangeEventListener(registryCleaner)
        Holders.clear()
    }
}
