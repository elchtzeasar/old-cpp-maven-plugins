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

package org.codehaus.mojo.cpp.car;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.DefaultMavenProjectHelper;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.cpp.car.settings.ArchivingSettings;
import org.codehaus.mojo.cpp.car.settings.ArchivingSettingsImpl;
import org.codehaus.plexus.archiver.ArchiverException;

/**
 * Packages the contents of the output directory into a C/C++ archive.
 *  
 * @goal car
 * @phase package
 * @threadSafe
 * @since 0.0.1
 */
public class CarMojo extends AbstractMojo {

	/**
	 * The Maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 * @since 0.0.1
	 */
	private MavenProject project;

	/**
	 * The output directory
	 *
	 * @parameter default-value="${project.build.outputDirectory}"
	 * @required
	 */
	protected File outputDirectory;	

	/**
	 * The test output directory
	 *
	 * @parameter default-value="${project.build.testOutputDirectory}"
	 * @required
	 */
	protected File testOutputDirectory;

	/**
	 * Any classifier to attach to the artifact
	 * 
	 * @parameter 
	 * @since 0.1.1
	 */
	private String classifier;

	/**
	 * The directory to archive, if other than the standard output directory
	 * 
	 * @parameter 
	 * @since 0.1.1
	 */
	private File directory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		ArchivingSettings settings = new ArchivingSettingsImpl(project, outputDirectory, testOutputDirectory);

		if( directory == null || classifier == null )
			createDefaultArtifacts(settings);
		else
			createCustomArtifacts(directory, classifier);
	}

	private void createCustomArtifacts(final File directory, final String classifier) throws MojoExecutionException {
		attach(createArchive(directory, getArchiveName(classifier)), classifier);

	}

	private void createDefaultArtifacts(final ArchivingSettings settings) throws MojoExecutionException {
		createMainArtifact(settings);
	}


	private void createMainArtifact(final ArchivingSettings settings) throws MojoExecutionException {
		project.getArtifact().setFile(createArchive(settings.getOutputDirectory(), getArchiveName(null)));
	}

	private File createArchive(final File archiveDirectory, final String archiveName) throws MojoExecutionException {
		try {
			final CarArchiver archiver = new CarArchiver();
			final File archive = new File(project.getBuild().getDirectory(), archiveName);
			archiver.addDirectory(archiveDirectory);
			archiver.setDestFile(archive);
			archiver.createArchive();
			return archive;
		} 
		catch (ArchiverException e) {
			throw new MojoExecutionException("Could not create archive.", e );
		} 
		catch (IOException e) {
			throw new MojoExecutionException("Could not create archive.", e );
		}
	}

	private String getArchiveName(final String classifier) {
		String archiveName = project.getArtifactId() + "-" + project.getVersion();
		if( classifier != null )
			archiveName += "-" + classifier;
		archiveName += ".car";
		return archiveName;
	}

	private void attach(final File archiveFile, final String classifier) {
		new DefaultMavenProjectHelper().attachArtifact(project, archiveFile, classifier);
	}
}
