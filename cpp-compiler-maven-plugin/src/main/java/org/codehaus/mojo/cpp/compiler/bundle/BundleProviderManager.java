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

package org.codehaus.mojo.cpp.compiler.bundle;

import java.util.Collection;
import java.util.Iterator;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.cpp.compiler.artifacts.AbstractArtifactBuilder;
import org.codehaus.mojo.cpp.compiler.artifacts.executables.Executable;
import org.codehaus.mojo.cpp.compiler.compilation.AbstractCompiler;
import org.codehaus.mojo.cpp.compiler.dependencies.DependencyExtractor;
import org.codehaus.mojo.cpp.compiler.settings.CompilationSettings;
import org.codehaus.mojo.cpp.tools.bundle.BundleLoader;
import org.codehaus.mojo.cpp.tools.environment.Environment;


public class BundleProviderManager implements Iterable<BundleProvider> {
	private final Collection<BundleProvider> providers;
	private final Log log;

	public BundleProviderManager(BundleLoader loader, Log log) throws MojoExecutionException {
		this.providers = loader.activate("BundleProvider", BundleProvider.class);
		this.log = log;
	}

	@Override
	public Iterator<BundleProvider> iterator() {
		return providers.iterator();
	}
	
	public Environment determineHostEnvironment() {
		Environment e = null;
		log.debug("Found " + providers.size() + " bundle providers.");
		for (BundleProvider provider : providers) {
			Environment potential = provider.determineHostEnvironment(System.getProperties());
			if (potential != null) {
				if (e == null)
					e = potential;
				else {
					log.warn(potential + " reported as possible host environment, but " + e + " had previously been reported.");
					log.warn("You appear to have conflicting bundles in your configuration.");
				}
			}
		}
		return e;
	}

	public AbstractCompiler selectCompiler(Environment host, Environment target, CompilationSettings settings) {
		AbstractCompiler ac = null;
		for (BundleProvider provider : providers) {
			AbstractCompiler potential = provider.selectCompiler(host, target, settings);
			if (potential != null) {
				if (ac == null)
					ac = potential;
				else
					log.warn("Multiple potential compilers available for building " + target + " on " + host);
			}
		}
		return ac;
	}

	public AbstractArtifactBuilder selectBuilder(Environment host, Environment target, CompilationSettings settings, Executable executable, DependencyExtractor extractor) {
		AbstractArtifactBuilder aab = null;
		for (BundleProvider provider : providers) {
			AbstractArtifactBuilder potential = provider.selectBuilder(host, target, settings, executable, extractor);
			if (potential != null) {
				if (aab == null)
					aab = potential;
				else
					log.warn("Multiple potential artifact builders available for building " + target + " on " + host);
			}
		}
		return aab;
	}
}
