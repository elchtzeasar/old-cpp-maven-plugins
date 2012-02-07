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
import java.util.ArrayList;
import java.util.Collection;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.cpp.compiler.artifacts.AbstractArtifactBuilder;
import org.codehaus.mojo.cpp.compiler.artifacts.StaticLibraryBuilder;
import org.codehaus.mojo.cpp.compiler.artifacts.executables.Executable;
import org.codehaus.mojo.cpp.compiler.artifacts.executables.ExecutablesMap;
import org.codehaus.mojo.cpp.compiler.bundle.BundleProviderManager;
import org.codehaus.mojo.cpp.compiler.compilation.AbstractCompiler;
import org.codehaus.mojo.cpp.compiler.compilation.CompilationOverseer;
import org.codehaus.mojo.cpp.compiler.dependencies.DependencyExtractor;
import org.codehaus.mojo.cpp.compiler.dependencies.DependencyType;
import org.codehaus.mojo.cpp.compiler.files.NativeCodeFile;
import org.codehaus.mojo.cpp.compiler.settings.CompilerPluginSettings;
import org.codehaus.mojo.cpp.tools.FileFinder;
import org.codehaus.mojo.cpp.tools.environment.Environment;


public class TargetManager {
	private final Log log;
	private final CompilerPluginSettings settings;
	private final Environment targetEnvironment;
	private final Collection<NativeCodeFile> compiledClasses = new ArrayList<NativeCodeFile>();
	private Collection<NativeCodeFile> allClasses;
	private Collection<AbstractArtifactBuilder> artifactBuilders;
	private CompilationOverseer compilationOverseer;
	private final DependencyExtractor dependencyExtractor;
	private final BundleProviderManager bundles;
	private final Environment hostEnvironment;

	public TargetManager(final Log log, final CompilerPluginSettings settings, Environment hostEnvironment, final Environment targetEnvironment, final DependencyExtractor dependencyExtractor, BundleProviderManager bundles) { 
		this.log = log;
		this.settings = settings;
		this.hostEnvironment = hostEnvironment;
		this.targetEnvironment = targetEnvironment;
		this.dependencyExtractor = dependencyExtractor;
		this.bundles = bundles;
	}

	public void compile() throws MojoFailureException, MojoExecutionException {
		if( getAllClasses().isEmpty() ) {
			log.debug(getTargetEnvironment() + ": No native code files to compile. Skipping compilation.");
			return;
		}
		
		dependencyExtractor.secureAvailabilityOfExtractedDependencies(DependencyType.INCLUDES, getTargetEnvironment());
		compiledClasses.addAll(getCompilationOverseer().compile());
	}

	public void buildArtifacts(final ExecutablesMap executables, final Collection<Artifact> dependencies) throws MojoExecutionException, MojoFailureException {
		final Collection<AbstractArtifactBuilder> artifactBuilders = getArtifactBuilders(executables);
		
		dependencyExtractor.secureAvailabilityOfExtractedDependencies(DependencyType.LIBS, getTargetEnvironment());
		
		for(AbstractArtifactBuilder builder : artifactBuilders)
			builder.build(getAllClasses(), compiledClasses, dependencies);

	}

	public Environment getTargetEnvironment() {
		return targetEnvironment;
	}
	
	public Collection<NativeCodeFile> getAllClasses() {
		if( allClasses == null )
			allClasses = findAllCodeFiles();

		return allClasses;
	}
	
	private Collection<AbstractArtifactBuilder> getArtifactBuilders(final ExecutablesMap executables) throws MojoExecutionException, MojoFailureException {
		if( artifactBuilders == null )
			artifactBuilders = createArtifactBuilders(executables);
		
		return artifactBuilders;
	}
	
	private Collection<AbstractArtifactBuilder> createArtifactBuilders(final ExecutablesMap executables) throws MojoExecutionException, MojoFailureException {
		Collection<AbstractArtifactBuilder> builders = new ArrayList<AbstractArtifactBuilder>();

		builders.add(new StaticLibraryBuilder(log, settings, targetEnvironment));

		for(Executable executable : executables.getAllExecutables(targetEnvironment)) {
			builders.add(createExecutableBuilder(executable));
		}
		
		return builders;
	}

	private CompilationOverseer getCompilationOverseer() throws MojoExecutionException {
		if( compilationOverseer == null )
			compilationOverseer = createCompilationOverseer();

		return compilationOverseer;
	}

	private AbstractCompiler createCompiler() throws MojoExecutionException {
		AbstractCompiler compiler = bundles.selectCompiler(hostEnvironment, targetEnvironment, settings);
		if (compiler == null)
			throw new MojoExecutionException("Don't know of any compiler for target environment " + targetEnvironment.getName() + " compatible with current host environment (" + hostEnvironment.getName() + "). See debug printouts for details of the compatibility check.");
		return compiler;
	}
	
	private CompilationOverseer createCompilationOverseer() throws MojoExecutionException {
		return new CompilationOverseer(settings, log, getAllClasses(), createCompiler());
	}
	
	private AbstractArtifactBuilder createExecutableBuilder(final Executable executable) throws MojoExecutionException, MojoFailureException {
		AbstractArtifactBuilder builder = bundles.selectBuilder(hostEnvironment, targetEnvironment, settings, executable, dependencyExtractor);
		if (builder == null)
			throw new MojoExecutionException("Don't know of any builder for target environment " + targetEnvironment.getName() + " compatible with current host environment (" + hostEnvironment.getName() + "). See debug printouts for details of the compatibility check.");
		return builder;
	}
	
	private Collection<NativeCodeFile> findAllCodeFiles() {
		Collection<NativeCodeFile> list = new ArrayList<NativeCodeFile>();
		collectNativeCodeFiles(settings.getCodeDirectory(null, settings.isTestCompilation()), list);
		collectNativeCodeFiles(settings.getCodeDirectory(targetEnvironment, settings.isTestCompilation()), list);
		return list;
	}

	private void collectNativeCodeFiles(final File sourceDirectory, final Collection<NativeCodeFile> fileList) {
		if( !sourceDirectory.exists() ) {
			log.debug("Source directory " + sourceDirectory + " doesn't exist.");
		}

		for(String suffix : NativeCodeFile.SOURCE_SUFFIXES)
			for(String fileName : new FileFinder(sourceDirectory, "**/*" + suffix).getFilenames())
				fileList.add(new NativeCodeFile(fileName, sourceDirectory, settings.getObjDirectory(getTargetEnvironment(), settings.isTestCompilation())));

		if( fileList.isEmpty() )
			log.debug("Found no classes in " + sourceDirectory);
	}
}
