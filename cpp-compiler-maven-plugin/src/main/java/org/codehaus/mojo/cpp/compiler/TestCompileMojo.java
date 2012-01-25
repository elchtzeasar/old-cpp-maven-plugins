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

package org.codehaus.mojo.cpp.compiler;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.cpp.compiler.artifacts.executables.ExecutablesMap;
import org.codehaus.mojo.cpp.compiler.files.NativeCodeFile;
import org.codehaus.mojo.cpp.tools.environment.Environment;
import org.codehaus.mojo.cpp.tools.settings.PluginSettingsImpl;
import org.codehaus.plexus.util.FileUtils;


/**
 * Checks whether the current execution environment is ok. If it isn't the build will fail.
 * Compiles the source into a static library and any specified executables for the current
 * execution environment.
 * 
 * @goal testCompile
 * @phase test-compile
 * @threadSafe
 * @requiresDependencyResolution test
 * @since 0.1.3
 */
public class TestCompileMojo extends AbstractCompileMojo {
	/**
	 * Map of executables to link for test.<br /> 
	 * Keys are environment names (similarly to e.g. compilerArguments), 
	 * values are semi-colon separated lists of named executables with 
	 * comma separated lists of source file patterns.
	 * [] as executable name will link each file into a separate executable.<br />
	 * <br />
	 * Example:<br />
	 * <br />
	 * &lt;testExecutables&gt;<br />
	 *   &lt;all>myExec=src/main/cpp/MyExec.cc;myOtherExec=src/main/cpp/MyOtherExec.cc,src/main/cpp/Other*cc&lt;/all&gt;<br />
	 *   &lt;linux_64>[]=src/linux/test/cpp/Test*.cc;specialTest=src/linux/main/cpp/Special*.cc&lt;/linux_64&gt;<br />
	 *   [...]<br />
	 * &lt;/testExecutables&gt;<br />
	 * <br />
	 * If undefined "src/test/cpp/*.cc" will be linked into "[]".
	 *
	 * @parameter
	 * @since 0.3.4
	 */
	private Map<String, String> testExecutables;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		initialize();

		if(!isExecutionEnvironmentAmongTargetEnvironments(determineTargetEnvironments(), hostEnvironment)) {
			getLog().warn("The current execution environment (" + hostEnvironment.getName() + ") is not among the target environments. Hence test compilation will be skipped, as it will be impossible to link test executables for " + hostEnvironment.getName() + ". The test output directory will be cleaned to avoid test result ambiguities.");
			cleanTestOutput();
		}
		else {
			run(true, hostEnvironment);
		}
	}

	private void cleanTestOutput() throws MojoExecutionException {
		final PluginSettingsImpl settings = new PluginSettingsImpl(project, null, outputDirectory, testOutputDirectory);
		final File testOutputDirectory = settings.getOutputDirectory(true);
		try {
			FileUtils.deleteDirectory(testOutputDirectory);
		} 
		catch (IOException e) {
			throw new MojoExecutionException("Failed to delete test output directory.", e);
		}
	}

	private boolean isExecutionEnvironmentAmongTargetEnvironments(final Environment[] targetEnvironments, final Environment executionEnvironment) {
		for(Environment targetEnvironment : targetEnvironments)
			if( targetEnvironment == executionEnvironment )
				return true;

		return false;
	}

	@Override
	protected ExecutablesMap createExecutablesMap(final Collection<NativeCodeFile> compiledClasses, final Environment targetEnvironment, final PluginSettingsImpl settings) throws MojoFailureException {
		final ExecutablesMap map = new ExecutablesMap(getLog(), compiledClasses, settings);

		if( testExecutables == null )
			map.addExecutable("all", "[]", "src/test/cpp/*" + NativeCodeFile.SOURCE_SUFFIXES[0]);
		else
			map.parseExecutableMapping(testExecutables, targetEnvironment);

		return map;
	}

}
