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

import org.apache.maven.artifact.Artifact;

public class DependencyIdentifier {
	private final String target;
	private final Artifact artifact;
	private final DependencyType dependencyType;

	public DependencyIdentifier(final Artifact artifact, final DependencyType dependencyType, final String target) {
		this.artifact = artifact;
		this.dependencyType = dependencyType;
		this.target = target;
	}
	
	@Override
	public int hashCode() {
		return getArtifact().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof DependencyIdentifier))
			return false;
		
		return equals((DependencyIdentifier)obj);
	}
	
	@Override
	public String toString() {
		return getArtifact().toString() + ":" + getTarget() + ":" + getDependencyType();
	}
	
	private boolean equals(final DependencyIdentifier other) {
		return getTarget().equals(other.getTarget())
			&& getDependencyType().equals(other.getDependencyType())
			&& getArtifact().equals(other.getArtifact());
	}

	public String getTarget() {
		return target;
	}

	public Artifact getArtifact() {
		return artifact;
	}

	public DependencyType getDependencyType() {
		return dependencyType;
	}

	public String getSubDirectoryName() {
		return getTarget() + "/" + getDependencyType().getDirectoryName();
	}
}
