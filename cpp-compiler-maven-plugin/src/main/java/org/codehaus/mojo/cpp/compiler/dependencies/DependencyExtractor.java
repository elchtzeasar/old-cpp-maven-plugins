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

package org.codehaus.mojo.cpp.compiler.dependencies;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.cpp.compiler.artifacts.ArtifactManager;
import org.codehaus.mojo.cpp.compiler.settings.DependencyExtractionSettings;
import org.codehaus.mojo.cpp.tools.environment.Environment;
import org.codehaus.plexus.util.FileUtils;


public class DependencyExtractor {
	private static final String VERSION_FILENAME = "version";
	private static final String NOARCH_ENVIRONMENT_NAME = "noarch";

	private final Log log;
	private final DependencyExtractionSettings settings;
	private final ArtifactManager artifactManager;
	private final List<DependencyIdentifier> securedDependencies = new ArrayList<DependencyIdentifier>();

	public DependencyExtractor(final Log log, final DependencyExtractionSettings settings, final ArtifactManager artifactManager) {
		this.log = log;
		this.settings = settings;
		this.artifactManager = artifactManager;
	}

	public void secureAvailabilityOfExtractedDependencies(final DependencyType dependencyType, final Environment targetEnvironment) throws MojoExecutionException {
		for(Artifact artifact : artifactManager.getDependencyArtifacts()) {
			secureAvailabilityOfExtractedDependency(new DependencyIdentifier(artifact, dependencyType, NOARCH_ENVIRONMENT_NAME));
			secureAvailabilityOfExtractedDependency(new DependencyIdentifier(artifact, dependencyType, targetEnvironment.getCanonicalName()));
		}
	}

	boolean isUpdatedSnapshot(final Artifact artifact, final File destination) {
		if( !artifact.isSnapshot() )
			return false;
		
		return artifact.getFile().lastModified() > destination.lastModified();
	}
	
	private void secureAvailabilityOfExtractedDependency(final DependencyIdentifier dep) throws MojoExecutionException {
		if ( securedDependencies.contains(dep) )
			return;

		final File destination = settings.getDirectoryForDependecyArtifactExtraction(dep.getArtifact().getScope(), dep.getArtifact().getGroupId(), dep.getArtifact().getArtifactId());

		if( destination.exists() )
			deleteDestinationIfInvalid(dep.getArtifact(), destination);

		if( !destination.exists() )
			setupDestination(dep.getArtifact(), destination);


		final File depSubDirectory = new File(destination, dep.getSubDirectoryName());
		if( !depSubDirectory.exists() )
			extractDependency(dep, destination);

		securedDependencies.add(dep);
	}

	private void extractDependency(final DependencyIdentifier dep, final File destination) throws MojoExecutionException {
		try {
			final ZipFile zipFile = new ZipFile(dep.getArtifact().getFile());
			final Enumeration<? extends ZipEntry> entriesEnum = zipFile.entries();
			while (entriesEnum.hasMoreElements()) {
				final ZipEntry entry = entriesEnum.nextElement();
				if( entryMatchesDependency(entry, dep) )
					extractEntry(entry, zipFile, destination);
			}

			zipFile.close();
		}
		catch(IOException e) {
			throw new MojoExecutionException("Failed to extract " + dep.getArtifact() + ".", e);
		} 

		log.debug("Extracted \"" + dep + "\" to " + destination);
	}

	private void extractEntry(final ZipEntry entry, final ZipFile zipFile, final File destination) throws MojoExecutionException {
		final File targetFile = new File(destination, entry.getName()); 

		if( entry.isDirectory() )
			targetFile.mkdirs();
		else
			writeFile(zipFile, entry, targetFile);
	}

	private boolean entryMatchesDependency(final ZipEntry entry, final DependencyIdentifier dep) {
		return entry.getName().startsWith(dep.getSubDirectoryName());
	}

	private void deleteDestinationIfInvalid(final Artifact artifact, final File destination) throws MojoExecutionException {
		final File versionFile = new File(destination, VERSION_FILENAME);

		try {
			if(isUpdatedSnapshot(artifact, destination)) {
				log.info(destination + " will be cleaned. There is a newer SNAPSHOT version in local repository.");
				destination.delete();
			}

			if( !versionFile.exists() ) {
				log.warn(destination + " will be cleaned. It contains no version file, which might indicate a previous failed extraction attempt.");
				destination.delete();
			}

			final String previouslyExtractedVersion = FileUtils.fileRead(versionFile);
			if(!previouslyExtractedVersion.equals(artifact.getVersion())) {
				log.warn(destination + " will be cleaned. It contains version " + previouslyExtractedVersion + ", but the current dependency is to " + artifact.getVersion() + ".");
				destination.delete();
			}
		} 
		catch (IOException e) {
			throw new MojoExecutionException("Inspection and/or cleaning of " + destination + " failed.", e);
		}
	}

	private void setupDestination(final Artifact artifact, final File destination) throws MojoExecutionException {
		destination.mkdirs();

		try {
			FileUtils.fileWrite(destination.getPath() + "/" + VERSION_FILENAME, artifact.getVersion());
		} 
		catch (IOException e){
			throw new MojoExecutionException("Failed to write version file.", e);
		}
	}

	private void writeFile(final ZipFile zipFile, final ZipEntry entry, final File targetFile) throws MojoExecutionException {
		InputStream in;
		try {
			in = zipFile.getInputStream(entry);
			final OutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile));

			byte[] buffer = new byte[1024];
			int bufferLength;

			while ((bufferLength = in.read(buffer)) >= 0)
				out.write(buffer, 0, bufferLength);

			in.close();
			out.close();
			targetFile.setReadOnly();
		} 
		catch (IOException e) {
			throw new MojoExecutionException("Failed to extract " + entry.getName() + " from " + zipFile.getName() + " to " + targetFile.getPath(), e);
		}
	}
}
