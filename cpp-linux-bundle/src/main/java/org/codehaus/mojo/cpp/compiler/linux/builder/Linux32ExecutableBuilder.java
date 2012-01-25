/* 
 *  Copyright 2011 Ericsson AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.codehaus.mojo.cpp.compiler.linux.builder;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.cpp.compiler.artifacts.executables.AbstractGccExecutableBuilder;
import org.codehaus.mojo.cpp.compiler.artifacts.executables.Executable;
import org.codehaus.mojo.cpp.compiler.dependencies.DependencyExtractor;
import org.codehaus.mojo.cpp.compiler.settings.CompilationSettings;
import org.codehaus.mojo.cpp.tools.environment.Environment;


public class Linux32ExecutableBuilder extends AbstractGccExecutableBuilder {

	public Linux32ExecutableBuilder(final Log log, final CompilationSettings settings, final Environment targetEnvironment, final Executable executable, final DependencyExtractor extractor) {
		super(log, settings, targetEnvironment, executable);
	}
	
	@Override
	protected String getMandatoryLinkerArguments() {
		return super.getMandatoryLinkerArguments() + " -m elf_i386";
	}
}
