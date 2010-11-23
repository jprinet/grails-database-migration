/* Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
package grails.plugin.databasemigration

import org.apache.log4j.Logger

import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.DatabaseChangeLog
import liquibase.exception.ChangeLogParseException
import liquibase.parser.ChangeLogParser
import liquibase.resource.ResourceAccessor

/**
 * Loads a DSL script and invokes the builder. Registered in DatabaseMigrationGrailsPlugin.doWithApplicationContext().
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class GrailsChangeLogParser implements ChangeLogParser {

	private Logger log = Logger.getLogger(getClass())

	/**
	 * {@inheritDoc}
	 * @see liquibase.parser.ChangeLogParser#parse(java.lang.String, liquibase.changelog.ChangeLogParameters,
	 * 	liquibase.resource.ResourceAccessor)
	 */
	DatabaseChangeLog parse(String physicalChangeLogLocation,
			ChangeLogParameters changeLogParameters,
			ResourceAccessor resourceAccessor) throws ChangeLogParseException {

		log.debug "parsing $physicalChangeLogLocation"

		def bindingVars = [:]

		def inputStream = resourceAccessor.getResourceAsStream(physicalChangeLogLocation)
		Script script = new GroovyShell(Thread.currentThread().contextClassLoader,
			new Binding(bindingVars)).parse(inputStream.text)
		script.run()

		def builder = new DslBuilder(changeLogParameters, resourceAccessor, physicalChangeLogLocation)

		def root = script.databaseChangeLog
		root.delegate = builder
		root()

		builder.databaseChangeLog
	}

	boolean supports(String changeLogFile, ResourceAccessor ra) { changeLogFile.endsWith 'groovy' }

	int getPriority() { PRIORITY_DEFAULT }
}
