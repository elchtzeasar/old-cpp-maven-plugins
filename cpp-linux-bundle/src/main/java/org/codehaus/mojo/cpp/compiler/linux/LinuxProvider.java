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

package org.codehaus.mojo.cpp.compiler.linux;

import java.util.Properties;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.cpp.compiler.artifacts.AbstractArtifactBuilder;
import org.codehaus.mojo.cpp.compiler.artifacts.executables.Executable;
import org.codehaus.mojo.cpp.compiler.bundle.BundleProvider;
import org.codehaus.mojo.cpp.compiler.compilation.AbstractCompiler;
import org.codehaus.mojo.cpp.compiler.compilation.gcc.GccIncludesAnalyzer;
import org.codehaus.mojo.cpp.compiler.compilation.gcc.GccRecompilationJudge;
import org.codehaus.mojo.cpp.compiler.dependencies.DependencyExtractor;
import org.codehaus.mojo.cpp.compiler.linux.builder.Linux32ExecutableBuilder;
import org.codehaus.mojo.cpp.compiler.linux.builder.Linux64ExecutableBuilder;
import org.codehaus.mojo.cpp.compiler.linux.compiler.Linux32Compiler;
import org.codehaus.mojo.cpp.compiler.linux.compiler.Linux64Compiler;
import org.codehaus.mojo.cpp.compiler.settings.CompilationSettings;
import org.codehaus.mojo.cpp.tools.environment.Environment;
import org.codehaus.mojo.cpp.tools.environment.EnvironmentManager;


public class LinuxProvider implements BundleProvider {

	private final Log log;
	private final CompatibilityChecker compatibilityChecker;

	public LinuxProvider(EnvironmentManager em, Log log) {
		this.log = log;
		this.compatibilityChecker = new CompatibilityChecker(log);
		
		log.debug("Loaded Linux provider");
	}

	@Override
	public AbstractCompiler selectCompiler(Environment host, Environment target, CompilationSettings settings) {
		if(!compatibilityChecker.supported(getClass().getName(), host, target))
			return null;
		
		if (target.equals(EnvironmentManager.LINUX_32))
			return new Linux32Compiler(log, settings, target, new GccRecompilationJudge(log, new GccIncludesAnalyzer()));
		
		if (target.equals(EnvironmentManager.LINUX_64))
			return new Linux64Compiler(log, settings, target, new GccRecompilationJudge(log, new GccIncludesAnalyzer()));

		log.warn(getClass().getSimpleName() + " failed to find a compiler for target " + target.getName() + ", even though it's supposedly supported!");
		return null;
	}

	@Override
	public AbstractArtifactBuilder selectBuilder(final Environment host, final Environment target, final CompilationSettings settings, Executable executable, final DependencyExtractor extractor) {
		if(!compatibilityChecker.supported(getClass().getName(), host, target))
			return null;
		
		if (target.equals(EnvironmentManager.LINUX_32))
			return new Linux32ExecutableBuilder(log, settings, target, executable, extractor);
		
		if (target.equals(EnvironmentManager.LINUX_64))
			return new Linux64ExecutableBuilder(log, settings, target, executable, extractor);

		log.warn(getClass().getSimpleName() + " failed to find a builder for target " + target.getName() + ", even though it's supposedly supported!");
		return null;
	}
	
	@Override
	public Environment determineHostEnvironment(Properties systemProperties) {
		String osArch = systemProperties.getProperty("os.arch","");
		String osName = systemProperties.getProperty("os.name","");
		
		if (osName.toLowerCase().contains("linux")) {
			if (osArch.equalsIgnoreCase("i386")
				|| osArch.equalsIgnoreCase("i686")
				|| osArch.equalsIgnoreCase("x86"))
				return EnvironmentManager.LINUX_32;
			else if (osArch.equalsIgnoreCase("amd64")
				|| osArch.equalsIgnoreCase("x86_64"))
				return EnvironmentManager.LINUX_64;
		}
		
		return null;
	}
}
