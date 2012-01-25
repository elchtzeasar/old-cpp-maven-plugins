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

import java.util.Collection;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.cpp.compiler.artifacts.executables.ExecutablesMap;
import org.codehaus.mojo.cpp.compiler.files.NativeCodeFile;
import org.codehaus.mojo.cpp.tools.environment.Environment;
import org.codehaus.mojo.cpp.tools.settings.PluginSettingsImpl;


/**
 * Compiles the source into a static library and any specified executables for 
 * each of the listed target environments.
 * 
 * @goal compile
 * @phase compile
 * @threadSafe
 * @requiresDependencyResolution compile
 * @since 0.0.1
 */

public class CompileMojo extends AbstractCompileMojo {
	/**
	 * Map of executables to link.<br/> 
	 * Keys are environment names (similarly to e.g. compilerArguments), 
	 * values are semi-colon separated lists of named executables with 
	 * comma separated lists of source file patterns.<br/>
	 * [] as executable name will link each file into a separate executable.<br/>
	 * <br/>
	 * Example:<br/>
	 * <br/>
	 * &lt;executables&gt;<br/>
	 *   &lt;all&gt;myExec=src/main/cpp/MyExec.cc;myOtherExec=src/main/cpp/MyOtherExec.cc,src/main/cpp/Other*cc&lt;/all&gt;<br/>
	 *   &lt;linux_64&gt;myLinuxExec=src/linux/main/cpp/LinuxMain.cc;[]=src/linux/main/cpp/entry-points/*.cc&lt;/linux_64&gt;<br/>
	 *   [...]<br/>
	 * &lt;/executables>
	 *
	 * @parameter
	 * @since 0.3.4
	 */
	private Map<String, String> executables;	

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		initialize();

		run(false, determineTargetEnvironments());
	}

	@Override
	protected ExecutablesMap createExecutablesMap(final Collection<NativeCodeFile> compiledClasses, final Environment targetEnvironment, final PluginSettingsImpl settings) throws MojoFailureException {
		final ExecutablesMap map = new ExecutablesMap(getLog(), compiledClasses, settings);
		if( executables != null )
			map.parseExecutableMapping(executables, targetEnvironment);
		return map;
	}
}
