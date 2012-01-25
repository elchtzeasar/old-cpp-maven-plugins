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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public class ArtifactManager {
	private final static String MAIN_ARTIFACT_KEY = "main";

	@SuppressWarnings("rawtypes")
	private final List remoteRepositories;
	private final ArtifactFactory factory;
	private final ArtifactResolver resolver;
	private final ArtifactRepository localRepository;
	private final Map<String, Collection<Artifact>> resolvedArtifacts = new HashMap<String, Collection<Artifact>>();


	@SuppressWarnings("rawtypes")
	public ArtifactManager(final Log log, final MavenProject project, final ArtifactFactory factory, final ArtifactResolver resolver, final ArtifactRepository localRepository, final List remoteRepositories) {
		this.factory = factory;
		this.resolver = resolver;
		this.localRepository = localRepository;
		this.remoteRepositories = remoteRepositories;

		final List<Artifact> mainArtifacts = new ArtifactFilter(log, "car").filter(project.getArtifacts());
		this.resolvedArtifacts.put(MAIN_ARTIFACT_KEY, mainArtifacts); 
	}

	public Artifact createProjectArtifact(final MavenProject project) {
		return factory.createProjectArtifact( project.getGroupId(), project.getArtifactId(), project.getVersion() );
	}

	public File getPomOfArtifact(final Artifact artifact) {
		return new File( localRepository.getBasedir(), localRepository.pathOf( artifact ) );
	}

	public Collection<Artifact> getDependencyArtifacts() throws MojoExecutionException {
		Collection<Artifact> dependencyArtifacts = new ArrayList<Artifact>();

		dependencyArtifacts.addAll(getResolvedArtifacts(MAIN_ARTIFACT_KEY));

		return dependencyArtifacts;
	}

	private Collection<Artifact> getResolvedArtifacts(final String key) throws MojoExecutionException {
		if( !resolvedArtifacts.containsKey(key) )
			resolve(key);

		return resolvedArtifacts.get(key);
	}

	private void resolve(final String classifier) throws MojoExecutionException {
		Collection<Artifact> artifacts = new ArrayList<Artifact>();

		for(Artifact mainArtifact : resolvedArtifacts.get(MAIN_ARTIFACT_KEY))
			artifacts.add(resolveClassifiedArtifact(mainArtifact, classifier));

		resolvedArtifacts.put(classifier, artifacts);
	}

	private Artifact resolveClassifiedArtifact(final Artifact mainArtifact, final String classifier) throws MojoExecutionException {
		final Artifact classifiedArtifact = factory.createArtifactWithClassifier( mainArtifact.getGroupId(), mainArtifact.getArtifactId(), mainArtifact.getVersion(), mainArtifact.getType(), classifier);
		
		try {
			resolver.resolve(classifiedArtifact, remoteRepositories, localRepository);
			classifiedArtifact.setScope(mainArtifact.getScope());
			return classifiedArtifact;
		} 
		catch (AbstractArtifactResolutionException e) {
			throw new MojoExecutionException("Failed to resolve artifact: " + classifiedArtifact);
		}
	}
}
