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

package org.codehaus.mojo.cpp.compiler.artifacts.executables;

import java.io.File;
import java.util.Collection;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.cpp.compiler.settings.CompilationSettings;
import org.codehaus.mojo.cpp.tools.CliExecutor;
import org.codehaus.mojo.cpp.tools.DirectoryHandler;
import org.codehaus.mojo.cpp.tools.environment.Environment;


public abstract class AbstractGccExecutableBuilder extends AbstractExecutableBuilder {

	public AbstractGccExecutableBuilder(final Log log, final CompilationSettings settings, final Environment targetEnvironment, final Executable executable) {
		super(log, settings, targetEnvironment, executable);
	}

	@Override
	public void buildExecutable(final Collection<File> libsToLink) throws MojoExecutionException, MojoFailureException {
		final DirectoryHandler directoryHandler = new DirectoryHandler(log);
		directoryHandler.create(settings.getBinDirectory(getTargetEnvironment(), settings.isTestCompilation()));

		final CliExecutor executor = new CliExecutor(log);
		executor.initialize(settings.getBinDirectory(getTargetEnvironment(), settings.isTestCompilation()), getLinkerExecutable());
		executor.getCommandline().createArg().setValue("-o");
		executor.getCommandline().createArg().setValue(getExecutable().getName());
		executor.getCommandline().createArg().setLine(getMandatoryLinkerArguments());
		executor.getCommandline().createArg().setValue(getStartGroupArgument());

		executor.appendFiles(getExecutable().getAllFilesToLink());
		executor.appendFiles(libsToLink);

		executor.getCommandline().createArg().setLine(getDefaultLibraries());
		executor.getCommandline().createArg().setValue(getEndGroupArgument());
		executor.getCommandline().createArg().setLine(settings.getLinkerArguments(getTargetEnvironment()));
		executor.execute();
	}

	protected String getStartGroupArgument() {
		return "-Wl,--start-group";
	}

	protected String getEndGroupArgument() {
		return "-Wl,--end-group";
	}

	protected String getDefaultLibraries() throws MojoFailureException, MojoExecutionException {
		return "-lstdc++";
	}

	protected String getLinkerExecutable() {
		return "gcc";
	}
	
	protected String getMandatoryLinkerArguments() {
		return "";
	}
}
