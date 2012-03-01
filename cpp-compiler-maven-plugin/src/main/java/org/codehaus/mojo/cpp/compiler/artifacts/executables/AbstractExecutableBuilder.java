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
import java.util.ArrayList;
import java.util.Collection;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.cpp.compiler.artifacts.AbstractArtifactBuilder;
import org.codehaus.mojo.cpp.compiler.files.NativeCodeFile;
import org.codehaus.mojo.cpp.compiler.settings.CompilationSettings;
import org.codehaus.mojo.cpp.tools.FileFinder;
import org.codehaus.mojo.cpp.tools.environment.Environment;


public abstract class AbstractExecutableBuilder extends AbstractArtifactBuilder {
	private final Executable executable;

	public AbstractExecutableBuilder(final Log log, final CompilationSettings settings, final Environment targetEnvironment, final Executable executable) {
		super(log, settings, targetEnvironment);
		this.executable = executable;
	}

	@Override
	public void build(Collection<NativeCodeFile> allClasses, Collection<NativeCodeFile> compiledClasses, Collection<Artifact> dependencies) throws MojoExecutionException, MojoFailureException {
		preBuild();
		
		if(executable.getAllFilesToLink().isEmpty()) {
			log.debug(getTargetEnvironment() + ": No objects to link found for executable " + executable + ". Skipping");
			return;
		}

		final Collection<File> libsToLink = findLibsToLink();
		
		if( isBinaryUpToDate(libsToLink) ) {
			log.debug(getTargetEnvironment() + ": Executable " + executable + " is up to date.");
			return;
		}

		buildExecutable(libsToLink);

		postBuild();
		log.info(getTargetEnvironment() + ": " + executable + " built.");
	}

	private boolean isBinaryUpToDate(final Collection<File> libsToLink) {
		final File preExistingExecutableFile = new File(settings.getBinDirectory(getTargetEnvironment(), settings.isTestCompilation()), executable.getName());
		if( !preExistingExecutableFile.exists() )
			return false;

		if( collectionContainsUpdatedFile(preExistingExecutableFile, executable.getAllFilesToLink()) )
			return false;
		
		if( collectionContainsUpdatedFile(preExistingExecutableFile, libsToLink) )
			return false;
		
		return true;
	}

	private boolean collectionContainsUpdatedFile(final File reference, final Collection<File> collection) {
		for(File file : collection) {
			if( file.lastModified() > reference.lastModified() ) {
				log.debug(getTargetEnvironment() + ": " + file.getName() + " has been updated more recently than " + reference.getName());
				return true;
			}
		}
		
		return false;
	}

	private Collection<File> findLibsToLink() {
		final Collection<File> libs = findLibsToLinkForScope("compile");

		if( settings.isTestCompilation() )
			libs.addAll(findLibsToLinkForScope("test"));

		return libs;
	}

	private Collection<File> findLibsToLinkForScope(final String scope) {
		final String staticPattern =  "lib/*.a";
		final String dynamicPattern =  "lib/*.so";
		final Collection<File> libs = new ArrayList<File>();
		
		final Collection<File> dependencyDirectories = settings.getDependencyDirectories(scope, getTargetEnvironment());
		for (final File dependency : dependencyDirectories) {
			libs.addAll(new FileFinder(dependency, staticPattern).getFiles());
			libs.addAll(new FileFinder(dependency, dynamicPattern).getFiles());
		}

		log.debug(getTargetEnvironment() + ": " + libs.size() + " libs matching static pattern \"" + staticPattern + "\", and dynamic pattern \"" + dynamicPattern + "\" found for scope " + scope + " in " + dependencyDirectories.size() + " dependency directories.");
		libs.addAll(new FileFinder(settings.getLibDirectory(getTargetEnvironment(), scope.equals("test")), "*.a").getFiles());
		libs.addAll(new FileFinder(settings.getLibDirectory(getTargetEnvironment(), scope.equals("test")), "*.so").getFiles());

		return libs;
	}
	
	protected void preBuild() throws MojoExecutionException, MojoFailureException {
	}
	
	protected void postBuild() throws MojoExecutionException, MojoFailureException {
	}
	
	protected abstract void buildExecutable(final Collection<File> libsToLink) throws MojoExecutionException, MojoFailureException;

	protected Executable getExecutable() {
		return executable;
	}
}
