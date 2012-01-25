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

package org.codehaus.mojo.cpp.compiler.artifacts;

import java.util.Collection;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.cpp.compiler.files.NativeCodeFile;
import org.codehaus.mojo.cpp.compiler.settings.CompilationSettings;
import org.codehaus.mojo.cpp.tools.environment.Environment;


public abstract class AbstractArtifactBuilder {
	protected final CompilationSettings settings;
	protected final Log log;
	private final Environment targetEnvironment;

	public AbstractArtifactBuilder(final Log log, final CompilationSettings settings, final Environment targetEnvironment) {
		this.log = log;
		this.settings = settings;
		this.targetEnvironment = targetEnvironment;
	}

	public abstract void build(final Collection<NativeCodeFile> allClasses, final Collection<NativeCodeFile> compiledClasses, final Collection<Artifact> dependencies) throws MojoExecutionException, MojoFailureException;

	public Environment getTargetEnvironment() {
		return targetEnvironment;
	}
}
