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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.cpp.compiler.files.NativeCodeFile;
import org.codehaus.mojo.cpp.compiler.settings.CompilationSettings;
import org.codehaus.mojo.cpp.tools.CliExecutor;
import org.codehaus.mojo.cpp.tools.DirectoryHandler;
import org.codehaus.mojo.cpp.tools.DirectoryHandler.OverwriteStyle;
import org.codehaus.mojo.cpp.tools.environment.Environment;


public class StaticLibraryBuilder extends AbstractArtifactBuilder {
	private static final int ARCHIVING_BATCH_SIZE = 250;

	public StaticLibraryBuilder(final Log log, final CompilationSettings settings, final Environment targetEnvironment) {
		super(log, settings, targetEnvironment);
	}

	@Override
	public void build(final Collection<NativeCodeFile> allClasses, final Collection<NativeCodeFile> compiledClasses, final Collection<Artifact> dependencies) throws MojoExecutionException, MojoFailureException {
		new DirectoryHandler(log).create(settings.getLibDirectory(getTargetEnvironment(), settings.isTestCompilation()));

		createStaticLibrary(allClasses, compiledClasses);
		publishHeaderFiles(null);
		publishHeaderFiles(getTargetEnvironment());
		publishPreExistingLibs(getTargetEnvironment());
	}

	private void createStaticLibrary(final Collection<NativeCodeFile> allClasses, final Collection<NativeCodeFile> compiledClasses) throws MojoExecutionException, MojoFailureException {
		final File libFile = new File(settings.getLibDirectory(getTargetEnvironment(), settings.isTestCompilation()), "lib" + settings.getProject().getArtifactId() + ".a");
		final Collection<NativeCodeFile> classesToArchive = determineClassesToArchive(libFile.exists(), allClasses, compiledClasses);

		if( classesToArchive.isEmpty() ) {
			log.debug("No files to archive - static library will not be updated.");
			return;
		}
		
		for(NativeCodeFile[] batch : createClassBatches(classesToArchive)) 
			archiveBatch(libFile, batch);

		log.info(getTargetEnvironment() + ": " + classesToArchive.size() + " files archived.");
	}

	private NativeCodeFile[][] createClassBatches(final Collection<NativeCodeFile> classes) {
		final NativeCodeFile[] allClasses = classes.toArray(new NativeCodeFile[classes.size()]);
		final int numberOfBatches = (allClasses.length + ARCHIVING_BATCH_SIZE - 1) / ARCHIVING_BATCH_SIZE;
		final NativeCodeFile[][] batches = new NativeCodeFile[numberOfBatches][];

		for(int i = 0; i < numberOfBatches; i++) {
			int start = i * ARCHIVING_BATCH_SIZE;
			int stop = Math.min(start + ARCHIVING_BATCH_SIZE, allClasses.length);

			batches[i] = Arrays.copyOfRange(allClasses, start, stop);
		}

		log.debug("Split classes to archive into " + batches.length + " batches of up to " + ARCHIVING_BATCH_SIZE + " each.");

		return batches;
	}

	private void archiveBatch(final File libFile, final NativeCodeFile[] batch) throws MojoFailureException, MojoExecutionException {
		CliExecutor executor = new CliExecutor(log);
		executor.initialize(libFile.getParentFile(), "ar");
		executor.getCommandline().createArg().setValue("rc");
		executor.getCommandline().createArg().setValue(libFile.getName());

		for(NativeCodeFile classToArchive : batch)
			executor.getCommandline().createArg().setValue(classToArchive.getObjectFile().getPath());

		executor.execute();

		log.debug("Archived batch of " + batch.length + " files.");
	}

	private Collection<NativeCodeFile> determineClassesToArchive(boolean libFileExists, Collection<NativeCodeFile> allClasses, Collection<NativeCodeFile> compiledClasses) {
		if( libFileExists )
			return compiledClasses;
		else
			return allClasses;
	}

	private void publishPreExistingLibs(Environment environment) throws MojoFailureException, MojoExecutionException {
		final File sourceDirectory = settings.getPreExistingLibDirectory(environment, settings.isTestCompilation());
		final File destinationDirectory = settings.getLibDirectory(environment, settings.isTestCompilation());
		log.debug("Copying " + sourceDirectory + " to " + destinationDirectory);
		new DirectoryHandler(log).copyRecursively(sourceDirectory, destinationDirectory, OverwriteStyle.OVERWRITE_IF_NEWER);
	}

	private void publishHeaderFiles(final Environment environment) throws MojoFailureException, MojoExecutionException {
		final File sourceDirectory = settings.getIncludeDirectory(environment, settings.isTestCompilation());
		final File destinationDirectory = settings.getOutputHeaderDirectory(environment, settings.isTestCompilation());
		log.debug("Copying " + sourceDirectory + " to " + destinationDirectory);
		new DirectoryHandler(log).copyRecursively(sourceDirectory, destinationDirectory, OverwriteStyle.OVERWRITE_IF_NEWER);
	}
}
